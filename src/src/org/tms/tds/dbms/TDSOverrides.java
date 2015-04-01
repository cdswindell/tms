package org.tms.tds.dbms;

import org.tms.api.Access;
import org.tms.api.ElementType;
import org.tms.api.exceptions.UnsupportedImplementationException;


public interface TDSOverrides
{
    default public void delete()
    {
        throw new UnsupportedImplementationException(ElementType.Cell, "Cannot delete a database element row/column");
    }
    
    default public boolean clear()
    {
        throw new UnsupportedImplementationException(ElementType.Cell, "Cannot clear a database element row/column");
    }
    
    default public boolean fill(Object o)
    {
        throw new UnsupportedImplementationException(ElementType.Cell, "Cannot fill a database row/column");
    }
    
    default public void fill(Object o, int n, Access access, Object... mda)
    {
        throw new UnsupportedImplementationException(ElementType.Cell, "Cannot fill a database row/column");
    }
    
    default public void fill(Object[] o, Access access, Object... mda)
    {
        throw new UnsupportedImplementationException(ElementType.Cell, "Cannot fill a database row/column");
    }
}
