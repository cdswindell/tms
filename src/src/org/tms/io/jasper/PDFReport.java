package org.tms.io.jasper;

import java.io.FileOutputStream;
import java.io.IOException;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperExportManager;

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
            JasperExportManager.exportReportToPdfStream(getPrint(), out);
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
