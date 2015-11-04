package org.tms.io;

import java.io.IOException;
import java.io.OutputStream;

import org.tms.api.io.XMLOptions;
import org.tms.io.xml.CellConverter;
import org.tms.io.xml.ColumnConverter;
import org.tms.io.xml.RowConverter;
import org.tms.io.xml.SubsetConverter;
import org.tms.io.xml.TableConverter;
import org.tms.tds.CellImpl;
import org.tms.tds.ColumnImpl;
import org.tms.tds.RowImpl;
import org.tms.tds.SubsetImpl;
import org.tms.tds.TableImpl;

import com.thoughtworks.xstream.XStream;

public class XMLWriter extends BaseWriter<XMLOptions>
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
        XStream xs = getXStream();
        xs.toXML(getTable(), this.getOutputStream());
    }
    
    private XStream getXStream()
    {
        XStream xmlStreamer = new XStream();
        xmlStreamer = new XStream();
            
        xmlStreamer.alias("table", TableImpl.class);
        xmlStreamer.alias("row", RowImpl.class);
        xmlStreamer.alias("column", ColumnImpl.class);
        xmlStreamer.alias("subset", SubsetImpl.class);
        xmlStreamer.alias("cell", CellImpl.class);
            
        xmlStreamer.registerConverter(new TableConverter(this));
        xmlStreamer.registerConverter(new RowConverter(this));
        xmlStreamer.registerConverter(new ColumnConverter(this));
        xmlStreamer.registerConverter(new SubsetConverter(this));
        xmlStreamer.registerConverter(new CellConverter(this));
        
        return xmlStreamer;
    }
}
