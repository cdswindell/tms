package org.tms.io;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.tms.api.Column;
import org.tms.api.Row;
import org.tms.api.Table;
import org.tms.api.io.options.BaseIOOptions;

public abstract class BaseWriter<E extends BaseIOOptions<?>> extends BaseIO
{
    private TableExportAdapter m_tableExportAdapter;
    private OutputStream m_outStream;
    private E m_baseOptions;
    
    private int m_nCols;
    private int m_nConsumableColumns;
    private Set<Integer> m_ignoredColumns;
    private List<Column> m_activeCols;
    
    abstract protected void export() throws IOException;
    
    protected BaseWriter(TableExportAdapter tw, OutputStream out, E options)
    {
        m_tableExportAdapter = tw;
        m_outStream = out;
        m_baseOptions = options;
        
        m_nCols = tw.getNumColumns();
        m_nConsumableColumns = -1; // to be initialized later
    }

    protected BaseWriter(TableExportAdapter tw, File f, E options) 
    throws IOException
    {
        this(tw, new FileOutputStream(f), options);
    }

    public OutputStream getOutputStream()
    {
        return m_outStream;
    }
    
    /*
     * Publicly available methods for use outside of package
     */
    public E options()
    {
        return m_baseOptions;
    }
    
    public Table getTable()
    {
        return m_tableExportAdapter.getTable();
    }
    
    public TableExportAdapter getExportAdapter()
    {
        return m_tableExportAdapter;
    }
    
    public int getNumConsumableColumns()
    {
        if (m_nConsumableColumns == -1) {
            m_nConsumableColumns = m_nCols;
            if (m_baseOptions.isIgnoreEmptyColumns()) {
                m_ignoredColumns = new HashSet<Integer>();
                int emptyColCnt = 0;
                for (Column c : m_tableExportAdapter.getColumns()) {
                    if (c.isNull()) {
                        emptyColCnt++;
                        m_ignoredColumns.add(c.getIndex());
                    }
                }
                
                m_nConsumableColumns -= emptyColCnt;
            }
            else {
                m_ignoredColumns = Collections.emptySet();
            }
        }
        
        return m_nConsumableColumns;
    }
    
    public boolean isIgnoreColumn(int i)
    {
        if (!m_baseOptions.isIgnoreEmptyColumns())
            return false;
        
        if (m_ignoredColumns == null)
            getNumConsumableColumns();
        
        return m_ignoredColumns.contains(i);           
    }
    
    public boolean isIgnoreColumn(Column c)
    {
        if (c == null)
            return true;
        
        if (!m_baseOptions.isIgnoreEmptyColumns())
            return false;
        
        if (m_ignoredColumns == null)
            getNumConsumableColumns();
        
        return m_ignoredColumns.contains(c.getIndex());           
    }
    
    /**
     * Return a list of active columns in the table. Active columns are non-empty columns, 
     * if isIgnoreEmptyColumns() is true, or all columns otherwise
     * @return
     */
    public List<Column> getActiveColumns()
    {
        if (m_baseOptions.isIgnoreEmptyColumns()) {
            if (m_activeCols == null) {                    
                if (m_activeCols == null) {
                    m_activeCols = new ArrayList<Column>(getNumConsumableColumns());
                    
                    for (Column c : m_tableExportAdapter.getColumns()) {
                        if (c != null && !isIgnoreColumn(c)) 
                            m_activeCols.add(c);
                    }
                }               
            }
            
            return m_activeCols;
        }
        else        
            return m_tableExportAdapter.getColumns();
    }
    
    public int getNumActiveColumns()
    {
        return getActiveColumns().size();
    }
    
    public List<Row> getRows()
    {
        return m_tableExportAdapter.getRows();
    }
}
