package org.tms.io;

import java.io.IOException;
import java.util.List;

import org.tms.api.Column;
import org.tms.api.Row;
import org.tms.api.Subset;
import org.tms.io.options.IOOptions;

public class SubsetExportAdapter extends TableExportAdapter
{
    private Subset m_subset;
    
    public SubsetExportAdapter(Subset s, String fileName, IOOptions options) 
    throws IOException
    {
        super(s.getTable(), fileName, options);
        m_subset = s;
    }

    @Override
    public int getNumRows()
    {
        return m_subset.getNumRows();
    }

    @Override
    public List<Row> getRows()
    {
        return null;
    }
    
    @Override
    public int getNumColumns()
    {
        return m_subset.getNumColumns();
    }
    
    @Override
    public List<Column> getColumns()
    {
        return null;
    }    
}
