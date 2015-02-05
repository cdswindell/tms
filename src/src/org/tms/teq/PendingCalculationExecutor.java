package org.tms.teq;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class PendingCalculationExecutor extends ThreadPoolExecutor
{
    private static final Map<Runnable, UUID> sf_RUNNABLE_UUID_MAP = new ConcurrentHashMap<Runnable, UUID>();

    private Semaphore m_limiter;
    
    public PendingCalculationExecutor()
    {
        this(10, 40, 15, TimeUnit.SECONDS);
    }
    
    public PendingCalculationExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit)
    {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, 
              new ArrayBlockingQueue<Runnable>(maximumPoolSize), 
              new PendingThreadFactory(),
              new ThreadPoolExecutor.AbortPolicy());
        m_limiter = new Semaphore(maximumPoolSize*2);
    }

    @Override
    protected void beforeExecute(Thread t, Runnable r)
    {
        UUID transactId = sf_RUNNABLE_UUID_MAP.remove(r);
        Derivation.associateTransactionID(t.getId(), transactId);
        
        // queue calculation
        super.beforeExecute(t, r);       
    }

    @Override
    protected void afterExecute(Runnable r, Throwable t) 
    {
        m_limiter.release();
    }
    
    @Override
    public void execute(Runnable r)
    {
        boolean acquired = false;
        do {
            try {
                m_limiter.acquire();
                acquired = true;
            } catch (InterruptedException e) {
                // wait forever!
            }                   
        } while(!acquired);
        
        try {
            super.execute(r);
        }
        catch(RuntimeException e) {
            System.out.println("ERROR: " + m_limiter.availablePermits());
            m_limiter.release();
            throw e;
        } catch(Error e) {
            m_limiter.release();
            throw e;
        }
    }
    
    public void execute(UUID transactId, Runnable r)
    {
        sf_RUNNABLE_UUID_MAP.put(r,  transactId);
        execute(r);
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
