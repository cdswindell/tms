package org.tms.io;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.QuoteMode;
import org.tms.api.Column;
import org.tms.api.Row;
import org.tms.api.Table;
import org.tms.tds.TableImpl;

public class CSVWriter extends BaseWriter
{
    public static void export(TableImpl table, File file, CSVOptions options) 
    throws IOException
    {
        CSVWriter writer = new CSVWriter(table, file, options);
        writer.exportCVS();        
    }
    
    private CSVOptions m_options;
       
    private CSVWriter(Table t, File f, CSVOptions options)
    {
        super(t, f, options);
        m_options = options;
    }
  
    private void exportCVS() 
    throws IOException
    {
        FileWriter fw = new FileWriter(m_outFile);
        CSVFormat format = CSVFormat.DEFAULT
                                        .withAllowMissingColumnNames(true)
                                        .withDelimiter(m_options.getDelimiter())
                                        .withQuote(m_options.getQuote())
                                        .withQuoteMode(QuoteMode.MINIMAL);
        
        CSVPrinter out = new CSVPrinter(fw, format);
        try {
            List<Object> emptyRow = null;
            
            List<Object> record = new ArrayList<Object>(getNumConsumableColumns());
            if (m_options.isColumnNames()) {
                if (m_options.isRowNames())
                    record.add(null);
                
                for (int cIdx = 1; cIdx <= getNumColumns(); cIdx++) {
                    if (!isIgnoreColumn(cIdx)) {
                        Column c = m_table.getColumn(cIdx);
                        if (c != null)
                            record.add(c.getLabel());
                        else
                            record.add(null);  
                    }
                }
                
                out.printRecord(record);
            }
            
            for (Row r : m_table.getRows()) {
                record.clear();
                boolean rowIsNull = true;
                if (!(r == null || r.isNull())) {
                    for (int cIdx = 1; cIdx <= getNumColumns(); cIdx++) {
                        if (!isIgnoreColumn(cIdx)) {
                            Column c = m_table.getColumn(cIdx);
                            Object value = m_table.getCellValue(r, c);
                            if (value == null)
                                record.add(null);
                            else {
                                rowIsNull = false;
                                record.add(value.toString());
                            }
                        }
                    }
                }
            
                // handle empty row
                if (rowIsNull) {
                    if (m_options.isIgnoreEmptyRows())
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
                
                if (m_options.isRowNames())
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
            
            if (fw != null)
                fw.close();
        }        
    }
    
    public Table getTable()
    {
        return m_table;
    }

    public File getOutputFile()
    {
        return m_outFile;
    }
}
