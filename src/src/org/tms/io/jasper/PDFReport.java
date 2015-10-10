package org.tms.io.jasper;

import java.io.FileOutputStream;
import java.io.IOException;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.export.JRPdfExporter;
import net.sf.jasperreports.export.ExporterInput;
import net.sf.jasperreports.export.OutputStreamExporterOutput;
import net.sf.jasperreports.export.SimpleExporterInput;
import net.sf.jasperreports.export.SimpleOutputStreamExporterOutput;

import org.tms.io.BaseWriter;

public class PDFReport extends TMSReport
{
    public PDFReport(BaseWriter w)
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
            out = prepareReport();
            
            // print report to file
            JRPdfExporter exporter = new JRPdfExporter();
            
            ExporterInput inp = new SimpleExporterInput(getExporterInputItems());
            exporter.setExporterInput(inp);
            
            OutputStreamExporterOutput output = new SimpleOutputStreamExporterOutput(out);
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
