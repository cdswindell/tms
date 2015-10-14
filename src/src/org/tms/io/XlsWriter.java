package org.tms.io;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Calendar;
import java.util.Date;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.FormulaError;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.tms.api.Column;
import org.tms.api.io.options.XlsOptions;

public class XlsWriter extends BaseWriter
{
    private XlsOptions m_options;

    public static void export(TableExportAdapter tea, OutputStream output, XlsOptions options) 
            throws IOException
    {
        XlsWriter writer = new XlsWriter(tea, output, options);
        writer.export();
    }

    private XlsWriter(TableExportAdapter tw, OutputStream out, XlsOptions options)
    {
        super(tw, out, options);        
        m_options = options;        
    }

    @Override
    protected void export() throws IOException
    {
        Workbook wb = m_options.isXlsXFormat() ? new XSSFWorkbook() : new HSSFWorkbook(); 
        String tableLabel = trimString(getTable().getLabel());
        Sheet sheet = tableLabel != null ? wb.createSheet(tableLabel) : wb.createSheet();

        int cellFontSize = m_options.getDefaultFontSize();
        int headerFontSize = m_options.getHeadingFontSize();
        
        int colWidthPx = m_options.getColumnWidth();
        int colWidth = colWidthPx > 0 ? (int)((colWidthPx - 5)/6.0) : 8;        
        sheet.setDefaultColumnWidth(colWidth);
        
        Font monthFont = wb.createFont();
        monthFont.setFontHeightInPoints((short)headerFontSize);
        monthFont.setColor(IndexedColors.WHITE.getIndex());
        CellStyle headingStyle = wb.createCellStyle();
        headingStyle.setAlignment(CellStyle.ALIGN_CENTER);
        headingStyle.setVerticalAlignment(CellStyle.VERTICAL_CENTER);
        headingStyle.setFillForegroundColor(IndexedColors.GREY_50_PERCENT.getIndex());
        headingStyle.setFillPattern(CellStyle.SOLID_FOREGROUND);
        headingStyle.setFont(monthFont);
        headingStyle.setWrapText(true);

        Font cellFont = wb.createFont();
        cellFont.setFontHeightInPoints((short)cellFontSize);
        CellStyle cellStyle = wb.createCellStyle();
        cellStyle.setFont(cellFont);
        cellStyle.setVerticalAlignment(CellStyle.VERTICAL_CENTER);
        
        int firstActiveRow = 0;
        int firstActiveCol = 0;
        
        int rowNum = 0;
        if (options().isColumnNames()) {
            firstActiveRow = 1;
            Row headerRow = sheet.createRow(rowNum++);
            headerRow.setHeightInPoints(30);
            Cell headerCell;

            short colCnt = (short)0;
            if (m_options.isRowNames()) {
                firstActiveCol = 1;
                headerCell = headerRow.createCell(colCnt++);
                headerCell.setCellStyle(headingStyle);
            }
            
            for (Column c : getActiveColumns()) {
                String label = c.getLabel();
                if (label == null || (label = label.trim()).length() <= 0) 
                    label = String.format("Col %d", colCnt);
                    
                headerCell = headerRow.createCell(colCnt);
                headerCell.setCellValue(label);
                headerCell.setCellStyle(headingStyle);

                colCnt++;
            }
        }

        if (options().isRowNames()) {
            firstActiveCol = 1;
            int rnColWidthPx = m_options.getRowNameColumnWidth();
            int rnColWidth = rnColWidthPx > 0 ? (int)(0.5 + (((rnColWidthPx)/6.0) * 256)) : 10 * 256;        
            sheet.setColumnWidth(0, rnColWidth);
        }
        
        // Fill data cells
        for (org.tms.api.Row tr : this.getRows()) {
            if (m_options.isIgnoreEmptyRows() && tr.isNull())
                continue;

            short colCnt = 0;
            Row r = sheet.createRow(rowNum++);
            if (options().isRowNames()) {
                String label = trimString(tr.getLabel());
                if (label == null)
                    label = String.format("Row %d", rowNum - firstActiveRow);
                Cell headerCell = r.createCell(colCnt++);
                headerCell.setCellValue(label);
                headerCell.setCellStyle(headingStyle);
            }

            for (Column tc : this.getActiveColumns()) {
                org.tms.api.Cell tCell = getTable().getCell(tr, tc);
                if (tCell == null)
                    ;
                else if (tCell.isErrorValue()) {
                    Cell excelC = r.createCell(colCnt);
                    excelC.setCellErrorValue(toExcelErrorValue(tCell));
                }
                else if (!tCell.isNull()) {
                    Cell excelC = r.createCell(colCnt);
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

                colCnt++;   
            }
        }

        // set active cell
        Row r = sheet.getRow(firstActiveRow);
        Cell activeCell = r.getCell(firstActiveCol);
        activeCell.setAsActiveCell();
        
        // write the output stream
        wb.write(getOutputStream());    
        wb.close();
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
