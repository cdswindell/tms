package org.tms.io;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Collections;
import java.util.List;

import org.tms.api.Column;
import org.tms.api.Row;
import org.tms.api.io.options.BaseIOOptions;

public class RowExportAdapter extends TableExportAdapter
{
    private Row m_row;
    
    public RowExportAdapter(Row r, String fileName, BaseIOOptions<?> options) 
    throws IOException
    {
        super(r.getTable(), fileName, options);
        m_row = r;
    }

    public RowExportAdapter(Row r, OutputStream out, BaseIOOptions<?> options) 
    throws IOException
    {
        super(r.getTable(), out, options);
        m_row = r;
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
