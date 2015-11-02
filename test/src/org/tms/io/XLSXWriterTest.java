package org.tms.io;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.hssf.usermodel.HSSFFormulaEvaluator;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.SpreadsheetVersion;
import org.apache.poi.ss.usermodel.Comment;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.usermodel.XSSFFormulaEvaluator;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.Test;
import org.tms.BaseTest;
import org.tms.api.Access;
import org.tms.api.Cell;
import org.tms.api.Column;
import org.tms.api.Row;
import org.tms.api.Subset;
import org.tms.api.Table;
import org.tms.api.derivables.ErrorCode;
import org.tms.api.factories.TableFactory;
import org.tms.api.io.XLSOptions;

public class XLSXWriterTest extends BaseTest
{
    private static final String SAMPLE1 = "sample1.csv";
    private static final String ExportTableGold = "testExportTable.xlsx";

    @Test
    public final void testExportTable() throws IOException
    {
        /*
         * Note: If you change this test, be sure to update
         * the gold standard file ExportTableGold
         */
        Path path = Paths.get(qualifiedFileName(ExportTableGold));
        byte[] gold = Files.readAllBytes(path);  

        assertNotNull(gold);
        assertThat(gold.length > 0, is(true));

        Table t = TableFactory.importCSV(qualifiedFileName(SAMPLE1), true, true);
        assertNotNull(t);
        t.setLabel("Test Table");
        
        Column dCol = t.addColumn();
        dCol.setDerivation("col 1 * 3");
        dCol.setDescription("This is dCol");
        dCol.setLabel("D Col");
        
        Column d2Col = t.addColumn();
        d2Col.setDerivation("col 1 * 10%");
        d2Col.setLabel("D2 Col");
        
        Column d3Col = t.addColumn();
        d3Col.setDerivation("pi + ridx + cidx + randBetween(1,8)! + rIdx^2");
        d3Col.setLabel("D3 Col");
        
        Column d4Col = t.addColumn();
        d4Col.setDerivation("(col 1 > 10) && (col \"D3 Col\" < 100)");
        
        Column d5Col = t.addColumn();
        d5Col.setDerivation("(col 1 > 10) || (col \"D3 Col\" < 100)");
        
        Column d6Col = t.addColumn();
        d6Col.setDerivation(" 1 + 2 + \" Row: \" + toString(ridx) + \" \" + '*' * ridx" );
        
        Column eCol = t.addColumn();        
        Row r1 = t.getRow(1);
        Cell cell = t.getCell(r1,  eCol);
        cell.setDerivation("col 1 / 0");
        
        Row r2 = t.getRow(2);
        r2.setDescription("This is TMS Row 2");
        cell = t.getCell(r2,  eCol);
        cell.setCellValue(Double.NaN);
        
        Row r3 = t.getRow(3);
        cell = t.getCell(r3,  eCol);
        cell.setCellValue(Double.POSITIVE_INFINITY);
        
        Row r4 = t.getRow(4);
        cell = t.getCell(r4,  eCol);
        cell.setCellValue(ErrorCode.ReferenceRequired);
        
        Row r5 = t.addRow(5);
        r5.setLabel("Sum");
        
        Row r6 = t.addRow(6);
        r6.setLabel("Mean");
        for (Column c = t.getColumn(Access.First); c != null; c = t.getColumn(Access.Next)) {
            t.getCell(r5,  c).setDerivation("sum(col " + c.getIndex() + ")");
            t.getCell(r6,  c).setDerivation("mean(col " + c.getIndex() + ")");
        }
        
        Cell tCell = t.getCell(r1, eCol);
        tCell.setLabel("Div By Zero");
        tCell.setDescription("This cell has intentionality been set to an error");
        
        // create a subset that should be exported
        Subset s = t.addSubset(Access.ByLabel, "1st Valid Subset");
        s.add(r2, r3, r4, t.getColumn(2), t.getColumn(3));
        
        // create a subset that should be exported
        Subset sv2 = t.addSubset(Access.ByLabel, "2nd Valid Subset");
        sv2.add(r2, r3);
        
        Subset s2 = t.addSubset(Access.ByLabel, "InValid Subset");
        s2.add(s, tCell);
        
        Subset s3 = t.addSubset(Access.ByLabel, "InValid Subset 2");
        s3.add(r2, r4, r5);
        
        // create output stream
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        t.export(bos, XLSOptions.Default
                .withColumnLabels(true)
                .withDefaultColumnWidthInInches(1.5)
                .withRowLabelColumnWidthInInches(1.25)
                .withCommentAuthor("TMS via POI")
                );
        bos.close();

        // test byte streams are the same, more or less
        byte [] output =  bos.toByteArray();
        assertNotNull(output);
        assertThat(this.closeTo(gold.length, output.length, 100), is(true));
        
        // now compare exported file to expected
        File outFile = null;
        try {
            outFile  = File.createTempFile("tmsExcelExportTest", "xlsx");
            OutputStream outputStream = new FileOutputStream (outFile); 
            bos.writeTo(outputStream);    
            outputStream.close();
            
            // open workbook
            Workbook wb = WorkbookFactory.create(outFile);
            assertNotNull(wb);
            assertThat(wb.getNumberOfSheets(), is(1));
            assertThat(wb.getSheetName(0), is("Test Table"));
            
            SpreadsheetVersion ssV = wb instanceof XSSFWorkbook ? SpreadsheetVersion.EXCEL2007 : SpreadsheetVersion.EXCEL97;  
            if (ssV == SpreadsheetVersion.EXCEL2007)
                XSSFFormulaEvaluator.evaluateAllFormulaCells((XSSFWorkbook) wb);
            else
                HSSFFormulaEvaluator.evaluateAllFormulaCells(wb);
            
            Sheet sheet = wb.getSheet("Test Table");
            assertNotNull(sheet);
            assertThat(sheet.getFirstRowNum(), is(0));
            assertThat(sheet.getLastRowNum(), is(t.getNumRows()));
            
            org.apache.poi.ss.usermodel.Row lastRow = sheet.getRow(sheet.getLastRowNum());
            assertNotNull(lastRow);
            assertThat(lastRow.getLastCellNum(), is((short)(t.getNumColumns() + 1)));
            
            // validate column 5 (index 4) of the spreadsheet
            vetExcelCell(sheet, 0, 4, "D Col", null, true);
            vetExcelCell(sheet, 1, 4, 36.0, "B2 * 3.0", false);
            vetExcelCell(sheet, 2, 4, 39000.0, "B3 * 3.0", false);
            vetExcelCell(sheet, 3, 4, -36.0, "B4 * 3.0", false);
            vetExcelCell(sheet, 4, 4, 52.95, "B5 * 3.0", false);
            vetExcelCell(sheet, 5, 4, 39052.95, "sum(E2:E5)", false);
            vetExcelCell(sheet, 6, 4, 9763.2375, "average(E2:E5)", false);
            
            vetExcelCell(sheet, 1, 9, "3 Row: 2 **", "(1.0 + 2.0) & \" Row: \" & (row()) & \" \" & rept(\"*\", row())", false);
        }
        catch (EncryptedDocumentException | InvalidFormatException e)
        {
            fail(e.getMessage());
        }
        finally {
            outFile.delete();
        }        
    }

