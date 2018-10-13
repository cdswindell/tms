package org.tms.io;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.tms.api.Access;
import org.tms.api.Cell;
import org.tms.api.Column;
import org.tms.api.ElementType;
import org.tms.api.Row;
import org.tms.api.Table;
import org.tms.api.TableContext;
import org.tms.api.TableElement;
import org.tms.api.TableProperty;
import org.tms.api.io.IOOption;

public abstract class BaseWriter<E extends IOOption<?>> extends BaseIO
{
    private TableExportAdapter m_tableExportAdapter;
    private OutputStream m_outStream;
    private E m_baseOptions;
    
    private int m_nCols;
    private int m_nConsumableColumns;
    private Set<Integer> m_ignoredColumns;
    private List<Column> m_activeCols;
    private Map<Integer, Integer> m_colIndexMap;
    
    private int m_nRows;
    private int m_nConsumableRows;
    private Set<Integer> m_ignoredRows;
    private List<Row> m_activeRows;
    private List<Row> m_allRows;
    private Map<Integer, Integer> m_rowIndexMap;
    private Map<Integer, Row> m_effRowIndexMap;
    
	private Writer m_outWriter;

    abstract protected void export() throws IOException;
    
    protected BaseWriter(TableExportAdapter tw, OutputStream out, E options)
    {
        m_outStream = out;
        m_baseOptions = options;
        
        reset(tw);
    }

    @SuppressWarnings("resource")
	protected BaseWriter(TableExportAdapter tw, File f, E options) 
    throws IOException
    {
        this(tw, new FileOutputStream(f), options);
    }

    protected void reset(TableExportAdapter tw)
    {
        m_tableExportAdapter = tw;
        
        m_nRows = tw.getNumRows();
        m_nConsumableRows = -1; // to be initialized later
        m_ignoredRows = null;
        m_activeRows = null;
        m_allRows = null;
        m_rowIndexMap = null;
        m_effRowIndexMap = null;
        
        m_nCols = tw.getNumColumns();
        m_nConsumableColumns = -1; // to be initialized later
        m_ignoredColumns = null;
        m_activeCols = null;
        m_colIndexMap = null;
    }
    
    public OutputStream getOutputStream()
    {
        return m_outStream;
    }
    
    public Writer getOutputWriter()
    {
    	if (m_outWriter == null)
    		m_outWriter = new OutputStreamWriter(getOutputStream());
    		
        return m_outWriter;
    }
    
    protected void flushOutput() throws IOException
    {
    	if (m_outWriter != null)
    		m_outWriter.flush();
    	
    	if (m_outStream != null)
    		m_outStream.flush();
    }
    
    /*
     * Publicly available methods for use outside of package
     */
    public E options()
    {
        return m_baseOptions;
    }
    
	protected Object getProperty(TableElement te, TableProperty tp) 
	{
		Object val = te.getProperty(tp);
		if (val != null) {
			switch (tp) {			
				case DataType:
					if (te.getElementType() == ElementType.Cell && !((Cell)te).isEnforceDataType())
						val = null;
					break;
				default:
					break;				
			}
		}
		
		return val;
	}

    protected boolean hasValue(TableElement te, TableProperty key)
    {
        if (te.hasProperty(key)) {
            Object val = getProperty(te, key);
            
            if (val != null) {
	            // one more check for empty strings
	            if (val instanceof String && ((String)val).trim().length() == 0)
	                return false;
	               
	            // if te isn't a table, check that value differs from parent table
	            if (te.getElementType() != ElementType.Table) {
	            	if (hasValue(te.getTable(), key) && te.getTable().getProperty(key) == val)
	            		return false;
	            }
	            
	            return true;
	        }
        }
        
        return false;        
    }
    
    public Table getTable()
    {
        return m_tableExportAdapter.getTable();
    }
    
    protected TableContext getTableContext()
    {
        return m_tableExportAdapter.getTableContext();
    }

    public ElementType getTableElementType()
    {
        return m_tableExportAdapter.getTableElementType();
    }
    
    public TableExportAdapter getExportAdapter()
    {
        return m_tableExportAdapter;
    }
    
