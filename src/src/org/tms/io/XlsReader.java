package org.tms.io;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.hssf.usermodel.HSSFEvaluationWorkbook;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.SpreadsheetVersion;
import org.apache.poi.ss.formula.FormulaParser;
import org.apache.poi.ss.formula.FormulaParsingWorkbook;
import org.apache.poi.ss.formula.FormulaType;
import org.apache.poi.ss.formula.ptg.AbstractFunctionPtg;
import org.apache.poi.ss.formula.ptg.AddPtg;
import org.apache.poi.ss.formula.ptg.AreaPtgBase;
import org.apache.poi.ss.formula.ptg.AttrPtg;
import org.apache.poi.ss.formula.ptg.BoolPtg;
import org.apache.poi.ss.formula.ptg.ConcatPtg;
import org.apache.poi.ss.formula.ptg.ControlPtg;
import org.apache.poi.ss.formula.ptg.DividePtg;
import org.apache.poi.ss.formula.ptg.EqualPtg;
import org.apache.poi.ss.formula.ptg.GreaterEqualPtg;
import org.apache.poi.ss.formula.ptg.GreaterThanPtg;
import org.apache.poi.ss.formula.ptg.IntPtg;
import org.apache.poi.ss.formula.ptg.LessEqualPtg;
import org.apache.poi.ss.formula.ptg.LessThanPtg;
import org.apache.poi.ss.formula.ptg.MissingArgPtg;
import org.apache.poi.ss.formula.ptg.MultiplyPtg;
import org.apache.poi.ss.formula.ptg.NamePtg;
import org.apache.poi.ss.formula.ptg.NotEqualPtg;
import org.apache.poi.ss.formula.ptg.NumberPtg;
import org.apache.poi.ss.formula.ptg.OperandPtg;
import org.apache.poi.ss.formula.ptg.OperationPtg;
import org.apache.poi.ss.formula.ptg.ParenthesisPtg;
import org.apache.poi.ss.formula.ptg.PercentPtg;
import org.apache.poi.ss.formula.ptg.PowerPtg;
import org.apache.poi.ss.formula.ptg.Ptg;
import org.apache.poi.ss.formula.ptg.RefPtgBase;
import org.apache.poi.ss.formula.ptg.ScalarConstantPtg;
import org.apache.poi.ss.formula.ptg.StringPtg;
import org.apache.poi.ss.formula.ptg.SubtractPtg;
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
import org.tms.api.TableElement;
import org.tms.api.derivables.Derivable;
import org.tms.api.derivables.ErrorCode;
import org.tms.api.derivables.Operator;
import org.tms.api.derivables.Token;
import org.tms.api.derivables.TokenType;
import org.tms.api.derivables.exceptions.InvalidExpressionException;
import org.tms.api.exceptions.TableIOException;
import org.tms.api.exceptions.UnimplementedException;
import org.tms.api.factories.TableContextFactory;
import org.tms.api.factories.TableFactory;
import org.tms.api.io.options.XlsOptions;
import org.tms.teq.BuiltinOperator;
import org.tms.teq.EquationStack;
import org.tms.teq.StackType;

public class XlsReader extends BaseReader<XlsOptions>
{
    private static final Map<Class<? extends OperationPtg>, Operator> sf_OperatorMap 
        = new HashMap<Class<? extends OperationPtg>, Operator>();
    
    static {
        sf_OperatorMap.put(AddPtg.class, BuiltinOperator.PlusOper);
        sf_OperatorMap.put(ConcatPtg.class, BuiltinOperator.PlusOper);
        sf_OperatorMap.put(SubtractPtg.class, BuiltinOperator.MinusOper);
        sf_OperatorMap.put(DividePtg.class, BuiltinOperator.DivOper);
        sf_OperatorMap.put(MultiplyPtg.class, BuiltinOperator.MultOper);
        sf_OperatorMap.put(PowerPtg.class, BuiltinOperator.PowerFuncOper);
        sf_OperatorMap.put(PercentPtg.class, BuiltinOperator.PercentOper);
        
        sf_OperatorMap.put(EqualPtg.class, BuiltinOperator.EqOper);
        sf_OperatorMap.put(NotEqualPtg.class, BuiltinOperator.NEqOper);
        sf_OperatorMap.put(GreaterEqualPtg.class, BuiltinOperator.GtEOper);
        sf_OperatorMap.put(LessEqualPtg.class, BuiltinOperator.LtEOper);
        sf_OperatorMap.put(GreaterThanPtg.class, BuiltinOperator.GtOper);
        sf_OperatorMap.put(LessThanPtg.class, BuiltinOperator.LtOper);
    }
    
