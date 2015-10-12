package org.tms.io;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.tms.api.io.options.TMSOptions;

public class TMSWriter extends BaseWriter
{
    public static void export(TableExportAdapter tableExportAdapter, OutputStream out, TMSOptions options) 
    throws IOException
    {
        TMSWriter tw = new TMSWriter(tableExportAdapter, out, options);
        tw.export();
    }

    public static void export(TableExportAdapter tableExportAdapter, File file, TMSOptions options) 
    throws IOException
    {
        export(tableExportAdapter, new FileOutputStream(file), options);
    }

    public TMSWriter(TableExportAdapter tw, OutputStream out, TMSOptions options)
    {
        super(tw, out, options);
    }

    @Override
    protected void export() throws IOException
    {
        // TODO Auto-generated method stub
        
    }
}