    private void vetExcelCell(Sheet sheet, int rowIdx, int colIdx, Object cellVal, String formula, boolean hasComment)
    {
        org.apache.poi.ss.usermodel.Row eRow = sheet.getRow(rowIdx);
        assertNotNull("Row is null: " + rowIdx, eRow);
        assertThat(eRow.getRowNum(), is(rowIdx));
        
        org.apache.poi.ss.usermodel.Cell eCell = eRow.getCell(colIdx);
        assertNotNull("Cell is null: " + colIdx, eCell);
        assertThat(eCell.getRowIndex(), is(rowIdx));
        assertThat(eCell.getColumnIndex(), is(colIdx));
        
        Object cv = fetchCellValue(eCell);
        if (Number.class.isAssignableFrom(cv.getClass()))
            assertThat(String.format("cell value (%f) not within tolerance of expected (%f)", (double)cellVal, ((Number)cv).doubleValue()),
                    closeTo(cellVal, ((Number)cv).doubleValue(), 0.0000001), is(true));
        else
            assertThat(cv, is(cellVal));   
        
        String excelFormula = eCell.getCellType() == org.apache.poi.ss.usermodel.Cell.CELL_TYPE_FORMULA ?
                                    eCell.getCellFormula() : null;
        if (formula == null)
            assertThat("No formula expected: " + excelFormula, excelFormula, nullValue());
        else
            assertThat(excelFormula, is(formula));
        
        Comment comment = eCell.getCellComment();
        if (hasComment)
            assertNotNull("Expected comment", comment);
        else
            assertThat("No comment expected", comment, nullValue());
    }
    
    private Object fetchCellValue(org.apache.poi.ss.usermodel.Cell eC) 
    {
        if (eC == null)
            return null;

        // decode based on the cell type
        int cellType = eC.getCellType();
        switch(cellType) {
            case org.apache.poi.ss.usermodel.Cell.CELL_TYPE_BOOLEAN:
                return eC.getBooleanCellValue();

            case org.apache.poi.ss.usermodel.Cell.CELL_TYPE_NUMERIC:
                return eC.getNumericCellValue();

            case org.apache.poi.ss.usermodel.Cell.CELL_TYPE_STRING:
                return eC.getRichStringCellValue().getString();

            case org.apache.poi.ss.usermodel.Cell.CELL_TYPE_FORMULA:
                return fetchFormulaCellValue(eC);

            default:
                return null;
        }
    }

    private Object fetchFormulaCellValue(org.apache.poi.ss.usermodel.Cell eC) 
    {
        // parse formula value
        Object cv = null;
        try {
            cv = eC.getNumericCellValue();
        }
        catch (IllegalStateException ise) {
            try {
                cv = eC.getStringCellValue();
            }
            catch (IllegalStateException iseStr) {
                try {
                    cv = eC.getBooleanCellValue();
                }
                catch (IllegalStateException iseBool) {
                    cv = ErrorCode.NaN;
                }
            }
        }

        return cv;
    }
}
