package org.tms.io;

import java.io.IOException;
import java.io.OutputStream;

import org.tms.api.exceptions.UnimplementedException;
import org.tms.io.jasper.DocXReport;
import org.tms.io.jasper.HTMLReport;
import org.tms.io.jasper.PDFReport;
import org.tms.io.jasper.RTFReport;
import org.tms.io.jasper.TMSReport;
import org.tms.io.options.TitledPageOptions;

public class JasperWriter extends BaseWriter<TitledPageOptions<?>>
{
    public static void export(TableExportAdapter tw, OutputStream out, TitledPageOptions<?> options)
    throws IOException
    {
        JasperWriter writer = new JasperWriter(tw, out, options);
        
        TMSReport report = null;
        switch (options.getFileFormat()) {
            case PDF:
                report = new PDFReport(writer);
                break;
                
            case RTF:
                report = new RTFReport(writer);
                break;
                
            case HTML:
                report = new HTMLReport(writer);
                break;
                
            case WORD:
                report = new DocXReport(writer);
                break;
                
            default:
                throw new UnimplementedException(String.format("%s export not supported", options.getFileFormat()));
        }
        
        // export the report using Jasper Reports
        writer.m_report = report;  
        writer.export();
    }
    
    private TMSReport m_report;
    
    private JasperWriter(TableExportAdapter t, OutputStream out, TitledPageOptions<?> options)
    {
        super(t, out, options);
    }

    @Override
    protected void export() throws IOException
    {
       m_report.export();
    }
}
