package org.tms.io;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.GZIPInputStream;

import org.tms.api.Table;
import org.tms.api.TableContext;
import org.tms.api.factories.TableContextFactory;
import org.tms.api.io.TMSOptions;

import com.thoughtworks.xstream.XStream;

public class TMSReader extends ArchivalReader<TMSOptions>
{
    public TMSReader(String fileName)
    {
        this(fileName, TableContextFactory.fetchDefaultTableContext(), TMSOptions.Default);
    }

    public TMSReader(String fileName, TMSOptions format)
    {
        this(fileName, TableContextFactory.fetchDefaultTableContext(), format);
    }

    public TMSReader(String fileName, TableContext context, TMSOptions format)
    {
        this(new File(fileName), context, format);
    }

    public TMSReader(File xmlFile, TableContext context, TMSOptions format)
    {
        super(xmlFile, context, format);       
    }
    
    public TMSReader(InputStream in, TableContext context, TMSOptions format)
    {
        super(in, context, format);       
    }

    public Table parse() throws IOException
    {
        XStream xs = super.getXStream(this);
        return (Table)xs.fromXML(new GZIPInputStream(getInputStream()));
    } 
}
