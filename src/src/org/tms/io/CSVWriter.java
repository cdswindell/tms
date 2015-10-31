package org.tms.io;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.QuoteMode;
import org.tms.api.Column;
import org.tms.api.Row;
import org.tms.api.io.options.CSVOptions;

public class CSVWriter extends BaseWriter<CSVOptions>
{
    public static void export(TableExportAdapter tea, OutputStream out, CSVOptions options) 
    throws IOException 
    {
        CSVWriter writer = new CSVWriter(tea, out, options);
        writer.export();        
    }
    
    public static void export(TableExportAdapter tea, File file, CSVOptions options) 
    throws IOException
    {
        export(tea, new FileOutputStream(file), options);        
    }
    
    private CSVWriter(TableExportAdapter t, OutputStream out, CSVOptions options)
    {
        super(t, out, options);
    }
  
    public CSVOptions options()
    {
        return (CSVOptions)super.options();
    }
 
    @Override
    protected void export() 
    throws IOException 
    {
        Appendable fw = new OutputStreamWriter(getOutputStream());
        CSVFormat format = CSVFormat.DEFAULT
                                        .withAllowMissingColumnNames(true)
                                        .withDelimiter(options().getDelimiter())
                                        .withQuote(options().getQuote())
                                        .withIgnoreSurroundingSpaces(true)
                                        .withQuoteMode(QuoteMode.MINIMAL);
        
        CSVPrinter out = new CSVPrinter(fw, format);
        try {
            List<Object> emptyRow = null;
            
            List<Object> record = new ArrayList<Object>(getNumConsumableColumns());
            if (options().isColumnLabels()) {
                if (options().isRowLabels())
                    record.add(null);
                
                for (Column c : getActiveColumns()) {
                    if (c != null)
                        record.add(c.getLabel());
                    else
                        record.add(null);  
                }
                
                out.printRecord(record);
            }
            
            for (Row r : getRows()) {
                record.clear();
                boolean rowIsNull = true;
                if (!(r == null || r.isNull())) {
                    for (Column c : getActiveColumns()) {
                        Object value = getTable().getCellValue(r, c);
                        if (value == null)
                            record.add(null);
                        else {
                            rowIsNull = false;
                            record.add(value.toString());
                        }
                    }
                }
            
                // handle empty row
                if (rowIsNull) {
                    if (options().isIgnoreEmptyRows())
                        continue;
                    else {
                        if (emptyRow == null) {                            
                            emptyRow = new ArrayList<Object>(getNumConsumableColumns());
                            for (int i = 0; i < getNumConsumableColumns(); i++)
                                emptyRow.add(null);
                            
                            record.addAll(emptyRow);
                        }
                    }
                }
                
                if (options().isRowLabels())
                  if (r == null)
                      record.add(0, null);
                  else
                      record.add(0, r.getLabel()) ;
                
                // output the record
                out.printRecord(record);
            }
        }
        finally {
            if (out != null)
                out.close();
        }        
    }
}
