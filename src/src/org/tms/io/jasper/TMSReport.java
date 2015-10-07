package org.tms.io.jasper;

import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRField;
import net.sf.jasperreports.engine.JasperCompileManager;
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
import net.sf.jasperreports.engine.type.PrintOrderEnum;
import net.sf.jasperreports.engine.type.SplitTypeEnum;
import net.sf.jasperreports.engine.type.StretchTypeEnum;
import net.sf.jasperreports.engine.type.VerticalTextAlignEnum;
import net.sf.jasperreports.engine.type.WhenNoDataTypeEnum;

import org.tms.api.Column;
import org.tms.api.Table;
import org.tms.io.BaseWriter;
import org.tms.io.options.DateTimeFormatOption;
import org.tms.io.options.FontedOption;
import org.tms.io.options.IOOptions;
import org.tms.io.options.PageableOption;
import org.tms.io.options.TitleableOption;


abstract public class TMSReport
{
    static final String sf_RowNameFieldName = "__ROW_NAME__";
    
    abstract public void export() throws IOException;
    
    private static final int sf_StringColWidth = 65;
    private static final int sf_RowNameColWidth = sf_StringColWidth;
    private static final int sf_InterColSpace = 5;
    
    private static final float sf_StandardFontSize = 8;
    private static final float sf_HeaderFontSize = 9;
    private static final float sf_TitleFontSize = 14;
    private static final String sf_DefaultFontFamily = "SansSerif";
    
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
    
    TMSReport(BaseWriter w)
    {
        m_writer = w;
        m_table = w.getTable();
        m_options = w.options();
    }
    
    BaseWriter getWriter()
    {
        return m_writer;
    }
    
    JasperPrint getPrint()
    {
        return m_jrPrint;
    }
    
    Table getTable()
    {
        return m_table;
    }
    
    IOOptions getOptions()
    {
        return m_options;
    }

    protected void generateReport() 
    throws JRException
    {
        m_jrDataSource = new TMSDataSource(this);
        fillJasperParams();
        printJasperReport();
    }

    protected FileOutputStream prepareReport() 
    throws IOException, JRException
    {
        // generate the design, compile it, and run it
        generateReport();
        
        return new FileOutputStream(m_writer.getOutputFile());
    }
    
