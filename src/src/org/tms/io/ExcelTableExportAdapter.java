package org.tms.io;

import java.io.IOException;
import java.io.OutputStream;

import org.tms.api.io.IOOption;
import org.tms.tds.excel.ExcelTableImpl;

public class ExcelTableExportAdapter extends TableExportAdapter
{
    public ExcelTableExportAdapter(ExcelTableImpl t, String fileName, IOOption<?> options) 
    throws IOException
    {
        super(t, fileName, options);
    }

    public ExcelTableExportAdapter(ExcelTableImpl t, OutputStream out, IOOption<?> options) 
    throws IOException
    {
        super(t, out, options);
    }
}
