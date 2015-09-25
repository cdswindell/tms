package org.tms.io;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.tms.api.Column;
import org.tms.api.Table;
import org.tms.io.options.IOOptions;

public abstract class BaseWriter
{
    private Table m_table;
    private File m_outFile;
    private IOOptions m_baseOptions;
    
    private int m_nCols;
    private int m_nConsumableColumns;
    private Set<Integer> m_ignoredColumns;
    private List<Column> m_activeCols;
    
    protected BaseWriter(Table t, File f, IOOptions options)
    {
        m_table = t;
        m_outFile = f;
        m_baseOptions = options;
        
        m_nCols = m_table.getNumColumns();
        m_nConsumableColumns = -1; // to be initialized later
    }

    /*
     * Publically available methods for use outside of package
     */
    public IOOptions options()
    {
        return m_baseOptions;
    }
    
    public Table getTable()
    {
        return m_table;
    }

    public File getOutputFile()
    {
        return m_outFile;
    }
    
    public int getNumColumns()
    {
        return m_nCols;
    }

    public int getNumConsumableColumns()
    {
        if (m_nConsumableColumns == -1) {
            m_nConsumableColumns = m_nCols;
            if (m_baseOptions.isIgnoreEmptyColumns()) {
                m_ignoredColumns = new HashSet<Integer>();
                int emptyColCnt = 0;
                for (int i = 1; i <= m_nCols; i++) {
                    Column c = m_table.getColumn(i);
                    if (c == null || c.isNull()) {
                        emptyColCnt++;
                        m_ignoredColumns.add(i);
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
    
    /**
     * Return a list of active columns in the table. Active columns are non-empty columns, 
     * if isIgnoreEmptyColumns() is true, or all columns otherwise
     * @return
     */
    public List<Column> getActiveColumns()
    {
        if (m_baseOptions.isIgnoreEmptyColumns()) {
            if (m_activeCols != null || getNumColumns() != getNumConsumableColumns()) {
                    
                if (m_activeCols == null) {
                    m_activeCols = new ArrayList<Column>(getNumConsumableColumns());
                    
                    for (int i = 1; i <= m_nCols; i++) {
                        Column c = m_table.getColumn(i);
                        if (c != null && !isIgnoreColumn(i)) 
                            m_activeCols.add(m_table.getColumn(i));
                    }
                }
                
                return m_activeCols;
            }
        }
        
        return m_table.getColumns();
    }
    
    public int getNumActiveColumns()
    {
        return getActiveColumns().size();
    }
}
