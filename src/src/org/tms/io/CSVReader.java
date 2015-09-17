package org.tms.io;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
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

public class CSVReader
{
    private File m_csvFile;
    private boolean m_isRowNames;
    private boolean m_isColumnNames;
    private TableContext m_context;
    private CSVFormat m_csvFormat;
    
    public CSVReader(String fileName, boolean hasRowNames, boolean hasColumnNames)
    {
        this(fileName, hasRowNames, hasColumnNames, TableContextFactory.fetchDefaultTableContext());
    }

    public CSVReader( String fileName, boolean hasRowNames, boolean hasColumnNames, TableContext context)
    {
        this(new File(fileName), hasRowNames, hasColumnNames, context);
    }

    public CSVReader(File csvFile, boolean hasRowNames, boolean hasColumnNames, TableContext context)
    {
        m_context = context;
        m_csvFile = csvFile;
        setRowNames(hasRowNames);
        setColumnNames(hasColumnNames);
        
        m_csvFormat = CSVFormat.DEFAULT.withIgnoreEmptyLines(false).withIgnoreSurroundingSpaces(true);
    }
    
    public Table parse() throws IOException
    {
        InputStreamReader in = null;
        CSVParser parser = null;
        
        try {
            // build a CSVParser to do the heavy lifting
            in = new InputStreamReader(new FileInputStream(m_csvFile));
            parser = m_csvFormat.parse(in);
            
            // create the table scaffold
            Table t = TableFactory.createTable(m_context);
            
            // read the data from the CSV file, one row at a time, 
            // and fill the table with it
            boolean firstRow = true;;
            for (CSVRecord csvRec : parser) {
                if (m_isColumnNames && firstRow)
                    parseColumnHeaders(t, csvRec);
                else {
                    Row row = t.addRow();
                    int colNum = 1;
                    boolean firstCol = true;
                    for (String s : csvRec) {
                        if (s != null && (s = s.trim()).length() == 0)
                            s = null;
                        
                        // TMS column indexes are 1-based, so we want to increment the
                        // column number here, no matter what
                        if (firstCol && m_isRowNames) {
                            row.setLabel(s);
                            firstCol = false;
                        }
                        else if (s != null) {
                            Column col = t.getColumn(colNum);
                            if (col == null)
                                col = t.addColumn(colNum);
                            
                            t.setCellValue(row, col, parseCellValue(s));
                            
                            colNum++;
                        }
                    }
                }
                
                firstRow = false;
            }
            
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

    private Object parseCellValue(String s)
    {
        try {
            char c = 0;
            if (Boolean.valueOf(s))
                return true;
            else if ("false".equalsIgnoreCase(s))
                return false;
            else if ((c = s.charAt(0)) > 0 && (!Character.isDigit(c) && c != '+' && c != '-'))
                return s;
            
            return Integer.parseInt(s);
        }
        catch (Exception e) {
            try {
                return Double.parseDouble(s);
            }
            catch (Exception e2) {
                return s;
            }
        }
    }

    private void parseColumnHeaders(Table t, CSVRecord csvRec)
    {
        boolean firstCol = true;
        int colNum = 0;
        for (String s : csvRec) {
            if (firstCol && this.m_isRowNames)
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

    /**
     * Return the {@link java.io.File} to parse.
     * @return the {@link java.io.File} to parse
     */
    public File getCSVFile()
    {
        return m_csvFile;
    }

    /**
     * Return the file name to parse.
     * @return the file name to parse
     */
    public String getCSVFileName()
    {
        return m_csvFile.getName();
    }

    /**
     * Return {@code true} if the CSV file contains row names.
     * @return true if the CSV file contains row names
     */
    public boolean isRowNames()
    {
        return m_isRowNames;
    }

    protected void setRowNames(boolean isRowNames)
    {
        m_isRowNames = isRowNames;
    }

    /**
     * Return {@code true} if the CSV file contains column names.
     * @return true if the CSV file contains column names
     */
    public boolean isColumnNames()
    {
        return m_isColumnNames;
    }

    public void setColumnNames(boolean isColumnNames)
    {
        m_isColumnNames = isColumnNames;
    }

}
