package org.tms.io;

import java.io.IOException;
import java.io.OutputStream;

import org.tms.api.io.XMLOptions;

import com.thoughtworks.xstream.XStream;

public class XMLWriter extends ArchivalWriter<XMLOptions>
{
    public static void export(TableExportAdapter tea, OutputStream out, XMLOptions options) 
    throws IOException
    {
        XMLWriter writer = new XMLWriter(tea, out, options);
        writer.export();        
    }
    
    private XMLWriter(TableExportAdapter t, OutputStream out, XMLOptions options)
    {
        super(t, out, options);
    }

    @Override
    protected void export() throws IOException
    {
        XStream xs = getXStream(this);
        xs.toXML(getTable(), getOutputStream());
    }
}
