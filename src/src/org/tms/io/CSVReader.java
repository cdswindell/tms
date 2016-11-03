package org.tms.io;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.tms.api.Column;
import org.tms.api.Row;
import org.tms.api.Table;
import org.tms.api.TableContext;
import org.tms.api.factories.TableContextFactory;
import org.tms.api.factories.TableFactory;
import org.tms.api.io.CSVOptions;
import org.tms.teq.MathUtil;

public class CSVReader extends BaseReader<CSVOptions>
{
    private CSVFormat m_csvFormat;
    
    public CSVReader(String fileName, CSVOptions format)
    {
        this(fileName, TableContextFactory.fetchDefaultTableContext(), format);
    }

    public CSVReader(String fileName, TableContext context, CSVOptions format)         
    {
        this(new File(fileName), context, format);
    }

    public CSVReader(File csvFile, TableContext context, CSVOptions format) 
    {
        super(csvFile, context, format);        
        initFormat(format);
    }
    
    public CSVReader(InputStream in, TableContext context, CSVOptions format)
    {
        super(in, context, format);
        initFormat(format);
    }

    private void initFormat(CSVOptions format)
    {
        m_csvFormat = CSVFormat.DEFAULT
                .withIgnoreEmptyLines(format.isIgnoreEmptyRows())
                .withIgnoreSurroundingSpaces(options().isIgnoreSuroundingSpaces())
                .withDelimiter(options().getDelimiter())
                .withQuote(options().getQuote());

    }
    
    public Table parse() throws IOException
    {
        InputStreamReader in = null;
        CSVParser parser = null;
        
        try {
            // build a CSVParser to do the heavy lifting
            in = new InputStreamReader(new FileInputStream(getInputFile()));
            parser = m_csvFormat.parse(in);
            
            // create the table scaffold
            Table t = TableFactory.createTable(getTableContext());
            
            // read the data from the Default file, one row at a time, 
            // and fill the table with it
            boolean firstRow = true;;
            for (CSVRecord csvRec : parser) {
                if (options().isColumnLabels() && firstRow)
                    parseColumnHeaders(t, csvRec);
                else {
                    if (options().isIgnoreEmptyRows() && isEmpty(csvRec))
                        continue;
                    
                    Row row = t.addRow();
                    int colNum = 1;
                    boolean firstCol = true;
                    for (String s : csvRec) {
                        if (s != null && (s = s.trim()).length() == 0)
                            s = null;
                        
                        // Default column indexes are 1-based, so we want to increment the
                        // column number here, no matter what
                        if (firstCol && isRowNames()) {
                            row.setLabel(s);
                            firstCol = false;
                        }
                        else {
                            if (s != null) {
                                Column col = t.getColumn(colNum);
                                if (col == null)
                                    col = t.addColumn(colNum);
                                
                                t.setCellValue(row, col, MathUtil.parseCellValue(s, options().isIgnoreSuroundingSpaces()));
                            }
                            
                            colNum++;
                        }
                    }
                }
                
                firstRow = false;
            }
            
            // if we're ignoring extra columns, we have one more check
            pruneEmptyColumns(t);
            
            return t;
        }
        finally {
            // close up shop
            if (parser != null)
                parser.close();
            
            if (in != null)
                in.close();
        }
    }

    private boolean isEmpty(CSVRecord csvRec)
    {
        if (csvRec != null) {
            boolean firstCol = true;
            for (String s : csvRec) {
                if (firstCol && options().isRowLabels())
                    ; // noop
                else {
                    // if s isn't null or empty, the record isn't empty
                    if (s != null && (s = s.trim()).length() > 0)
                        return false;
                }
                
                firstCol = false;
            }
        }
        
        return true;
    }

    private void parseColumnHeaders(Table t, CSVRecord csvRec)
    {
        boolean firstCol = true;
        int colNum = 0;
        for (String s : csvRec) {
            if (firstCol && isRowNames())
                ;
            else {
                colNum++;
                
                if (s != null && (s = s.trim()).length() > 0) {
                    Column col = t.getColumn(colNum);
                    if (col == null)
                        col = t.addColumn(colNum);
                    
                    col.setLabel(s);
                }
            }
            
            firstCol = false;
        }        
    }
}