    private static final Map<String, Operator> sf_FunctionMap 
        = new HashMap<String, Operator>();

    static {
        sf_FunctionMap.put("FACT", BuiltinOperator.FactFuncOper);
        sf_FunctionMap.put("PI", BuiltinOperator.PiOper);
        sf_FunctionMap.put("MOD", BuiltinOperator.ModFuncOper);
        sf_FunctionMap.put("POWER", BuiltinOperator.PowerFuncOper);
        
        sf_FunctionMap.put("UPPER",  BuiltinOperator.toUpperOper);
        sf_FunctionMap.put("LOWER",  BuiltinOperator.toLowerOper);
        sf_FunctionMap.put("LEN",  BuiltinOperator.LenOper);
        sf_FunctionMap.put("TRIM",  BuiltinOperator.trimOper);
        sf_FunctionMap.put("CONCATENATE",  BuiltinOperator.PlusOper);
        sf_FunctionMap.put("REPT",  BuiltinOperator.MultOper);
        sf_FunctionMap.put("EXACT", BuiltinOperator.EqOper);
        sf_FunctionMap.put("VALUE", BuiltinOperator.toNumberOper);
        sf_FunctionMap.put("LEFT", BuiltinOperator.LeftOper);
        sf_FunctionMap.put("RIGHT", BuiltinOperator.RightOper);
        sf_FunctionMap.put("MID", BuiltinOperator.MidOper);
        
        sf_FunctionMap.put("ISTEXT",  BuiltinOperator.IsTextOper);
        sf_FunctionMap.put("ISNUMBER",  BuiltinOperator.IsNumberOper);
        sf_FunctionMap.put("ISTEXT",  BuiltinOperator.IsTextOper);
        sf_FunctionMap.put("ISLOGICAL",  BuiltinOperator.IsLogicalOper);
        sf_FunctionMap.put("ISBLANK", BuiltinOperator.IsNullOper);
        sf_FunctionMap.put("ISERR", BuiltinOperator.IsErrorOper);
        sf_FunctionMap.put("ISERROR", BuiltinOperator.IsErrorOper);
        
        sf_FunctionMap.put("IF", BuiltinOperator.IfOper);
        
        sf_FunctionMap.put("AVERAGE", BuiltinOperator.MeanOper);
        sf_FunctionMap.put("MEDIAN", BuiltinOperator.MedianOper);
        sf_FunctionMap.put("MODE", BuiltinOperator.ModeOper);
        sf_FunctionMap.put("STDEV", BuiltinOperator.StDevSampleOper);
        sf_FunctionMap.put("VAR", BuiltinOperator.VarSampleOper);
        sf_FunctionMap.put("MIN", BuiltinOperator.MinOper);
        sf_FunctionMap.put("MAX", BuiltinOperator.MaxOper);
        sf_FunctionMap.put("COUNT", BuiltinOperator.CountOper);
        sf_FunctionMap.put("SKEW", BuiltinOperator.SkewOper);
        sf_FunctionMap.put("SUMSQ", BuiltinOperator.Sum2Oper);
        sf_FunctionMap.put("KURT", BuiltinOperator.KurtosisOper);
        sf_FunctionMap.put("DEVSQ", BuiltinOperator.SumSqD2Oper);
        sf_FunctionMap.put("QUARTILE", BuiltinOperator.QuartileOper);        
    }
    
