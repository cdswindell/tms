package org.tms.io.jasper;

import java.io.IOException;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperExportManager;

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
        try
        {
            generateReport();
            
            // print report to file
            JasperExportManager.exportReportToHtmlFile(getPrint(), getWriter().getOutputFile().getAbsolutePath());
        }
        catch (JRException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
