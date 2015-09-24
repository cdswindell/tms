package org.tms.io;

import java.io.File;
import java.io.IOException;

import org.tms.api.Table;
import org.tms.io.jasper.TMSReport;
import org.tms.io.options.PDFOptions;
import org.tms.tds.TableImpl;

public class PDFWriter extends BaseWriter
{
    public static void export(TableImpl table, File file, PDFOptions options)
    throws IOException
    {
        PDFWriter writer = new PDFWriter(table, file, options);
        writer.exportPDF();        
    }
    
    private PDFWriter(Table t, File f, PDFOptions options)
    {
        super(t, f, options);
    }

    public PDFOptions options()
    {
        return (PDFOptions)super.options();
    }
 
    private void exportPDF() 
    throws IOException
    {
        TMSReport report = new TMSReport(this);
        report.export();
    }
}
