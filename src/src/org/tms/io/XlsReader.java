package org.tms.io;

import java.io.File;
import java.io.IOException;

import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.tms.api.Access;
import org.tms.api.Column;
import org.tms.api.Table;
import org.tms.api.TableContext;
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
            if (sheet.getSheetName() != null)
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
                        Object cv = fetchCellValue(eC);
                        if (i == 0 && isRowNames()) {
                            if (cv != null)                       
                                tR.setLabel(cv.toString());
                        }
                        else {
                            if (tC == null)
                                tC = t.addColumn();
                            
                            if (eC != null || cv != null) {
                                org.tms.api.Cell tCell = t.getCell(tR, tC);
                                if (cv != null)
                                    tCell.setCellValue(cv);
                            }
                            
                            // bump column
                            tC = t.getColumn(Access.Next);                        }
                    }
                }
            }
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

    private Object fetchCellValue(Cell eC)
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
                return decodeExcelFormula(eC.getCellFormula());
                
            default:
                return null;
        }
    }

    private Object decodeExcelFormula(String cellFormula)
    {
        switch (cellFormula) {
            case "TRUE":
                return true;
                
            case "FALSE":
                return false;
        }
        
        return null;
    }
}
