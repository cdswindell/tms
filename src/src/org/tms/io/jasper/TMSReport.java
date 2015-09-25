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
import net.sf.jasperreports.engine.design.JRDesignBand;
import net.sf.jasperreports.engine.design.JRDesignExpression;
import net.sf.jasperreports.engine.design.JRDesignField;
import net.sf.jasperreports.engine.design.JRDesignParameter;
import net.sf.jasperreports.engine.design.JRDesignStyle;
import net.sf.jasperreports.engine.design.JRDesignTextField;
import net.sf.jasperreports.engine.design.JasperDesign;
import net.sf.jasperreports.engine.type.HorizontalTextAlignEnum;
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
    private static final float sf_StandardFontPointSize = 8;
    private static final float sf_TitleFontPointSize = 14;
    
    private static final int sf_PortraitPageWidth = (int)(72 * 8.5); // 612px
    private static final int sf_PortraitPageHeight = (int)(72 * 11); // 792px
    
    private static final int sf_PageLeftMargin = 60;
    private static final int sf_PageRightMargin = 60;
    
    private static final int sf_PageTopMargin = 50;
    private static final int sf_PageBottomMargin = 30;
    
    private BaseWriter m_writer;
    private Table m_table;
    private IOOptions m_options;
    
    private Map<Column, JRField> m_colFieldMap;
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
        return m_colFieldMap;
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
        // Figure out how many columns we have
        int nCols = m_writer.getNumActiveColumns();
        
        m_jrDesign = new JasperDesign();
        m_jrDesign.setName(m_table.getLabel() != null ? m_table.getLabel() : "TMS Table");
        
        // Paginated??
        boolean paginated = (m_options instanceof PageableOption) ? ((PageableOption)m_options).isPaged() : false;
        m_jrDesign.setIgnorePagination(!paginated);
        
        int pageWidth = getPageWidth();
        int colWidth = paginated ? pageWidth - sf_PageLeftMargin - sf_PageRightMargin : pageWidth;
                
        // set page parameters, including size and margin
        m_jrDesign.setPageWidth(pageWidth);
        if (paginated) {
            m_jrDesign.setLeftMargin(sf_PageLeftMargin);
            m_jrDesign.setRightMargin(sf_PageRightMargin);
            m_jrDesign.setColumnCount(1);
            m_jrDesign.setColumnWidth(colWidth);
        }
        else {
            m_jrDesign.setLeftMargin(0);
            m_jrDesign.setRightMargin(0);
        }
        
        int pageHeight = getPageHeight();
        m_jrDesign.setPageHeight(pageHeight);
        if (paginated) {
            m_jrDesign.setTopMargin(sf_PageTopMargin);
            m_jrDesign.setBottomMargin(sf_PageBottomMargin);
        }
        else {
            m_jrDesign.setTopMargin(0);
            m_jrDesign.setBottomMargin(0);
        }    

        // define font styles
        JRDesignStyle boldStyle = defineStyle(m_jrDesign, "Sans_Bold", sf_StandardFontPointSize, false, true);
        JRDesignStyle normalStyle = defineStyle(m_jrDesign, "Sans_Normal", sf_StandardFontPointSize, true, false);
        
        defineGlobalParameters(m_jrDesign);
        
        // create JR fields for each printable column
        m_colFieldMap = new HashMap<Column, JRField>(nCols);
        for (Column col : m_writer.getActiveColumns()) {
            JRDesignField jrField = new JRDesignField();
            jrField.setName(String.valueOf(col.getIndex()));
            
            m_jrDesign.addField(jrField);
            m_colFieldMap.put(col, jrField);
        }
            
        // finally, set title and force recompile
        if ((m_options instanceof TitleableOption) && ((TitleableOption)m_options).hasTitle())
            defineTitleBand(m_jrDesign, boldStyle, colWidth);
        
        m_jrReport = null;
        m_jrPrint = null;
    }

    private int getPageWidth()
    {
        int pageWidth = 0;
        if ((m_options instanceof PageableOption)) {
            pageWidth = ((PageableOption)m_options).getPageWidth();
            if (pageWidth <= 0)
                pageWidth = sf_PortraitPageWidth;
        }
        
        return pageWidth;
    }

    private int getPageHeight()
    {
        int pageHeight = 0;
        if ((m_options instanceof PageableOption)) {
            pageHeight = ((PageableOption)m_options).getPageHeight();
            if (pageHeight <= 0)
                pageHeight = sf_PortraitPageHeight;
        }
        
        return pageHeight;
    }

    private JRDesignBand defineTitleBand(JasperDesign jrDesign, JRDesignStyle boldStyle, int colWidth)
    {
        JRDesignBand titleBand = new JRDesignBand();
        titleBand.setHeight(35);
        
        JRDesignTextField titleField = new JRDesignTextField();
        titleField.setBlankWhenNull(true);
        titleField.setX(0);
        titleField.setY(5);
        titleField.setWidth(colWidth);
        titleField.setStretchWithOverflow(true);
        titleField.setHeight(30);
        titleField.setHorizontalTextAlign(HorizontalTextAlignEnum.CENTER);
        titleField.setStyle(boldStyle);
        titleField.setFontSize(sf_TitleFontPointSize);
        
        JRDesignExpression titleExpression = new JRDesignExpression();
        titleExpression.setText("$P{ReportTitle}");
        titleField.setExpression(titleExpression);
        titleBand.addElement(titleField);
        jrDesign.setTitle(titleBand);
        
        return titleBand;
    }

    private JRDesignStyle defineStyle(JasperDesign jrDesign, String name, float fontSize, boolean isDefault, boolean isBold) 
    throws JRException
    {
        JRDesignStyle style = new JRDesignStyle();
        style.setName(name);
        style.setDefault(isDefault);
        style.setFontName("SansSerif");
        style.setFontSize(fontSize);
        style.setBold(isBold);
        style.setPdfEmbedded(false);
        style.setMode(ModeEnum.OPAQUE);
        
        jrDesign.addStyle(style);
        
        return style;
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
