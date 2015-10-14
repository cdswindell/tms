package org.tms.io;

import java.io.IOException;
import java.io.OutputStream;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.tms.api.Column;
import org.tms.api.io.options.XLSXOptions;

public class XLSXWriter extends BaseWriter
{
    public static void export(TableExportAdapter tea, OutputStream output, XLSXOptions options) 
    throws IOException
    {
        XLSXWriter writer = new XLSXWriter(tea, output, options);
        writer.export();
    }

    private XLSXWriter(TableExportAdapter tw, OutputStream out, XLSXOptions options)
    {
        super(tw, out, options);
    }

    @Override
    protected void export() throws IOException
    {
        Workbook wb = new XSSFWorkbook(); 
        Sheet sheet = wb.createSheet();   
        
        Font monthFont = wb.createFont();
        monthFont.setFontHeightInPoints((short)14);
        monthFont.setColor(IndexedColors.WHITE.getIndex());
        CellStyle style = wb.createCellStyle();
        style.setAlignment(CellStyle.ALIGN_CENTER);
        style.setVerticalAlignment(CellStyle.VERTICAL_CENTER);
        style.setFillForegroundColor(IndexedColors.GREY_50_PERCENT.getIndex());
        style.setFillPattern(CellStyle.SOLID_FOREGROUND);
        style.setFont(monthFont);
        style.setWrapText(true);
        
        int rowNum = 0;
        if (options().isColumnNames()) {
            short colCnt = (short)(options().isRowNames() ? 1 : 0);
            Row headerRow = sheet.createRow(rowNum++);
            headerRow.setHeightInPoints(30);
            Cell headerCell;
            
            for (Column c : getActiveColumns()) {
                String label = c.getLabel();
                if (label != null && (label = label.trim()).length() > 0) {
                    headerCell = headerRow.createCell(colCnt);
                    headerCell.setCellValue(label);
                    headerCell.setCellStyle(style);
                }
                
                colCnt++;
            }
        }
        
        for (org.tms.api.Row tr : this.getRows()) {
            short colCnt = 0;
            Row r = sheet.createRow(rowNum++);
            if (options().isRowNames()) {
                Cell headerCell = r.createCell(colCnt++);
                headerCell.setCellValue(tr.getLabel());
                headerCell.setCellStyle(style);
            }
        }

        // write the output stream
        wb.write(getOutputStream());    
        wb.close();
    }
}
