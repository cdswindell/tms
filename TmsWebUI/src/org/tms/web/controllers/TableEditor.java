package org.tms.web.controllers;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.faces.event.ValueChangeEvent;

import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.UploadedFile;
import org.tms.api.Cell;
import org.tms.api.Column;
import org.tms.api.Row;
import org.tms.api.TableContext;
import org.tms.api.TableRowColumnElement;
import org.tms.api.derivables.Derivation;
import org.tms.io.Printable;

@ManagedBean(name="tableEdit" )
@ViewScoped

public class TableEditor implements Serializable
{
	private static final long serialVersionUID = 1L;
	private static final String sf_GROOVY = "Groovy";
	private static final String sf_PYTHON = "Python";

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

	private UploadedFile m_file;
	private String m_scriptType;
	private int m_updateEvery;
	private String m_entityDeriv;

	public TableEditor()
	{
		m_scriptType = sf_GROOVY;
	}
	
	public void setScriptType(String st)
	{
		m_scriptType = st;
	}
	
	public String getScriptType()
	{
		return m_scriptType;
	}
	
	public String getFilePattern()
	{
		switch(m_scriptType) {
			case "Python":
				return "/(\\.|\\/)(python|jython|py|jy|txt)$/";
				
			case "Groovy":
			default:
				return "/(\\.|\\/)(groovy|gvy|gy|gsh|txt)$/";
		}
	}
	
	public void scriptTypeChanged(ValueChangeEvent vce)
	{
		System.out.println("FileType changed: " + m_scriptType);		
	}
	
	public UploadedFile getFile() 
	{
		return m_file;
	}

	public void setFile(UploadedFile file) 
	{
		this.m_file = file;
	}

	public void uploadOps(FileUploadEvent event) 
	{
		m_file = event.getFile();
		if (m_file != null) {
			TableContext tc = m_tableViewer.getTable().getTableContext();
			
			if (sf_PYTHON.equals(m_scriptType)) {
				tc.registerJythonOperators(new String(m_file.getContents()), extractClassName(event));
			}
			else
				tc.registerGroovyOperators(new String(m_file.getContents()));
			
            FacesMessage message = new FacesMessage("Succesful", "Operator(s) in: " + m_file.getFileName() + " are now available.");
            FacesContext.getCurrentInstance().addMessage(null, message);
            m_file = null;
		}
	}
	
	private String extractClassName(FileUploadEvent event) 
	{
		String fileName = event.getFile().getFileName();
		int idx = fileName.lastIndexOf('.');
		if (idx > -1)
			return fileName.substring(0, idx);
		else
			return fileName;
	}

	public boolean isNoFile()
	{
		return m_file == null;
	}
	
	public void reset()
	{
		clearSelectedEntity();
		m_eRows = null;
		m_eCols = null;
		
		m_tableViewer = null;
		setTableViewer(new TableViewer());
	}
	
	public void recalc()
	{
		m_tableViewer.getTable().recalculate();
	}
		
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
	
	public String getEntityTag()
	{
		if (m_selectedRow != null)
			return "Row";
		else if (m_selectedCol != null)
			return "Column";
		else
			return "";		
	}
	
	public String getEntityFormat()
	{
		if (getSelectedEntity() != null)
			return getSelectedEntity().getDisplayFormat();
		else
			return "";		
	}
	
	public void setEntityFormat(String format)
	{
		if (getSelectedEntity() != null) 			
			getSelectedEntity().setDisplayFormat(format);
			
		clearSelectedEntity();
	}
	
	public String getEntityLabel()
	{
		if (getSelectedEntity() != null)
			return getSelectedEntity().getLabel();
		else
			return "";		
	}
	
	public void setEntityLabel(String label)
	{
		if (getSelectedEntity() != null) {
			if (label != null && (label = label.trim()).length() <= 0)
				label = null;
			
			getSelectedEntity().setLabel(label);
			
			clearSelectedEntity();
		}
	}	

	public int getUpdateEvery()
	{
		return m_updateEvery;
	}
	
	public void setUpdateEvery(int updateEvery)
	{
		m_updateEvery = updateEvery;
	}
	
	public String getEntityDerivation()
	{
		if (getSelectedEntity() != null && getSelectedEntity().getTE().isDerived())
			return getSelectedEntity().getTE().getDerivation().getAsEnteredExpression();
		else
			return "";		
	}
	
	public void setEntityDerivation(String deriv)
	{
		m_entityDeriv = deriv;
	}	

	public void applyDerivation()
	{
		if (getSelectedEntity() != null) {
			if (m_entityDeriv != null && (m_entityDeriv = m_entityDeriv.trim()).length() <= 0)
				m_entityDeriv = null;
			
			EditBase eb = getSelectedEntity();
			if (m_entityDeriv == null)
				eb.getTE().clearDerivation();
			else {
				Derivation d = eb.getTE().setDerivation(m_entityDeriv);				
				if (m_updateEvery > 0) 
					d.recalculateEvery(m_updateEvery, TimeUnit.SECONDS);
			}
			
			m_entityDeriv = null;
			clearSelectedEntity();
		}
	}
	
	public EditBase getSelectedEntity()
	{
		if (m_selectedRow != null)
			return m_selectedRow;
		else if (m_selectedCol != null)
			return m_selectedCol;
		else
			return null;		
	}
	
	private void clearSelectedEntity()
	{
		m_selectedRow = null;
		m_selectedCol = null;
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
		m_selectedRow = null;
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
		m_selectedCol = null;
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
    	m_selectedCol = null;
    }
    
    public EditColumn getSelectedColumn()
    {
    	return m_selectedCol;
    }
    
    public void setSelectedColumn(EditColumn ec)
    {
    	m_selectedCol = ec;
    	m_selectedRow = null;
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
			if (val != null && (val = val.trim()).length() <= 0)
				val = null;
			m_te.setLabel(val);
		}
		
		public String getDisplayFormat()
		{
			return m_te.getDisplayFormat();
		}
		
		public void setDisplayFormat(String val)
		{
			if (val != null && (val = val.trim()).length() <= 0)
				val = null;
			m_te.setDisplayFormat(val);
		}
		
		public String getUuid()
		{
			return m_te.getUuid();
		}
		
		public String getDerivation()
		{
			if (isValid() && isDerived())
				return m_te.getDerivation().getAsEnteredExpression();
			else
				return "";
		}
		
		public boolean isDerived()
		{    
			if (isValid())
				return m_te.isDerived();
			else
				return false;
		}
		
		public boolean isValid()
		{
			return m_te.isValid();
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
				if (p.isCenterAligned() || cell.isErrorValue())
					return "display: table-cell;text-align: center;";
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
			if (cell != null) {
				if (cell.isErrorValue())
					return "--Error--";
				else
					return cell.getFormattedCellValue();
			}
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
