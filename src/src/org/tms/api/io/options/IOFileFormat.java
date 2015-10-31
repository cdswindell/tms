package org.tms.api.io.options;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * The external file formats that TMS {@link org.tms.api.Table Table}s can be exported to
 * and, in some cases, imported from.
 * <p>
 * @since {@value org.tms.api.utils.ApiVersion#IO_ENHANCEMENTS_STR}
 * @version {@value org.tms.api.utils.ApiVersion#CURRENT_VERSION_STR}
 */
public enum IOFileFormat 
{
    /** Comma-separated file (CSV) format */
    CSV(true, "csv"),
    /** MS Word file format */
    WORD(false, "docx", "doc"),
    /** MS Excel (both xls and xlsx) file format*/
    EXCEL(true, "xlsx", "xls"), 
    /** HTML file format */
    HTML(false, "htm", "html"),
    /** PDF file format */
    PDF(false, "pdf"),
    JSON(true, "json", "jsn"),
    /** Rich Text File (RTF) file format */
    RTF(false, "rtf"),
    /** XML file format */
    XML(true, "xml"),
    /** Native TMS file format */
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
    
    /**
     * Returns {@code true} if this file format supports import, meaning that
     * {@link org.tms.api.Table Table}s can be created from files in this format.
     * @return {@code true} if this file format supports import
     */
    public boolean isSupportsImport()
    {
        return m_supportsImport;
    }

    /**
     * Returns a {@link java.util.List List} view of the file name extensions
     * commonly associated with this file format.
     * @return a {@code List} of the extensions commonly associated with this file format
     */
    public List<String> getFileExtensions()
    {
        return Collections.unmodifiableList(new ArrayList<String>(m_fileExtensions));
    }
}
