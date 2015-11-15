package org.tms.web.controllers;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;

import org.tms.api.Cell;
import org.tms.api.Column;
import org.tms.api.Row;
import org.tms.api.TableRowColumnElement;
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
    
    abstract static public class EditBase implements Serializable
    {
		private static final long serialVersionUID = 1L;
		
		protected TableRowColumnElement m_te;   	
    	public EditBase(TableRowColumnElement te)
    	{
    		m_te = te;
    	}
    	
    	public TableRowColumnElement getTE()
    	{
    		return m_te;
    	}
    	
		public int getIndex()
		{
			return m_te.getIndex();
		}
		
		public String getLabel()
		{
			String label = m_te.getLabel();
			if (label == null || (label = label.trim()).length() <= 0)
				label = (m_te instanceof Row ? "Row " : "Col ") + m_te.getIndex();
			
			return label;
		}
		
		public void setLabel(String val)
		{
			m_te.setLabel(val);
		}
		
		public String getUuid()
		{
			return m_te.getUuid();
		}
    }
    
	public static class EditRow extends EditBase 
	{
		private static final long serialVersionUID = 1L;
		
		public EditRow(Row r)
		{
			super(r);
		}
		
		public Row getRow() 
		{
			return (Row)m_te;
		}
	}
	
	public static class EditColumn extends EditBase implements Serializable
	{
		private static final long serialVersionUID = 1L;
		
		public EditColumn(Column c)
		{
			super(c);
		}
		
		public Column getColumn() 
		{
			return (Column)m_te;
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
			Cell cell = m_te.getTable().getCell(er.getRow(), (Column)m_te);
			
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
