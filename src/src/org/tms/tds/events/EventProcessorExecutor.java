package org.tms.tds.events;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.tms.api.events.Listenable;
import org.tms.api.events.TableElementEvent;
import org.tms.api.events.TableElementListener;
import org.tms.api.exceptions.IllegalTableStateException;
import org.tms.util.ThreadLocalUtils;

public class EventProcessorExecutor extends ThreadPoolExecutor implements Runnable, EventProcessorThreadPool
{
    private BlockingQueue<TableElementEvent> m_queuedEvents;
    private boolean m_continueDraining;
    private Thread m_drainThread = null;
    
    public EventProcessorExecutor()
    {
        this(1, 5, 30, TimeUnit.SECONDS);
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
        
        prestartAllCoreThreads();
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
        m_drainThread.setName("EventDrainer");
        m_drainThread.start();
    }

    @Override
    public void submitEvents(Collection<TableElementEvent> events)
    {
        if (isShutdown())
            throw new IllegalTableStateException("Event Processor Thread pool has been shutdown...");
        
        if (events != null) 
            m_queuedEvents.addAll(events);
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
        ThreadLocalUtils.resetThreadLocal(this);
    }
    
    @Override
    public void shutdownEventProcessorThreadPool()
    {
        super.shutdown();
        
        m_continueDraining = false;
        m_drainThread.interrupt();
        
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
            t.setName("EventProcessorThread-" + (m_threadNo++));
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
