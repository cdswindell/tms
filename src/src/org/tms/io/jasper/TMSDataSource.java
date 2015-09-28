package org.tms.io.jasper;

import java.util.HashMap;
import java.util.Map;

import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRField;
import net.sf.jasperreports.engine.design.JRDesignTextField;
import net.sf.jasperreports.engine.type.HorizontalTextAlignEnum;

import org.tms.api.Cell;
import org.tms.api.Column;
import org.tms.api.Row;
import org.tms.api.Table;
import org.tms.io.options.IOOptions;

public class TMSDataSource implements JRDataSource
{
    private TMSReport m_report;
    private Table m_table;
    private IOOptions m_options;
    
    private int m_maxRows;
    private int m_rowIndex;
    private Map<String, Column> m_fieldToColMap;
    
    TMSDataSource(TMSReport tr)
    {
        m_report = tr;
        m_table = tr.getTable();
        m_options = tr.getOptions();
        
        m_maxRows = m_table.getNumRows();
        m_rowIndex = 0;
    }
    
    @Override
    public Object getFieldValue(JRField jrField) throws JRException
    {
        if (m_fieldToColMap == null) {
            m_fieldToColMap = new HashMap<String, Column>(m_table.getNumColumns());
            for (Map.Entry<Column, JRField> e : m_report.getColumnFieldMap().entrySet()) {
                m_fieldToColMap.put(e.getValue().getName(), e.getKey());
            }
        }
        
        Row row = m_table.getRow(m_rowIndex);
        Column col = m_fieldToColMap.get(jrField.getName());
        Object o = m_table.getCellValue(row, col);
        
        if (o != null) {
            Cell c = m_table.getCell(row, col);
            if (c.isFormatted())
                o = c.getFormattedCellValue();
            o = c;
        }
                
        return o;
    }

    @Override
    public boolean next() throws JRException
    {
        if (m_rowIndex + 1 <= m_maxRows) {
            m_rowIndex++;
            
            // check for empty rows, if we're suppose to ignore them,
            // skip over the empty row and move m_rowIndex to the next
            if (m_options.isIgnoreEmptyRows()) {
                Row r = m_table.getRow(m_rowIndex);
                if (r == null || r.isNull())
                    return next();
            }
            
            return true;
        }
        
        return false;
    }
    
    public TMSReport getReport()
    {
        return m_report;
    }
}
