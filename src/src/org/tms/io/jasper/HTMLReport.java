package org.tms.io.jasper;

import java.io.IOException;
import java.io.OutputStream;

import org.tms.api.exceptions.TableIOException;
import org.tms.io.LabeledWriter;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.export.HtmlExporter;
import net.sf.jasperreports.export.ExporterInput;
import net.sf.jasperreports.export.HtmlExporterOutput;
import net.sf.jasperreports.export.SimpleExporterInput;
import net.sf.jasperreports.export.SimpleHtmlExporterOutput;

public class HTMLReport extends TMSReport
{
    public HTMLReport(LabeledWriter<?> w)
    {
        super(w);
    }
    
    @Override
    public void export() 
    throws IOException
    {
        OutputStream out = null;
        try
        {
            // generate the report and convert the file name to an output stream
            out = prepareReport();
            
            // print report to file
            HtmlExporter exporter = new HtmlExporter();
            
            ExporterInput inp = new SimpleExporterInput(getExporterInputItems());
            exporter.setExporterInput(inp);
            
            HtmlExporterOutput output = new SimpleHtmlExporterOutput(out);
            exporter.setExporterOutput(output);
            
            exporter.exportReport();
        }
        catch (JRException e)
        {
            throw new TableIOException(e);
        }
        finally {
        	if (out != null)
        		out.close();
        }
        
    }
}