    Map<Column, JRField> getColumnFieldMap()
    {
        return m_colFieldMap;
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
        int printableWidth = colWidth;
                
        // set page parameters, including size and margin
        m_jrDesign.setPageWidth(pageWidth);
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
        float defaultFontSize = m_options instanceof FontedOption ?
                ((FontedOption)m_options).getDefaultFontSize() : sf_StandardFontSize;
        if (defaultFontSize <= 0)
            defaultFontSize = sf_StandardFontSize;
        
        float headingFontSize = m_options instanceof FontedOption ?
                ((FontedOption)m_options).getHeadingFontSize() : sf_HeaderFontSize;
        if (headingFontSize <= 0)
            headingFontSize = sf_HeaderFontSize;
        
        JRDesignStyle boldStyle = defineStyle(m_jrDesign, "Sans_Bold", defaultFontSize, false, true);
        JRDesignStyle normalStyle = defineStyle(m_jrDesign, "Sans_Normal", defaultFontSize, true, false);
        
        defineGlobalParameters(m_jrDesign);
        
        // create JR fields for each printable column
        int colHeadBandHeight = (int)(headingFontSize * 1.5);
        int detailBandHeight = (int)(defaultFontSize * 1.5);
        JRDesignBand colHeaderBand = new JRDesignBand();
        colHeaderBand.setHeight(colHeadBandHeight);
        
        JRDesignBand detailBand = new JRDesignBand();
        detailBand.setHeight(detailBandHeight);
        detailBand.setSplitType(SplitTypeEnum.PREVENT);
        
        int tfX = 0;
        int tfY = 2;
        m_colFieldMap = new HashMap<Column, JRField>(nCols);
        
        int fieldWidth = (m_options instanceof PageableOption) && ((PageableOption)m_options).getColumnWidth() > 0 ?
                ((PageableOption)m_options).getColumnWidth() : sf_StringColWidth;
        
                
        if (m_options.isRowNames()) {
            JRDesignField jrField = new JRDesignField();
            jrField.setName(sf_RowNameFieldName);
            jrField.setValueClass(String.class);
            m_jrDesign.addField(jrField);
            
            JRDesignTextField tf = defineTextField(sf_RowNameFieldName, tfX, tfY, sf_RowNameColWidth, detailBandHeight - 2, 
                    boldStyle, VerticalTextAlignEnum.TOP, HorizontalTextAlignEnum.LEFT,
                    "$F{%s}",
                    null);   
            tf.setFontSize(headingFontSize);
            tf.setBold(true);
            detailBand.addElement(tf);
            
            // bump the field
            tfX += sf_InterColSpace + sf_RowNameColWidth;
        }
        
        for (Column col : m_writer.getActiveColumns()) {            
            // create multiple columns to handle overflow
            // if report gets too wide
//            if (paginated && (tfX + fieldWidth) > colWidth) {
//                
//                // add the filled band to the report               
//                ((JRDesignSection)m_jrDesign.getDetailSection()).addBand(detailBand);   
//                
//                // and create a new band
//                detailBand = new JRDesignBand();
//                detailBand.setHeight(detailBandHeight);
//                detailBand.setSplitType(SplitTypeEnum.PREVENT);
//                
//                // reset starting point for next band
//                tfX = 0;
//                                    
//                // add row names, if sticky
//                if (m_options.isRowNames() && ((PageableOption)m_options).isStickyRowNames()) {
//                    JRDesignTextField tf = defineTextField(sf_RowNameFieldName, tfX, tfY, sf_RowNameColWidth, detailBandHeight - 2, 
//                            boldStyle, VerticalTextAlignEnum.TOP, HorizontalTextAlignEnum.LEFT,
//                            "$F{%s}",
//                            null);   
//                    tf.setFontSize(sf_HeaderFontPointSize);
//                    detailBand.addElement(tf);
//                    
//                    // bump the field
//                    tfX += sf_InterColSpace + sf_RowNameColWidth;
//                }
//            }
            
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
                hf.setFontSize(headingFontSize);
                hf.setBold(true);
                hf.setStretchType(StretchTypeEnum.RELATIVE_TO_TALLEST_OBJECT);
                
                colHeaderBand.addElement(hf);
                
                JRDesignLine line = new JRDesignLine();
                line.setX(tfX);
                line.setY(colHeadBandHeight-2);
                line.setWidth(fieldWidth);
                line.setHeight(0);
                line.getLinePen().setLineWidth((float) 0.5);
                line.setPositionType(PositionTypeEnum.FLOAT);

                colHeaderBand.addElement(line);
            }
            
            // bump the field
            tfX += sf_InterColSpace + fieldWidth;
        }
        
        // set the number of columns, now that we know
        m_jrDesign.setColumnSpacing(0);
        m_jrDesign.setColumnCount(1);
        m_jrDesign.setPrintOrder(PrintOrderEnum.VERTICAL);
        
        // add the detail band; this is essentially the report data
        ((JRDesignSection)m_jrDesign.getDetailSection()).addBand(detailBand);    
        
        //Column header
        if (m_options.isColumnNames()) {
            if (paginated && !((PageableOption)m_options).isStickyColumnNames()) {
                JRDesignExpression firstPageOnly = new JRDesignExpression();
                firstPageOnly.setText("$V{PAGE_NUMBER} == 1");               
                colHeaderBand.setPrintWhenExpression(firstPageOnly);
            }
            
            m_jrDesign.setColumnHeader(colHeaderBand);
        }
        
        if (paginated && ((PageableOption)m_options).isPageNumbers()) {
            JRDesignBand footerBand = defineFooterBand(m_jrDesign, normalStyle, printableWidth);
            m_jrDesign.setPageFooter(footerBand);
        }
        
