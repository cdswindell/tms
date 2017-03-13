package org.tms.tds.excel;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;

import org.apache.poi.ss.SpreadsheetVersion;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.tms.api.Access;
import org.tms.api.ElementType;
import org.tms.api.exceptions.UnsupportedImplementationException;
import org.tms.api.io.IOOption;
import org.tms.api.io.XLSOptions;
import org.tms.io.ExcelTableExportAdapter;
import org.tms.io.TableExportAdapter;
import org.tms.tds.ContextImpl;
import org.tms.tds.ExternalDependenceTableElement;
import org.tms.tds.TableImpl;

public class ExcelTableImpl extends TableImpl implements ExternalDependenceTableElement
{
    public static final ExcelTableImpl createTable(File excelFile, XLSOptions options) 
    throws IOException
    {
        return new ExcelTableImpl(excelFile, options, ContextImpl.fetchDefaultContext());
    }
    
    public static final ExcelTableImpl createTable(File excelFile, XLSOptions options, ContextImpl tc) 
    throws IOException
    {
        return new ExcelTableImpl(excelFile, options, tc);
    }

    private int m_numExcelCols;
    private int m_numExcelRows;
    
	private File m_excelFile;
	private XLSOptions m_opts;
	
    private Workbook m_wb;
    private Sheet m_sheet;
    private SpreadsheetVersion m_ssV;
    
    ExcelTableImpl(File excelFile, XLSOptions options, ContextImpl tc) 
    throws IOException
    {
        // initialize the default table object
        super(tc.getRowCapacityIncr(), tc.getColumnCapacityIncr(), tc);
        m_numExcelCols = m_numExcelRows = 0;
        
    	if (excelFile == null)
    		throw new IllegalArgumentException("Excel File required");
    	
    	if (!excelFile.exists())
    		throw new FileNotFoundException("Excel File not found: " + excelFile.getPath());
    	
    	if (!excelFile.canRead())
    		throw new IllegalArgumentException(excelFile.getPath() + " cannot be opened for read access");
    	
        m_excelFile = excelFile;
        m_opts = options;
        
        processFile(null);
    }
    
    @Override
    public void export(String fileName, IOOption<?> options) 
    throws IOException
    {
        TableExportAdapter writer = new ExcelTableExportAdapter(this, fileName, options);
        writer.export();
    }

    @Override
    public void export(OutputStream out, IOOption<?> options) 
    throws IOException
    {
        TableExportAdapter writer = new ExcelTableExportAdapter(this, out, options);
        writer.export();
    }

    public int getNumExcelColumns()
    {
        return m_numExcelCols;
    }
    
    public int getNumExcelRows()
    {
        return m_numExcelRows;
    }
    
    public File getExcelFile() 
    {
    	return m_excelFile;
    }
    
    boolean isRowNames()
    {
        return m_opts.isRowLabels();
    }
    
    /**
     * Return {@code true} if the Default file contains column names.
     * @return true if the Default file contains column names
     */
    boolean isColumnNames()
    {
        return m_opts.isColumnLabels();
    }
    
    public SpreadsheetVersion getVersion()
    {
    	return m_ssV;
    }
    

	public Sheet getSheet() 
	{
		return m_sheet;
	}
	
    @SuppressWarnings("resource")
	private void processFile(Object sheetRef) 
    throws IOException 
    {
    	FileInputStream excelFileInputStream = null;
        try {
            m_numExcelCols = m_numExcelRows = 0;
            
            // open excel file for read access
            excelFileInputStream = new FileInputStream(m_excelFile);
            
            m_wb = WorkbookFactory.create(excelFileInputStream);
            m_ssV = m_wb instanceof XSSFWorkbook ? SpreadsheetVersion.EXCEL2007 : SpreadsheetVersion.EXCEL97;     
            
            // get appropriate sheet
            if (sheetRef == null) {
            	int asi = m_wb.getActiveSheetIndex();
            	m_sheet = m_wb.getSheetAt(asi);
            }
            else if (sheetRef instanceof Integer) {
            	int si = (Integer) sheetRef;
            	if (si >= 0 && si < m_wb.getNumberOfSheets())
            		m_sheet = m_wb.getSheetAt(si);
            	else
            		throw new IllegalArgumentException("Sheet index out of bounds");
            }
            else if (sheetRef instanceof String) {
            	m_sheet = m_wb.getSheet((String)sheetRef);
            	
            	if (m_sheet == null)
            		throw new IllegalArgumentException("Named sheet not found");
            }
            else
        		throw new IllegalArgumentException("Invalid Sheet Reference");

            // create row data structures
            m_numExcelRows = m_sheet.getLastRowNum() + 1;
            
            setRowsCapacity(calcRowsCapacity(Math.max(m_numExcelRows, getNumRows())));
            int rIdx = 1;
            int numCols = 0;
            org.apache.poi.ss.usermodel.Row eR = null;
            for (int i = isColumnNames() ? 1 : 0; i < m_numExcelRows; i++) {
        		eR = m_sheet.getRow(i);
            	if (m_opts.isIgnoreEmptyRows()) {
            		if (eR == null || eR.getFirstCellNum() < 0)
            			continue;
            	}
            	
            	numCols = Math.max(eR.getLastCellNum(), numCols);
            	
                ExcelRowImpl row = new ExcelRowImpl(this, eR);
                add(row, false, false, Access.ByIndex, rIdx++);
            }
           
            // process column information
        	m_numExcelCols = numCols - (isRowNames() ? 1 : 0);
            setColumnsCapacity(calcColumnsCapacity(m_numExcelCols));
            
            int cIdx = 1;
        	for (int i = isRowNames() ? 1 : 0; i < numCols; i++) {
        		ExcelColumnImpl c = new ExcelColumnImpl(this, i);
                add(c, false, false, Access.ByIndex, cIdx++);
        	}            
        } 
        catch (Exception e) {
			releaseResources();
			
			if (excelFileInputStream != null)
				excelFileInputStream.close();
			
			if (e instanceof IllegalArgumentException)
				throw (IllegalArgumentException)e;
		}
    }

    private void releaseResources()
    {
    	if (m_wb != null) {
    		try {
    			m_wb.close();
			} catch (IOException e) { /* noop */ }
     	}

    }
    
    @Override
    protected void delete(boolean compress)
    {
        try {
            super.delete(compress);
        }
        finally {
            releaseResources();
        }
    }
    
    @Override
    public void finalize() 
    {
        try {
            super.finalize();   
        }
        finally {
            releaseResources();
        }
    }
    
    @Override
    public boolean clear()
    {
        throw new UnsupportedImplementationException(ElementType.Cell, "Cannot clear a log file element");
    }
    
    @Override
    public boolean fill(Object o)
    {
        throw new UnsupportedImplementationException(ElementType.Cell, "Cannot fill a log file row/column");
    }
}