    private Map<String, DerivationScope> m_derivCache = null;
    private Map<org.tms.api.Cell, String> m_derivedCells = null;
    private Map<String, TableElement> m_namedTableElements = null;
    private Table m_table;
    
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

    public TableElement getTmsColumn(int excelColNo)
    {
        return m_table.getColumn(excelColNo + 1 - (XlsReader.this.options().isRowNames() ? 1 : 0));
    }

    public TableElement getTmsRow(int excelRowNo)
    {
        return m_table.getRow(excelRowNo + 1 - (XlsReader.this.options().isColumnNames() ? 1 : 0));
    }

    public Table parse() throws IOException
    {
        // create the table scaffold
        Table t = TableFactory.createTable(getTableContext());
        m_table = t;
        Workbook wb = null;
        try
        {
            wb = WorkbookFactory.create(getInputFile());
            int asi = wb.getActiveSheetIndex();
            
            List<ParsedFormula> parsedFormulas = new ArrayList<ParsedFormula>();

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
                        Object cv = fetchCellValue(eC);
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
                                    ParsedFormula pf = processExcelFormula(wb, sheet, asi, eC, tCell);
                                    parsedFormulas.add(pf);
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
            
            // handle equations
            processEquations(wb, sheet, sheetName, asi, parsedFormulas, t);
        }
        catch (EncryptedDocumentException | InvalidFormatException e)
        {
            throw new TableIOException(e);
        }

        return t;
    }

    /**
     * For each Excel formula discovered, create a TMS-style infix formula
     * and cache it along with the TMS cell it is associated with
     */
    private void processEquations(Workbook wb, Sheet sheet, String sheetName, int asi, 
            List<ParsedFormula> parsedFormulas, Table t)
    {
        for (ParsedFormula pf : parsedFormulas) {
            try {
                EquationStack es = EquationStack.createPostfixStack();
                for (Ptg eT : pf.getTokens()) {
                    Token tmsT = excelToken2TmsToken(pf, eT);
                    if (tmsT != null) 
                        es.push(tmsT);
                }
                
                // we now have a post fix stack in TMS form
                String formula = trimString(es.toExpression(StackType.Infix));
                if (formula != null)
                    cacheDerivation(formula, pf.getTmsCell());
            }
            catch (UnimplementedException e) {
                System.out.println(e.getMessage());
            }
        } 
        
        // with all excel formulas processed and transformed
        // into tms derivations, apply the derivations to their targets
        if (m_derivCache != null) {
            boolean autoRecalc = t.isAutoRecalculate();
            t.setAutoRecalculate(false);
            for(Map.Entry<String, DerivationScope> e : m_derivCache.entrySet()) {
                String deriv = e.getKey();
                for (Derivable d : e.getValue().getTargets()) {
                    try {
                        d.setDerivation(deriv);
                    }
                    catch (InvalidExpressionException iee) {
                        System.out.println(iee.getMessage());
                    }
                }
            }
            
            t.setAutoRecalculate(autoRecalc);
            t.recalculate();
        }
    }

    private void cacheDerivation(String deriv, org.tms.api.Cell tmsCell)
    {
        if (m_derivedCells == null)
            m_derivedCells = new HashMap<org.tms.api.Cell, String>();
                
        // and record the cell as derived
        m_derivedCells.put(tmsCell,  deriv);
        
        if (m_derivCache == null)
            m_derivCache = new HashMap<String, DerivationScope>();
        
        // create DerivationScope, if one doesn't exist
        DerivationScope  ds = m_derivCache.get(deriv);
        if (ds == null) {
            ds = new DerivationScope();
            m_derivCache.put(deriv, ds);
        }
        
        // add this cell to the scope object
        ds.cache(tmsCell);
    }

