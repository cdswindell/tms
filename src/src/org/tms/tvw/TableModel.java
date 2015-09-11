package org.tms.tvw;

import javax.swing.table.AbstractTableModel;

import org.tms.api.Access;
import org.tms.api.Cell;
import org.tms.api.Column;
import org.tms.api.Table;

public class TableModel extends AbstractTableModel
{
    private static final long serialVersionUID = -6146935692468978650L;
    
    private Table m_table;
    
    public TableModel(Table table)
    {
        m_table = table;
    }

    @Override
    public int getRowCount()
    {
        return m_table.getNumRows();
    }

    @Override
    public int getColumnCount()
    {
        return m_table.getNumColumns();
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex)
    {
        Object value = m_table.getCellValue(m_table.getRow(rowIndex + 1), 
                                            m_table.getColumn(columnIndex + 1));
        return value;
    }

    @Override
    public String getColumnName(int column)
    {
        Column col = m_table.getColumn(column + 1);
        if (col != null) {
            String label = col.getLabel();
            if (label != null)
                return label;
        }
        
        return "Col " + (column + 1);
    }

    @Override
    public Class<?> getColumnClass(int column) 
    {
        Column col = m_table.getColumn(column + 1);
        if (col != null) {
            Class<?> clazz = col.getDataType();
            if (clazz != null)
                return clazz;
        }
        
        return super.getColumnClass(column);
    }

    @Override
    public int findColumn(String columnName)
    {
        Column col = m_table.getColumn(Access.ByLabel, columnName);
        if (col != null)
            return col.getIndex() - 1;
        else
            return -1;
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex)
    {
        Cell c = m_table.getCell(m_table.getRow(rowIndex + 1), 
                                 m_table.getColumn(columnIndex + 1));
        if (c != null)
            return !c.isWriteProtected();
        else
            return false;
    }

    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex)
    {
        if (aValue != null && aValue instanceof String && ((String)aValue).startsWith("=") && ((String)aValue).length() > 1) {
            Cell c = m_table.getCell(m_table.getRow(rowIndex + 1), 
                                          m_table.getColumn(columnIndex + 1));
            if (c != null) 
                c.setDerivation(((String)aValue).substring(1));
        }
        else {
            // see if aValue is a number
            if (aValue != null && aValue instanceof String && 
                    ((String)(aValue = ((String)aValue).trim())).length() > 0) {
                try {
                    double d = Double.parseDouble((String) aValue);
                    aValue = d;
                }
                catch (Exception e) {
                    // try boolean conversion
                    try {
                        boolean b = Boolean.parseBoolean((String)aValue);
                        if (b)
                            aValue = b;
                        else if (((String)aValue).equalsIgnoreCase("false"))
                            aValue = false;
                    }
                    catch (Exception eb) {}
                }
            }

            m_table.setCellValue(m_table.getRow(rowIndex + 1), 
                    m_table.getColumn(columnIndex + 1), aValue);
        }
        
        fireTableDataChanged();
    }

    
    public Table getTable()
    {
        return m_table;
    }
}
