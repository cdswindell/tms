package org.tms.io;

import java.io.File;
import java.io.IOException;

import org.tms.api.Table;
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

    protected PDFOptions options()
    {
        return (PDFOptions)super.options();
    }
 
    private void exportPDF()
    {
        // TODO Auto-generated method stub
        
    }
}
