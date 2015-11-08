package org.tms.io;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Collections;
import java.util.List;

import org.tms.api.Column;
import org.tms.api.ElementType;
import org.tms.api.Row;
import org.tms.api.io.IOOption;

public class RowExportAdapter extends TableExportAdapter
{
    private Row m_row;
    
    public RowExportAdapter(Row r, String fileName, IOOption<?> options) 
    throws IOException
    {
        super(r.getTable(), fileName, options);
        m_row = r;
    }

    public RowExportAdapter(Row r, OutputStream out, IOOption<?> options) 
    throws IOException
    {
        super(r.getTable(), out, options);
        m_row = r;
    }

    public ElementType getTableElementType()
    {
    	return ElementType.Row;
    }
    
    @Override
    public int getNumRows()
    {
        return 1;
    }

    @Override
    public List<Row> getRows()
    {
        return Collections.singletonList(m_row);
    }
    
    @Override
    public int getNumColumns()
    {
        return getTable().getNumColumns();
    }
    
    @Override
    public List<Column> getColumns()
    {
        return getTable().getColumns();
    }    
}
