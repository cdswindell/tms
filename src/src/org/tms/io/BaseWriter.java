package org.tms.io;

import java.io.File;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.tms.api.Column;
import org.tms.api.Table;

abstract class BaseWriter
{
    protected Table m_table;
    protected File m_outFile;
    private IOOptions m_baseOptions;
    
    private int m_nCols;
    private int m_nConsumableColumns;
    private Set<Integer> m_ignoredColumns;
    
    protected BaseWriter(Table t, File f, IOOptions options)
    {
        m_table = t;
        m_outFile = f;
        m_baseOptions = options;
        
        m_nCols = m_table.getNumColumns();
        m_nConsumableColumns = -1; // to be initialized later
    }

    protected int getNumColumns()
    {
        return m_nCols;
    }

    protected int getNumConsumableColumns()
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
    
    protected boolean isIgnoreColumn(int i)
    {
        if (m_ignoredColumns == null)
            getNumConsumableColumns();
        
        return m_ignoredColumns.contains(i);           
    }
}
