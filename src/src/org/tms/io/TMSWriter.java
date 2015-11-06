package org.tms.io;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.GZIPOutputStream;

import org.tms.api.io.TMSOptions;
import org.tms.api.utils.ApiVersion;

import com.thoughtworks.xstream.XStream;

public class TMSWriter extends ArchivalWriter<TMSOptions>
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
        writeHeader();
        XStream xs = getXStream(this);
        GZIPOutputStream gz = new GZIPOutputStream(getOutputStream());
        xs.toXML(getTable(), gz);
        gz.finish();
    }

    private void writeHeader() throws IOException
    {
        OutputStream out = getOutputStream();
        
        String head = "TMSTB[" + ApiVersion.CURRENT_VERSION.toFullVersionString() + "]";
        out.write(head.getBytes());
        out.flush();
    }   
}
