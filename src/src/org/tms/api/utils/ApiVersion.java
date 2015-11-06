package org.tms.api.utils;

/**
 * Chronicles the major releases of the TMS Framework
 * <p>
 * @since {@value org.tms.api.utils.ApiVersion#INITIAL_VERSION_STR}
 * @version {@value org.tms.api.utils.ApiVersion#CURRENT_VERSION_STR}
 */
public final class ApiVersion
{
    protected static final ApiVersion IO_ENHANCEMENTS_VERSION = new ApiVersion(1,1,0,0, "IO Enhancements");
    protected static final ApiVersion INITIAL_VERSION = new ApiVersion(1,0,0,0, "Initial Version");
    
    public static final ApiVersion CURRENT_VERSION = IO_ENHANCEMENTS_VERSION;
            
    public static final String IO_ENHANCEMENTS_STR = "1.1";
    public static final String INITIAL_VERSION_STR = "1.0";    
    
    public static final String CURRENT_VERSION_STR = IO_ENHANCEMENTS_STR;

    private int m_epoch;
    private int m_major;
    private int m_minor;
    private int m_build;
    private String m_label;
   
    private ApiVersion(int epoch, int major, int minor, int build, String label)
    {
        m_epoch = epoch;
        m_major = major;
        m_minor = minor;
        m_build = build;
        m_label = label;                
    }

    public int getEpoch() { return m_epoch; }
    public int getMajor() { return m_major; }
    public int getMinor() { return m_minor; }
    public int getBuild() { return m_build; }
    public String getLabel() { return m_label; }
    
    public String toString() { return String.format("%d.%d.%d.%d", m_epoch, m_major, m_minor, m_build); }
    
    public String toVersionString() { return String.format("%d.%d", m_epoch, m_major); }
    public String toFullVersionString() { return toString(); }

    public static ApiVersion parse(String verStr)
    {
        // TODO Auto-generated method stub
        return null;
    }
}
