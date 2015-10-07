package org.tms.io;

import java.io.File;
import java.io.IOException;

import org.tms.api.io.options.TMSOptions;

public class TMSWriter extends BaseWriter
{
    public static void export(TableExportAdapter tableExportAdapter, File file, TMSOptions options) 
    throws IOException
    {
        TMSWriter tw = new TMSWriter(tableExportAdapter, file, options);
        tw.export();
    }

    public TMSWriter(TableExportAdapter tw, File f, TMSOptions options)
    {
        super(tw, f, options);
    }

    @Override
    protected void export() throws IOException
    {
        // TODO Auto-generated method stub
        
    }

}
