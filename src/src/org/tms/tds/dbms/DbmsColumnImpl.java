package org.tms.tds.dbms;

import org.tms.api.Access;
import org.tms.api.ElementType;
import org.tms.api.derivables.Derivation;
import org.tms.api.exceptions.IllegalTableStateException;
import org.tms.api.exceptions.UnsupportedImplementationException;
import org.tms.tds.CellImpl;
import org.tms.tds.ColumnImpl;
import org.tms.tds.RowImpl;
import org.tms.tds.TableImpl;

public class DbmsColumnImpl extends ColumnImpl
{
    private int m_resultSetIndex;

    public DbmsColumnImpl(TableImpl parentTable, int resultSetIndex, String clazzName)
    {
        super(parentTable);
        
        m_resultSetIndex = resultSetIndex;    
        setReadOnly(true);
        try
        {
            if (clazzName != null && (clazzName = clazzName.trim()).length() > 0) {
                setDataTypeInternal(Class.forName(clazzName));
                setStronglyTyped(true);
            }
        }
        catch (ClassNotFoundException e)
        {
            throw new IllegalTableStateException("Cannot load DBMS driver: " + e.getMessage());
        }
    }

    @Override
    protected CellImpl getCellInternal(RowImpl row, boolean createIfSparse, boolean setCurrent)
    {
        // in that this column is a database cell, we need to override createIfSparse
        if (!createIfSparse && (row instanceof DbmsRowImpl))
            createIfSparse = true;
        
        return super.getCellInternal(row, createIfSparse, setCurrent);
    }
    
    /**
     * Return the 1-based index of the row in the dbms table result set
     * @return the 1-based index of the row in the dbms table result set
     */
    protected int getResultSetIndex()
    {
        return m_resultSetIndex;
    }
    
    @Override
    public DbmsTableImpl getTable()
    {
        return (DbmsTableImpl)super.getTable();
    }
    
    @Override
    protected CellImpl createNewCell(RowImpl row)
    {
        if (row instanceof DbmsRowImpl)
            return new DbmsCellImpl((DbmsRowImpl) row, this);
        else
            return super.createNewCell(row);
    }

	protected void setLabelInternal(String columnLabel) 
	{
		super.setLabel(columnLabel);		
	}
	
    protected void setDataTypeInternal(Class<?> dataType)
    {
        super.setDataType(dataType);;
    }
    
    @Override
    public void setDataType(Class<?> dataType)
    {
        throw new UnsupportedImplementationException(this, "Cannot set the data type on a database column");
    }
    
    @Override
    public Derivation setDerivation(String expr) 
    {
        throw new UnsupportedImplementationException(this, "Cannot set a derivation on a database column");
    }
    
    @Override
    public boolean clear()
    {
        throw new UnsupportedImplementationException(ElementType.Cell, "Cannot clear a database element");
    }
    
    @Override
    public boolean fill(Object o)
    {
        throw new UnsupportedImplementationException(ElementType.Cell, "Cannot fill a database row/column");
    }
    
    @Override
    public void fill(Object o, int n, Access access, Object... mda)
    {
        throw new UnsupportedImplementationException(ElementType.Cell, "Cannot fill a database row/column");
    }
    
    @Override
    public void fill(Object[] o, Access access, Object... mda)
    {
        throw new UnsupportedImplementationException(ElementType.Cell, "Cannot fill a database row/column");
    }
}
