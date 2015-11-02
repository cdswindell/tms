package org.tms.io;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

import org.tms.api.Column;
import org.tms.api.Row;
import org.tms.api.Subset;
import org.tms.api.io.BaseIOOption;

public class SubsetExportAdapter extends TableExportAdapter
{
    private Subset m_subset;
    
    public SubsetExportAdapter(Subset s, String fileName, BaseIOOption<?> options) 
    throws IOException
    {
        super(s.getTable(), fileName, options);
        m_subset = s;
    }

    public SubsetExportAdapter(Subset s, OutputStream out, BaseIOOption<?> options) 
    throws IOException
    {
        super(s.getTable(), out, options);
        m_subset = s;
    }

    @Override
    public int getNumRows()
    {
        int nRows = m_subset.getNumRows();
        return nRows > 0 ? nRows : getTable().getNumRows();
    }

    @Override
    public List<Row> getRows()
    {
        List<Row> rows = m_subset.getRows();
        return rows != null && rows.size() > 0 ? rows : getTable().getRows();
    }
    
    @Override
    public int getNumColumns()
    {
        int nCols = m_subset.getNumColumns();
        return nCols > 0 ? nCols : getTable().getNumColumns();
    }
    
    @Override
    public List<Column> getColumns()
    {
        List<Column> cols = m_subset.getColumns();
        return cols != null && cols.size() > 0 ? cols : getTable().getColumns();
    }    
}
