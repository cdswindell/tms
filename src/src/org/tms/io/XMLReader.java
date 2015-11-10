package org.tms.io;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.tms.api.Table;
import org.tms.api.TableContext;
import org.tms.api.factories.TableContextFactory;
import org.tms.api.io.XMLOptions;

import com.thoughtworks.xstream.XStream;

public class XMLReader extends ArchivalReader<XMLOptions>
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
    
    public XMLReader(InputStream in, TableContext context, XMLOptions format)
    {
        super(in, context, format);       
    }

    public Table parse() throws IOException
    {
        XStream xs = super.getXStream(this);
        return (Table)xs.fromXML(getInputStream());
    }

    public void parseTableContext()
    {
        XStream xs = super.getXStream(this);
        xs.fromXML(getInputStream());
    } 
}