    private Token excelToken2TmsToken(ParsedFormula pf, Ptg eT)
    {
        if (eT instanceof ControlPtg) {
            if (eT instanceof ParenthesisPtg)
                return null;
            else if (eT instanceof AttrPtg) {
                AttrPtg etA = (AttrPtg)eT;
                if (etA.isSum()) // very special case
                    return new Token(BuiltinOperator.SumOper);
                if (etA.isOptimizedIf()) // very special case
                    return null;
                if (etA.isSkip()) // very special case
                    return null;
            }           
        }
        else if (eT instanceof ScalarConstantPtg) 
            return createOperandToken((ScalarConstantPtg)eT);
        
        else if (eT instanceof OperationPtg) 
            return createOperationToken((OperationPtg)eT);
        
        else if (eT instanceof OperandPtg) 
            return createOperationToken((OperandPtg)eT, pf);
        
        // if we get here, we don't support this excel token
        throw new UnimplementedException(eT.getClass().getSimpleName());            
    }

    private Token createOperationToken(OperandPtg eT, ParsedFormula pf)
    {
        switch (eT.getClass().getSimpleName()) {
            case "Ref3DPxg":
            case "Ref3DPtg":
            case "RefNPtg":
            case "RefPtg":
                return createOperandToken((RefPtgBase)eT, pf);
                
            case "AreaPtg":
                return createOperandToken((AreaPtgBase)eT, pf);
                
            case "NamePtg":
                return createOperandToken((NamePtg)eT, pf);
        }           
        
        // if we get here, we don't support this excel token
        throw new UnimplementedException(eT.getClass().getSimpleName());            
    }

    private Token createOperandToken(NamePtg eT, ParsedFormula pf)
    {
        if (m_namedTableElements != null) {
            Name namedRange = pf.getExcelCell().getSheet().getWorkbook().getNameAt(eT.getIndex());
            
            if (namedRange != null) {
                String refName = namedRange.getNameName();
                TableElement te = m_namedTableElements.get(refName);
                if (te != null) {
                    if (te instanceof org.tms.api.Cell)
                        return new Token(TokenType.CellRef, te);
                    else if (te instanceof Subset)
                        return new Token(TokenType.SubsetRef, te);
                }
            }
        }
            
        throw new TableIOException("Excel range reference not found");            
    }

    private Token createOperandToken(AreaPtgBase eT, ParsedFormula pf)
    {
        /*
         * Excel ranges naturally map into TMS Subsets
         * That said, if the subset consists of all non-
         * derived cells in the column/row, then set the
         * range to the parent row/column
         */
        int rngColNo = eT.getFirstColumn();
        int rngRowNo = eT.getFirstRow();
        
        int numCols = eT.getLastColumn() - rngColNo + 1;
        int numRows = eT.getLastRow() - rngRowNo + 1;
        
        int eCCol = pf.getExcelCell().getColumnIndex();
        int eCRow = pf.getExcelCell().getRowIndex();
        
        if (numCols == 1 && rngColNo == eCCol) {
            if (rangeIsColumn(rngRowNo, eT.getLastRow(), rngColNo, eT, pf))
                return new Token(TokenType.ColumnRef, getTmsColumn(rngColNo));
        }
        
        if (numRows == 1 && rngRowNo == eCRow) {
            if (rangeIsRow(rngColNo, eT.getLastColumn(), rngRowNo, eT, pf))
                return new Token(TokenType.RowRef, getTmsRow(rngRowNo));
        }
        
        // create subset, if needed
        Table t = pf.getTable();
        String label = "excel_" + eT.toFormulaString();
        Subset subset = t.getSubset(Access.ByLabel, label);
        
        if (subset == null) {
            subset = t.addSubset(Access.ByLabel, label);
            for (int colIdx = rngColNo; colIdx <= eT.getLastColumn(); colIdx++) {
                subset.add(getTmsColumn(colIdx));
            }
            
            for (int rowIdx = rngRowNo; rowIdx <= eT.getLastRow(); rowIdx++) {
                subset.add(getTmsRow(rowIdx));
            }
        }
        
        return new Token(TokenType.SubsetRef, subset);
    }

