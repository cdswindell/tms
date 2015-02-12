package org.tms.teq;

import java.lang.ref.Reference;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.tms.api.DerivableThreadPool;

public class PendingDerivationExecutor extends ThreadPoolExecutor implements Runnable, DerivableThreadPool
{
    static private Field threadLocalsField;
    static private Class<?> threadLocalMapClass;
    static private Field tableField;
    static private Field referentField;
    
    static {
        try
        {
            threadLocalsField = Thread.class.getDeclaredField("threadLocals");
            threadLocalMapClass = Class.forName("java.lang.ThreadLocal$ThreadLocalMap");
            tableField = threadLocalMapClass.getDeclaredField("table");
            referentField = Reference.class.getDeclaredField("referent");   
        }
        catch (NoSuchFieldException | SecurityException | ClassNotFoundException e)
        {
            // TODO: log error
        }       
    }
    
    static private Map<Runnable, UUID> sf_runnableUuidMap = new ConcurrentHashMap<Runnable, UUID>(1024);
    
    private BlockingQueue<Runnable> m_queuedRunnables;
    private boolean m_continueDraining;
    private Thread m_drainThread = null;
    
    public PendingDerivationExecutor()
    {
        this(5, 100, 30, TimeUnit.SECONDS);
    }
    
    public PendingDerivationExecutor(int corePoolSize, int maximumPoolSize)
    {
        this(corePoolSize, maximumPoolSize, 30, TimeUnit.SECONDS);
    }
    
    public PendingDerivationExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit)
    {
        this(corePoolSize, maximumPoolSize, keepAliveTime, unit, true);
    }
    
    public PendingDerivationExecutor(int corePoolSize, int maximumPoolSize, 
                                     long keepAliveTime, TimeUnit unit, boolean timeOutCores)
    {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, 
              new SynchronousQueue<Runnable>(), 
              new PendingThreadFactory(),
              new ThreadPoolExecutor.AbortPolicy());
        
        m_queuedRunnables = new LinkedBlockingQueue<Runnable>();
        m_continueDraining = true;      
        
        allowCoreThreadTimeOut(timeOutCores);
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
        m_drainThread.setName("PendingQueueDrainer");
        m_drainThread.start();
    }

    @Override
    public void submitCalculation(UUID transactionId, Runnable r)
    {
        if (transactionId != null && r != null) {
            sf_runnableUuidMap.put(r,  transactionId);
            m_queuedRunnables.add(r);
        }
        else
            throw new NullPointerException("TransactionId and Runnable are required.");
    }

    @Override
    public boolean remove(Runnable r)
    {
        if (r == null)
            return false;
        
        // backing derivation is being cleared, we don't 
        // want running threads to report back a result
        sf_runnableUuidMap.remove(r);
        
        // runnable could be in the executors queue, the unbounded queue, or already running
        return getQueue().remove(r) ||
               m_queuedRunnables.remove(r);
    }
    
    @Override
    public void run()
    {
        while (m_continueDraining) {
            try
            {
                Runnable r = m_queuedRunnables.take();
                
                // offer the runnable to the Executor, 
                // blocking if necessary
                execute(r);
            }
            catch (InterruptedException e)
            {
                m_continueDraining = false;
            }
        }
        
        m_queuedRunnables.clear();
    }
    
    @Override
    protected void beforeExecute(Thread t, Runnable r)
    {
        // if the UUID is not found, the backing derivation is
        // in the process of being cleared
        UUID transactId = sf_runnableUuidMap.remove(r);
        if (transactId != null)
            Derivation.associateTransactionID(t.getId(), transactId);    
        
        super.beforeExecute(t, r);  
    }

    @Override
    protected void afterExecute(Runnable r, Throwable t) 
    {
        super.afterExecute(r, t);
        
        sf_runnableUuidMap.remove(r);
        resetThreadLocal();
    }

    synchronized private void resetThreadLocal()
    {
        // Get a reference to the thread locals table of the current thread
        try {
            Thread thread = Thread.currentThread();
            threadLocalsField.setAccessible(true);
            Object threadLocalTable = threadLocalsField.get(thread);
            
            // Get a reference to the array holding the thread local variables inside the
            // ThreadLocalMap of the current thread
            
            tableField.setAccessible(true);
            Object table = tableField.get(threadLocalTable);

            // The key to the ThreadLocalMap is a WeakReference object. The referent field of this object
            // is a reference to the actual ThreadLocal variable
            referentField.setAccessible(true);

            for (int i=0; i < Array.getLength(table); i++) {
                // Each entry in the table array of ThreadLocalMap is an Entry object
                // representing the thread local reference and its value
                Object entry = Array.get(table, i);
                if (entry != null) {
                    // Get a reference to the thread local object and remove it from the table
                    ThreadLocal<?> threadLocal = (ThreadLocal<?>)referentField.get(entry);
                    threadLocal.remove();
                }
            }
        }
        catch (Exception e) { } //noop
        finally {
            try {
            if (referentField != null)
                referentField.setAccessible(false);
            if (tableField != null)
                tableField.setAccessible(false);
            if (threadLocalsField != null)
                threadLocalsField.setAccessible(false);
            }
            catch (Throwable e) {
                // TODO: log error
            }
        }
    }
    
    @Override
    public void shutdown()
    {
        super.shutdown();
        
        m_continueDraining = false;
        m_queuedRunnables.clear();       
    }
    
    protected static class PendingThreadFactory implements ThreadFactory
    {
        static private int m_threadNo = 0;
        
        @Override
        public Thread newThread(Runnable r)
        {
            Thread t = new Thread(r);
            
            t.setDaemon(true);
            t.setName("PendingCalculationThread-" + (m_threadNo++));
            return t;
        }        
    }
}
