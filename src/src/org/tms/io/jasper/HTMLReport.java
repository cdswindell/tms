package org.tms.io.jasper;

import java.io.FileOutputStream;
import java.io.IOException;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.export.HtmlExporter;
import net.sf.jasperreports.export.ExporterInput;
import net.sf.jasperreports.export.HtmlExporterOutput;
import net.sf.jasperreports.export.SimpleExporterInput;
import net.sf.jasperreports.export.SimpleHtmlExporterOutput;

import org.tms.io.BaseWriter;

public class HTMLReport extends TMSReport
{
    public HTMLReport(BaseWriter w)
    {
        super(w);
    }
    
    @Override
    public void export() 
    throws IOException
    {
        FileOutputStream out = null;
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
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        finally {
            if (out != null)
                out.close();
        }        
    }
}