    /**
     * If all non-range cells in the column are formuli, return true
     * @param colNo 
     */
    private boolean rangeIsColumn(int rng1stRowNo, int rngLstRowNo, int colNo, AreaPtgBase rng, ParsedFormula pF)
    {
        Sheet sheet = pF.getExcelCell().getSheet();
        int firstEffectiveRow = 0 + (options().isColumnNames() ? 1 : 0);
        int lastRow = sheet.getLastRowNum();
        
        for (int i = firstEffectiveRow; i < rng1stRowNo; i++) {
            Row r = sheet.getRow(i);
            Cell cell = r.getCell(colNo, Row.RETURN_BLANK_AS_NULL);
            if (cell == null || cell.getCellType() != Cell.CELL_TYPE_FORMULA)
                return false;
        }
        
        for (int i = rngLstRowNo + 1; i < lastRow; i++) {
            Row r = sheet.getRow(i);
            Cell cell = r.getCell(colNo, Row.RETURN_BLANK_AS_NULL);
            if (cell == null || cell.getCellType() != Cell.CELL_TYPE_FORMULA)
                return false;
        }
        
        return true;
    }

    /**
     * If all non-range cells in the row are formuli, return true
     * @param colNo 
     */
    private boolean rangeIsRow(int rng1stColNo, int rngLstColNo, int rowNo, AreaPtgBase rng, ParsedFormula pF)
    {
        Sheet sheet = pF.getExcelCell().getSheet();
        int firstEffectiveCol = 0 + (options().isRowNames() ? 1 : 0);
        Row row = sheet.getRow(rowNo);
        int lastCol = row.getLastCellNum();
        
        for (int i = firstEffectiveCol; i < rng1stColNo; i++) {
            Cell cell = row.getCell(i, Row.RETURN_BLANK_AS_NULL);
            if (cell == null || cell.getCellType() != Cell.CELL_TYPE_FORMULA)
                return false;
        }
        
        for (int i = rngLstColNo + 1; i < lastCol; i++) {
            Cell cell = row.getCell(i, Row.RETURN_BLANK_AS_NULL);
            if (cell == null || cell.getCellType() != Cell.CELL_TYPE_FORMULA)
                return false;
        }
        
        return true;
    }

    private Token createOperandToken(RefPtgBase eT, ParsedFormula pf)
    {
        // determine tms-based indices of reference
        int rowRef = eT.getRow() + 1 - (options().isColumnNames() ? 1 : 0);
        int colRef = eT.getColumn() + 1 - (options().isRowNames() ? 1 : 0);
        
        // if the reference is in the same row or column as the tms target cell, 
        // return a column or row reference
        org.tms.api.Cell tCell = pf.getTmsCell();
        Table t = tCell.getTable();
        if (tCell.getRow().getIndex() == rowRef)
            return new Token(TokenType.ColumnRef, t.getColumn(colRef));
        else if (tCell.getColumn().getIndex() == colRef)
            return new Token(TokenType.RowRef, t.getRow(rowRef));
        else {
            org.tms.api.Cell refedCell = t.getCell(t.getRow(rowRef), t.getColumn(colRef));
            if (trimString(refedCell.getLabel()) == null)
                refedCell.setLabel(String.format("FROM_EXCEL_%d_%d", eT.getRow(), eT.getColumn())); // assign string a unique name
            
            return new Token(TokenType.CellRef, refedCell);
        }
    }

    private Token createOperationToken(AbstractFunctionPtg eT)
    {
        String funcName = trimString(eT.getName());
        if (funcName != null) {
            Operator op = sf_FunctionMap.get(funcName.toUpperCase());
            
            if (op != null)
                return new Token(op);            
        }
        
        // if we get here, we don't support this excel token
        throw new UnimplementedException(eT.getClass().getSimpleName());            
    }
    
    private Token createOperationToken(OperationPtg eT)
    {
        Operator op = sf_OperatorMap.get(eT.getClass());
        if (op != null)
            return new Token(op);
        
        // check if the operation is a AbstractFunctionPtg
        if (eT instanceof AbstractFunctionPtg)
            return createOperationToken((AbstractFunctionPtg)eT);
        
        // if we get here, we don't support this excel token
        throw new UnimplementedException(eT.getClass().getSimpleName());            
    }

