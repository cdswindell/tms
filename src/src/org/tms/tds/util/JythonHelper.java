package org.tms.tds.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.python.core.Py;
import org.python.core.PyDictionary;
import org.python.core.PyFloat;
import org.python.core.PyFunction;
import org.python.core.PyInteger;
import org.python.core.PyList;
import org.python.core.PyLong;
import org.python.core.PyNone;
import org.python.core.PyObject;
import org.python.core.PyString;
import org.python.util.PythonInterpreter;
import org.tms.api.derivables.InvalidOperatorException;

public class JythonHelper 
{
	public static Object pythonToPig(PyObject pyObject) 
	{
		try {
			Object javaObj = null;
			// Add code for all supported pig types here
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
			else
				throw new InvalidOperatorException(String.format("Cannot convert python type %s", pyObject.fastGetClass()));

			return javaObj;
		} 
		catch (InvalidOperatorException e) {
			throw e;
		}
		catch (Exception e) {
			throw new InvalidOperatorException(e);
		}
	}

	public  static Map<String, PyFunction> extractMethods(PythonInterpreter pi, PyObject dirObj) 
	{
		PyObject pyThing;
		@SuppressWarnings("unchecked")
		List<Object> dirList = (List<Object>)JythonHelper.pythonToPig(dirObj);
		Map<String, PyFunction> posibleFuncMap = new HashMap<String, PyFunction>();
		for (Object li : dirList) {
			if (li instanceof String) {
				String s = (String)li;
				if (!(s.startsWith("__") && s.endsWith("__"))) {
					pyThing = pi.get(s);
					if (pyThing != null && pyThing instanceof PyFunction) {
						PyFunction pyFunc = (PyFunction)pyThing;

						// only harvest functions with valid @sigs
						if ((pyFunc.__doc__ != null && !(pyFunc.__doc__ instanceof PyNone)))
							posibleFuncMap.put(s, pyFunc);
					}
				}
			}
		}
		
		return posibleFuncMap;
	}

	private String m_methodName;
	private PyFunction m_pyFunction;
	private Class<?> m_returnType;
	private Class<?>[] m_argTypes;

	public JythonHelper(String methodName, PyFunction pyFunc) 
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
			t = t.trim();
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

			case "short":
				return short.class;

			case "byte":
				return byte.class;

			case "char":
				return char.class;

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
