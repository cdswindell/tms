package org.tms.io;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.ClientAnchor;
import org.apache.poi.ss.usermodel.Comment;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Drawing;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.FormulaError;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Name;
import org.apache.poi.ss.usermodel.RichTextString;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.tms.api.Column;
import org.tms.api.Table;
import org.tms.api.derivables.Derivation;
import org.tms.api.io.options.XlsOptions;

public class XlsWriter extends BaseWriter<XlsOptions>
{
    private Map<String, CellStyle> m_styleCache;
    private Map<Sheet, Map<org.tms.api.Row, Row>> m_rowMap;
    private Map<Sheet, Map<org.tms.api.Column, Integer>> m_colMap;
    private CreationHelper m_wbHelper = null;

    public static void export(TableExportAdapter tea, OutputStream output, XlsOptions options) 
            throws IOException
    {
        XlsWriter writer = new XlsWriter(tea, output, options);
        writer.export();
    }

    private XlsWriter(TableExportAdapter tw, OutputStream out, XlsOptions options)
    {
        super(tw, out, options);        
        m_styleCache = new HashMap<String, CellStyle>();
        m_rowMap = new HashMap<Sheet, Map<org.tms.api.Row, Row>>();
        m_colMap = new HashMap<Sheet, Map<org.tms.api.Column, Integer>>();
    }

    @Override
    protected void export() throws IOException
    {
        Workbook wb = options().isXlsXFormat() ? new XSSFWorkbook() : new HSSFWorkbook(); 
        m_wbHelper = wb.getCreationHelper();
        
        // perform the export
        export(wb, getTable());
        
        // write the output stream
        wb.write(getOutputStream());    
        wb.close();
    }
    
    protected void export(Workbook wb, Table t) throws IOException
    {
        String tableLabel = trimString(t.getLabel());
        Sheet sheet = tableLabel != null ? wb.createSheet(tableLabel) : wb.createSheet();

        int cellFontSize = options().getDefaultFontSize();
        int commentFontSize = options().getDefaultFontSize() - 3;
        int headerFontSize = options().getHeadingFontSize();
        
        int colWidthPx = options().getColumnWidth();
        int colWidth = colWidthPx > 0 ? (int)((colWidthPx - 5)/6.0) : 8;        
        sheet.setDefaultColumnWidth(colWidth);
        
        CellStyle cellStyle = getCachedCellStyle("default", wb, cellFontSize, false);
        CellStyle headingStyle = getCachedCellStyle("heading", wb, headerFontSize, false);
        
        // make the comment styles, we may or may not need them, but this 
        // saves us from having to cache the comment font size
        getCachedCellStyle("comment", wb, commentFontSize, false);
        getCachedCellStyle("author", wb, commentFontSize, true);
               
        int firstActiveRow = 0;
        int firstActiveCol = 0;
        
        int rowNum = 0;
        if (options().isColumnNames()) {
            firstActiveRow = 1;
            Row headerRow = sheet.createRow(rowNum++);
            headerRow.setHeightInPoints(30);
            Cell headerCell = null;

            short colCnt = (short)0;
            if (options().isRowNames()) {
                firstActiveCol = 1;
                headerCell = headerRow.createCell(colCnt++);
                headerCell.setCellStyle(headingStyle);
            }
            
            for (Column c : getActiveColumns()) {
                cacheColAssociation(sheet, c, colCnt);
                String label = c.getLabel();
                if (label == null || (label = label.trim()).length() <= 0) 
                    label = String.format("Col %d", colCnt);
                    
                headerCell = headerRow.createCell(colCnt);
                headerCell.setCellValue(label);
                headerCell.setCellStyle(headingStyle);
                
                if (options().isDescriptions())
                    applyComment(headerCell, c.getDescription());

                colCnt++;
            }
            
            // set print headings
            sheet.setRepeatingRows(CellRangeAddress.valueOf("$1:$1"));
        }

        if (options().isRowNames()) {
            firstActiveCol = 1;
            int rnColWidthPx = options().getRowNameColumnWidth();
            int rnColWidth = rnColWidthPx > 0 ? (int)(0.5 + (((rnColWidthPx)/6.0) * 256)) : 10 * 256;        
            sheet.setColumnWidth(0, rnColWidth);
            
            // set print headings
            sheet.setRepeatingColumns(CellRangeAddress.valueOf("$A:$A"));
        }
        
        // Fill data cells
        for (org.tms.api.Row tr : this.getRows()) {
            if (options().isIgnoreEmptyRows() && tr.isNull())
                continue;

            short colCnt = 0;
            Row r = sheet.createRow(rowNum++);
            cacheRowAssociation(sheet, tr, r);
            if (options().isRowNames()) {
                String label = trimString(tr.getLabel());
                if (label == null)
                    label = String.format("Row %d", rowNum - firstActiveRow);
                
                Cell headerCell = r.createCell(colCnt++);
                headerCell.setCellValue(label);
                headerCell.setCellStyle(headingStyle);
                
                if (options().isDescriptions())
                    applyComment(headerCell, tr.getDescription());
            }

            for (Column tc : this.getActiveColumns()) {
                if (rowNum < 3)
                    cacheColAssociation(sheet, tc, colCnt);
                org.tms.api.Cell tCell = t.isCellDefined(tr, tc) ? t.getCell(tr, tc) : null;
                if (tCell != null) {
                    Cell excelC  = r.createCell(colCnt);
                    if (tCell.isErrorValue()) 
                        excelC.setCellErrorValue(toExcelErrorValue(tCell));
                    else if (!tCell.isNull()) {
                        excelC.setCellStyle(cellStyle);
                        Object cv = tCell.getCellValue();
                        if (tCell.isNumericValue()) {
                            Number nv = (Number)cv;
                            excelC.setCellValue(nv.doubleValue());
                        }
                        else if (tCell.isBooleanValue()) 
                            excelC.setCellValue((Boolean)cv);
                        else {
                            if (Calendar.class.isAssignableFrom(cv.getClass()))
                                excelC.setCellValue((Calendar)cv);
                            else if (Date.class.isAssignableFrom(cv.getClass()))
                                excelC.setCellValue((Date)cv);
                            else // string value
                                excelC.setCellValue((String)cv);
                        }
                    }
                    
                    String label = trimString(tCell.getLabel());
                    if (label != null) 
                        createNamedCell(tCell, label, wb, sheet, excelC);
                    
                    if (options().isDescriptions()) {
                        String desc = trimString(tCell.getDescription());
                        if (desc != null) 
                            applyComment(excelC, desc);
                    }
                    
                    Derivation deriv = getDerivation(tCell);
                    if (deriv != null) {
                        System.out.println(tCell);
                    }
                }

                colCnt++;   
            }
        }

        // Freeze pains        
        if (options().isRowNames() && options().isColumnNames())
            sheet.createFreezePane( 1, 1, 1, 1 );
        else if (options().isColumnNames())
            sheet.createFreezePane( 0, 1, 0, 1 );
        else if (options().isRowNames())
            sheet.createFreezePane( 1, 0, 1, 0 );

        // set active cell
        Row r = sheet.getRow(firstActiveRow);
        Cell activeCell = r.getCell(firstActiveCol);
        activeCell.setAsActiveCell();        
    }

