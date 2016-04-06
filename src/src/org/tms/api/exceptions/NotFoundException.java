package org.tms.api.exceptions;

import org.tms.api.ElementType;

public class NotFoundException extends InvalidException 
{
	private static final long serialVersionUID = 1626436251290341849L;

	public NotFoundException(ElementType tet, String msg) 
	{
		super(tet, msg);
	}
}