        // finally, set title and force recompile
        if ((m_options instanceof TitleableOption) && ((TitleableOption)m_options).hasTitle()) {
            JRDesignBand titleBand = defineTitleBand(m_jrDesign, boldStyle, printableWidth);
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
        tf.setBold(false);
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

    private String getFontFamily()
    {
        String fontFamily = sf_DefaultFontFamily;
        if ((m_options instanceof FontedOption)) {
            fontFamily = ((FontedOption)m_options).getFontFamily();
            if (fontFamily == null || (fontFamily = fontFamily.trim()).length() <= 0)
                fontFamily = sf_DefaultFontFamily;
        }
        
        return fontFamily;
    }

    private JRDesignBand defineTitleBand(JasperDesign jrDesign, JRDesignStyle boldStyle, int colWidth)
    {
        float titleFontSize = ((FontedOption)m_options).getTitleFontSize();
        if (titleFontSize <= 0)
            titleFontSize = sf_TitleFontSize;
        
        int height = (int)(titleFontSize * 4);
        int offset = 5;
        
        JRDesignBand titleBand = new JRDesignBand();
        titleBand.setHeight(height);
        
        JRDesignTextField titleField = new JRDesignTextField();
        titleField.setBlankWhenNull(true);
        titleField.setX(0);
        titleField.setY(offset);
        titleField.setWidth(colWidth);
        titleField.setHeight(height - offset);
        titleField.setStretchWithOverflow(true);
        titleField.setStretchType(StretchTypeEnum.RELATIVE_TO_BAND_HEIGHT);
        titleField.setHorizontalTextAlign(HorizontalTextAlignEnum.CENTER);
        titleField.setStyle(boldStyle);
        titleField.setFontSize(titleFontSize);
        titleField.setBold(true);
        
        JRDesignExpression titleExpression = new JRDesignExpression();
        titleExpression.setText("$P{ReportTitle}");
        titleField.setExpression(titleExpression);
        titleBand.addElement(titleField);
                
        return titleBand;
    }

    private JRDesignBand defineFooterBand(JasperDesign jrDesign, JRDesignStyle normalStyle, int pageWidth)
    {
        float fontSize = (int)(((FontedOption)m_options).getDefaultFontSize() * .9);
        if (fontSize <= 0)
            fontSize = (int)(sf_StandardFontSize * .9);
            
        JRDesignBand pageFooter = new JRDesignBand();
        int height = (int)(fontSize * 2);
        int offset = (int)(fontSize / 2);
        pageFooter.setHeight(height);
        
        JRDesignTextField nowField = new JRDesignTextField();
        nowField.setStyle(normalStyle);
        nowField.setFontSize(fontSize);
        nowField.setBold(false);
        nowField.setHeight(height - offset);
        nowField.setWidth(pageWidth); 
        nowField.setY(offset);
        nowField.setMode(ModeEnum.TRANSPARENT);
        nowField.setHorizontalTextAlign( HorizontalTextAlignEnum.RIGHT);     
        
        JRDesignExpression nowFieldEx = new JRDesignExpression();
        nowFieldEx.setText("$P{now}");
        
        nowField.setExpression(nowFieldEx);
        pageFooter.addElement(nowField);
        
        JRDesignTextField pageNoField = new JRDesignTextField();
        pageNoField.setStyle(normalStyle);
        pageNoField.setFontSize(fontSize);
        pageNoField.setBold(false);
        pageNoField.setMode(ModeEnum.TRANSPARENT);
        pageNoField.setHeight(height - offset);
        pageNoField.setWidth(pageWidth);
        pageNoField.setY(offset);
        pageNoField.setHorizontalTextAlign(HorizontalTextAlignEnum.CENTER);    
        
        JRDesignExpression pageNoFieldEx = new JRDesignExpression();
        pageNoFieldEx.setText("String.format(\"- %d -\", $V{PAGE_NUMBER})");
        
        pageNoField.setExpression(pageNoFieldEx);
        pageFooter.addElement(pageNoField);
        
        return pageFooter;
    }
    
    private JRDesignStyle defineStyle(JasperDesign jrDesign, String name, float fontSize, boolean isDefault, boolean isBold) 
    throws JRException
    {
        JRDesignStyle style = new JRDesignStyle();
        style.setName(name);
        style.setDefault(isDefault);
        if (m_options.isPDF()) {
            style.setFontName(sf_DefaultFontFamily);
            style.setPdfFontName(getFontFamily());
        }
        else
            style.setFontName(getFontFamily());
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