    private void createNamedCell(org.tms.api.Cell tCell, String label, Workbook wb, Sheet sheet, Cell excelC)
    {
        CellReference cr = new CellReference(excelC.getRowIndex(), excelC.getColumnIndex(), true, true) ;
        
        Name namedCell = wb.createName();
        namedCell.setNameName(label);
        
        StringBuffer sb = new StringBuffer();
        String sn = sheet.getSheetName();
        boolean snNeedsQuote = sn.indexOf(' ') > -1 || sn.indexOf("'") > -1;
        
        if (snNeedsQuote)
            sb.append("'");
        sb.append(sn);
        if (snNeedsQuote)
            sb.append("'");
        sb.append('!');
        
        sb.append(cr.formatAsString()); // area reference
        namedCell.setRefersToFormula(sb.toString());      
    }

    private void applyComment(Cell excelC, String comment)
    {
        comment = trimString(comment);
        if (comment != null) {
            Sheet sheet = excelC.getSheet();
            Drawing dp = sheet.createDrawingPatriarch();
            
            // create the anchor object and associate it with the cell
            ClientAnchor anchor = m_wbHelper.createClientAnchor();
            anchor.setCol1(excelC.getColumnIndex());
            anchor.setCol2(excelC.getColumnIndex()+1);
            anchor.setRow1(excelC.getRowIndex());
            anchor.setRow2(excelC.getRowIndex()+3);
            
            // create the comment structure
            Comment eComment = dp.createCellComment(anchor);            
            RichTextString str = m_wbHelper.createRichTextString(comment);
            
            int authorLen = -1;
            if (options().isCommentAuthor()) {
                String author = options().getCommentAuthor();
                eComment.setAuthor(author);
                str = m_wbHelper.createRichTextString(author + ":\n" + str.getString());
                authorLen = author.length() + 1; // account for colon
            }
            
            // format the comment text
            short commentFont  = this.getCachedCellFontIndex("comment");
            str.applyFont(commentFont);
            
            if (authorLen > -1) {
                short authorFont  = this.getCachedCellFontIndex("author");
                str.applyFont(0, authorLen, authorFont);
            }
            
            // finally, assign the text to the comment
            eComment.setString(str);
            
           // Assign the comment to the cell
            excelC.setCellComment(eComment);
        }
    }

