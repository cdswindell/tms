package org.tms.teq.ops;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.python.core.Py;
import org.python.core.PyCode;
import org.python.core.PyDictionary;
import org.python.core.PyFloat;
import org.python.core.PyFunction;
import org.python.core.PyInteger;
import org.python.core.PyList;
import org.python.core.PyLong;
import org.python.core.PyNone;
import org.python.core.PyObject;
import org.python.core.PyString;
import org.python.core.PyStringMap;
import org.python.util.PythonInterpreter;
import org.tms.api.derivables.InvalidOperatorException;
import org.tms.api.derivables.Operator;
import org.tms.api.derivables.Token;
import org.tms.api.derivables.TokenMapper;
import org.tms.api.derivables.TokenType;
import org.tms.api.exceptions.TableIOException;

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
        	pi = JythonOp.getPyInterpreterInstance();
        	
            // compile the code in the supplied file name
            // or, if text doesn't refer to a file, treat it as jython code
            File file = new File(text);         
            if (file.exists() && file.canRead())
            	pyCode = pi.compile(new InputStreamReader(new FileInputStream(file)));
            else
            	pyCode = pi.compile(text);
            
            synchronized (pi) {
            	pi.exec(pyCode);
            }
            
            // first, see if we can instantiate the object, and if we can, is it an Operator?
            // if it is, register it and return           
            PyObject pyThing = pi.get(className);
			if (pyThing != null ) {
				PyObject pyObj = pyThing.__call__();
	            try {
	            	// construct the object
					if (pyObj != null) {
						// cast the object to an operator, success??
						Operator op = (Operator)pyObj.__tojava__(Operator.class);
						tokenMapper.registerOperator(op);
						return;
					}
	            } 
	            catch (Exception e) { }
	            
	            // if we get here, the object wasn't an operator, try it as just a class
	            PyStringMap pSm = (PyStringMap)pyThing.fastGetDict();
	            Map<String, PyFunction> posibleFuncMap = new HashMap<String, PyFunction>();
	            for (Object k : pSm.keys()) {
	            	if (k instanceof String) {
	            		String methodName = (String)k;
	            		if (!(methodName.startsWith("__") || methodName.endsWith("__"))) {
	            			Object val = pSm.get(new PyString(methodName));
	            			if (val != null && val instanceof PyFunction) {
	            				PyFunction pyFunc = (PyFunction)val;
	            				if (pyFunc.__doc__ != null && !(pyFunc.__doc__ instanceof PyNone))
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
	                	SigParser pyArgs = new SigParser(methodName, pyFunc);
	                	
	                    JythonOp op = new JythonOp(methodName, pyArgs.getArgTypes(), pyArgs.getResultType(), className, methodName, pyObj);
	                    tokenMapper.registerOperator(op);
	                }           
	            }
            
			}
        }
        catch (Exception e)
        {
            throw new InvalidOperatorException(e);
        }   
    }
    
    private String m_methodName;
    private PyObject m_pyObj;
    
    private JythonOp(String label, Class<?>[] pTypes, Class<?> resultType, String className, String methodName, PyObject pyObj)
    {
        super(label, TokenType.numArgsToTokenType(pTypes != null ? pTypes.length : 0), pTypes, resultType);
        m_methodName = methodName;
        m_pyObj = pyObj;
    }

	public JythonOp(String label, TokenType tt, Class<?>[] argTypes, Class<?> resultType) 
	{
		super(label, tt, argTypes, resultType);
	}

	@Override
	public Token evaluate(Token... args) 
	{
        // Transfer the args from the TMS system into
        // an array to set up for the method call
        PyObject [] mArgs = new PyObject [numArgs()];
        for (int i = 0; i < numArgs(); i++) {
            mArgs[i] = Py.java2py(args[i].getValue());
        }
        
        PyObject pyResult = m_pyObj.invoke(m_methodName, mArgs);
        
        // and return the result
        Object result = pythonToPig(pyResult);        
        return new Token(TokenType.Operand, result);
	}

    public static Object pythonToPig(PyObject pyObject) 
    {
        try {
            Object javaObj = null;
            // Add code for all supported pig types here
            // Tuple, bag, map, int, long, float, double, chararray, bytearray 
            if (pyObject instanceof PyList) {
                List<Object> list = new ArrayList<Object>();
                for (PyObject bagTuple : ((PyList) pyObject).asIterable()) {
                    // In jython, list need not be a bag of tuples, as it is in case of pig
                    // So we fail with cast exception if we dont find tuples inside bag
                    // This is consistent with java udf (bag should be filled with tuples)
                    list.add(pythonToPig(bagTuple));
                }
                javaObj = list;
            } else if (pyObject instanceof PyDictionary) {
                @SuppressWarnings("unchecked")
				Map<?, PyObject> map = Py.tojava(pyObject, Map.class);
                Map<Object, Object> newMap = new HashMap<Object, Object>();
                for (Map.Entry<?, PyObject> entry : map.entrySet()) {
                    newMap.put(entry.getKey(), pythonToPig(entry.getValue()));
                }
                javaObj = newMap;
            } else if (pyObject instanceof PyLong) {
                javaObj = pyObject.__tojava__(Long.class);
            } else if (pyObject instanceof PyInteger) {
                javaObj = pyObject.__tojava__(Integer.class);
            } else if (pyObject instanceof PyFloat) {
                // J(P)ython is loosely typed, supports only float type, 
                // hence we convert everything to double to save precision
                javaObj = pyObject.__tojava__(Double.class);
            } else if (pyObject instanceof PyString) {
                javaObj = pyObject.__tojava__(String.class);
            } else if (pyObject instanceof PyNone) {
                return null;
            } 
            return javaObj;
        } catch (Exception e) {
            throw new TableIOException("Cannot convert jython type to pig datatype "+ e);
        }
    }
    
	static protected class SigParser 
	{
		private String m_methodName;
		private PyFunction m_pyFunction;
		private Class<?> m_returnType;
		private Class<?>[] m_argTypes;
		
		public SigParser(String methodName, PyFunction pyFunc) 
		{
			m_methodName = methodName;
			m_pyFunction = pyFunc;
			
			init();
		}

		public Class<?> getResultType()
		{
			return m_returnType;
		}
		
		public Class<?> [] getArgTypes()
		{
			return m_argTypes;
		}
		
		/**
		 * Parse out the signature, will be in the form:
		 * public <dataType> 
		 */
		private void init() 
		{
			String sig = m_pyFunction.__doc__.toString();
			
			// remove multiple runs of spaces
			while (sig.indexOf("  ") > -1) {
				sig = sig.replace("  ", " ");
			}
			
			while (sig.indexOf(" (") > -1) {
				sig = sig.replace(" (", "(");
			}
			
			String tokens[] = sig.split(" ");
			// should be at least 3 tokens at this point; 
			// "public", "<data type>", and <methodName(...
			if (tokens.length < 4)
				throw new InvalidOperatorException("Malformed J/Python: Invalid @sig: " + sig); 
			
			if (!"@sig".equals(tokens[0]))
				throw new InvalidOperatorException("Malformed J/Python: @sig not found: " + sig); 
			
			if (!"public".equals(tokens[1]))
				throw new InvalidOperatorException("Malformed J/Python: @sig not \"public\": " + sig); 
			
			m_returnType = parseClassRef(tokens[2]);
			
			// token 3 should start with the method name
			String mnP = m_methodName + "(";
			if (!tokens[3].startsWith(mnP))
				throw new InvalidOperatorException("Malformed J/Python: method name mismatch: " + sig); 
			
			int idx = sig.indexOf(mnP);
			String sigPrime = sig.substring(idx + mnP.length());
			if (sigPrime.endsWith(")"))
				sigPrime = sigPrime.substring(0, sigPrime.length() - 1);
			
			tokens = sigPrime.split("\\,");
			List<Class<?>> argTypes = new ArrayList<Class<?>>(tokens.length);
			
			for (String t : tokens) {
				int spIdx = t.indexOf(" ");
				if (spIdx == -1)
					throw new InvalidOperatorException("Malformed J/Python: missing arg type: " + sig); 
					
				Class<?> argType = parseClassRef(t.substring(0, spIdx));
				if (argType == null || argType == void.class)
					throw new InvalidOperatorException("Malformed J/Python: missing/invalid arg type: " + sig); 
				
				argTypes.add(argType);
			}
			
			m_argTypes = argTypes.toArray(new Class<?>[] {});
		}

		private Class<?> parseClassRef(String className) 
		{
			
			try {
				return Class.forName(className);
			} 
			catch (ClassNotFoundException e) {
				switch (className) {
					case "double":
						return double.class;
						
					case "float":
						return float.class;
					
					case "int":
						return int.class;
						
					case "long":
						return long.class;
						
					case "boolean":
						return boolean.class;
						
					case "void":
						return void.class;
						
					default:
						return null;
				}
			}
		}
		
	}
}
