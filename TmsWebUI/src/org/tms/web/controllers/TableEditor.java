package org.tms.web.controllers;

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

@ManagedBean(name="tableEdit" )
@ViewScoped

public class TableEditor 
{
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
	
    public void onCellEdit(CellEditEvent event) 
    {
        Object oldValue = event.getOldValue();
        Object newValue = event.getNewValue();
         
        if(newValue != null && !newValue.equals(oldValue)) {
            //FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "Cell Changed", "Old: " + oldValue + ", New:" + newValue);
            //FacesContext.getCurrentInstance().addMessage(null, msg);
        }
    }
    
    public EditRow getSelectedRow()
    {
    	return m_selectedRow;
    }
    
    public void setSelectedRow(EditRow er)
    {
    	m_selectedRow = er;
    	
		EditColumn ec = TableEditor.findBean("col");
		setSelectedColumn(ec);
    }
    
    public EditColumn getSelectedColumn()
    {
    	return m_selectedCol;
    }
    
    public void setSelectedColumn(EditColumn ec)
    {
    	m_selectedCol = ec;
    }
    
    public void clearCell()
    {
		EditRow er = TableEditor.findBean("row");
		EditColumn ec = TableEditor.findBean("col");
		
		if (er != null && ec != null) {
			
		}    	
    }
    
	public static class EditRow 
	{
		private Row m_row;
		
		public EditRow(Row r)
		{
			m_row = r;
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
	}
	
	public static class EditColumn 
	{
		private Column m_col;
		
		public EditColumn(Column c)
		{
			m_col = c;
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
		
		public String getFormattedValue()
		{
			EditRow er = TableEditor.findBean("row");
			
			Cell cell = m_col.getTable().getCell(er.m_row, m_col);
			if (cell != null)
				return cell.getFormattedCellValue();
			else
				return "";
		}
		
		public String getRawValue()
		{
			EditRow er = TableEditor.findBean("row");
			
			Cell cell = m_col.getTable().getCell(er.m_row, m_col);
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
			EditRow er = TableEditor.findBean("row");
			
			Cell cell = m_col.getTable().getCell(er.m_row, m_col);
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
