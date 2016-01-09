package org.tms.io;

import java.io.IOException;
import java.io.OutputStream;

import org.tms.api.io.IOOption;
import org.tms.tds.dbms.DbmsTableImpl;

public class DbmsTableExportAdapter extends TableExportAdapter
{
    public DbmsTableExportAdapter(DbmsTableImpl t, String fileName, IOOption<?> options) 
    throws IOException
    {
        super(t, fileName, options);
    }

    public DbmsTableExportAdapter(DbmsTableImpl t, OutputStream out, IOOption<?> options) 
    throws IOException
    {
        super(t, out, options);
    }
}
