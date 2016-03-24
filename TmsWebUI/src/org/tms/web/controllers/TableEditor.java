package org.tms.web.controllers;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.faces.event.ValueChangeEvent;

import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.LazyDataModel;
import org.primefaces.model.SortOrder;
import org.primefaces.model.StreamedContent;
import org.primefaces.model.UploadedFile;
import org.tms.api.Access;
import org.tms.api.Cell;
import org.tms.api.Column;
import org.tms.api.Row;
import org.tms.api.Table;
import org.tms.api.TableContext;
import org.tms.api.TableRowColumnElement;
import org.tms.api.derivables.Derivation;
import org.tms.api.factories.TableFactory;
import org.tms.api.io.TMSOptions;
import org.tms.io.Printable;

@ManagedBean(name="tableEdit" )
@ViewScoped

public class TableEditor implements Serializable
{
	private static final long serialVersionUID = 1L;
	private static final String sf_GROOVY = "Groovy";
	private static final String sf_PYTHON = "Python";
	
	private static final String sf_STANDARD = "Standard";
	private static final String sf_TIMESERIES = "TimeSeries";

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
	private UploadedFile m_tableFile;

	private LazyTableModel m_lazyTableModel;
	private String m_derivType;
	
	public TableEditor()
	{
		m_derivType = sf_STANDARD;
		m_scriptType = sf_GROOVY;
		m_lazyTableModel = null;
	}	
	
	public void processChanges()
	{
		// noop
	}
		
	public LazyTableModel getPagedRows()
	{
		if (m_lazyTableModel == null)
			m_lazyTableModel = new LazyTableModel();
		
		return m_lazyTableModel;
	}	
	
	public boolean isBigTable()
	{
		return m_tableViewer.isBigTable();
	}
	
	public boolean isCellEditable(EditRow r, EditColumn c)
	{
		if (r != null && c != null) {
			Cell cell = getTableCell(r, c);
			return !cell.isReadOnly();
		}
			
		return true;
	}
	
	private Cell getTableCell(EditRow r, EditColumn c) 
	{
		return m_tableViewer.getTable().getCell((Row)r.getTE(), (Column)c.getTE());
	}

	public void clearTable()
	{
		m_tableViewer.getTable().clear();
		resetTableEditor();
	}
	
	public String getTableName()
	{
		return m_tableViewer.getTable().getLabel();
	}
	
	public void setTableName(String tableName)
	{
		m_tableViewer.getTable().setLabel(tableName);
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
		System.out.println(String.format("FileType changed, Old: %s New: %s", vce.getOldValue(), vce.getNewValue()));		
	}
	
	public UploadedFile getFile() 
	{
		return m_file;
	}

	public void setFile(UploadedFile file) 
	{
		this.m_file = file;
	}

	public UploadedFile getTableFile() 
	{
		return m_tableFile;
	}

	public void setTableFile(UploadedFile file) 
	{
		this.m_tableFile = file;
	}

