package org.tms.tds.util;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import org.codehaus.groovy.control.CompilationFailedException;
import org.tms.api.exceptions.ConstraintViolationException;
import org.tms.api.utils.TableCellTransformer;
import org.tms.api.utils.TableCellValidator;

import groovy.lang.GroovyClassLoader;

public class GroovyCellTransformer implements TableCellTransformer
{
    private static final long serialVersionUID = -6882519397752443785L;
    
    public static final TableCellValidator construct(String text, String valMethodName, String transMethodName)
    {
    	return construct(TableCellTransformer.class, text, valMethodName, transMethodName);
    }
    
    static final TableCellValidator construct(Class<? extends TableCellValidator> clazz,  
    		String text, String valMethodName, String transMethodName)
    {
    	try {
	        Class<?> groovyClazz;
	        File file = new File(text);
	        if (file.exists() && file.canRead())
	            groovyClazz = new GroovyClassLoader().parseClass(file);
	        else
	            groovyClazz = new GroovyClassLoader().parseClass(text);
	        
		    // if this object is a TableCellTransformer, construct it and return
		    if (TableCellTransformer.class.isAssignableFrom(groovyClazz)) {
		    	return (TableCellTransformer)groovyClazz.newInstance();
		    }
		    else if (TableCellValidator.class.isAssignableFrom(groovyClazz)) {
		    	if (clazz == TableCellValidator.class)
		    		return (TableCellValidator)groovyClazz.newInstance();
		    	else
		            throw new IllegalArgumentException("TableCellTransformer request but TableCellValidator found"); 
		    }
		    
		    // otherwise, return our special handler
		    return new GroovyCellTransformer(clazz, groovyClazz, text, valMethodName, transMethodName);
    	} 
    	catch (CompilationFailedException | IOException | InstantiationException | IllegalAccessException e) {
            throw new IllegalArgumentException("Exception encountered while processing Groovy: " + e.getMessage()); 
		}
    }
    
    private Class<? extends TableCellValidator> m_requestedClazz;
    private Class<?> m_groovyClazz;
    private Method m_groovyTransformMethod;
    private Method m_groovyValidationMethod;
    private Object m_groovyInst;
    
    private GroovyCellTransformer(Class<? extends TableCellValidator> clazz, Class<?> groovyClazz, 
    		String text, String valMethodName, String transMethodName)
    {
    	m_requestedClazz = clazz;
    	
        boolean valReq = valMethodName != null && (valMethodName = valMethodName.trim()).length() > 0;
        String valMethName = valMethodName != null ? valMethodName : "validate";

        boolean transReq = m_requestedClazz == TableCellTransformer.class;
        String transMethName = transReq && transMethodName != null && (transMethodName = transMethodName.trim()).length() > 0 
        		? transMethodName : null;
        	
        try {
        	m_groovyClazz = groovyClazz;
        	
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
                                if (transMethName == null || m_groovyTransformMethod != null)
                                    break;                                
                            }
                            else if (transMethName != null) {
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
        catch (InstantiationException | IllegalAccessException e)
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
