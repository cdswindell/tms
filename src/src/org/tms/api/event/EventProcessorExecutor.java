package org.tms.api.event;

import java.lang.ref.Reference;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class EventProcessorExecutor extends ThreadPoolExecutor implements Runnable, EventProcessorThreadPool
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
    
    private BlockingQueue<TableElementEvent> m_queuedEvents;
    private boolean m_continueDraining;
    private Thread m_drainThread = null;
    
    public EventProcessorExecutor()
    {
        this(5, 25, 30, TimeUnit.SECONDS);
    }
    
    public EventProcessorExecutor(int corePoolSize, int maximumPoolSize)
    {
        this(corePoolSize, maximumPoolSize, 30, TimeUnit.SECONDS);
    }
    
    public EventProcessorExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit)
    {
        this(corePoolSize, maximumPoolSize, keepAliveTime, unit, true);
    }
    
    public EventProcessorExecutor(int corePoolSize, int maximumPoolSize, 
                                     long keepAliveTime, TimeUnit unit, boolean timeOutCores)
    {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, 
              new SynchronousQueue<Runnable>(), 
              new PendingThreadFactory(),
              new ThreadPoolExecutor.AbortPolicy());
        
        m_queuedEvents = new LinkedBlockingQueue<TableElementEvent>();
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
        m_drainThread.setName("PendingEventDrainer");
        m_drainThread.start();
    }

    @Override
    public void submitEvents(Collection<TableElementEvent> events)
    {
        if (events != null) {
            m_queuedEvents.addAll(events);
        }
        else
            throw new NullPointerException("Collection<TableElementEvent> required.");
    }

    @Override
    public boolean remove(TableElementEvent e)
    {
        if (e == null)
            return false;
        
        // can only remove events from queue
        return m_queuedEvents.remove(e);
    }
    
    @Override
    public void run()
    {
        while (m_continueDraining) {
            try
            {
                TableElementEvent e = m_queuedEvents.take();
                
                Runnable r = new EventRunner(e);
                
                // offer the runnable to the Executor, 
                // blocking if necessary
                execute(r);
            }
            catch (InterruptedException e)
            {
                m_continueDraining = false;
            }
        }
        
        m_queuedEvents.clear();
    }
    
    @Override
    protected void beforeExecute(Thread t, Runnable r)
    {
        super.beforeExecute(t, r);  
    }

    @Override
    protected void afterExecute(Runnable r, Throwable t) 
    {
        super.afterExecute(r, t);       
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
        m_queuedEvents.clear();       
    }
    
    protected static class PendingThreadFactory implements ThreadFactory
    {
        static private int m_threadNo = 0;
        
        @Override
        public Thread newThread(Runnable r)
        {
            Thread t = new Thread(r);
            
            t.setDaemon(true);
            t.setName("PendingEventThread-" + (m_threadNo++));
            return t;
        }        
    }
    
    protected static class EventRunner implements Runnable
    {
        private TableElementEvent m_event;
        
        public EventRunner(TableElementEvent e)
        {
            m_event = e;
        }

        @Override
        public void run()
        {
            List<TableElementListener> listeners = ((Listenable) m_event.getSource()).getListeners(m_event.getType());
            if (listeners != null) {
                for (TableElementListener listener : listeners) {
                    listener.eventOccured(m_event);
                }
            }
        }       
    }
}
