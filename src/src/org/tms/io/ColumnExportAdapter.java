package org.tms.io;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import org.tms.api.Column;
import org.tms.api.Row;
import org.tms.api.io.options.IOOptions;

public class ColumnExportAdapter extends TableExportAdapter
{
    private Column m_col;
    
    public ColumnExportAdapter(Column c, String fileName, IOOptions options) 
    throws IOException
    {
        super(c.getTable(), fileName, options);
        m_col = c;
    }

    @Override
    public int getNumRows()
    {
        return getTable().getNumRows();
    }

    @Override
    public List<Row> getRows()
    {
        return getTable().getRows();
    }
    
    @Override
    public int getNumColumns()
    {
        return 1;
    }
    
    @Override
    public List<Column> getColumns()
    {
        return Collections.singletonList(m_col);
    }    
}