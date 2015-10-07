package org.tms.io;

import java.io.File;
import java.io.IOException;

import org.tms.api.Table;
import org.tms.api.exceptions.UnimplementedException;
import org.tms.io.jasper.DOCXReport;
import org.tms.io.jasper.HTMLReport;
import org.tms.io.jasper.PDFReport;
import org.tms.io.jasper.RTFReport;
import org.tms.io.jasper.TMSReport;
import org.tms.io.options.IOOptions;
import org.tms.tds.TableImpl;

public class JasperWriter extends BaseWriter
{
    public static void export(TableImpl table, File file, IOOptions options)
    throws IOException
    {
        JasperWriter writer = new JasperWriter(table, file, options);
        
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
                
            case DOCX:
                report = new DOCXReport(writer);
                break;
                
            default:
                throw new UnimplementedException(String.format("%s export not supported", options.getFileFormat()));
        }
        
        // export the report using Jasper Reports
        report.export();        
    }
    
    private JasperWriter(Table t, File f, IOOptions options)
    {
        super(t, f, options);
    }

    public IOOptions options()
    {
        return super.options();
    }
}