	public StreamedContent getSavedFile() 
	throws IOException 
	{
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
		m_tableViewer.getTable().export(bos, TMSOptions.Default);
		bos.close();
		
		InputStream stream = new ByteArrayInputStream(bos.toByteArray());
		
		String tableName = m_tableViewer.getTable().getLabel();
		if (tableName == null || (tableName = tableName.trim()).length() <= 0)
			tableName = "table";
		tableName+= ".tms";
		
		return new DefaultStreamedContent(stream, "application/octet-stream", tableName);
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
	
	public void uploadTable(FileUploadEvent event) throws IOException 
	{
		m_tableFile = event.getFile();
		if (m_tableFile != null) {
			TableContext tc = m_tableViewer.getTable().getTableContext();
			
			Table t = TableFactory.importFile(m_tableFile.getInputstream(), tc, TMSOptions.Default);
			m_tableViewer.init(t);
            
			resetTableEditor();
	        m_tableFile = null;
			
            FacesMessage message = new FacesMessage("Succesful", "Table loaded...");
            FacesContext.getCurrentInstance().addMessage(null, message);
		}
	}
	
	public void resetTableEditor()
	{
		clearSelectedEntity();
		
		m_eRows = null;
		m_eCols = null;
		
		m_lazyTableModel = null;
        
        if (m_tableViewer != null)
        	m_tableViewer.resetRefreshNeeded();
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
		resetTableEditor();
		m_tableViewer.init(true);
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
		if (m_tableViewer.isRefreshNeeded())
			resetTableEditor();
		
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
		if (m_tableViewer.isRefreshNeeded())
			resetTableEditor();
		
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

	public void derivationTypeChanged(ValueChangeEvent vce)
	{
		System.out.println(String.format("Derivation Type changed, Old: %s New: %s", vce.getOldValue(), vce.getNewValue()));
		m_derivType = (String)vce.getNewValue();
		resetDerivAndUpdate();
	}
	
	public int getUpdateEvery()
	{
		if (m_updateEvery < 0) {
			if (m_derivType == null)
				getEntityDerivationType();
			
			m_updateEvery = 0;
			
			TableRowColumnElement te = getSelectedEntity() != null ? getSelectedEntity().getTE() : null;
			if (te != null) {
				if (sf_TIMESERIES.equals(m_derivType)) 
					m_updateEvery = (int)te.getTable().getTimeSeriesedRowsPeriodInMilliSeconds() / 1000;
				else if (sf_STANDARD.equals(m_derivType) && te.isDerived()) 
					m_updateEvery = (int)te.getDerivation().getPeriodInMilliSeconds() / 1000;
			}
		}
		
		return m_updateEvery;
	}
	
	public long getGeneration()
	{
		return System.currentTimeMillis();
	}
	
	public void setUpdateEvery(int updateEvery)
	{
		m_updateEvery = updateEvery;
	}
	
	public String getEntityDerivation()
	{
		if (m_entityDeriv == null) {
			if (m_derivType == null)
				getEntityDerivationType();
			
			TableRowColumnElement te = getSelectedEntity() != null ? getSelectedEntity().getTE() : null;
			if (te != null) {
				if (sf_TIMESERIES.equals(m_derivType)) {
					if (te.isTimeSeries())
						m_entityDeriv = te.getTimeSeries().getAsEnteredExpression();
				}
				else if (te.isDerived())
					m_entityDeriv = te.getDerivation().getAsEnteredExpression();
			}
		}
		
		return m_entityDeriv;		
	}
	
	public void setEntityDerivation(String deriv)
	{
		m_entityDeriv = deriv;
	}	

	public String getEntityDerivationType()
	{
		if (m_derivType == null) {
			m_derivType = sf_STANDARD;	
			
			TableRowColumnElement te = getSelectedEntity() != null ? getSelectedEntity().getTE() : null;
			if (te != null) {
				if (te.isTimeSeries())
					m_derivType = sf_TIMESERIES;
			}
		}
		
		return m_derivType;
	}
	
	public void setEntityDerivationType(String dt)
	{
		m_derivType = dt;
	}

	public String getResetAllDerivParams()
	{
		m_derivType = null;
		resetDerivAndUpdate();
		return "";
	}
	
	public void resetDerivAndUpdate()
	{
		m_entityDeriv = null;
		m_updateEvery = -1;
	}
	
	public void applyDerivation()
	{
		TableRowColumnElement te = getSelectedEntity() != null ? getSelectedEntity().getTE() : null;
		if (te != null) {
			if (m_entityDeriv != null && (m_entityDeriv = m_entityDeriv.trim()).length() <= 0)
				m_entityDeriv = null;
			
			if (m_entityDeriv == null) {
				if (sf_TIMESERIES.equals(m_derivType))
					te.clearTimeSeries();
				else
					te.clearDerivation();
			}
			else {
				if (sf_TIMESERIES.equals(m_derivType)) {
					te.clearDerivation();
					te.setTimeSeries(m_entityDeriv);						
					if (!te.getTable().isTimeSeriesedRowsActive() && m_updateEvery > 0) {
						if (te instanceof Column) {
							Column tsCol = te.getTable().getColumn(Access.ByLabel, "Time Stamp");
							if (tsCol == null) {
								tsCol = te.getTable().addColumn(Access.ByIndex, 1);
								tsCol.setLabel("Time Stamp");
							}
							
							TimeSeriesScheduler tss = new TimeSeriesScheduler(te.getTable(), tsCol, m_updateEvery);
							tss.start();
						}
					}
				}
				else {
					te.clearTimeSeries();
					Derivation d = te.setDerivation(m_entityDeriv);				
					if (m_updateEvery > 0) 
						d.recalculateEvery(m_updateEvery, TimeUnit.SECONDS);
				}
			}
			
			m_entityDeriv = null;
			clearSelectedEntity();
		}
	}
	
	public void suspendTimeSeries()
	{
		m_tableViewer.getTable().suspendAllTimeSeries();
	}
	
	public void resumeTimeSeries()
	{
		m_tableViewer.getTable().resumeAllTimeSeries();
	}
	
	public boolean isSuspendTSDisabled()
	{
		Table t = m_tableViewer.getTable();
		return !t.isTimeSeriesedRowsActive() && !t.isTimeSeriesedColumnsActive();
	}
	
	public boolean isResumeTSDisabled()
	{
		Table t = m_tableViewer.getTable();
		boolean resumeAvailable = isSuspendTSDisabled() && 
								  ((t.isTimeSeriesedRows() && t.getTimeSeriesedRowsPeriodInMilliSeconds() > 0) || 
							       (t.isTimeSeriesedColumns() && t.getTimeSeriesedColumnsPeriodInMilliSeconds() > 0));								  
		return !resumeAvailable;
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
			m_tableViewer.getTable().addRow(idx);
		}
		else
			m_tableViewer.getTable().addRow();
		
		m_eRows = null;
		m_lazyTableModel = null;
	}
	
	public void deleteRow()
	{
		if (getSelectedRow() != null) 			
			m_tableViewer.getTable().delete(getSelectedRow().getRow());
		
		m_eRows = null;
		m_lazyTableModel = null;
		m_selectedRow = null;
	}
	
	public void addColumn(EditColumn eCol)
	{
		if (eCol != null) {
			int idx = eCol.getIndex();
			m_tableViewer.getTable().addColumn(idx);
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
			if (isValid() && m_te.isTimeSeries())
				return m_te.getTimeSeries().getAsEnteredExpression() + " (TS)";
			else if (isValid() && isDerived())
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
		
		public boolean isTimeSeries()
		{    
			if (isValid())
				return m_te.isTimeSeries();
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
	
	public class LazyTableModel extends LazyDataModel<EditRow>
	{
		private static final long serialVersionUID = -1L;
		
		private List<EditRow> m_rows;
		
		public LazyTableModel() 
		{
			super();
			m_rows = null;
			
			setPageSize(10);
			setRowIndex(0);
		}
		
		@Override
	    public List<EditRow> load(int first, int pageSize, String sortField, SortOrder sortOrder, Map<String,Object> filters) 
	    {
	        Table t = m_tableViewer.getTable();
			if (m_rows == null || first != getRowIndex() || pageSize != getPageSize() || m_rows.isEmpty() ) {
		        first = first>= 0 ? first : 0;
		        pageSize = pageSize > 0 ? pageSize : getPageSize() > 0 ?  getPageSize() : 10;
		        m_rows = new ArrayList<EditRow>(pageSize);
		        
		        int numRows = t.getNumRows();
		        for (int i = 0; i < pageSize && first + i + 1 <= numRows; i++) {
		        	m_rows.add(new EditRow(t.getRow( first + i + 1)));
		        }				
			}
			
			this.setRowCount(t.getNumRows());
			return m_rows;	        
	    }
		
		@Override
		public int getPageSize()
		{
			return super.getPageSize() > 0 ? super.getPageSize() : 10;
		}
		
		@Override
		public void setPageSize(int pageSize)
		{
			super.setPageSize(pageSize);
		}
		
	    @Override
	    public Object getRowKey(EditRow row) 
	    {
	        return row.getTE().getIndex();
	    }		
	    
	    @Override
	    public void setRowIndex(int rowIndex) {
	        /*
	         * The following is in ancestor (LazyDataModel):
	         * this.rowIndex = rowIndex == -1 ? rowIndex : (rowIndex % pageSize);
	         */
	        if (rowIndex == -1 || getPageSize() == 0) {
	            super.setRowIndex(-1);
	        }
	        else
	            super.setRowIndex(rowIndex % getPageSize());
	    }
	}
	
	static class TimeSeriesScheduler extends Thread
	{
		private Table m_parentTable;
		private Column m_timeStampCol;
		private int m_frequency;

		TimeSeriesScheduler(Table t, Column tsCol, int freq)
		{
			m_parentTable = t;
			m_timeStampCol = tsCol;
			m_frequency = freq;
		}
		
		@Override
		public void run() 
		{
			try {
				Thread.sleep(250);
			} catch (InterruptedException e) { /* noop */ }
			
			m_parentTable.enableTimeSeriesedRows(m_timeStampCol, m_frequency * 1000);
		}		
	}
}
