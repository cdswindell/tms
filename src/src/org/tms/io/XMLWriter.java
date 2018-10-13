package org.tms.io;

import java.io.IOException;
import java.io.OutputStream;

import org.tms.api.io.XMLOptions;

import com.thoughtworks.xstream.XStream;

public class XMLWriter extends XStreamWriter<XMLOptions>
{
    public static void export(TableExportAdapter tea, OutputStream out, XMLOptions options) 
    throws IOException
    {
        XMLWriter writer = new XMLWriter(tea, out, options);
        writer.export();        
    }
    
    public static void exportTableContext(TableContextExportAdapter tea, OutputStream out, XMLOptions options)
    {
        XMLWriter writer = new XMLWriter(tea, out, options);
        writer.exportTableContext();        
    }
    
    /*
     * Constructors
     */
    private XMLWriter(TableExportAdapter t, OutputStream out, XMLOptions options)
    {
        super(t, out, options);
    }

    XMLWriter(TableExportAdapter tw, XMLOptions options)
    {
        super(tw, null, options);
    }

    /*
     * Methods
     */
    @Override
    protected void export() throws IOException
    {
        XStream xs = getXStream(this);
        xs.toXML(getTable(), getOutputStream());
    }

    private void exportTableContext()
    {
        XStream xs = getXStream(this);
        xs.toXML(getTableContext(), getOutputStream());
    }
    
    @Override
    public XStreamWriter<?> createDelegate(TableExportAdapter tea)
    {
    	XStreamWriter<?> writer = new XMLWriter(tea, (XMLOptions)options());;
        
        this.reset(tea);
        return writer;
    }
}
