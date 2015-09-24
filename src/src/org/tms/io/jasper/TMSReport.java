package org.tms.io.jasper;

import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRField;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.design.JRDesignParameter;
import net.sf.jasperreports.engine.design.JRDesignStyle;
import net.sf.jasperreports.engine.design.JasperDesign;
import net.sf.jasperreports.engine.type.ModeEnum;

import org.tms.api.Column;
import org.tms.api.Table;
import org.tms.io.BaseWriter;
import org.tms.io.options.DateTimeFormatOption;
import org.tms.io.options.IOOptions;
import org.tms.io.options.PageableOption;
import org.tms.io.options.TitleableOption;


public class TMSReport
{
    private static final int sf_StandardFontPointSize = 8;
    private static final int sf_TitleFontPointSize = 14;
    
    private static final int sf_PortraitPageWidth = (int)(72 * 8.5); // 612px
    private static final int sf_PortraitPageHeight = (int)(72 * 11); // 792px
    
    private static final int sf_PageLeftMargin = 60;
    private static final int sf_PageRightMargin = 60;
    
    private static final int sf_PageTopMargin = 50;
    private static final int sf_PageBottomMargin = 30;
    
    private BaseWriter m_writer;
    private Table m_table;
    private IOOptions m_options;
    
    private Map<Column, JRField> m_columnFieldMap;
    private TMSDataSource m_jrDataSource;
    private Map<String, Object> m_jrParams;
    
    private JasperDesign m_jrDesign;
    private JasperReport m_jrReport;
    private JasperPrint m_jrPrint;
    
    public TMSReport(BaseWriter w)
    {
        m_writer = w;
        m_table = w.getTable();
        m_options = w.options();
    }
    
    BaseWriter getWriter()
    {
        return m_writer;
    }
    
    Table getTable()
    {
        return m_table;
    }
    
    IOOptions getOptions()
    {
        return m_options;
    }

    public void export() 
    throws IOException
    {
        FileOutputStream out = null;
        try
        {
            // generate the design, compile it, and run it
            generateReport();
            
            out = new FileOutputStream(m_writer.getOutputFile());
            
            // print report to file
            JasperExportManager.exportReportToPdfStream(m_jrPrint, out);
        }
        catch (JRException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        finally {
            if (out != null)
                out.close();
        }        
    }
    
    Map<Column, JRField> getColumnFieldMap()
    {
        return m_columnFieldMap;
    }
    
    void generateReport() 
    throws JRException
    {
        m_jrDataSource = new TMSDataSource(this);
        fillJasperParams();
        printJasperReport();
    }

    private void fillJasperParams()
    {
        m_jrParams = new HashMap<String, Object>();
        
        if (m_options instanceof TitleableOption) {
            if (((TitleableOption)m_options).hasTitle())
                m_jrParams.put("ReportTitle", ((TitleableOption)m_options).getTitle());
        }
               
        if (m_options instanceof DateTimeFormatOption) {
            if (((DateTimeFormatOption)m_options).hasDateTimeFormat()) {
                SimpleDateFormat sdf = new SimpleDateFormat(((DateTimeFormatOption)m_options).getDateTimeFormat());
                m_jrParams.put("now", sdf.format(new java.util.Date()));
            }
        }
    }

    private void printJasperReport() 
    throws JRException
    {
        if (m_jrParams == null)
            fillJasperParams();
        
        if (m_jrReport == null)
            compileJasperReport();
        
        m_jrPrint = JasperFillManager.fillReport(m_jrReport,  m_jrParams, m_jrDataSource);        
    }

    private void compileJasperReport() 
    throws JRException
    {
        if (m_jrDesign == null)
            buildJasperDesign();
        
        m_jrReport = JasperCompileManager.compileReport(m_jrDesign);        
    }

    private void buildJasperDesign() throws JRException
    {
        m_jrDesign = new JasperDesign();
        m_jrDesign.setName(m_table.getLabel() != null ? m_table.getLabel() : "TMS Table");
        
        // Paginated??
        boolean paginated = (m_options instanceof PageableOption) ? ((PageableOption)m_options).isPageNumbers() : false;
        m_jrDesign.setIgnorePagination(!paginated);
           
        int colWidth = 0;
        JRDesignStyle boldStyle = defineBoldStyle(m_jrDesign);
        
        defineGlobalParameters(m_jrDesign);
        
        // finally, set title and force recompile
        defineTitleBand(m_jrDesign, boldStyle, colWidth);
        
        m_jrReport = null;
        m_jrPrint = null;
    }

    private void defineTitleBand(JasperDesign jrDesign, JRDesignStyle boldStyle, int colWidth)
    {
        // TODO Auto-generated method stub
        
    }

    private JRDesignStyle defineBoldStyle(JasperDesign jrDesign) 
    throws JRException
    {
        float fontSize = sf_StandardFontPointSize;
        
        JRDesignStyle boldStyle = new JRDesignStyle();
        boldStyle.setName("Sans_Bold");
        boldStyle.setDefault(false);
        boldStyle.setFontName("SansSerif");
        boldStyle.setFontSize(fontSize);
        boldStyle.setBold(true);
        boldStyle.setPdfEmbedded(false);
        boldStyle.setMode(ModeEnum.OPAQUE);
        
        jrDesign.addStyle(boldStyle);
        
        return boldStyle;
    }

    private void defineGlobalParameters(JasperDesign jrDesign) throws JRException
    {
        JRDesignParameter titleParam = new JRDesignParameter();
        titleParam.setName("ReportTitle");
        titleParam.setForPrompting(false);
        titleParam.setValueClass(java.lang.String.class);
        jrDesign.addParameter(titleParam);
        
        JRDesignParameter nowParam = new JRDesignParameter();
        nowParam.setName("now");
        nowParam.setForPrompting(false);
        nowParam.setValueClass(java.lang.String.class);
        jrDesign.addParameter(nowParam);       
    }
}
