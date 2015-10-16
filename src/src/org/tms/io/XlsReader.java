package org.tms.io;

import java.io.File;
import java.io.IOException;
import java.util.Date;

import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.hssf.usermodel.HSSFEvaluationWorkbook;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.SpreadsheetVersion;
import org.apache.poi.ss.formula.FormulaParser;
import org.apache.poi.ss.formula.FormulaParsingWorkbook;
import org.apache.poi.ss.formula.FormulaType;
import org.apache.poi.ss.formula.ptg.Ptg;
import org.apache.poi.ss.formula.ptg.Ref3DPxg;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Comment;
import org.apache.poi.ss.usermodel.Name;
import org.apache.poi.ss.usermodel.RichTextString;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.ss.util.AreaReference;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.usermodel.XSSFEvaluationWorkbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.tms.api.Access;
import org.tms.api.Column;
import org.tms.api.Subset;
import org.tms.api.Table;
import org.tms.api.TableContext;
import org.tms.api.derivables.ErrorCode;
import org.tms.api.factories.TableContextFactory;
import org.tms.api.factories.TableFactory;
import org.tms.api.io.options.XlsOptions;

public class XlsReader extends BaseReader<XlsOptions>
{
    public XlsReader(String fileName, XlsOptions format)
    {
        this(fileName, TableContextFactory.fetchDefaultTableContext(), format);
    }

    public XlsReader(String fileName, TableContext context, XlsOptions format)
    {
        this(new File(fileName), context, format);
    }

    public XlsReader(File inputFile, TableContext context, XlsOptions format)
    {
        super(inputFile, context, format);

        if (!(format instanceof XlsOptions))
            throw new IllegalArgumentException("XlsOptions required");
    }

