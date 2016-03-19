package org.tms.teq.ops;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import org.codehaus.groovy.control.CompilationFailedException;
import org.tms.api.derivables.InvalidOperatorException;
import org.tms.api.derivables.Operator;
import org.tms.api.derivables.Token;
import org.tms.api.derivables.TokenType;
import org.tms.api.exceptions.IllegalTableStateException;
import org.tms.tds.TokenMapper;
import org.tms.teq.AbstractOperator;

import groovy.lang.GroovyClassLoader;
import groovy.lang.GroovyObject;

public class GroovyOp extends BaseOp
{   
    public static void registerAllOps(TokenMapper tokenMapper, String text)
    {
        // compile the Groovy class
        Class<?> groovyClazz = null;
    	GroovyClassLoader gcl = null;
        try
        {
            // compile the code in the supplied file name
            // or, if text doesn't refer to a file, treat it as groovy code
            File file = new File(text);
            gcl = new GroovyClassLoader();
            if (file.exists() && file.canRead())
                groovyClazz = gcl.parseClass(file);
            else
                groovyClazz = gcl.parseClass(text);
            
            // first, see if we can instantiate the object, and if we can, is it an Operator?
            // if it is, register it and return
            try {
				GroovyObject groovyObject = (GroovyObject)groovyClazz.newInstance();
				if (groovyObject != null && groovyObject instanceof AbstractOperator) {
					tokenMapper.registerOperator((AbstractOperator)groovyObject);
					return;
				}
				else if (groovyObject != null && groovyObject instanceof Operator) {
					tokenMapper.registerOperator((Operator)groovyObject);
					return;
				}
            } 
            catch (IllegalTableStateException | InstantiationException | IllegalAccessException e) {}
            
            // Otherwise, register executable methods
            for (Method m : groovyClazz.getDeclaredMethods()) {
                int methodType = m.getModifiers();
                String methodName = m.getName();
                if (methodType == Modifier.PUBLIC) {
                    Class<?> resultType = m.getReturnType();
                    if (resultType != null && resultType != void.class) {
                        Class<?> [] args = m.getParameterTypes();
                        
                        GroovyOp op = new GroovyOp(methodName, args, resultType, groovyClazz, m);
                        tokenMapper.registerOperator(op);
                    }
                }
            }           
        }
        catch (CompilationFailedException | IOException e)
        {
            throw new InvalidOperatorException(e);
        }        
        finally {
        	try {
	        	if (gcl != null) {
	        		gcl.close();
	        		gcl = null;
	        	}
        	} 
        	catch (IOException e) { /* noop */ }
        }
    }
    
    /*
     * Instance properties and methods 
     */
    
    private File m_file;
    private String m_methodName;
    private Class<?> m_groovyClazz;
    private Method m_method;
    private Object m_groovyInst;
    
    private GroovyOp(String label, Class<?>[] pTypes, Class<?> resultType, Class<?> groovyClazz, Method m)
    {
        super(label, TokenType.numArgsToTokenType(pTypes != null ? pTypes.length : 0), pTypes, resultType);
        m_groovyClazz = groovyClazz;
        m_method = m;
    }

    public GroovyOp(String label, Class<?> [] pTypes, Class<?> resultType, String fileName)
    {
        this(label, pTypes, resultType, fileName, label);
    }
    
    public GroovyOp(String label, Class<?> [] pTypes, Class<?> resultType, String fileName, String methodName)
    {
        this(label, TokenType.numArgsToTokenType(pTypes != null ? pTypes.length : 0), pTypes, resultType, fileName, methodName);
    }

    public GroovyOp(String label, TokenType tt, Class<?> [] pTypes, Class<?> resultType, String fileName, String methodName)
    {
        super(label, tt, pTypes, resultType);
        m_file = new File(fileName);
        m_methodName = methodName;
    }

    @Override
    public Token evaluate(Token... args)
    {
    	GroovyClassLoader gcl = null;
        try {
            // compile the Groovy class
            if (m_groovyClazz == null && m_file != null) {
            	gcl = new GroovyClassLoader();
                m_groovyClazz = gcl.parseClass(m_file); 
            }
            
            // if the class is null at this point, we're done
            if (m_groovyClazz == null)
                throw new InvalidOperatorException("cannot load Groovy class");
            
            // now get the method name we're going to call
            if (m_method == null)
                m_method = m_groovyClazz.getDeclaredMethod(m_methodName, getArgTypes());
            
            // and create an instance object from the class 
            m_groovyInst = m_groovyClazz.newInstance();

            // Transfer the args from the TMS system into
            // an array to set up for the method call
            Object [] mArgs = unpack(args);
            
            // Invoke the method on the Groovy object, with args
            Object result = m_method.invoke(m_groovyInst, mArgs);
            
            // and return the result
            return new Token(TokenType.Operand, result);
        }
        catch (CompilationFailedException | IOException | InstantiationException | 
               IllegalAccessException | NoSuchMethodException | SecurityException | 
               IllegalArgumentException | InvocationTargetException e)
        {
            return Token.createErrorToken(e.getMessage());
        }
        finally {
        	try {
	        	if (gcl != null) {
	        		gcl.close();
	        		gcl = null;
	        	}
        	} 
        	catch (IOException e) { /* noop */ }
        }
    }
}
