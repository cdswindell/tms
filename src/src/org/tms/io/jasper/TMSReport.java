package org.tms.io.jasper;

import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.tms.api.Column;
import org.tms.api.Table;
import org.tms.api.io.DateTimeFormatIOOption;
import org.tms.api.io.IOFileFormat;
import org.tms.api.io.IOOption;
import org.tms.api.io.LabeledIOOption;
import org.tms.api.io.PageableIOOption;
import org.tms.api.io.StyleableIOOption;
import org.tms.api.io.TitleableIOOption;
import org.tms.io.LabeledWriter;

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
import net.sf.jasperreports.engine.design.JRDesignVariable;
import net.sf.jasperreports.engine.design.JasperDesign;
import net.sf.jasperreports.engine.type.HorizontalTextAlignEnum;
import net.sf.jasperreports.engine.type.ModeEnum;
import net.sf.jasperreports.engine.type.PositionTypeEnum;
import net.sf.jasperreports.engine.type.PrintOrderEnum;
import net.sf.jasperreports.engine.type.SplitTypeEnum;
import net.sf.jasperreports.engine.type.StretchTypeEnum;
import net.sf.jasperreports.engine.type.VerticalTextAlignEnum;
import net.sf.jasperreports.engine.type.WhenNoDataTypeEnum;
import net.sf.jasperreports.export.ExporterInputItem;
import net.sf.jasperreports.export.SimpleExporterInputItem;

abstract public class TMSReport
{
    static final String sf_RowNameFieldName = "__ROW_NAME__";
    
    static final Set<String> sf_JavaLogicalFonts = new HashSet<String>();
    {
        sf_JavaLogicalFonts.add(java.awt.Font.SANS_SERIF.toLowerCase());
        sf_JavaLogicalFonts.add(java.awt.Font.SERIF.toLowerCase());
        sf_JavaLogicalFonts.add(java.awt.Font.MONOSPACED.toLowerCase());
        sf_JavaLogicalFonts.add(java.awt.Font.DIALOG.toLowerCase());
        sf_JavaLogicalFonts.add(java.awt.Font.DIALOG_INPUT.toLowerCase());
    }
    
    static final boolean isLogicalFontFamily(String ff)
    {
        if (ff != null)
            return sf_JavaLogicalFonts.contains(ff.trim().toLowerCase());
        else
            return false;
    }
    
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
    
    private LabeledWriter<?> m_writer;
    private Table m_table;
    private LabeledIOOption<?> m_options;
    
    private Map<Column, JRField> m_colFieldMap;
    private Map<String, Object> m_jrParams;
    
    private List<JasperDesign> m_jrDesigns;
    private List<JasperPrint> m_jrPrints;
    
    TMSReport(LabeledWriter<?> w)
    {
        m_writer = w;
        m_table = w.getTable();
        m_options = w.options();
    }
    
    boolean isPDF()
    {
        return m_options.getFileFormat() == IOFileFormat.PDF;
    }
    
    LabeledWriter<?> getWriter()
    {
        return m_writer;
    }
    
    List<JasperPrint> getPrints()
    {
        return m_jrPrints;
    }
    
    List<ExporterInputItem> getExporterInputItems()
    {
        List<ExporterInputItem> items = new ArrayList<ExporterInputItem>();
        
        for (JasperPrint jp : getPrints()) {
            ExporterInputItem item = new SimpleExporterInputItem(jp);
            items.add(item);
        }
        
        return items;
    }
    
    Table getTable()
    {
        return m_table;
    }
    
    IOOption<?> getOptions()
    {
        return m_options;
    }

    protected void generateReport() 
    throws JRException
    {
        m_jrDesigns = new ArrayList<JasperDesign>();
        fillJasperParams();
        printJasperReport();
    }

    protected OutputStream prepareReport() 
    throws IOException, JRException
    {
        // generate the design, compile it, and run it
        generateReport();
        
        return m_writer.getOutputStream();
    }
    
    Map<Column, JRField> getColumnFieldMap()
    {
        return m_colFieldMap;
    }
    