    public boolean isTable()
    {
        return m_tableExportAdapter.getTableElementType() == ElementType.Table;
    }
    
   public int getNumConsumableColumns()
    {
        if (m_nConsumableColumns == -1) {
            m_nConsumableColumns = m_nCols;
            if (m_baseOptions.isIgnoreEmptyColumns()) {               
                Table t = getTable();
                m_ignoredColumns = new HashSet<Integer>();
                int emptyColCnt = 0;
                int remappedIdx = 0;
                for (int i = 1; i <= m_nCols; i++) {
                    if (t.isColumnDefined(Access.ByIndex, i)) {
                        Column c = t.getColumn(i);
                        if (c.isNull()) {
                            emptyColCnt++;
                            m_ignoredColumns.add(c.getIndex());
                        }
                        else {
                            if (m_colIndexMap == null)
                                m_colIndexMap = new HashMap<Integer, Integer>(m_nCols);
                            m_colIndexMap.put(c.getIndex(), ++remappedIdx);
                        }
                    }
                    else
                        m_ignoredColumns.add(i);
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
    
    public boolean isIgnore(Column c)
    {
        if (c == null)
            return true;
        
        if (!m_baseOptions.isIgnoreEmptyColumns())
            return false;
        
        if (m_ignoredColumns == null)
            getNumConsumableColumns();
        
        return m_ignoredColumns.contains(c.getIndex());           
    }
    
    /**
     * Return a list of active columns in the table. Active columns are non-empty columns, 
     * if isIgnoreEmptyColumns() is true, or all columns otherwise
     * @return
     */
    public List<Column> getActiveColumns()
    {
        if (m_baseOptions.isIgnoreEmptyColumns()) {
            if (m_activeCols == null) {                    
                if (m_activeCols == null) {
                    m_activeCols = new ArrayList<Column>(getNumConsumableColumns());
                    for (int i = 1; i <= m_nCols; i++) {
                        if (!isIgnoreColumn(i))
                            m_activeCols.add(getTable().getColumn(i));                               
                    }
                }               
            }
            
            return m_activeCols;
        }
        else        
            return m_tableExportAdapter.getColumns();
    }
    
    public int getNumActiveColumns()
    {
        return getActiveColumns().size();
    }
    
    public int getRemappedColumnIndex(Column col)
    {
        if (col == null)
            return -1;
        
        return getRemappedColumnIndex(col.getIndex());
    }
    
    public int getRemappedColumnIndex(int colIdx)
    {
        if (!m_baseOptions.isIgnoreEmptyColumns())
            return colIdx;
        
        // make sure we have the info computed
        if (m_ignoredColumns == null)
            getNumConsumableColumns();
        
        int remappedIdx = isIgnoreColumn(colIdx) ? -1 : colIdx;
        if (m_colIndexMap != null && remappedIdx > -1) {
            if (m_colIndexMap.containsKey(colIdx))
                remappedIdx = m_colIndexMap.get(remappedIdx);
            else
                remappedIdx = -1;
        }
        
        return remappedIdx;
    }
    
    public int getNumConsumableRows()
    {
        if (m_nConsumableRows == -1) {
            m_nConsumableRows = getNumRows();
            m_activeRows = new ArrayList<Row>(m_nConsumableRows);
            if (m_baseOptions.isIgnoreEmptyRows()) {               
                Table t = getTable();
                m_ignoredRows = new HashSet<Integer>();
                int emptyRowCnt = 0;
                int remappedIdx = 0;
                if (getTableElementType() == ElementType.Table) {
	                for (int i = 1; i <= m_nRows; i++) {
	                    if (t.isRowDefined(Access.ByIndex, i)) {
	                        Row r = t.getRow(i);
	                        if (r.isNull()) {
	                            emptyRowCnt++;
	                            m_ignoredRows.add(r.getIndex());
	                        }
	                        else {
	                            if (m_rowIndexMap == null)
	                                m_rowIndexMap = new HashMap<Integer, Integer>(m_nRows);	                            
	                            
	                            if (m_effRowIndexMap == null)
	                            	m_effRowIndexMap = new HashMap<Integer, Row>(m_nRows);
	                            
	                            m_rowIndexMap.put(r.getIndex(), ++remappedIdx);
	                            m_effRowIndexMap.put(remappedIdx, r);
	                            m_activeRows.add(r);
	                        }
	                    }
	                    else {
                            emptyRowCnt++;
	                        m_ignoredRows.add(i);
	                    }
	                }
                }
                else {
                	for (Row r : getRows()) {
                        if (r != null && r.isNull()) {
                            emptyRowCnt++;
                            m_ignoredRows.add(r.getIndex());
                        }
                        else {
                            if (m_rowIndexMap == null)
                                m_rowIndexMap = new HashMap<Integer, Integer>(m_nRows);
                            
                            if (m_effRowIndexMap == null)
                            	m_effRowIndexMap = new HashMap<Integer, Row>(m_nRows);
                            
                            m_rowIndexMap.put(r.getIndex(), ++remappedIdx);
                            m_effRowIndexMap.put(remappedIdx, r);
                            m_activeRows.add(r);
                        }                		
                	}
                }
                
                m_nConsumableRows -= emptyRowCnt;
            }
            else {
                if (getTableElementType() == ElementType.Table) {
                	m_ignoredRows = Collections.emptySet();
                	m_activeRows = getRows();
                }
                else {
                    m_ignoredRows = new HashSet<Integer>();
                    int remappedIdx = 0;
                	for (Row r : getRows()) {
                        if (r != null) {
                            if (m_rowIndexMap == null)
                                m_rowIndexMap = new HashMap<Integer, Integer>(m_nRows);
                            
                            if (m_effRowIndexMap == null)
                            	m_effRowIndexMap = new HashMap<Integer, Row>(m_nRows);
                            
                            m_rowIndexMap.put(r.getIndex(), ++remappedIdx);
                            m_effRowIndexMap.put(remappedIdx, r);
                            m_activeRows.add(r);
                        }                		
                	}
                	
                	m_nConsumableRows = remappedIdx;
                }
            }
        }
        
        return m_nConsumableRows;
    }
    
    public boolean isIgnoreRow(int i)
    {
        if (!m_baseOptions.isIgnoreEmptyRows())
            return false;
        
        if (m_ignoredRows == null)
            getNumConsumableRows();
        
        return m_ignoredRows.contains(i);           
    }
    
    public boolean isIgnore(Row r)
    {
        if (r == null)
            return true;
        
        return isIgnoreRow(r.getIndex());        
    }
    
    public int getRemappedRowIndex(Row row)
    {
        if (row == null)
            return -1;
        
        return getRemappedRowIndex(row.getIndex());
    }
    
    public int getRemappedRowIndex(int rowIdx)
    {
        if (!m_baseOptions.isIgnoreEmptyRows() && getTableElementType() == ElementType.Table)
            return rowIdx;
        
        // make sure we have the info computed
        if (m_ignoredRows == null)
            getNumConsumableRows();
        
        int remappedIdx = isIgnoreRow(rowIdx) ? -1 : rowIdx;
        if (m_rowIndexMap != null && remappedIdx > -1) {
            if (m_rowIndexMap.containsKey(rowIdx))
                remappedIdx = m_rowIndexMap.get(remappedIdx);
            else
                remappedIdx = -1;
        }
        
        return remappedIdx;
    }
    
    private List<Row> getRows()
    {
    	if (m_allRows == null)
    		m_allRows = m_tableExportAdapter.getRows();
    	
        return m_allRows;
    }
    
    public List<Row> getActiveRows()
    {
    	if (m_activeRows == null)
    		getNumConsumableRows();
    	
        return m_activeRows;
    }
    
    public int getNumRows()
    {
        return m_tableExportAdapter.getNumRows();
    }

	public Row getRow(int rowIdx) 
	{
		rowIdx = getRemappedRowIndex(rowIdx);
		if (rowIdx > -1)
			return getRows().get(rowIdx - 1);
		else
			return null;
	}

	public Row getRowByEffectiveIndex(int i) 
	{
		if (m_effRowIndexMap == null)
			return getRow(i);
		
		return m_effRowIndexMap.get(i);
	}

    public BaseWriter<?> createDelegate(TableExportAdapter tea)
    {
        return this;
    }
}
