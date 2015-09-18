package org.tms.io;

import java.io.File;

import org.tms.api.Table;

public class CSVWriter
{
    private Table m_table;
    private File m_outFile;
       
    public CSVWriter(Table t, String fileName, boolean writeRowNames, boolean writeColumnHeadings, boolean outputEmptyRows)
    {
        this(t, new File(fileName), writeRowNames, writeColumnHeadings, outputEmptyRows);
    }

    public CSVWriter(Table t, File outFile, boolean writeRowNames, boolean writeColumnHeadings, boolean outputEmptyRows)
    {
        m_table = t;
        m_outFile = outFile;
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