    private void fillJasperParams()
    {
        m_jrParams = new HashMap<String, Object>();
        
        if (m_options instanceof TitleableIOOption) {
            if (((TitleableIOOption<?>)m_options).hasTitle())
                m_jrParams.put("ReportTitle", ((TitleableIOOption<?>)m_options).getTitle());
        }
               
        if (m_options instanceof DateTimeFormatIOOption) {
            if (((DateTimeFormatIOOption<?>)m_options).hasDateTimeFormat()) {
                SimpleDateFormat sdf = new SimpleDateFormat(((DateTimeFormatIOOption<?>)m_options).getDateTimeFormat());
                m_jrParams.put("now", sdf.format(new java.util.Date()));
            }
        }
    }

    private void printJasperReport() 
    throws JRException
    {
        if (m_jrParams == null)
            fillJasperParams();
        
        if (m_jrDesigns == null || m_jrDesigns.isEmpty())
            buildJasperDesign();
        
        m_jrPrints = new ArrayList<JasperPrint>(m_jrDesigns.size());
        boolean paginated = (m_options instanceof PageableIOOption) ? ((PageableIOOption<?>)m_options).isPaged() : false;
        int pageCnt = 1;
        for (JasperDesign jd : m_jrDesigns) {
            if (paginated) {
                // adjust page number
                JRDesignVariable jv = (JRDesignVariable)jd.getVariablesMap().get("PAGE_NUMBER");
                if (jv != null) {
                    JRDesignExpression pnEx = new JRDesignExpression();
                    pnEx.setText(String.format("($V{%s} != null)?(new Integer($V{%<s}.intValue() + 1)):(new Integer(%s))", "PAGE_NUMBER", pageCnt));  
                    jv.setInitialValueExpression(pnEx);
                }
            }
            
            // compile the design
            JasperReport jr = JasperCompileManager.compileReport(jd);  
            
            // fill the report
            JasperPrint jrPrint = JasperFillManager.fillReport(jr,  m_jrParams, new TMSDataSource(this));  
            m_jrPrints.add(jrPrint);
            
            // calculate starting page number of next section, if any
            if (paginated) 
                pageCnt += jrPrint.getPages().size();
        }
    }

    private JasperDesign createReportDesign(int rptNo, boolean paginated, int pageWidth, int pageHeight, int colWidth) 
    throws JRException
    {
        JasperDesign jrDesign = new JasperDesign();
        String tblLbl = (m_table.getLabel() != null ? m_table.getLabel() : "TMS Table") + rptNo;
        jrDesign.setName(tblLbl);
        
        jrDesign.setIgnorePagination(!paginated);
        
        // set page parameters, including size and margin
        jrDesign.setPageWidth(pageWidth);
        jrDesign.setWhenNoDataType(WhenNoDataTypeEnum.ALL_SECTIONS_NO_DETAIL);
        if (paginated) {
            jrDesign.setLeftMargin(sf_PageLeftMargin);
            jrDesign.setRightMargin(sf_PageRightMargin);
            jrDesign.setColumnWidth(colWidth);
        }
        else {
            jrDesign.setLeftMargin(2);
            jrDesign.setRightMargin(0);
        }
        
        jrDesign.setPageHeight(pageHeight);
        if (paginated) {
            jrDesign.setTopMargin(sf_PageTopMargin);
            jrDesign.setBottomMargin(sf_PageBottomMargin);
        }
        else {
            jrDesign.setTopMargin(0);
            jrDesign.setBottomMargin(0);
        }    

        // set the number of columns, now that we know
        jrDesign.setColumnSpacing(0);
        jrDesign.setColumnCount(1);
        jrDesign.setPrintOrder(PrintOrderEnum.VERTICAL);
        
        // define global params 
        defineGlobalParameters(jrDesign);
        
        return jrDesign;
    }
    
