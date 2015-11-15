package org.tms.web.controllers;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;

import org.primefaces.event.CellEditEvent;
import org.tms.api.Cell;
import org.tms.api.Column;
import org.tms.api.Row;
import org.tms.io.Printable;

@ManagedBean(name="tableEdit" )
@ViewScoped

public class TableEditor implements Serializable
{
	private static final long serialVersionUID = 1L;

	@SuppressWarnings("unchecked")
	static <T> T findBean(String beanName) {
	    FacesContext context = FacesContext.getCurrentInstance();
	    return (T) context.getApplication().evaluateExpressionGet(context, "#{" + beanName + "}", Object.class);
	}
	
	@ManagedProperty(value = "#{tableView}", name="tableViewer")
	private TableViewer m_tableViewer;

	private List<EditRow> m_eRows;	
	private List<EditColumn> m_eCols;
	
	private EditRow m_selectedRow;
	private EditColumn m_selectedCol;
	
	public void setTableViewer(TableViewer tv) 
	{
		m_tableViewer = tv;
	}
	
	public List<EditRow> getRows()
	{
		if (m_eRows == null) {
			m_eRows = new ArrayList<EditRow>(m_tableViewer.getNumRows()); 
			for (Row r : m_tableViewer.getRows()) {
				m_eRows.add(new EditRow(r));
			}
		}
		
		return m_eRows;
	}
	
	public List<EditColumn> getColumns()
	{
		if (m_eCols == null) {
			m_eCols = new ArrayList<EditColumn>(m_tableViewer.getNumColumns()); 
			for (Column c : m_tableViewer.getColumns()) {
				m_eCols.add(new EditColumn(c));
			}
		}
		
		return m_eCols;
	}
	
	public void clearRow(EditRow eRow)
	{
		if (eRow != null) 			
			eRow.getRow().clear();
	}
	
	public void addRow(EditRow eRow)
	{
		if (eRow != null) {
			int idx = eRow.getIndex();
			m_tableViewer.getTable().addRow(idx+1);
		}
		else
			m_tableViewer.getTable().addRow();
		
		m_eRows = null;
	}
	
	public void deleteRow()
	{
		if (getSelectedRow() != null) 			
			m_tableViewer.getTable().delete(getSelectedRow().getRow());
		
		m_eRows = null;
	}
	
	public void addColumn(EditColumn eCol)
	{
		if (eCol != null) {
			int idx = eCol.getIndex();
			m_tableViewer.getTable().addColumn(idx+1);
		}
		else
			m_tableViewer.getTable().addColumn();
		
		m_eCols = null;
	}
	
	public void deleteColumn()
	{
		if (getSelectedColumn() != null) 			
			m_tableViewer.getTable().delete(getSelectedColumn().getColumn());
		
		m_eCols = null;
	}
	
	public void clearColumn(EditColumn eCol)
	{
		if (eCol != null) 			
			eCol.getColumn().clear();
	}
	
    public EditRow getSelectedRow()
    {
    	return m_selectedRow;
    }
    
    public void setSelectedRow(EditRow er)
    {
    	m_selectedRow = er;
    }
    
    public EditColumn getSelectedColumn()
    {
    	return m_selectedCol;
    }
    
    public void setSelectedColumn(EditColumn ec)
    {
    	m_selectedCol = ec;
    }
    
    public void onCellEdit()
    {
    	System.out.println("Cell Updated..");
    }
    
    public void onCellUpdate(CellEditEvent event)
    {
    	System.out.println("Cell Updated..");
    }
    
	public static class EditRow implements Serializable
	{
		private static final long serialVersionUID = 1L;
		
		private Row m_row;
		
		public EditRow(Row r)
		{
			m_row = r;
		}
		
		public Row getRow() 
		{
			return m_row;
		}

		public int getIndex()
		{
			return m_row.getIndex();
		}
		
		public String getLabel()
		{
			String label = m_row.getLabel();
			if (label == null || (label = label.trim()).length() <= 0)
				label = "Row " + m_row.getIndex();
			return label;
		}
		
		public void setLabel(String val)
		{
			m_row.setLabel(val);
		}
		
		public String getUuid()
		{
			return m_row.getUuid();
		}
	}
	
	public static class EditColumn implements Serializable
	{
		private static final long serialVersionUID = 1L;
		
		private Column m_col;
		
		public EditColumn(Column c)
		{
			m_col = c;
		}
		
		public Column getColumn() 
		{
			return m_col;
		}

		public String getLabel()
		{
			String label = m_col.getLabel();
			if (label == null || (label = label.trim()).length() <= 0)
				label = "Col " + m_col.getIndex();
			return label;
		}
		
		public void setLabel(String val)
		{
			m_col.setLabel(val);
		}
				
		public String getUuid()
		{
			return m_col.getUuid();
		}
		
		public int getIndex()
		{
			return m_col.getIndex();
		}
		
		public String getStyle()
		{
			Cell cell = getTableCell();
			if (cell instanceof Printable) {
				Printable p = (Printable)cell;
				if (p.isCenterAligned())
					return "float:center;";
				else if (p.isRightAligned())
					return "float:right;";
			}
			
			return "float:left;";
		}
		
		private Cell getTableCell() 
		{
			EditRow er = TableEditor.findBean("row");			
			Cell cell = m_col.getTable().getCell(er.m_row, m_col);
			
			return cell;
		}

		public String getFormattedValue()
		{
			Cell cell = getTableCell();
			if (cell != null)
				return cell.getFormattedCellValue();
			else
				return "";
		}
		
		public String getRawValue()
		{
			Cell cell = getTableCell();
			if (cell != null && !cell.isNull()) {
				if (cell.isDerived())
					return "=" + cell.getDerivation().getAsEnteredExpression();
				else
					return cell.getCellValue().toString();
			}
			else
				return "";
		}
		
		public void setRawValue(String val)
		{
			Cell cell = getTableCell();
			if (cell != null) {
				if (val == null || (val = val.trim()).length() <= 0) 
					cell.setCellValue(null);
				else if (val.startsWith("="))
					cell.setDerivation(val.substring(1));
				else
					setCellValue(cell, val);
			}
		}

		private void setCellValue(Cell cell, String val) 
		{
			if ("true".equalsIgnoreCase(val))
				cell.setCellValue(true);
			else if ("false".equalsIgnoreCase(val))
				cell.setCellValue(false);
			else {
				try {
					cell.setCellValue(Double.parseDouble(val));
				}
				catch (NumberFormatException nfe) {
					cell.setCellValue(val);
				}
			}			
		}
	}
}
