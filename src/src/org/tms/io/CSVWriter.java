package org.tms.io;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.QuoteMode;
import org.tms.api.Column;
import org.tms.api.Row;
import org.tms.api.Table;
import org.tms.tds.TableImpl;

public class CSVWriter
{
    public static void export(TableImpl table, File file, CSVOptions options) 
    throws IOException
    {
        CSVWriter writer = new CSVWriter(table, file, options);
        writer.exportCVS();        
    }
    

    private Table m_table;
    private File m_outFile;
    private CSVOptions m_options;
       
    protected CSVWriter(Table t, File f, CSVOptions options)
    {
        m_table = t;
        m_outFile = f;
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
            int nCols = m_table.getNumColumns();    
            int nConsumableColumns = nCols;
            Set<Integer> ignoredColumns = new HashSet<Integer>();
            if (m_options.isIgnoreEmptyColumns()) {
                int emptyColCnt = 0;
                for (int i = 1; i <= nCols; i++) {
                    Column c = m_table.getColumn(i);
                    if (c == null || c.isNull()) {
                        emptyColCnt++;
                        ignoredColumns.add(i);
                    }
                }
                
                nConsumableColumns -= emptyColCnt;
            }
            
            List<Object> record = new ArrayList<Object>(nConsumableColumns);
            if (m_options.isColumnNames()) {
                if (m_options.isRowNames())
                    record.add(null);
                
                for (int cIdx = 1; cIdx <= nCols; cIdx++) {
                    if (!ignoredColumns.contains(cIdx)) {
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
                    for (int cIdx = 1; cIdx <= nCols; cIdx++) {
                        if (!ignoredColumns.contains(cIdx)) {
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
                            emptyRow = new ArrayList<Object>(nConsumableColumns);
                            for (int i = 0; i < nConsumableColumns; i++)
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
