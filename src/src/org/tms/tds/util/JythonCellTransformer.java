package org.tms.tds.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Map;

import org.codehaus.groovy.control.CompilationFailedException;
import org.python.core.Py;
import org.python.core.PyCode;
import org.python.core.PyFunction;
import org.python.core.PyObject;
import org.python.util.PythonInterpreter;
import org.tms.api.exceptions.ConstraintViolationException;
import org.tms.api.utils.TableCellTransformer;
import org.tms.api.utils.TableCellValidator;

public class JythonCellTransformer implements TableCellTransformer
{
	private static final long serialVersionUID = 4765579905455224444L;

	public static TableCellValidator construct(String fileName, String valName, String transName)
    {
    	File f = new File(fileName);
    	String className = f.getName();
    	int idx = className.indexOf('.');
    	if (idx > -1)
    		className = className.substring(0, idx);
    	
    	return construct(fileName, className, valName, transName);
    }
    
    public static final TableCellValidator construct(String text, String className, String valName, String transName)
    {
    	return construct(TableCellTransformer.class, text, className, valName, transName);
    }
    
    static final TableCellValidator construct(Class<? extends TableCellValidator> clazz, 
    		String text, String className, String valName, String transName)
    {
        PyCode pyCode;
        PythonInterpreter pi = null;
    	try {
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
		            // first, see if we can instantiate the object, and if we can, is it an TableCellTransformer?
		            // if it is, return it           
		            try {
						// cast the object to an operator, success??
		            	TableCellTransformer tcv = (TableCellTransformer)pyObj.__tojava__(TableCellTransformer.class);
						return tcv;
		            } 
		            catch (Exception e) { }
		            
		            // ok, is it a validator?
		            try {
						// cast the object to an operator, success??
		            	TableCellValidator tcv = (TableCellValidator)pyObj.__tojava__(TableCellValidator.class);
		            	
		            	// success, maybe...
		            	if (clazz == TableCellValidator.class)
		            		return tcv;
		            	else
				            throw new IllegalArgumentException("TableCellTransformer request but TableCellValidator found"); 
		            } 
		            catch (IllegalArgumentException e) { throw e;}
		            catch (Exception e) { }
				}
			}
			
			// do a dir() and see what we have
			// if there was no class, harvest functions, if any
			PyObject dirObj = pi.eval("dir()");
			if (dirObj != null) {
		        String valMethName = valName != null ? valName : "validate";

		        boolean transReq = clazz == TableCellTransformer.class;
		        String transMethName = transReq && transName != null && (transName = transName.trim()).length() > 0 
		        		? transName : null;
		        
		        // extract all of the methods from the jython script
				Map<String, PyFunction> posibleFuncMap = JythonHelper.extractMethods(pi, dirObj);
				
				// now get validation function
				PyFunction valFunc = posibleFuncMap.get(valMethName);				
				PyFunction transFunc = transMethName != null ? posibleFuncMap.get(transMethName) : null;
				
				if (valFunc == null)
		            throw new IllegalArgumentException("Cell Validation method not found: " + valMethName); 
				if (transReq && transFunc == null)
		            throw new IllegalArgumentException("Cell Transformation method not found: " + transMethName); 	            	            
	            
			    // otherwise, return our special handler
			    return new JythonCellTransformer(valFunc, transFunc);
			}		
			
            throw new IllegalArgumentException(clazz.getSimpleName() + " not present"); 
    	} 
    	catch (CompilationFailedException | IOException e) {
            throw new IllegalArgumentException("Exception encountered while processing Jython: " + e.getMessage()); 
		}
        finally {
        	if (pi !=  null)
        		pi.close();
        }
    }

    private PyFunction m_validator;
    private PyFunction m_transform;
    
    public JythonCellTransformer(PyFunction valFunc, PyFunction transFunc) 
    {
    	m_validator = valFunc;
    	m_transform = transFunc;
    }

	@Override
    public void validate(Object newValue) throws ConstraintViolationException
    {
        if (m_validator != null) {
            try {
                // set up to call Jython transformer
                PyObject [] mArgs = new PyObject [] { Py.java2py(newValue) };
                
                // Invoke the method on the Jython object, with args
                m_validator.__call__(mArgs);
            }
            catch (Exception e) {
               throw new ConstraintViolationException(e.getMessage());
            }
        }
    }

    @Override
    public Object transform(Object newValue)
    {
        // perform validation
        validate(newValue);
        
        // now transform
        if (m_transform != null) {
            try {
                // set up to call Jython transformer
                PyObject [] mArgs = new PyObject [] { Py.java2py(newValue) };
                
                // Invoke the method on the Jython object, with args
                PyObject pyResult = m_validator.__call__(mArgs);
                return pyResult != null ? JythonHelper.pythonToPig(pyResult) : null;
            }
            catch (Exception e)
            {
                return newValue;
            }
        }
        else
            return newValue;
    }
}
