package org.tms.tds.util;

import java.io.File;

import org.tms.api.utils.TableCellValidator;

public class JythonCellValidator 
{
	public static TableCellValidator construct(String fileName, String valMethod) 
	{
    	File f = new File(fileName);
    	String className = f.getName();
    	int idx = className.indexOf('.');
    	if (idx > -1)
    		className = className.substring(0, idx);
    	
		return JythonCellTransformer.construct(TableCellValidator.class, fileName, className, valMethod, null);
	}
	
	public static TableCellValidator construct(String fileName, String className, String valMethod) 
	{
		return JythonCellTransformer.construct(TableCellValidator.class, fileName, className, valMethod, null);
	}
}
