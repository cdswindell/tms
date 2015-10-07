package org.tms.io.jasper;

import java.io.FileOutputStream;
import java.io.IOException;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.export.ooxml.JRDocxExporter;
import net.sf.jasperreports.export.ExporterInput;
import net.sf.jasperreports.export.OutputStreamExporterOutput;
import net.sf.jasperreports.export.SimpleExporterInput;
import net.sf.jasperreports.export.SimpleOutputStreamExporterOutput;

import org.tms.io.BaseWriter;

public class DOCXReport extends TMSReport
{
    public DOCXReport(BaseWriter w)
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
            JRDocxExporter rtfExporter  = new JRDocxExporter();
            
            ExporterInput inp = new SimpleExporterInput(getPrint());
            rtfExporter.setExporterInput(inp);
            
            OutputStreamExporterOutput output = new SimpleOutputStreamExporterOutput(out);
            rtfExporter.setExporterOutput(output);
            
            rtfExporter.exportReport();           
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
