package org.tms.io;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.GZIPInputStream;

import org.tms.api.ElementType;
import org.tms.api.Table;
import org.tms.api.TableContext;
import org.tms.api.exceptions.TableIOException;
import org.tms.api.factories.TableContextFactory;
import org.tms.api.io.TMSOptions;
import org.tms.api.utils.ApiVersion;

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
        ElementType eType = readHeader();
        if (eType == ElementType.TableContext)
            throw new TableIOException("Table file required; TableContext file was found.");
        
        XStream xs = super.getXStream(this);        
        return (Table)xs.fromXML(new GZIPInputStream(getInputStream()));
    }


    public void parseTableContext() throws IOException
    {
        readHeader();
        XStream xs = super.getXStream(this);
        xs.fromXML(new GZIPInputStream(getInputStream()));       
    } 
    
    @SuppressWarnings("resource")
	private ElementType readHeader() throws IOException
    {
        InputStream in = getInputStream();
        if (in == null || in.available() < 32)
            throw new TableIOException("Invalid File Format");

        byte [] head = new byte[64];
        in.read(head, 0, 6);
        
        String tmsStr = new String( new byte[] {head[0], head[1], head[2]});
        if (tmsStr == null || !tmsStr.equalsIgnoreCase("TMS"))
            throw new TableIOException("Invalid File Format");
        
        String teStr = new String( new byte[] {head[3], head[4]});
        ElementType eType = parseElementType(teStr);
        if (eType == null)
            throw new TableIOException("Invalid File Format: " + teStr);

        String verStr = "";
        byte [] b = new byte [1];
        int readBytes = 0;
        while (readBytes < 32 && (b[0] != ']')) {
            in.read(b);
            if (b[0] != ']')
                verStr += new String(b);
            readBytes++;
        }
        
        if (b[0] != ']')
            throw new TableIOException("Invalid File Format: No Version");
        
        ApiVersion ver = ApiVersion.parse(verStr);
        if (ver == null || ver.isUnknown())
        	throw new TableIOException("Invalid File Format: No Version");
        
        return eType;
    }

    private ElementType parseElementType(String teStr)
    {
        if ("TC".equalsIgnoreCase(teStr))
            return ElementType.TableContext;
        
        if ("TB".equalsIgnoreCase(teStr))
            return ElementType.Table;
        
        return null;
    }
}
