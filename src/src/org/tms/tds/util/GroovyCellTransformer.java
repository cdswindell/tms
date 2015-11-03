package org.tms.tds.util;

import groovy.lang.GroovyClassLoader;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import org.tms.api.exceptions.ConstraintViolationException;
import org.tms.api.utils.TableCellTransformer;

public class GroovyCellTransformer implements TableCellTransformer
{
    private Class<?> m_groovyClazz;
    private Method m_groovyTransformMethod;
    private Method m_groovyValidationMethod;
    private Object m_groovyInst;
    
    public GroovyCellTransformer(String text, String valMethodName, String transMethodName)
    {
        boolean valReq = valMethodName != null && (valMethodName = valMethodName.trim()).length() > 0;
        String valMethName = valMethodName != null ? valMethodName : "validate";

        boolean transReq = transMethodName != null && (transMethodName = transMethodName.trim()).length() > 0;
        String transMethName = transMethodName != null ? transMethodName : "transform";
        try {
            File file = new File(text);
            if (file.exists() && file.canRead())
                m_groovyClazz = new GroovyClassLoader().parseClass(file);
            else
                m_groovyClazz = new GroovyClassLoader().parseClass(text);
            
            for (Method m : m_groovyClazz.getDeclaredMethods()) {
                int methodType = m.getModifiers();
                String curMethodName = m.getName();
                if (methodType == Modifier.PUBLIC) {
                    Class<?> resultType = m.getReturnType();
                    if (resultType != null) {
                        Class<?> [] args = m.getParameterTypes();
                        if (args != null && args.length == 1 && args[0] == Object.class) {
                            if (resultType == void.class) {
                                if (m_groovyValidationMethod != null || (valMethName != null && !valMethName.equals(curMethodName)))
                                    continue;
                                
                                m_groovyValidationMethod = m;
                                if (m_groovyTransformMethod != null)
                                    break;                                
                            }
                            else {
                                if (m_groovyTransformMethod != null || (transMethName != null && !transMethName.equals(curMethodName)))
                                    continue;
                                
                                m_groovyTransformMethod = m;
                                if (m_groovyValidationMethod != null)
                                    break;
                            }
                        }
                    }
                }
            } 
            
            if (transReq && m_groovyTransformMethod == null)
                throw new IllegalArgumentException("Transform method: " + transMethName + " not found"); 
            
            if (valReq && m_groovyValidationMethod == null)
                throw new IllegalArgumentException("Validation method: " + valMethName + " not found"); 
            
            if (m_groovyTransformMethod == null && m_groovyValidationMethod == null)
                throw new IllegalArgumentException("Groovy code has no validation or transform method(s)"); 
            
            // create validation instance object
            m_groovyInst = m_groovyClazz.newInstance();
        }
        catch (IOException | InstantiationException | IllegalAccessException e)
        {
            throw new IllegalArgumentException("Exception encountered while processing Groovy: " + e.getMessage()); 
        }
    }

    @Override
    public void validate(Object newValue) throws ConstraintViolationException
    {
        if (m_groovyValidationMethod != null) {
            try {
                // set up to call Groovy transformer
                Object [] mArgs = new Object [] { newValue };
                
                // Invoke the method on the Groovy object, with args
                m_groovyValidationMethod.invoke(m_groovyInst, mArgs);
            }
            catch (IllegalAccessException | IllegalArgumentException e)
            {
                System.out.println(e.getMessage());
                // TODO: log validation call error
            }
            catch (InvocationTargetException ve) {
                Throwable cve = ve.getTargetException();
                if (cve != null)
                    throw new ConstraintViolationException(cve.getMessage());
                else
                    System.out.println(ve.getMessage());
            }
        }
    }

    @Override
    public Object transform(Object newValue)
    {
        // perform validation
        validate(newValue);
        
        // now transform
        if (m_groovyTransformMethod != null) {
            try {
                // set up to call Groovy transformer
                Object [] mArgs = new Object [] {newValue};
                
                // Invoke the method on the Groovy object, with args
                Object result = m_groovyTransformMethod.invoke(m_groovyInst, mArgs);
                return result;
            }
            catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e)
            {
                return newValue;
            }
        }
        else
            return newValue;
    }
}
