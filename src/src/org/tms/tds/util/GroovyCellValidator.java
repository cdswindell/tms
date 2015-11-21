package org.tms.tds.util;

import org.tms.api.utils.TableCellValidator;

public class GroovyCellValidator 
{
	public static TableCellValidator construct(String fileName, String valMethod) 
	{
		return GroovyCellTransformer.construct(TableCellValidator.class, fileName, valMethod, (String)null);
	}
}
