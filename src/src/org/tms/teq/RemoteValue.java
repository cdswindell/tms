package org.tms.teq;

import java.lang.ref.WeakReference;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.tms.api.Column;
import org.tms.api.Row;
import org.tms.api.derivables.ErrorCode;
import org.tms.api.derivables.Operator;
import org.tms.api.derivables.Token;
import org.tms.api.exceptions.IllegalTableStateException;
import org.tms.tds.RowImpl;
import org.tms.teq.PendingDerivationExecutor.PendingThreadFactory;
import org.tms.tds.ColumnImpl;

public class RemoteValue extends ThreadPoolExecutor implements Runnable
{
	static final private Map<RCKey, String> sf_KEY_TO_UUID = new ConcurrentHashMap<RCKey, String>(1024);	
	static final private Map<String, RCKey> sf_UUID_TO_KEY = new ConcurrentHashMap<String, RCKey>(1024);	
	static final private Map<String, Object> sf_UUID_TO_VALUE = new ConcurrentHashMap<String, Object>(1024);
	
	public static Token prepareHandler(final DerivationImpl deriv, final BuiltinOperator oper, final Row row, final Column col, Token x) 
	{
		if (deriv == null || deriv.isBeingDestroyed())
			return Token.createNullToken();
		
		// calculate the key from the row/column value
		RCKey key = new RCKey(deriv, oper, row, col);
		
		// have we already registered this location?
		String uuid = sf_KEY_TO_UUID.get(key);
		
		// handle token
		String remoteUuid = null;
		
		if (x != null) {
			if (x.isString())
				remoteUuid = x.getStringValue();
			else
				return Token.createErrorToken(ErrorCode.InvalidOperand);
		}
		
		// deactivate the old reference
		if (remoteUuid != null && uuid != null && !remoteUuid.equals(uuid)) {
			sf_KEY_TO_UUID.remove(key);
			sf_UUID_TO_KEY.remove(uuid);
			sf_UUID_TO_VALUE.remove(uuid);
			
			deriv.unregisterRemoteUUID(uuid);
			((RowImpl)row).unregisterRemoteUUID(uuid);
			((ColumnImpl)col).unregisterRemoteUUID(uuid);
			
			// set uuid to null to force reprocess
			uuid = null;
		}
		
		// if null, register the new value
		if (uuid == null) {
			if (remoteUuid != null) 
				uuid = remoteUuid;
			else
				uuid = UUID.randomUUID().toString();
			
			System.out.println(uuid);
			sf_KEY_TO_UUID.put(key, uuid);
			sf_UUID_TO_KEY.put(uuid, key);
			
			// remember this uuid so we can clean up when derivation is deleted
			deriv.registerRemoteUUID(uuid);
			((RowImpl)row).registerRemoteUUID(uuid);
			((ColumnImpl)col).registerRemoteUUID(uuid);
		}
		
		// finally, return the current value
		Object value = sf_UUID_TO_VALUE.get(uuid);
		if (value == null)
			return Token.createAwaitingToken(uuid);
		else
			return new Token(value);
	}
	
	public static void postRemoteValue(final String uuid, Object value)
	{
		RCKey key = sf_UUID_TO_KEY.get(uuid);
		if (key != null && key.isValid()) {
			if (key.getOperator() == BuiltinOperator.RemoteNumericOper || key.getOperator() == BuiltinOperator.RemoteCellNumericOper)
				value = Double.parseDouble((String)value);
			
			sf_UUID_TO_VALUE.put(uuid, value);
			
			// submit calculation
			getInstance().queueRecalculation(uuid, key);
		}
	}
	
	/**
	 * Called when a derivation is deleted
	 * @param uuids
	 */
    public static void removeRemoteHandlers(final String[] uuids) 
	{
		for (String uuid : uuids) {
			RCKey key = sf_UUID_TO_KEY.remove(uuid);
			if (key != null)
				sf_KEY_TO_UUID.remove(key);
			
			sf_UUID_TO_VALUE.remove(uuid);
			
			getInstance().removeRecalculation(uuid);
		}		
	}
    
    static public final int numHandlers()
    {
    	return sf_KEY_TO_UUID != null ? sf_KEY_TO_UUID.size() : 0;
    }
    
    /*
     * This class double duties, it also provides an executor to process remote value submissions
     */
	private static RemoteValue sf_SINGLETON_INSTANCE = null;
	
	synchronized private static final RemoteValue getInstance()
	{
		if (sf_SINGLETON_INSTANCE == null) {
			sf_SINGLETON_INSTANCE = new RemoteValue();
		}
		
		return sf_SINGLETON_INSTANCE;
	}

	private LinkedBlockingQueue<String> m_queuedCalculations;
	private boolean m_continueDraining;
	private Thread m_drainThread;
	