    public Derivation getDerivation(org.tms.api.Cell tCell)
    {
        Derivation deriv = tCell.getDerivation();
        if (deriv == null && tCell.getRow() != null && tCell.getRow().isDerived())
            deriv = tCell.getRow().getDerivation();
        if (deriv == null && tCell.getColumn() != null && tCell.getColumn().isDerived())
            deriv = tCell.getColumn().getDerivation();
        
        return deriv;
    }
    
    private void cacheRowAssociation(Sheet sheet, org.tms.api.Row tmsR, Row excelR)
    {
        Map<org.tms.api.Row, Row> rowMap = m_rowMap.get(sheet);
        if (rowMap == null) {
            rowMap = new HashMap<org.tms.api.Row, Row>(tmsR.getTable().getNumRows());
            m_rowMap.put(sheet,  rowMap);
        }
        
        rowMap.put(tmsR, excelR);
    }

    private void cacheColAssociation(Sheet sheet, org.tms.api.Column tmsC, int excelC)
    {
        Map<org.tms.api.Column, Integer> colMap = m_colMap.get(sheet);
        if (colMap == null) {
            colMap = new HashMap<org.tms.api.Column, Integer>(tmsC.getTable().getNumColumns());
            m_colMap.put(sheet,  colMap);
        }
        
        colMap.put(tmsC, excelC);
    }

    private short getCachedCellFontIndex(String styleName)
    {
        CellStyle cs = m_styleCache.get(styleName);
        if (cs != null)
            return cs.getFontIndex();
        else
            return 0;
    }
    
    private CellStyle getCachedCellStyle(String styleName, Workbook wb, int fontSize, boolean isBold)
    {
        if (!m_styleCache.containsKey(styleName)) {
            switch(styleName) {
                case "heading":
                    m_styleCache.put(styleName, createHeadingStyle(wb, fontSize, isBold));
                    break;
                    
                case "comment":
                case "author":
                case "default":
                    m_styleCache.put(styleName, createDefaultStyle(wb, fontSize, isBold));
                    break;
            }
        }
        
        return m_styleCache.get(styleName);
    }

    private CellStyle createHeadingStyle(Workbook wb, int fontSize, boolean isBold) 
    {
        Font monthFont = wb.createFont();
        monthFont.setFontHeightInPoints((short)fontSize);
        monthFont.setColor(IndexedColors.WHITE.getIndex());
        monthFont.setBold(isBold);
        CellStyle headingStyle = wb.createCellStyle();
        
        headingStyle.setAlignment(CellStyle.ALIGN_CENTER);
        headingStyle.setVerticalAlignment(CellStyle.VERTICAL_CENTER);
        headingStyle.setFillForegroundColor(IndexedColors.GREY_50_PERCENT.getIndex());
        headingStyle.setFillPattern(CellStyle.SOLID_FOREGROUND);
        headingStyle.setFont(monthFont);
        headingStyle.setWrapText(true);

        return headingStyle;
    }
    
    private CellStyle createDefaultStyle(Workbook wb, int fontSize, boolean isBold)
    {
        Font cellFont = wb.createFont();
        cellFont.setFontHeightInPoints((short)fontSize);
        cellFont.setBold(isBold);
        
        CellStyle cellStyle = wb.createCellStyle();
        cellStyle.setFont(cellFont);
        cellStyle.setVerticalAlignment(CellStyle.VERTICAL_CENTER);
        
        return cellStyle;
    }
    
    private byte toExcelErrorValue(org.tms.api.Cell tCell)
    {
        org.tms.api.derivables.ErrorCode ec = tCell.getErrorCode();
        switch (ec) {
            case DivideByZero:
                return FormulaError.DIV0.getCode();
            
            case NaN:
            case Infinity:
                return FormulaError.NUM.getCode();
            
            case ReferenceRequired:
                return FormulaError.NAME.getCode();
            
            default:
                 return FormulaError.NA.getCode();   
        }
    }
}
