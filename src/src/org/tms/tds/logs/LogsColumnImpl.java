package org.tms.tds.logs;

import org.tms.api.Access;
import org.tms.api.ElementType;
import org.tms.api.derivables.Derivation;
import org.tms.api.exceptions.UnsupportedImplementationException;
import org.tms.tds.CellImpl;
import org.tms.tds.ColumnImpl;
import org.tms.tds.RowImpl;
import org.tms.tds.TableImpl;

public class LogsColumnImpl extends ColumnImpl
{
	private int m_fieldIndex;
	
    public LogsColumnImpl(TableImpl parentTable, int fieldIndex, String colName, Class<?> dataType)
    {
        super(parentTable);
        
        m_fieldIndex = fieldIndex;
        setReadOnly(true);
    	if (colName != null)
    		setLabel(colName);
    	
        if (dataType != null) {
            setDataType(dataType);
            setStronglyTyped(true);
        }
    }

    @Override
    protected CellImpl getCellInternal(RowImpl row, boolean createIfSparse, boolean setCurrent)
    {
        // in that this column is a database cell, we need to override createIfSparse
        if (!createIfSparse && (row instanceof LogsRowImpl))
            createIfSparse = true;
        
        return super.getCellInternal(row, createIfSparse, setCurrent);
    }
    
    /**
     * Return the 1-based index of the row in the dbms table result set
     * @return the 1-based index of the row in the dbms table result set
     */
    protected int getFieldIndex()
    {
        return m_fieldIndex;
    }
    
    @Override
    public LogsTableImpl getTable()
    {
        return (LogsTableImpl)super.getTable();
    }
    
    @Override
    protected CellImpl createNewCell(RowImpl row)
    {
        if (row instanceof LogsRowImpl)
            return new LogsCellImpl((LogsRowImpl) row, this);
        else
            return super.createNewCell(row);
    }

    @Override
    public Derivation setDerivation(String expr) 
    {
        throw new UnsupportedImplementationException(this, "Cannot set a derivation on a log file column");
    }
    
    @Override
    public boolean clear()
    {
        throw new UnsupportedImplementationException(ElementType.Cell, "Cannot clear a log file element");
    }
    
    @Override
    public boolean fill(Object o)
    {
        throw new UnsupportedImplementationException(ElementType.Cell, "Cannot fill a log file row/column");
    }
    
    @Override
    public void fill(Object o, int n, Access access, Object... mda)
    {
        throw new UnsupportedImplementationException(ElementType.Cell, "Cannot fill a log file row/column");
    }
    
    @Override
    public void fill(Object[] o, Access access, Object... mda)
    {
        throw new UnsupportedImplementationException(ElementType.Cell, "Cannot fill a log file row/column");
    }
}
