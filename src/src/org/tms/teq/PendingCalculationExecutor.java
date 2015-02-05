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

public class PendingCalculationExecutor extends ThreadPoolExecutor implements Runnable
{
    private static final ThreadLocal<UUID> sf_GUID_CACHE = new ThreadLocal<UUID>();
    
    private Map<Runnable, UUID> m_runnableUuidMap;
    private BlockingQueue<Runnable> m_queuedRunnables;
    private boolean m_continueDraining;
    private Thread m_drainThread = null;
    
    public PendingCalculationExecutor()
    {
        this(5, 100, 30, TimeUnit.SECONDS);
    }
    
    public PendingCalculationExecutor(int corePoolSize, int maximumPoolSize)
    {
        this(corePoolSize, maximumPoolSize, 30, TimeUnit.SECONDS);
    }
    
    public PendingCalculationExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit)
    {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, 
              new SynchronousQueue<Runnable>(), 
              new PendingThreadFactory(),
              new ThreadPoolExecutor.AbortPolicy());
        
        m_queuedRunnables = new LinkedBlockingQueue<Runnable>();
        m_runnableUuidMap = new ConcurrentHashMap<Runnable, UUID>();
        m_continueDraining = true;       
        
        allowCoreThreadTimeOut(true);
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
        super.beforeExecute(t, r);  
        
        UUID transactId = m_runnableUuidMap.remove(r);
        Derivation.associateTransactionID(t.getId(), transactId);
        sf_GUID_CACHE.set(transactId);
    }

    @Override
    protected void afterExecute(Runnable r, Throwable t) 
    {
        super.afterExecute(r, t);
        
        if (t != null)
            System.out.println(t.getMessage());
    }
    
    @Override
    public void shutdown()
    {
        super.shutdown();
        
        m_continueDraining = false;
        m_queuedRunnables.clear();       
    }
    
    public void execute(UUID transactId, Runnable r)
    {
        m_runnableUuidMap.put(r,  transactId);
        m_queuedRunnables.add(r);
        //int qSize = m_queuedRunnables.size();
        //if (qSize % 100 == 0) System.out.println("Producer: " + qSize);
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
