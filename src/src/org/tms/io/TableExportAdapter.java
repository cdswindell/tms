package org.tms.io;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.tms.api.Column;
import org.tms.api.ElementType;
import org.tms.api.Row;
import org.tms.api.Table;
import org.tms.api.TableContext;
import org.tms.api.exceptions.UnimplementedException;
import org.tms.api.io.IOOption;
import org.tms.api.io.CSVOptions;
import org.tms.api.io.HTMLOptions;
import org.tms.api.io.IOFileFormat;
import org.tms.api.io.PDFOptions;
import org.tms.api.io.RTFOptions;
import org.tms.api.io.TMSOptions;
import org.tms.api.io.XMLOptions;
import org.tms.api.io.XLSOptions;
import org.tms.io.options.TitledPageIOOptions;

public class TableExportAdapter
{
    public static IOOption<?> generateOptionsFromFileExtension(String fileName)
    {
        return generateOptionsFromFileExtension(new File(fileName));
    }
    
    public static IOOption<?> generateOptionsFromFileExtension(File file)
    {
        if (sf_FileFormatMap.isEmpty()) {
            for (IOFileFormat ff : IOFileFormat.values()) {
                for (String ext : ff.getFileExtensions()) {
                    sf_FileFormatMap.put(ext, ff);                    
                }
            }
        }
        
        String fileName = file.getName();
        int idx = fileName.lastIndexOf('.');
        if (idx >= -1) {
            String ext = fileName.substring(idx + 1).trim().toLowerCase();
            IOFileFormat fmt = sf_FileFormatMap.get(ext);
            if (fmt != null) {
                switch (fmt) {
                    case TMS:
                        return TMSOptions.Default;
                        
                    case CSV:
                        return CSVOptions.Default;
                        
                    case HTML:
                        return HTMLOptions.Default;
                        
                    case RTF:
                        return RTFOptions.Default;
                        
                    case PDF:
                        return PDFOptions.Default;
                        
                    case XML:
                        return XMLOptions.Default;
                        
                    case EXCEL:
                        // if xls file, return modified option
                        if ("xls".equalsIgnoreCase(ext))
                            return XLSOptions.Default.withXlsFormat();
                        else
                            return XLSOptions.Default;
                        
                    default:
                        break;
                }
            }
            
            return null;
        }
        else
            return TMSOptions.Default;
    }
    
    private static final Map<String, IOFileFormat> sf_FileFormatMap = new HashMap<String, IOFileFormat>();
    
    private Table m_table;
    private IOOption<?> m_options;
    private File m_file;
    private OutputStream m_output;
    private boolean m_isFileBased;
    private List<Row> m_cachedRows;
    
    public TableExportAdapter(Table t, IOOption<?> options) 
    {
        if (t == null)
            throw new IllegalArgumentException("Table required");
        
        m_table = t;
    }
        
    public TableExportAdapter(Table t, String fileName, IOOption<?> options) 
    throws IOException
    {
        this(fileName, options, true);
        
        if (t == null)
            throw new IllegalArgumentException("Table required");
        
        m_table = t;
    }
        
    protected TableExportAdapter(String fileName, IOOption<?> options, boolean performExportCheck) 
    throws IOException
    {
        if (fileName == null || (fileName = fileName.trim()).length() <= 0)
            throw new IllegalArgumentException("File name required");
        
        m_file = new File(fileName);
        
        // check if file can be written
        if (!canWrite())
                throw new IOException("Cannot write to " + fileName);
        
        // select default options, if none provided
        if (options == null) {
            m_options = TableExportAdapter.generateOptionsFromFileExtension(m_file);
            
            if (m_options == null)
                throw new UnimplementedException(String.format("No support for writting %s", fileName));
        }
        else {
            m_options = options;
        }
        
        if (performExportCheck && !m_options.canExport())
            throw new UnimplementedException(String.format("No support for exporting %s (%s)", fileName, m_options.getFileFormat()));
        
        // create the output stream
        m_output = new FileOutputStream(m_file);
        
        // indicate that this writer is based on a file, output stream should be closed
        m_isFileBased = true;
    }

    public TableExportAdapter(Table t, OutputStream out, IOOption<?> options) 
    throws IOException
    {
        this(out, options, true);
        m_table = t;
    }
    
    protected TableExportAdapter(OutputStream out, IOOption<?> options, boolean performExportCheck) 
    throws IOException
    {        
        if (options == null)
            throw new UnimplementedException("Options required");
        
        m_output = out;
        m_options = options;
        m_isFileBased = false;
        
        if (performExportCheck && !m_options.canExport())
            throw new UnimplementedException(String.format("No support for exporting %s", m_options.getFileFormat()));        
    }
    
    protected OutputStream getOutputStream()
    {
        return m_output;
    }
    
    protected boolean isFileBased()
    {
        return m_isFileBased;
    }
    
    protected IOOption<?> options()
    {
        return m_options;
    }
    
    public ElementType getTableElementType()
    {
    	return ElementType.Table;
    }
    
    private boolean canWrite()
    {
        if (m_file.exists())
            return m_file.canWrite();
          
        // have to try to create the file; if we can, then delete it right away
        try
        {
            m_file.createNewFile();
            return true;
        }
        catch (IOException e)
        {
            return false;
        }
        finally {
            if (m_file.exists())
                m_file.delete();
        }
    }

    public void export() 
    throws IOException
    {
        switch (m_options.getFileFormat()) {
            case TMS:
                TMSWriter.export(this, m_output, (TMSOptions)m_options);
                break;
                
            case CSV:
                CSVWriter.export(this, m_output, (CSVOptions)m_options);
                break;
                
            case EXCEL:
                XlsWriter.export(this, m_output, (XLSOptions)m_options);
                break;
                
            case XML:
                XMLWriter.export(this, m_output, (XMLOptions)m_options);
                break;
                
            case PDF:
            case RTF:
            case HTML:
            case WORD:
                JasperWriter.export(this, m_output, (TitledPageIOOptions<?>)m_options);
                break;
                
            default:
                break;
        }
        
        if (m_isFileBased && m_output != null) {
            m_output.flush();
            m_output.close();
        }
    }

    public int getNumColumns()
    {
        return m_table.getNumColumns();
    }
    
    public List<Column> getColumns()
    {
        return m_table.getColumns();
    }

    public int getNumRows()
    {
        return m_table.getNumRows();
    }
    
    public List<Row> getRows()
    {
    	if (m_cachedRows == null)
    		m_cachedRows = m_table.getRows();
    	
        return m_cachedRows;
    }

    public Table getTable()
    {
        return m_table;
    }

    public TableContext getTableContext()
    {
        return m_table.getTableContext();
    }
    
    public Row getRow(int rowIndex)
    {
        List<Row> rows = getRows();
        if (rowIndex >= 1 && rowIndex <= rows.size())
            return rows.get(rowIndex - 1);
        else
            return null;
    }

    public BaseWriter<?> createProxy()
    {
        return null;
    }
}
