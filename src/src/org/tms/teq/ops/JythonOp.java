package org.tms.teq.ops;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

import org.python.core.Py;
import org.python.core.PyCode;
import org.python.core.PyFunction;
import org.python.core.PyNone;
import org.python.core.PyObject;
import org.python.core.PyString;
import org.python.core.PyStringMap;
import org.python.util.PythonInterpreter;
import org.tms.api.derivables.InvalidOperatorException;
import org.tms.api.derivables.Operator;
import org.tms.api.derivables.Token;
import org.tms.api.derivables.TokenType;
import org.tms.tds.TokenMapper;
import org.tms.tds.util.JythonHelper;
import org.tms.teq.AbstractOperator;

public class JythonOp extends BaseOp 
{	
	private static PythonInterpreter sf_PyInterpreter;
	protected static final PythonInterpreter getPyInterpreterInstance()
	{
		synchronized(JythonOp.class) {
		if (sf_PyInterpreter == null) 
			sf_PyInterpreter = new PythonInterpreter();
		}
		
		return sf_PyInterpreter;		
	}
	
    public static void registerAllOps(TokenMapper tokenMapper, String fileName)
    {
    	File f = new File(fileName);
    	String className = f.getName();
    	int idx = className.indexOf('.');
    	if (idx > -1)
    		className = className.substring(0, idx);
    	
    	registerAllOps(tokenMapper, fileName, className);
    }
    
    public static void registerAllOps(TokenMapper tokenMapper, String text, String className)
    {
        // compile the Jython class
        PyCode pyCode;
        PythonInterpreter pi = null;
        try
        {
        	pi = new PythonInterpreter();
        	
            // compile the code in the supplied file name
            // or, if text doesn't refer to a file, treat it as jython code
            File file = new File(text);         
            if (file.exists() && file.canRead()) {
            	InputStreamReader isr = new InputStreamReader(new FileInputStream(file));
            	pyCode = pi.compile(isr);
            	isr.close();
            }
            else
            	pyCode = pi.compile(text);
            
            // compile the code
            pi.exec(pyCode);

            PyObject pyThing = className != null ? pi.get(className) : null;
			if (pyThing != null ) {
            	// construct the object, this is the only way we can tell the
	            // actual class (I think...)
				PyObject pyObj = pyThing.__call__();
				if (pyObj != null) {					            
		            // first, see if we can instantiate the object, and if we can, is it an AbstractOperator?
		            // if it is, register it and return           
		            try {
						// cast the object to an AbstractOperator, success??
						Operator op = (AbstractOperator)pyObj.__tojava__(AbstractOperator.class);
						tokenMapper.registerOperator(op);
						return;
		            } 
		            catch (Exception e) { }
		            
		            // then, see if we can instantiate the object, and if we can, is it an Operator?
		            // if it is, register it and return           
		            try {
						// cast the object to an operator, success??
						Operator op = (Operator)pyObj.__tojava__(Operator.class);
						tokenMapper.registerOperator(op);
						return;
		            } 
		            catch (Exception e) { }
	            	            
		            // otherwise see if file was a Jython class, if so, harvest the 
		            // methods with signatures in the 
					// we'll need to consult the dictionary below
		            PyStringMap pSm = (PyStringMap)pyThing.fastGetDict();
		            Map<String, PyFunction> posibleFuncMap = new HashMap<String, PyFunction>();
		            for (Object k : pSm.keys()) {
		            	if (k instanceof String) {
		            		String methodName = (String)k;
		            		if (!(methodName.startsWith("__") || methodName.endsWith("__"))) {
		            			Object val = pSm.get(new PyString(methodName));
		            			if (val != null && val instanceof PyFunction) {
		            				PyFunction pyFunc = (PyFunction)val;
		            				if ((pyFunc.__doc__ != null && !(pyFunc.__doc__ instanceof PyNone)))
		            					posibleFuncMap.put(methodName, pyFunc);
		            			}
		            		}
		            	}	            		
		            }	
		            
		            if (posibleFuncMap != null && !posibleFuncMap.isEmpty()) {
		                // Otherwise, register executable methods
		                for (Map.Entry<String, PyFunction> e : posibleFuncMap.entrySet()) {
		                	String methodName = e.getKey();
		                	PyFunction pyFunc = e.getValue();
		                	JythonHelper pyArgs = new JythonHelper(methodName, pyFunc);
		                	
		                    JythonOp op = new JythonOp(methodName, pyArgs.getArgTypes(), pyArgs.getResultType(), methodName, pyObj);
		                    tokenMapper.registerOperator(op);
		                }           
		            }
				} 
				
				return;
			}
			
			// if there was no class, harvest functions, if any
			PyObject dirObj = pi.eval("dir()");
			if (dirObj != null) {
				Map<String, PyFunction> posibleFuncMap = JythonHelper.extractMethods(pi, dirObj);
	            	            
	            if (posibleFuncMap != null && !posibleFuncMap.isEmpty()) {
	                // Otherwise, register executable methods
	                for (Map.Entry<String, PyFunction> e : posibleFuncMap.entrySet()) {
	                	String methodName = e.getKey();
	                	PyFunction pyFunc = e.getValue();
	                	JythonHelper pyArgs = new JythonHelper(methodName, pyFunc);
	                	
	                	JythonOp op = new JythonOp(methodName, pyArgs.getArgTypes(), pyArgs.getResultType(), methodName, pyFunc);
	                    tokenMapper.registerOperator(op);
	                }           
	            }
			}		
        }
        catch (InvalidOperatorException e) {
        	throw e;
        }
        catch (Exception e)
        {
            throw new InvalidOperatorException(e);
        }   
        finally {
        	if (pi !=  null)
        		pi.close();
        }
    }
    
    private String m_methodName;
    private PyObject m_pyObj;
    
    private JythonOp(String label, Class<?>[] pTypes, Class<?> resultType, String methodName, PyObject pyObj)
    {
        super(label, TokenType.numArgsToTokenType(pTypes != null ? pTypes.length : 0), pTypes, resultType);
        m_methodName = methodName;
        m_pyObj = pyObj;
    }

    private JythonOp(String label, TokenType tt, Class<?>[] argTypes, Class<?> resultType) 
	{
		super(label, tt, argTypes, resultType);
	}

	@Override
	final public Token evaluate(Token... args) 
	{
		try {
	        // Transfer the args from the TMS system into
	        // an array to set up for the method call
	        PyObject [] mArgs = new PyObject [numArgs()];
	        for (int i = 0; i < numArgs(); i++) {
	            mArgs[i] = Py.java2py(args[i].getValue());
	        }
	        
	        PyObject pyResult;
	        if (m_pyObj instanceof PyFunction)
	        	pyResult = ((PyFunction)m_pyObj).__call__(mArgs);
	        else
	        	pyResult = m_pyObj.invoke(m_methodName, mArgs);
	        
	        // and return the result
	        Object result = JythonHelper.pythonToPig(pyResult);        
	        return new Token(TokenType.Operand, result);
		}
		catch (Exception e)
        {
            return Token.createErrorToken(e.getMessage());
        }
	}
}
