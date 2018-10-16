package org.tms.io;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.GZIPOutputStream;

import org.tms.api.ElementType;
import org.tms.api.io.TMSOptions;
import org.tms.api.utils.ApiVersion;

import com.thoughtworks.xstream.XStream;

public class TMSWriter extends XStreamWriter<TMSOptions>
{
    public static void export(TableExportAdapter tableExportAdapter, OutputStream out, TMSOptions options) 
    throws IOException
    {
        TMSWriter tw = new TMSWriter(tableExportAdapter, out, options);
        tw.export();
    }

    @SuppressWarnings("resource")
	public static void export(TableExportAdapter tableExportAdapter, File file, TMSOptions options) 
    throws IOException
    {
        export(tableExportAdapter, new FileOutputStream(file), options);
    }

    public static void exportTableContext(TableContextExportAdapter tcea, OutputStream out, TMSOptions options) 
    throws IOException
    {
        TMSWriter tw = new TMSWriter(tcea, out, options);
        tw.exportTableContext();       
    }   
    
    TMSWriter(TableExportAdapter tw, TMSOptions options)
    {
        super(tw, null, options);
    }

    private TMSWriter(TableExportAdapter tw, OutputStream out, TMSOptions options)
    {
        super(tw, out, options);
    }

    @Override
    protected void export() throws IOException
    {
    	try {
	        writeHeader(ElementType.Table);
	        XStream xs = getXStream(this);
	        GZIPOutputStream gz = new GZIPOutputStream(getOutputStream());
	        xs.toXML(getTable(), gz);
	        gz.finish();
    	}
    	catch (Exception e) {
    		throw e;
    	}
    }

    private void exportTableContext() throws IOException
    {
    	try {
	        writeHeader(ElementType.TableContext);
	        XStream xs = getXStream(this);
	        GZIPOutputStream gz = new GZIPOutputStream(getOutputStream());
	        xs.toXML(getTableContext(), gz);
	        gz.finish();
    	}
    	catch (Exception e) {
    		throw e;
    	}
    }

    @SuppressWarnings("resource")
	private void writeHeader(ElementType et) throws IOException
    {
        OutputStream out = getOutputStream();
        String eType = et == ElementType.Table ? "TB" : et == ElementType.TableContext ? "TC" : null;
        
        String head = "TMS" + eType + "[" + ApiVersion.CURRENT_VERSION.toFullVersionString() + "]";
        out.write(head.getBytes());
        out.flush();
    }
    
    @Override
    public XStreamWriter<?> createDelegate(TableExportAdapter tea)
    {
    	XStreamWriter<?> writer = new TMSWriter(tea, (TMSOptions)options());;
        
        this.reset(tea);
        return writer;
    }
}
