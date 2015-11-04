package org.tms.io;

import java.io.File;
import java.io.IOException;

import org.tms.api.Table;
import org.tms.api.TableContext;
import org.tms.api.factories.TableContextFactory;
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

public class XMLReader extends BaseReader<XMLOptions>
{
    public XMLReader(String fileName, XMLOptions format)
    {
        this(fileName, TableContextFactory.fetchDefaultTableContext(), format);
    }

    public XMLReader(String fileName, TableContext context, XMLOptions format)
    {
        this(new File(fileName), context, format);
    }

    public XMLReader(File xmlFile, TableContext context, XMLOptions format)
    {
        super(xmlFile, context, format);       
    }
    
    public Table parse() throws IOException
    {
        XStream xs = getXStream();
        return (Table)xs.fromXML(getInputFile());
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