    private RemoteValue()
    {
        super(5, 50, 30, TimeUnit.SECONDS, 
                new SynchronousQueue<Runnable>(), 
                new PendingThreadFactory(),
                new ThreadPoolExecutor.AbortPolicy());
          
          m_queuedCalculations = new LinkedBlockingQueue<String>();
          m_continueDraining = true;      
                 
          allowCoreThreadTimeOut(true);
          setRejectedExecutionHandler(new RejectedExecutionHandler() {
              public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
                 // this will block if the queue is full
                  try
                  {
                      executor.getQueue().put(r);
                  }
                  catch (InterruptedException e) { }// noop
              }
           });  
          
          // start the drainer thread
          m_drainThread = new Thread(this);
          m_drainThread.setDaemon(true);
          m_drainThread.setName("AwaitingQueueDrainer");
          m_drainThread.start();  	
    }
    
	private void queueRecalculation(String uuid, RCKey key) 
	{
        if (isShutdown())
            throw new IllegalTableStateException("Awaiting Remote Value Thread pool has been shutdown...");
        
		m_queuedCalculations.add(uuid);	
	}
	
	private void removeRecalculation(String uuid)
	{
		m_queuedCalculations.remove(uuid);
	}

    @Override
    public void run()
    {
        while (m_continueDraining) {
            try
            {
                String uuid = m_queuedCalculations.take();
                
                RCKey key = sf_UUID_TO_KEY.get(uuid);
                Runnable r = buildCalculationRunnable(uuid, key);
                
                // offer the runnable to the Executor, 
                // blocking if necessary
                if (r != null)
                	execute(r);
            }
            catch (InterruptedException e)
            {
                m_continueDraining = false;
            }
        }
        
        m_queuedCalculations.clear();
    }
    
	private Runnable buildCalculationRunnable(String uuid, RCKey key) 
	{
		CalculationRunnable r = new CalculationRunnable(uuid, key);
		return r != null && r.isValid() ? r : null;
	}

	/**
	 * Inner class to run recalculation
	 */
	static class CalculationRunnable implements Runnable
	{
		private DerivationImpl m_deriv;
		private Row m_row;
		private Column m_col;
		private String m_uuid;

		CalculationRunnable(String uuid, RCKey key)
		{
			m_uuid = uuid;
			if (key != null && key.isValid()) {
				m_deriv = key.getDerivation();
				m_row = key.getRow();
				m_col = key.getColumn();
			}
		}
		
		public boolean isValid()
		{
			return m_deriv != null && m_row != null && m_row.isValid() && m_col != null && m_col.isValid();
		}
		
		public String getUuid() 
		{
			return m_uuid;
		}
		
		@Override
		public void run() 
		{
			m_deriv.recalculateTargetCell(m_row, m_col);
		}		
	}
	
	/**
	 * Inner class to provide unique key
	 */
	static class RCKey
	{
		private WeakReference<DerivationImpl> m_deriv;
		private BuiltinOperator m_oper;
		private WeakReference<Row> m_row;
		private WeakReference<Column> m_col;
		
		private int m_derivIdent;
		private long m_rowIdent;
		private long m_colIdent;
		
		RCKey(DerivationImpl d, BuiltinOperator oper, Row r, Column c)
		{
			m_deriv = new WeakReference<DerivationImpl>(d);
			m_oper = oper;
			m_row = new WeakReference<Row>(r);
			m_col = new WeakReference<Column>(c);
			
			m_derivIdent = d.getIdent();
			m_rowIdent = r.getIdent();
			m_colIdent = c.getIdent();
		}
		
		Row getRow()
		{
			return m_row.get();
		}
		
		Column getColumn()
		{
			return m_col.get();
		}
		
		DerivationImpl getDerivation()
		{
			return m_deriv.get();
		}
		
		Operator getOperator()
		{
			return m_oper;
		}
		
		boolean isValid()
		{
			Row row = getRow();
			Column col = getColumn();
			DerivationImpl deriv = getDerivation();
			
			return deriv != null && row != null && row.isValid() && col != null && col.isValid();
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + Long.hashCode(m_colIdent);
			result = prime * result + m_derivIdent;
			result = prime * result + ((m_oper == null) ? 0 : m_oper.hashCode());
			result = prime * result + Long.hashCode(m_rowIdent);
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			
			if (obj == null)
				return false;
			
			if (getClass() != obj.getClass())
				return false;
			
			RCKey other = (RCKey) obj;
			if (m_colIdent != other.m_colIdent)
				return false;
			
			if (m_derivIdent != other.m_derivIdent)
				return false;
			
			if (m_oper == null) {
				if (other.m_oper != null)
					return false;
			} 
			else if (!m_oper.equals(other.m_oper))
				return false;
			
			if (m_rowIdent != other.m_rowIdent)
				return false;
			
			return true;
		}

	}
}