    private int addRowNames(JasperDesign jrDesign, int tfX, int tfY, JRDesignBand detailBand, 
            int detailBandHeight, JRDesignStyle boldStyle, float headingFontSize, int rowNameColWidth) 
    throws JRException
    {
        if (m_options.isRowLabels()) {
            JRDesignField jrField = new JRDesignField();
            jrField.setName(sf_RowNameFieldName);
            jrField.setValueClass(String.class);
            jrDesign.addField(jrField);
            
            JRDesignTextField tf = defineTextField(sf_RowNameFieldName, tfX, tfY, rowNameColWidth, detailBandHeight - 2, 
                    boldStyle, VerticalTextAlignEnum.TOP, HorizontalTextAlignEnum.LEFT,
                    "$F{%s}",
                    null);   
            tf.setFontSize(headingFontSize);
            tf.setBold(Boolean.TRUE);
            detailBand.addElement(tf);
            
            // bump the field
            tfX += sf_InterColSpace + rowNameColWidth;
        }
        
        return tfX;
    }
    
    private void completeReport(boolean paginated, int printableWidth, JasperDesign jrDesign,
            JRDesignStyle normalStyle, JRDesignBand colHeaderBand, JRDesignBand detailBand)
    {
        // add the detail band; this is essentially the report data
        ((JRDesignSection)jrDesign.getDetailSection()).addBand(detailBand);    
        
        //Column header
        if (m_options.isColumnLabels()) {
            if (paginated && !((PageableIOOption<?>)m_options).isStickyColumnLabels()) {
                JRDesignExpression firstPageOnly = new JRDesignExpression();
                firstPageOnly.setText("$V{PAGE_NUMBER} == 1");               
                colHeaderBand.setPrintWhenExpression(firstPageOnly);
            }
            
            jrDesign.setColumnHeader(colHeaderBand);
        }
        
        if (paginated && ((PageableIOOption<?>)m_options).isPageNumbers()) {
            JRDesignBand footerBand = defineFooterBand(jrDesign, normalStyle, printableWidth);
            jrDesign.setPageFooter(footerBand);
        }
        
        m_jrDesigns.add(jrDesign);
    }

