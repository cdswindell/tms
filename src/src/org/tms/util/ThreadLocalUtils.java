package org.tms.util;

import java.lang.ref.Reference;
import java.lang.reflect.Array;
import java.lang.reflect.Field;

public class ThreadLocalUtils
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
    
    static final public void resetThreadLocal(Object lock)
    {
        synchronized(lock) {
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
    }
}
