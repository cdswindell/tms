package org.tms.io;

import java.io.IOException;
import java.io.OutputStream;

import org.tms.api.io.IOOption;
import org.tms.tds.logs.LogsTableImpl;

public class LogsTableExportAdapter extends TableExportAdapter
{
    public LogsTableExportAdapter(LogsTableImpl t, String fileName, IOOption<?> options) 
    throws IOException
    {
        super(t, fileName, options);
    }

    public LogsTableExportAdapter(LogsTableImpl t, OutputStream out, IOOption<?> options) 
    throws IOException
    {
        super(t, out, options);
    }
}