    private void buildJasperDesign() throws JRException
    {
        // Figure out how many columns we have
        int nCols = m_writer.getNumActiveColumns();
        int rptNo = 1;
        
        // Paginated??
        boolean paginated = (m_options instanceof PageableIOOption) ? ((PageableIOOption<?>)m_options).isPaged() : false;
       
        int pageWidth = getPageWidth();
        int colWidth = paginated ? pageWidth - sf_PageLeftMargin - sf_PageRightMargin : pageWidth;
        int printableWidth = colWidth;                
        
        int pageHeight = getPageHeight();

        // define font styles
        float defaultFontSize = m_options instanceof StyleableIOOption ?
                ((StyleableIOOption<?>)m_options).getDefaultFontSize() : sf_StandardFontSize;
        if (defaultFontSize <= 0)
            defaultFontSize = sf_StandardFontSize;
        
        float headingFontSize = m_options instanceof StyleableIOOption ?
                ((StyleableIOOption<?>)m_options).getHeadingFontSize() : sf_HeaderFontSize;
        if (headingFontSize <= 0)
            headingFontSize = sf_HeaderFontSize;
        
        JasperDesign jrDesignFirst = createReportDesign(rptNo, paginated, pageWidth, pageHeight, colWidth);
        JasperDesign jrDesign = jrDesignFirst;
        
        JRDesignStyle boldStyle = defineStyle(jrDesign, "Sans_Bold", defaultFontSize, false, true);
        JRDesignStyle normalStyle = defineStyle(jrDesign, "Sans_Normal", defaultFontSize, true, false);
                        
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
        
        int fieldWidth = (m_options instanceof StyleableIOOption) && ((StyleableIOOption<?>)m_options).getDefaultColumnWidth() > 0 ?
                ((StyleableIOOption<?>)m_options).getDefaultColumnWidth() : sf_StringColWidth;
                
        int rowNameColWidth =  (m_options instanceof StyleableIOOption) && ((StyleableIOOption<?>)m_options).getRowLabelColumnWidth() > 0 ?
                ((StyleableIOOption<?>)m_options).getRowLabelColumnWidth() : sf_RowNameColWidth;
        
        tfX = addRowNames(jrDesign, tfX, tfY, detailBand, detailBandHeight, boldStyle, 
                          headingFontSize, rowNameColWidth);

        for (Column col : m_writer.getActiveColumns()) {            
            // create multiple columns to handle overflow
            // if report gets too wide
            if (paginated && (tfX + fieldWidth) > colWidth) {                  
                // complete this report and initialize a new one                     
                completeReport(paginated, printableWidth, jrDesign, normalStyle, colHeaderBand, detailBand);
                
                // initialize a new report
                jrDesign = createReportDesign(rptNo, paginated, pageWidth, pageHeight, colWidth);
                boldStyle = defineStyle(jrDesign, "Sans_Bold", defaultFontSize, false, true);
                normalStyle = defineStyle(jrDesign, "Sans_Normal", defaultFontSize, true, false);
                
                // and create a new band
                detailBand = new JRDesignBand();
                detailBand.setHeight(detailBandHeight);
                detailBand.setSplitType(SplitTypeEnum.PREVENT);
                
                colHeaderBand = new JRDesignBand();
                colHeaderBand.setHeight(colHeadBandHeight);
                
                // reset starting point for next band
                tfX = 0;
                if (paginated && ((PageableIOOption<?>)m_options).isStickyRowLabels()) 
                    tfX = addRowNames(jrDesign, tfX, tfY, detailBand, detailBandHeight, boldStyle, 
                                      headingFontSize, rowNameColWidth);
            }
            
            String colName = String.valueOf(col.getIndex());
            JRDesignField jrField = new JRDesignField();
            jrField.setName(colName);
            jrField.setValueClass(Object.class);
            
            jrDesign.addField(jrField);
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
            if (m_options.isColumnLabels()) {
                String label = col.getLabel();
                if (label == null || (label = label.trim()).length() <= 0)
                    label = String.format("Column %d", col.getIndex());
                
                JRDesignTextField hf = defineTextField(colName, tfX, 0, fieldWidth, colHeadBandHeight - 3, 
                        boldStyle, VerticalTextAlignEnum.TOP, HorizontalTextAlignEnum.LEFT,
                        "\"" + label + "\"",
                        null);        
                hf.setFontSize(headingFontSize);
                hf.setBold(Boolean.TRUE);
                hf.setStretchType(StretchTypeEnum.ELEMENT_GROUP_HEIGHT);
                
                colHeaderBand.addElement(hf);
                
                JRDesignLine line = new JRDesignLine();
                line.setX(tfX);
                line.setY(colHeadBandHeight-2);
                line.setWidth(fieldWidth);
                line.setHeight(0);
                line.getLinePen().setLineWidth(Float.valueOf((float)0.5));
                line.setPositionType(PositionTypeEnum.FLOAT);

                colHeaderBand.addElement(line);
            }
            
            // bump the field
            tfX += sf_InterColSpace + fieldWidth;
        }
        
        completeReport(paginated, printableWidth, jrDesign, normalStyle, colHeaderBand, detailBand);
        
        // fix up print width, if we are not paginated
        if (!paginated && tfX > printableWidth) {
            jrDesignFirst.setPageWidth(tfX);
            jrDesignFirst.setColumnWidth(tfX - sf_InterColSpace);
            printableWidth = tfX;
        }
        
        // add title, but only on first report
        JRDesignBand titleBand = null;
        if ((m_options instanceof TitleableIOOption) && ((TitleableIOOption<?>)m_options).hasTitle()) {
            float titleFontSize = ((TitleableIOOption<?>)m_options).getTitleFontSize();
            if (titleFontSize <= 0)
                titleFontSize = sf_TitleFontSize;
            
            titleBand = defineTitleBand(boldStyle, printableWidth, titleFontSize);
            jrDesignFirst.setTitle(titleBand);
            
            JRDesignBand pageHeader = new JRDesignBand();
            pageHeader.setHeight((int)titleFontSize);
            jrDesignFirst.setPageHeader(pageHeader);
        }
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
        tf.setBold(Boolean.FALSE);
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
        if ((m_options instanceof PageableIOOption)) 
            pageWidth = ((PageableIOOption<?>)m_options).getPageWidth();
        
        if (pageWidth <= 0)
            pageWidth = sf_PortraitPageWidth;
        
        return pageWidth;
    }

