package org.tms.teq;

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
    private Map<Runnable, UUID> m_runnableUuidMap;
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
        m_runnableUuidMap = new ConcurrentHashMap<Runnable, UUID>();
        m_continueDraining = true;       
        allowCoreThreadTimeOut(timeOutCores);
        setRejectedExecutionHandler(new RejectedExecutionHandler() {
            public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
               // this will block if the queue is full
                try
                {
                    executor.getQueue().put(r);
                }
                catch (InterruptedException e)
                {
                    // noop;
                }
            }
         });  
        
        // start the drainer thread
        m_drainThread = new Thread(this);
        m_drainThread.setDaemon(true);
        m_drainThread.setName("PendingDrainer");
        m_drainThread.start();
    }

    @Override
    public void run()
    {
        while (m_continueDraining) {
            try
            {
                Runnable r = m_queuedRunnables.take();
                //int qSize = m_queuedRunnables.size();
                //if (qSize % 100 == 0) System.out.println("Consumer: " + qSize);
                
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
        UUID transactId = m_runnableUuidMap.remove(r);
        if (transactId != null)
            Derivation.associateTransactionID(t.getId(), transactId);
        
        super.beforeExecute(t, r);  
    }

    @Override
    protected void afterExecute(Runnable r, Throwable t) 
    {
        super.afterExecute(r, t);
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

    @Override
    public void submitCalculation(UUID transactionId, Runnable r)
    {
        m_runnableUuidMap.put(r,  transactionId);
        m_queuedRunnables.add(r);
    }

    @Override
    public boolean remove(Runnable r)
    {
        if (r == null)
            return false;
        
        // backing derivation is being cleared, we don't 
        // want running threads to report back a result
        m_runnableUuidMap.remove(r);
        
        // runnable could be in the executors queue, the unbounded queue, or already running
        return getQueue().remove(r) ||
               m_queuedRunnables.remove(r);
    }
}
