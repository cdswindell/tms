package org.tms.io.jasper;

import java.io.IOException;
import java.io.OutputStream;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.export.ooxml.JRDocxExporter;
import net.sf.jasperreports.export.ExporterInput;
import net.sf.jasperreports.export.OutputStreamExporterOutput;
import net.sf.jasperreports.export.SimpleDocxReportConfiguration;
import net.sf.jasperreports.export.SimpleExporterInput;
import net.sf.jasperreports.export.SimpleOutputStreamExporterOutput;

import org.tms.api.exceptions.TableIOException;
import org.tms.io.BaseWriter;

public class DocXReport extends TMSReport
{
    public DocXReport(BaseWriter<?> w)
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
            out = prepareReport();
            
            // print report to file
            JRDocxExporter docExporter  = new JRDocxExporter();
            
            SimpleDocxReportConfiguration rc = new SimpleDocxReportConfiguration();           
            rc.setFlexibleRowHeight(true);
            docExporter.setConfiguration(rc);
            
            ExporterInput inp = new SimpleExporterInput(getExporterInputItems());
            docExporter.setExporterInput(inp);
            
            OutputStreamExporterOutput output = new SimpleOutputStreamExporterOutput(out);
            docExporter.setExporterOutput(output);
            
            docExporter.exportReport();           
        }
        catch (JRException e)
        {
            throw new TableIOException(e);
        }
    }
}
