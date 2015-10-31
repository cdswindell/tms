package org.tms.io.options;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.tms.api.io.options.CSVOptions;
import org.tms.api.io.options.HTMLOptions;
import org.tms.api.io.options.IOOptions;
import org.tms.api.io.options.PDFOptions;
import org.tms.api.io.options.RTFOptions;
import org.tms.api.io.options.TMSOptions;
import org.tms.api.io.options.XMLOptions;
import org.tms.api.io.options.XlsOptions;

public enum IOFileFormat 
{
    CSV(true, "csv"),
    WORD(false, "docx", "doc"),
    EXCEL(true, "xlsx", "xls"), 
    HTML(false, "htm", "html"),
    PDF(false, "pdf"),
    JSON(true, "json"),
    RTF(false, "rtf"),
    XML(true, "xml"),
    TMS(true, "tms");
    
    private boolean m_supportsImport;
    private Set<String> m_fileExtensions;
    
    private IOFileFormat(boolean supportsImport, String... fileExtensions)
    {
        m_supportsImport = supportsImport;
        m_fileExtensions = new HashSet<String>();
        if (fileExtensions != null) {
            for (String ext : fileExtensions) {
                m_fileExtensions.add(ext.trim().toLowerCase());
            }
        }
    }
    
    public boolean isSupportsImport()
    {
        return m_supportsImport;
    }
    
    public static IOOptions generateOptionsFromFileExtension(File file)
    {
        if (sf_FileFormatMap.isEmpty()) {
            for (IOFileFormat ff : IOFileFormat.values()) {
                for (String ext : ff.m_fileExtensions) {
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
                            return XlsOptions.Default.withXlsFormat();
                        else
                            return XlsOptions.Default;
                        
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
    
}