    public Table parse() throws IOException
    {
        // create the table scaffold
        Table t = TableFactory.createTable(getTableContext());
        Workbook wb = null;
        try
        {
            wb = WorkbookFactory.create(getInputFile());
            int asi = wb.getActiveSheetIndex();

            Sheet sheet = wb.getSheetAt(asi); 
            String sheetName = trimString(sheet.getSheetName());
            if (sheetName != null)
                t.setLabel(sheet.getSheetName());

            // handle column headings
            int rowNum = 0;
            if (isColumnNames()) {
                Row eR = sheet.getRow(rowNum++);
                for (short i = 0; i < eR.getLastCellNum(); i++) {
                    if (i == 0 && isRowNames())
                        continue; // skip r1c1

                    Column tC= t.addColumn(); // add the TMS column
                    Cell eC = eR.getCell(i, Row.RETURN_BLANK_AS_NULL);
                    Object cv = fetchCellValue(eC);
                    String note = fetchCellComment(eC, true);

                    if (note != null)
                        tC.setDescription(note);

                    if (cv != null)                       
                        tC.setLabel(cv.toString());
                }
            }

            // handle row data
            while (rowNum <= sheet.getLastRowNum()) {
                Row eR = sheet.getRow(rowNum++);
                if (eR != null) {
                    org.tms.api.Row tR = t.addRow();
                    org.tms.api.Column tC = t.getColumn(Access.First);
                    for (short i = 0; i < eR.getLastCellNum(); i++) {
                        Cell eC = eR.getCell(i, Row.RETURN_BLANK_AS_NULL);
                        Object cv = fetchCellValue(wb, sheet, asi, eC, t);
                        String note = fetchCellComment(eC, true);
                        if (i == 0 && isRowNames()) {
                            if (note != null)
                                tR.setDescription(note);

                            if (cv != null)                       
                                tR.setLabel(cv.toString());
                        }
                        else {
                            if (tC == null)
                                tC = t.addColumn();

                            if (eC != null || cv != null) {
                                org.tms.api.Cell tCell = t.getCell(tR, tC);
                                if (note != null)
                                    tCell.setDescription(note);

                                if (cv != null)
                                    tCell.setCellValue(cv);

                                // record presence of cell formula; we will process
                                // once all of table is imported
                                if (eC.getCellType() == Cell.CELL_TYPE_FORMULA) {
                                    // TODO: implement
                                    processExcelFormula(wb, sheet, asi, eC, t);
                                }
                            }

                            // bump column
                            tC = t.getColumn(Access.Next);
                        }
                    }
                }
                else if (!options().isIgnoreEmptyRows()) {
                    // excel row is empty and we don't want to ignore empty rows
                    // add a new row
                    t.addRow();
                }
            }

            // handle named ranges
            processNamedRegions(wb, sheet, sheetName, t);
        }
        catch (EncryptedDocumentException | InvalidFormatException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        finally {
            if (wb != null)
                wb.close();
        }

        return t;
    }

    private void processNamedRegions(Workbook wb, Sheet activeSheet, String sheetName, Table t)
    {
        int numNamedRegions = wb.getNumberOfNames();
        if (numNamedRegions > 0) {
            SpreadsheetVersion ssV = wb instanceof XSSFWorkbook ? SpreadsheetVersion.EXCEL2007 : SpreadsheetVersion.EXCEL97;
            for (int i = 0; i < numNamedRegions; i++) {
                Name namedRegion = wb.getNameAt(i);
                if (namedRegion != null && (sheetName == null || sheetName.equals(namedRegion.getSheetName()))) {
                    // get the region name and encoded region reference
                    String name = namedRegion.getNameName();
                    String regionRef = namedRegion.getRefersToFormula();

                    // use the helper classes AreaRef and CellReference to
                    // decode region reference
                    // Note: we cannot rely on aref.isSingleCell 
                    AreaReference aref = new AreaReference(regionRef, ssV);
                    CellReference[] cRefs = aref.getAllReferencedCells();

                    if (isSingleCell(cRefs)) {
                        org.tms.api.Cell tCell = getSingleCell(cRefs, t);
                        if (tCell != null)
                            tCell.setLabel(name);
                    }  
                    else 
                        createSubset(cRefs, t, name);
                }
            }
        }
    }

    private Subset createSubset(CellReference[] cRefs, Table t, String label)
    {
        // assume success
        Subset s = t.addSubset(Access.ByLabel, label);

        // iterate over cell references, abstracting rows and columns
        for (CellReference cRef : cRefs) {
            int excelRowNo = cRef.getRow();
            int excelColNo = cRef.getCol();

            if (excelColNo >= 0)
                s.add(t.getColumn(excelColNo + 1 - (options().isRowNames() ? 1 : 0)));

            if (excelRowNo >= 0)
                s.add(t.getRow(excelRowNo + 1 - (options().isColumnNames() ? 1 : 0)));           
        }

        return s;
    }

    private org.tms.api.Cell getSingleCell(CellReference[] cRefs, Table t)
    {
        // first, get Excel row and column values; they will be zero-based
        int excelRowNo = cRefs[0].getRow();
        int excelColNo = cRefs[0].getCol();

        // now, return cell, correcting for 1-based TMS row/column indexes
        return t.getCell(t.getRow(excelRowNo + 1 - (options().isColumnNames() ? 1 : 0)), 
                t.getColumn(excelColNo + 1 - (options().isRowNames() ? 1 : 0)));
    }

    private boolean isSingleCell(CellReference[] cRefs)
    {
        /*
         * we have a single cell iff there is only one cRef (array len == 1)
         * and getRow() is valid and getCol() is valid
         */
        return cRefs.length == 1 && 
                cRefs[0].getCol() > -1 && 
                cRefs[0].getRow() > -1 &&
                cRefs[0].isColAbsolute() &&
                cRefs[0].isRowAbsolute() &&
                (!options().isColumnNames() || cRefs[0].getRow() > 0) &&
                (!options().isRowNames() || cRefs[0].getCol() > 0);
    }

    private String fetchCellComment(Cell eC, boolean removeAuthors)
    {
        if (eC != null) {        
            Comment cellComment = eC.getCellComment();
            if (cellComment != null) {        
                RichTextString rts = cellComment.getString();
                if (rts != null) {
                    String note = trimString(rts.getString());

                    if (removeAuthors) {
                        String author = trimString(cellComment.getAuthor());
                        if (author != null) 
                            note = removeString(note, author);
                    }

                    return note;
                }
            }
        }

        return null;       
    }

    private Object fetchCellValue(Cell eC) 
    {
        return fetchCellValue(null, null, -1, eC, null);
    }

    private Object fetchCellValue(Workbook wb, Sheet sheet, int asi, Cell eC, Table t) 
    {
        if (eC == null)
            return null;

        // decode based on the cell type
        int cellType = eC.getCellType();
        switch(cellType) {
            case Cell.CELL_TYPE_BOOLEAN:
                return eC.getBooleanCellValue();

            case Cell.CELL_TYPE_NUMERIC:
                return eC.getNumericCellValue();

            case Cell.CELL_TYPE_STRING:
                return eC.getRichStringCellValue().getString();

            case Cell.CELL_TYPE_FORMULA:
                return fetchFormulaCellValue(eC);

            default:
                return null;
        }
    }

    private Object fetchFormulaCellValue(Cell eC) 
    {
        String cellFormula = eC.getCellFormula();
        switch (cellFormula) {
            case "TRUE":
                return true;

            case "FALSE":
                return false;

            case "NOW":
                return new Date();
        }

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

    private void processExcelFormula(Workbook wb, Sheet sheet, int asi, Cell eC, Table t)
    {
        // parse the formula
        String formula = eC.getCellFormula();
        FormulaParsingWorkbook fpWb = wb instanceof HSSFWorkbook ? 
                HSSFEvaluationWorkbook.create((HSSFWorkbook)wb) : XSSFEvaluationWorkbook.create((XSSFWorkbook)wb);

        Ptg [] tokens = FormulaParser.parse(formula, fpWb, FormulaType.NAMEDRANGE, asi);
        ((Ref3DPxg)tokens[0]).getColumn();
        ((Ref3DPxg)tokens[0]).getRow();
    }
}
