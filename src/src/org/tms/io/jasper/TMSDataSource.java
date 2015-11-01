package org.tms.io.jasper;

import java.util.HashMap;
import java.util.Map;

import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRField;
import net.sf.jasperreports.engine.JRRewindableDataSource;

import org.tms.api.Column;
import org.tms.api.Row;
import org.tms.api.io.options.BaseIOOptions;
import org.tms.io.BaseWriter;
import org.tms.io.TableExportAdapter;

public class TMSDataSource implements JRDataSource, JRRewindableDataSource
{
    private TMSReport m_report;
    private BaseWriter<?> m_writer;
    private TableExportAdapter m_exportAdapter;
    private BaseIOOptions<?> m_options;
    
    private int m_maxRows;
    private int m_rowIndex;
    private Map<String, Column> m_fieldToColMap;
    
    TMSDataSource(TMSReport tr)
    {
        m_report = tr;
        m_writer = tr.getWriter();
        m_exportAdapter = m_writer.getExportAdapter();
        m_options = tr.getOptions();
        
        m_maxRows = m_exportAdapter.getNumRows();
        m_rowIndex = 0;
    }
    
    @Override
    public Object getFieldValue(JRField jrField) throws JRException
    {
        if (m_fieldToColMap == null) {
            m_fieldToColMap = new HashMap<String, Column>(m_writer.getNumActiveColumns());
            for (Map.Entry<Column, JRField> e : m_report.getColumnFieldMap().entrySet()) {
                m_fieldToColMap.put(e.getValue().getName(), e.getKey());
            }
        }
        
        Row row = m_exportAdapter.getRow(m_rowIndex);
        String fieldName = jrField.getName();
        
        if (TMSReport.sf_RowNameFieldName.equals(fieldName)) {
            String label = row.getLabel();
            if (label == null || (label = label.trim()).length() <= 0)
                label = String.format("Row %d", m_rowIndex);
            
            return label;
        }
        else if (TMSReport.sf_RowIndexFieldName.equals(fieldName)) 
            return row.getIndex();
        else {
            Column col = m_fieldToColMap.get(fieldName);
            Object o = m_exportAdapter.getTable().getCellValue(row, col);
            
            if (o != null) 
                o = m_exportAdapter.getTable().getCell(row, col);
                    
            return o;
        }
    }

    @Override
    public boolean next() throws JRException
    {
        if (m_rowIndex + 1 <= m_maxRows) {
            m_rowIndex++;
            
            // check for empty rows, if we're suppose to ignore them,
            // skip over the empty row and move m_rowIndex to the next
            if (m_options.isIgnoreEmptyRows()) {
                Row r = m_exportAdapter.getRow(m_rowIndex);
                if (r == null || r.isNull())
                    return next();
            }
            
            return true;
        }
        
        return false;
    }
    
    @Override
    public void moveFirst() throws JRException
    {
        m_maxRows = m_exportAdapter.getNumRows();
        m_rowIndex = 0;
    }
    
    public TMSReport getReport()
    {
        return m_report;
    }
}