    private int getPageHeight()
    {
        int pageHeight = 0;
        if ((m_options instanceof PageableIOOption)) 
            pageHeight = ((PageableIOOption<?>)m_options).getPageHeight();
        
        if (pageHeight <= 0)
            pageHeight = sf_PortraitPageHeight;        
        
        return pageHeight;
    }

    private String getFontFamily()
    {
        String fontFamily = sf_DefaultFontFamily;
        if ((m_options instanceof StyleableIOOption)) {
            fontFamily = ((StyleableIOOption<?>)m_options).getFontFamily();
            if (fontFamily == null || (fontFamily = fontFamily.trim()).length() <= 0)
                fontFamily = sf_DefaultFontFamily;
        }
        
        return fontFamily;
    }

    private JRDesignBand defineTitleBand(JRDesignStyle boldStyle, int colWidth, float titleFontSize)
    {
        int height = (int)(titleFontSize);
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
        titleField.setStretchType(StretchTypeEnum.CONTAINER_HEIGHT);
        titleField.setHorizontalTextAlign(HorizontalTextAlignEnum.CENTER);
        titleField.setStyle(boldStyle);
        titleField.setFontSize(titleFontSize);
        titleField.setBold(Boolean.TRUE);
        
        JRDesignExpression titleExpression = new JRDesignExpression();
        titleExpression.setText("$P{ReportTitle}");
        titleField.setExpression(titleExpression);
        titleBand.addElement(titleField);
                
        return titleBand;
    }

    private JRDesignBand defineFooterBand(JasperDesign jrDesign, JRDesignStyle normalStyle, int pageWidth)
    {
        float fontSize = (int)(((StyleableIOOption<?>)m_options).getDefaultFontSize() * .9);
        if (fontSize <= 0)
            fontSize = (int)(sf_StandardFontSize * .9);
            
        JRDesignBand pageFooter = new JRDesignBand();
        int height = (int)(fontSize * 2);
        int offset = (int)(fontSize / 2);
        pageFooter.setHeight(height);
        
        if (m_options instanceof DateTimeFormatIOOption) {
            if (((DateTimeFormatIOOption<?>)m_options).hasDateTimeFormat()) {
                JRDesignTextField nowField = new JRDesignTextField();
                nowField.setStyle(normalStyle);
                nowField.setFontSize(fontSize);
                nowField.setBold(Boolean.FALSE);
                nowField.setHeight(height - offset);
                nowField.setWidth(pageWidth); 
                nowField.setY(offset);
                nowField.setMode(ModeEnum.TRANSPARENT);
                nowField.setHorizontalTextAlign( HorizontalTextAlignEnum.RIGHT);     

                JRDesignExpression nowFieldEx = new JRDesignExpression();
                nowFieldEx.setText("$P{now}");

                nowField.setExpression(nowFieldEx);
                pageFooter.addElement(nowField);
            }
        }
        
        JRDesignTextField pageNoField = new JRDesignTextField();
        pageNoField.setStyle(normalStyle);
        pageNoField.setFontSize(fontSize);
        pageNoField.setBold(Boolean.FALSE);
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
    
    private JRDesignStyle defineStyle(JasperDesign jrDesign, String name, float fontSize, boolean isDefault, Boolean isBold) 
    throws JRException
    {
        JRDesignStyle style = new JRDesignStyle();
        style.setName(name);
        style.setDefault(isDefault);
        if (isPDF()) {
            style.setFontName(sf_DefaultFontFamily);
            String ff = getFontFamily();
            if (!isLogicalFontFamily(ff))
                style.setPdfFontName(getFontFamily());
        }
        else
            style.setFontName(getFontFamily());
        style.setFontSize(fontSize);
        style.setBold(isBold);
        style.setPdfEmbedded(Boolean.FALSE);
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
