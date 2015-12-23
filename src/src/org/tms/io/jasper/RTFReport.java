package org.tms.io.jasper;

import java.io.IOException;
import java.io.OutputStream;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.export.JRRtfExporter;
import net.sf.jasperreports.export.ExporterInput;
import net.sf.jasperreports.export.SimpleExporterInput;
import net.sf.jasperreports.export.SimpleWriterExporterOutput;

import org.tms.api.exceptions.TableIOException;
import org.tms.io.BaseWriter;

public class RTFReport extends TMSReport
{
    public RTFReport(BaseWriter<?> w)
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
            JRRtfExporter rtfExporter  = new JRRtfExporter();
            
            ExporterInput inp = new SimpleExporterInput(getExporterInputItems());
            rtfExporter.setExporterInput(inp);
            
            SimpleWriterExporterOutput output = new SimpleWriterExporterOutput(out);
            rtfExporter.setExporterOutput(output);
            
            rtfExporter.exportReport();           
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