    private Token createOperandToken(ScalarConstantPtg eT)
    {
        if (eT instanceof IntPtg)
            return new Token(TokenType.Operand, ((IntPtg)eT).getValue());
        else if (eT instanceof BoolPtg)
            return new Token(TokenType.Operand, ((BoolPtg)eT).getValue());
        else if (eT instanceof NumberPtg)
            return new Token(TokenType.Operand, ((NumberPtg)eT).getValue());
        else if (eT instanceof StringPtg) {
            String val = ((StringPtg)eT).getValue();
            if (options().isBlanksAsNull() && val != null && val.length() == 0)
                return new Token(BuiltinOperator.NullOper);
            return new Token(TokenType.Operand, ((StringPtg)eT).getValue());
        }
        else if (eT instanceof MissingArgPtg)
            return Token.createNullToken();
        
        // if we get here, we don't support this excel token
        throw new UnimplementedException(eT.getClass().getSimpleName());            
    }

    private void processNamedRegions(Workbook wb, Sheet activeSheet, String sheetName, Table t)
    {
        int numNamedRegions = wb.getNumberOfNames();
        if (numNamedRegions > 0) {
            m_namedTableElements = new HashMap<String, TableElement>(numNamedRegions);
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
                        if (tCell != null) {
                            tCell.setLabel(name);
                            m_namedTableElements.put(name, tCell);
                        }
                    }  
                    else {
                        Subset s = createSubset(cRefs, t, name);
                        if (s != null)
                            m_namedTableElements.put(name, s);
                    }
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

    private ParsedFormula processExcelFormula(Workbook wb, Sheet sheet, int asi, Cell eC, org.tms.api.Cell tCell)
    {
        // parse the formula
        String formula = eC.getCellFormula();
        FormulaParsingWorkbook fpWb = wb instanceof HSSFWorkbook ? 
                HSSFEvaluationWorkbook.create((HSSFWorkbook)wb) : XSSFEvaluationWorkbook.create((XSSFWorkbook)wb);

        Ptg [] tokens = FormulaParser.parse(formula, fpWb, FormulaType.NAMEDRANGE, asi);
        return new ParsedFormula(eC, tCell, tokens);
    }
    
    class ParsedFormula
    {
        private Cell m_excelCell;
        private org.tms.api.Cell m_tmsCell;
        private Ptg [] m_tokens;

        ParsedFormula(Cell eC, org.tms.api.Cell tCell, Ptg [] tokens)
        {
            m_excelCell = eC;
            m_tmsCell = tCell;
            m_tokens = tokens;
        }
        
        public Table getTable()
        {
            return m_tmsCell.getTable();
        }

        Cell getExcelCell()
        {
            return m_excelCell;
        }
        
        org.tms.api.Cell getTmsCell()
        {
            return m_tmsCell;
        }
        
        Ptg [] getTokens()
        {
            return m_tokens;
        }
    }
    
    class DerivationScope
    {
        private Set<Derivable> m_targets;
        private Map<org.tms.api.Column, Set<org.tms.api.Cell>> m_colTargets;
        private Map<org.tms.api.Row, Set<org.tms.api.Cell>> m_rowTargets;
        
        DerivationScope()
        {
            m_targets = new LinkedHashSet<Derivable>();
            
            m_colTargets = new HashMap<org.tms.api.Column, Set<org.tms.api.Cell>>(0);
            m_rowTargets = new HashMap<org.tms.api.Row, Set<org.tms.api.Cell>>(0);
        }
        
        public void cache(org.tms.api.Cell tmsCell)
        {
            m_targets.add(tmsCell);
            Table t = tmsCell.getTable();
            
            // if each cell in the row is this derivation, then
            // use the formula as the row derivation
            org.tms.api.Row row = tmsCell.getRow();
            Set<org.tms.api.Cell> rowCells = m_rowTargets.get(row);
            if (rowCells == null) {
                rowCells = new HashSet<org.tms.api.Cell>();
                m_rowTargets.put(row,  rowCells);
            }
            
            rowCells.add(tmsCell);
            if (rowCells.size() == t.getNumColumns()) {
                m_targets.add(row);
                m_targets.removeAll(rowCells);
                m_derivedCells.keySet().removeAll(rowCells);
                m_rowTargets.remove(row);
            }
            
            // if each cell in the column is this derivation, then
            // use the formula as the column derivation
            org.tms.api.Column col = tmsCell.getColumn();  
            Set<org.tms.api.Cell> colCells = m_colTargets.get(col);
            if (colCells == null) {
                colCells = new HashSet<org.tms.api.Cell>();
                m_colTargets.put(col, colCells);
            }
            
            colCells.add(tmsCell);
            if (colCells.size() == t.getNumRows()) {
                m_targets.add(col);
                m_targets.removeAll(colCells);
                m_derivedCells.keySet().removeAll(colCells);
                m_colTargets.remove(col);
            }
        }
        
        List<Derivable> getTargets()
        {
            /*
             * At this point, all possible derived cells have been identified.
             * Revisit each row/column with derived cells and determine
             * if all of the non-recorded cells are also derived; if they are,
             * then we can use the entire row/column as the derivation target
             */
            Set<org.tms.api.Column> processedCols = new HashSet<org.tms.api.Column>();
            for (Map.Entry<Column, Set<org.tms.api.Cell>> e : m_colTargets.entrySet()) {
                org.tms.api.Column col = e.getKey();
                Set<org.tms.api.Cell> derivedCells = e.getValue();
                
                if (derivedCells != null && derivedCells.size() > 1) {
                    // remove this set from our map of derived cells,
                    // as we already know they are derived
                    m_derivedCells.keySet().removeAll(derivedCells);
                    
                    // get all remaining derived cells for this column
                    Set<org.tms.api.Cell> otherDerivedCells = getOtherDerivedCells(col);
                    if (otherDerivedCells.size() + derivedCells.size() >= col.getTable().getNumRows()) {
                        m_targets.removeAll(derivedCells);
                        m_targets.add(col);
                        processedCols.add(col);
                    }
                }
            }
            
            // remove processed columns
            m_colTargets.keySet().removeAll(processedCols);
            
            Set<org.tms.api.Row> processedRows = new HashSet<org.tms.api.Row>();
            for (Map.Entry<org.tms.api.Row, Set<org.tms.api.Cell>> e : m_rowTargets.entrySet()) {
                org.tms.api.Row row = e.getKey();
                Set<org.tms.api.Cell> derivedCells = e.getValue();
                
                if (derivedCells != null && derivedCells.size() > 1) {
                    // remove this set from our map of derived cells,
                    // as we already know they are derived
                    m_derivedCells.keySet().removeAll(derivedCells);
                    
                    // get all remaining derived cells for this row
                    Set<org.tms.api.Cell> otherDerivedCells = getOtherDerivedCells(row);
                    if (otherDerivedCells.size() + derivedCells.size() >= row.getTable().getNumColumns()) {
                        m_targets.removeAll(derivedCells);
                        m_targets.add(row);
                        processedRows.add(row);
                    }
                }
            }
            
            // remove processed columns
            m_rowTargets.keySet().removeAll(processedRows);
            
            return  new ArrayList<Derivable>(m_targets);
        }

        private Set<org.tms.api.Cell> getOtherDerivedCells(org.tms.api.Column col)
        {
            Set<org.tms.api.Cell> otherDerivedCells = new HashSet<org.tms.api.Cell>();
            for (org.tms.api.Cell cell : m_derivedCells.keySet()) {
                if (cell.getColumn() == col)
                    otherDerivedCells.add(cell);
            }
            
            return otherDerivedCells;
        }

        private Set<org.tms.api.Cell> getOtherDerivedCells(org.tms.api.Row row)
        {
            Set<org.tms.api.Cell> otherDerivedCells = new HashSet<org.tms.api.Cell>();
            for (org.tms.api.Cell cell : m_derivedCells.keySet()) {
                if (cell.getRow() == row)
                    otherDerivedCells.add(cell);
            }
            
            return otherDerivedCells;
        }
    }
}
