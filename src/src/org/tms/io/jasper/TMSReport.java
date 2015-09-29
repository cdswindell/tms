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
import net.sf.jasperreports.engine.design.JRDesignLine;
import net.sf.jasperreports.engine.design.JRDesignParameter;
import net.sf.jasperreports.engine.design.JRDesignSection;
import net.sf.jasperreports.engine.design.JRDesignStyle;
import net.sf.jasperreports.engine.design.JRDesignTextField;
import net.sf.jasperreports.engine.design.JasperDesign;
import net.sf.jasperreports.engine.type.HorizontalTextAlignEnum;
import net.sf.jasperreports.engine.type.ModeEnum;
import net.sf.jasperreports.engine.type.PositionTypeEnum;
import net.sf.jasperreports.engine.type.SplitTypeEnum;
import net.sf.jasperreports.engine.type.StretchTypeEnum;
import net.sf.jasperreports.engine.type.VerticalTextAlignEnum;
import net.sf.jasperreports.engine.type.WhenNoDataTypeEnum;

import org.tms.api.Column;
import org.tms.api.Table;
import org.tms.io.BaseWriter;
import org.tms.io.options.DateTimeFormatOption;
import org.tms.io.options.IOOptions;
import org.tms.io.options.PageableOption;
import org.tms.io.options.TitleableOption;


public class TMSReport
{
    private static final int sf_StringColWidth = 65;
    private static final int sf_InterColSpace = 5;
    
    private static final float sf_StandardFontPointSize = 8;
    private static final float sf_HeaderFontPointSize = 9;
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
        m_jrDesign.setColumnSpacing(0);
        m_jrDesign.setColumnCount(1);
        m_jrDesign.setWhenNoDataType(WhenNoDataTypeEnum.ALL_SECTIONS_NO_DETAIL);
        if (paginated) {
            m_jrDesign.setLeftMargin(sf_PageLeftMargin);
            m_jrDesign.setRightMargin(sf_PageRightMargin);
            m_jrDesign.setColumnWidth(colWidth);
        }
        else {
            m_jrDesign.setLeftMargin(2);
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
        int colHeadBandHeight = 15;
        int detailBandHeight = 18;
        JRDesignBand pageHeaderBand = new JRDesignBand();
        pageHeaderBand.setHeight(colHeadBandHeight);
        
        JRDesignBand detailBand = new JRDesignBand();
        detailBand.setHeight(detailBandHeight);
        detailBand.setSplitType(SplitTypeEnum.PREVENT);
        
        int tfX = 0;
        int tfY = 2;
        m_colFieldMap = new HashMap<Column, JRField>(nCols);
        
        int fieldWidth = (m_options instanceof PageableOption) && ((PageableOption)m_options).getColumnWidth() > 0 ?
                ((PageableOption)m_options).getColumnWidth() : sf_StringColWidth;
                
        for (Column col : m_writer.getActiveColumns()) {
            String colName = String.valueOf(col.getIndex());
            JRDesignField jrField = new JRDesignField();
            jrField.setName(colName);
            jrField.setValueClass(Object.class);
            
            m_jrDesign.addField(jrField);
            m_colFieldMap.put(col, jrField);
            
            JRDesignTextField tf = defineTextField(colName, tfX, tfY, fieldWidth, detailBandHeight - 2, 
                    normalStyle, VerticalTextAlignEnum.TOP, HorizontalTextAlignEnum.LEFT,
                    "$F{%s} == null ? null : ((org.tms.api.Cell)$F{%<s}).getFormattedCellValue()",
                    "$F{%s} == null ? false : ((org.tms.io.Printable)$F{%<s}).isLeftAligned()");   
            detailBand.addElement(tf);
            
            // now add text field for numeric values
            tf = defineTextField(colName, tfX, tfY, fieldWidth, detailBandHeight - 2, 
                    normalStyle, VerticalTextAlignEnum.TOP, HorizontalTextAlignEnum.RIGHT,
                    "$F{%s} == null ? null : ((org.tms.api.Cell)$F{%<s}).getFormattedCellValue()",
                    "$F{%s} == null ? false : ((org.tms.io.Printable)$F{%<s}).isRightAligned()");            
            detailBand.addElement(tf);
            
            // and text field for boolean values
            tf = defineTextField(colName, tfX, tfY, fieldWidth, detailBandHeight - 2, 
                    normalStyle, VerticalTextAlignEnum.TOP, HorizontalTextAlignEnum.CENTER,
                    "$F{%s} == null ? null : ((org.tms.api.Cell)$F{%<s}).getFormattedCellValue()",
                    "$F{%s} == null ? false : ((org.tms.io.Printable)$F{%<s}).isCenterAligned()");            
            detailBand.addElement(tf);
            
            // now add column heading, if one is defined
            if (m_options.isColumnNames()) {
                String label = col.getLabel();
                if (label == null || (label = label.trim()).length() <= 0)
                    label = String.format("Column %d", col.getIndex());
                
                JRDesignTextField hf = defineTextField(colName, tfX, 0, fieldWidth, colHeadBandHeight - 3, 
                        boldStyle, VerticalTextAlignEnum.TOP, HorizontalTextAlignEnum.LEFT,
                        "\"" + label + "\"",
                        null);        
                hf.setFontSize(sf_HeaderFontPointSize);
                hf.setStretchType(StretchTypeEnum.RELATIVE_TO_TALLEST_OBJECT);
                
                pageHeaderBand.addElement(hf);
                
                JRDesignLine line = new JRDesignLine();
                line.setX(tfX);
                line.setY(colHeadBandHeight-2);
                line.setWidth(fieldWidth);
                line.setHeight(0);
                line.getLinePen().setLineWidth((float) 0.5);
                line.setPositionType(PositionTypeEnum.FLOAT);

                pageHeaderBand.addElement(line);
            }
            
            // bump the field
            tfX += sf_InterColSpace + fieldWidth;
        }
        
        // add the detail band; this is essentially the report data
        ((JRDesignSection)m_jrDesign.getDetailSection()).addBand(detailBand);    
        
        //Page header
        if (m_options.isColumnNames()) {
            m_jrDesign.setPageHeader(pageHeaderBand);
        }
        
        // finally, set title and force recompile
        if ((m_options instanceof TitleableOption) && ((TitleableOption)m_options).hasTitle()) {
            JRDesignBand titleBand = defineTitleBand(m_jrDesign, boldStyle, colWidth);
            m_jrDesign.setTitle(titleBand);
      }
        
        m_jrReport = null;
        m_jrPrint = null;
    }

    private JRDesignTextField defineTextField(String colName, int tfX, int tfY, int fw, int fh, JRDesignStyle ns, 
            VerticalTextAlignEnum va, HorizontalTextAlignEnum ha, String formatEx, String printWhenEx)
    {
        JRDesignTextField tf = new JRDesignTextField();
        tf.setX(tfX);
        tf.setY(tfY);
        tf.setWidth(fw);
        tf.setHeight(fh);
        tf.setMode(ModeEnum.OPAQUE);
        tf.setStretchWithOverflow(true);
        tf.setBlankWhenNull(true);
        tf.setStyle(ns);
        tf.setVerticalTextAlign(va);
        tf.setHorizontalTextAlign(ha);
        
        JRDesignExpression ex = new JRDesignExpression();
        ex.setText(String.format(formatEx, colName));
        tf.setExpression(ex);

        if (printWhenEx != null) {
            JRDesignExpression pwe = new JRDesignExpression();
            pwe.setText(String.format(printWhenEx, colName));
            tf.setPrintWhenExpression(pwe);
        }
        
        return tf;
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
