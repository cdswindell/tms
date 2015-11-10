package org.tms.io;

import java.io.IOException;
import java.io.OutputStream;

import org.tms.api.TableContext;
import org.tms.api.io.IOOption;
import org.tms.api.io.TMSOptions;
import org.tms.api.io.XMLOptions;

public class TableContextExportAdapter extends TableExportAdapter
{
    private TableContext m_context;
    
    public TableContextExportAdapter(TableContext context, String fileName, IOOption<?> options) 
    throws IOException
    {
        super(fileName, options, false);
        m_context = context;
    }
    
    public TableContextExportAdapter(TableContext context, OutputStream out, IOOption<?> options) 
    throws IOException
    {
        super(out, options, false);
        m_context = context;
    }
    
    public TableContext getTableContext()
    {
        return m_context;
    }
    
    @Override
    public void export() 
    throws IOException
    {
        switch (options().getFileFormat()) {
            case TMS:
                TMSWriter.exportTableContext(this, getOutputStream(), (TMSOptions)options());
                break;
                
            case XML:
                XMLWriter.exportTableContext(this, getOutputStream(), (XMLOptions)options());
                break;
                
            default:
                break;
        }
        
        if (isFileBased() && getOutputStream() != null) {
            getOutputStream().flush();
            getOutputStream().close();
        }
        
    }    
    
    @Override
    public int getNumRows()
    {
        return 0;
    }
    
    @Override
    public int getNumColumns()
    {
        return 0;
    }    
}
