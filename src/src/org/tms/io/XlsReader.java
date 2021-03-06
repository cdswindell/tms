package org.tms.io;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.hssf.usermodel.HSSFEvaluationWorkbook;
import org.apache.poi.hssf.usermodel.HSSFFormulaEvaluator;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.SpreadsheetVersion;
import org.apache.poi.ss.formula.FormulaParser;
import org.apache.poi.ss.formula.FormulaParsingWorkbook;
import org.apache.poi.ss.formula.FormulaRenderingWorkbook;
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
import org.apache.poi.ss.formula.ptg.NameXPxg;
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
import org.apache.poi.ss.usermodel.CellType;
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
import org.apache.poi.xssf.usermodel.XSSFFormulaEvaluator;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.tms.api.Access;
import org.tms.api.Column;
import org.tms.api.Subset;
import org.tms.api.Table;
import org.tms.api.TableContext;
import org.tms.api.TableElement;
import org.tms.api.derivables.Derivable;
import org.tms.api.derivables.ErrorCode;
import org.tms.api.derivables.InvalidExpressionException;
import org.tms.api.derivables.Operator;
import org.tms.api.derivables.Token;
import org.tms.api.derivables.TokenType;
import org.tms.api.exceptions.TableIOException;
import org.tms.api.exceptions.UnimplementedException;
import org.tms.api.factories.TableContextFactory;
import org.tms.api.factories.TableFactory;
import org.tms.api.io.XLSOptions;
import org.tms.teq.BuiltinOperator;
import org.tms.teq.EquationStack;
import org.tms.teq.StackType;

public class XlsReader extends LabeledReader<XLSOptions>
{
    static final Map<Class<? extends OperationPtg>, Operator> sf_OperatorMap 
        = new HashMap<Class<? extends OperationPtg>, Operator>();
    
    static {
        sf_OperatorMap.put(AddPtg.class, BuiltinOperator.PlusOper);
        sf_OperatorMap.put(SubtractPtg.class, BuiltinOperator.MinusOper);
        sf_OperatorMap.put(DividePtg.class, BuiltinOperator.DivOper);
        sf_OperatorMap.put(MultiplyPtg.class, BuiltinOperator.MultOper);
        
        sf_OperatorMap.put(PercentPtg.class, BuiltinOperator.PercentOper);
        sf_OperatorMap.put(PowerPtg.class, BuiltinOperator.PowerOper);
        sf_OperatorMap.put(ConcatPtg.class, BuiltinOperator.PlusOper);
        
        sf_OperatorMap.put(EqualPtg.class, BuiltinOperator.EqOper);
        sf_OperatorMap.put(NotEqualPtg.class, BuiltinOperator.NEqOper);
        sf_OperatorMap.put(GreaterEqualPtg.class, BuiltinOperator.GtEOper);
        sf_OperatorMap.put(LessEqualPtg.class, BuiltinOperator.LtEOper);
        sf_OperatorMap.put(GreaterThanPtg.class, BuiltinOperator.GtOper);
        sf_OperatorMap.put(LessThanPtg.class, BuiltinOperator.LtOper);
    }
    
    static final Map<String, Operator> sf_FunctionMap = new LinkedHashMap<String, Operator>();

    static {
        sf_FunctionMap.put("FACT", BuiltinOperator.FactFuncOper);
        sf_FunctionMap.put("PI", BuiltinOperator.PiOper);
        sf_FunctionMap.put("MOD", BuiltinOperator.ModFuncOper);
        sf_FunctionMap.put("POWER", BuiltinOperator.PowerFuncOper);
        sf_FunctionMap.put("QUOTIENT", BuiltinOperator.QuotientFuncOper);
        
        sf_FunctionMap.put("RAND", BuiltinOperator.RandOper);
        sf_FunctionMap.put("RANDBETWEEN", BuiltinOperator.RandBetweenOper);
        sf_FunctionMap.put("ABS", BuiltinOperator.AbsOper);
        sf_FunctionMap.put("SQRT", BuiltinOperator.SqrtOper);
        sf_FunctionMap.put("SIGN", BuiltinOperator.SignOper);
        sf_FunctionMap.put("INT", BuiltinOperator.FloorOper);
        sf_FunctionMap.put("TRUNC", BuiltinOperator.FloorOper);
        sf_FunctionMap.put("CEILING", BuiltinOperator.CeilOper);
        sf_FunctionMap.put("ROUND", BuiltinOperator.RoundOper);
        sf_FunctionMap.put("GCD", BuiltinOperator.GcdOper);
        sf_FunctionMap.put("LCM", BuiltinOperator.LcmOper);
        sf_FunctionMap.put("EVEN", BuiltinOperator.toEvenOper);
        sf_FunctionMap.put("ODD", BuiltinOperator.toOddOper);
        
        sf_FunctionMap.put("COMBIN", BuiltinOperator.CombOper);
        sf_FunctionMap.put("PERMUT", BuiltinOperator.PermOper);
        
        sf_FunctionMap.put("EXP", BuiltinOperator.ExpOper);
        sf_FunctionMap.put("LN", BuiltinOperator.LogOper);
        sf_FunctionMap.put("LOG", BuiltinOperator.Log10Oper);
        sf_FunctionMap.put("LOG10", BuiltinOperator.Log10Oper);
        
        sf_FunctionMap.put("DEGREES", BuiltinOperator.toDegreesOper);
        sf_FunctionMap.put("RADIANS", BuiltinOperator.toRadiansOper);
        sf_FunctionMap.put("ACOS", BuiltinOperator.ACosOper);
        sf_FunctionMap.put("ASIN", BuiltinOperator.ASinOper);
        sf_FunctionMap.put("ATAN", BuiltinOperator.ATanOper);

        sf_FunctionMap.put("COS", BuiltinOperator.CosOper);
        sf_FunctionMap.put("SIN", BuiltinOperator.SinOper);
        sf_FunctionMap.put("TAN", BuiltinOperator.TanOper);
               
        sf_FunctionMap.put("SINH", BuiltinOperator.SinHOper);
        sf_FunctionMap.put("COSH", BuiltinOperator.CosHOper);
        sf_FunctionMap.put("TANH", BuiltinOperator.TanHOper);
        
        sf_FunctionMap.put("UPPER",  BuiltinOperator.toUpperOper);
        sf_FunctionMap.put("LOWER",  BuiltinOperator.toLowerOper);
        sf_FunctionMap.put("LEN",  BuiltinOperator.LenOper);
        sf_FunctionMap.put("TRIM",  BuiltinOperator.trimOper);
        sf_FunctionMap.put("CONCATENATE",  BuiltinOperator.PlusOper);
        sf_FunctionMap.put("CONCAT",  BuiltinOperator.PlusOper);
        sf_FunctionMap.put("REPT",  BuiltinOperator.MultOper);
        sf_FunctionMap.put("EXACT", BuiltinOperator.EqOper);
        sf_FunctionMap.put("VALUE", BuiltinOperator.toNumberOper);
        sf_FunctionMap.put("LEFT", BuiltinOperator.LeftOper);
        sf_FunctionMap.put("RIGHT", BuiltinOperator.RightOper);
        sf_FunctionMap.put("MID", BuiltinOperator.MidOper);
        
        sf_FunctionMap.put("ISTEXT",  BuiltinOperator.IsTextOper);
        sf_FunctionMap.put("ISNUMBER",  BuiltinOperator.IsNumberOper);
        sf_FunctionMap.put("ISLOGICAL",  BuiltinOperator.IsLogicalOper);
        sf_FunctionMap.put("ISBLANK", BuiltinOperator.IsNullOper);
        sf_FunctionMap.put("ISERR", BuiltinOperator.IsErrorOper);
        sf_FunctionMap.put("ISERROR", BuiltinOperator.IsErrorOper);
        
        sf_FunctionMap.put("ISEVEN", BuiltinOperator.IsEvenOper);
        sf_FunctionMap.put("ISODD", BuiltinOperator.IsOddOper);

        sf_FunctionMap.put("AND", BuiltinOperator.AndOper);
        sf_FunctionMap.put("OR", BuiltinOperator.OrOper);
        sf_FunctionMap.put("NOT", BuiltinOperator.NotOper);
        sf_FunctionMap.put("XOR", BuiltinOperator.XorOper);
        sf_FunctionMap.put("TRUE", BuiltinOperator.TrueOper);
        sf_FunctionMap.put("FALSE", BuiltinOperator.FalseOper);
        
        sf_FunctionMap.put("IF", BuiltinOperator.IfOper);
        
        sf_FunctionMap.put("PV", BuiltinOperator.PvOper);
        sf_FunctionMap.put("FV", BuiltinOperator.FvOper);
        sf_FunctionMap.put("PMT", BuiltinOperator.PmtOper);
        sf_FunctionMap.put("NPER", BuiltinOperator.NPerOper);
        sf_FunctionMap.put("RATE", BuiltinOperator.RateOper);
        
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
        
        sf_FunctionMap.put("SLOPE", BuiltinOperator.LinearSlopeOper);        
        sf_FunctionMap.put("INTERCEPT", BuiltinOperator.LinearInterceptOper);        
        sf_FunctionMap.put("CORREL", BuiltinOperator.LinearROper);        
        sf_FunctionMap.put("RSQ", BuiltinOperator.LinearR2Oper);        
    }
    
    static final Set<Operator> sf_InvertedArgs = new HashSet<Operator>(); 
    static {
        sf_InvertedArgs.add(BuiltinOperator.LinearSlopeOper);
        sf_InvertedArgs.add(BuiltinOperator.LinearInterceptOper);
        sf_InvertedArgs.add(BuiltinOperator.LinearROper);
        sf_InvertedArgs.add(BuiltinOperator.LinearR2Oper);
    }
    
    static final Map<Operator, Integer> sf_OmitArgs = new HashMap<Operator, Integer>(); 
    static {
    	sf_OmitArgs.put(BuiltinOperator.CeilOper, 1);
    	sf_OmitArgs.put(BuiltinOperator.RoundOper, 1);
    }
    
    private Map<String, DerivationScope> m_derivCache = null;
    private Map<org.tms.api.Cell, String> m_derivedCells = null;
    private Map<String, TableElement> m_namedTableElements = null;
    private Map<Sheet, Set<Integer>> m_excludedRowsMap;
    private Map<Sheet, Table> m_sheetTableMap;
    private Map<Sheet, List<ParsedFormula>> m_sheetParsedFormulaMap;
    private Map<Sheet, Integer> m_sheetMaxColMap;
    private Map<Table, Set<org.tms.api.Row>> m_emptyRowsMap;
    private EquationStack m_externalFuncRefStack;
    
    private Workbook m_wb;
	private FormulaParsingWorkbook m_fpWb;
	private FormulaRenderingWorkbook m_frWb;
    private SpreadsheetVersion m_ssV;
    
    public XlsReader(String fileName, XLSOptions format)
    {
        this(fileName, TableContextFactory.fetchDefaultTableContext(), format);
    }

    public XlsReader(String fileName, TableContext context, XLSOptions format)
    {
        this(new File(fileName), context, format);
    }

    public XlsReader(File inputFile, TableContext context, XLSOptions format)
    {
        super(inputFile, context, format);
        initState();
    }

    public XlsReader(InputStream in, TableContext tc, XLSOptions format)
    {
        super(in, tc, format);
        initState();
    }

    protected void initState()
    {
        // initialize state variables
        m_wb = null;
        m_fpWb = null;
        m_frWb = null;
        m_sheetTableMap = new LinkedHashMap<Sheet, Table>();
        m_sheetParsedFormulaMap = new LinkedHashMap<Sheet, List<ParsedFormula>>();
        m_sheetMaxColMap = new HashMap<Sheet, Integer>();
        m_namedTableElements = new HashMap<String, TableElement>();
    }

    public void close()
    {
        m_wb = null;
        m_fpWb = null;
        m_frWb = null;
        m_ssV = null;
        
        m_sheetTableMap.clear();
        m_sheetParsedFormulaMap.clear();
        m_sheetMaxColMap.clear();
        m_namedTableElements.clear();
        
        if (m_externalFuncRefStack != null)
            m_externalFuncRefStack.clear();
        
        if (m_derivCache != null)
            m_derivCache.clear();
        
        if (m_derivedCells != null)
            m_derivedCells.clear();
        
        if (m_excludedRowsMap != null)
            m_excludedRowsMap.clear();
            
        if (m_emptyRowsMap != null)
            m_emptyRowsMap.clear();
            
        if (m_externalFuncRefStack != null)
            m_externalFuncRefStack.clear();
    }
    
    public void parseWorkbook() 
    throws IOException
    {
        try
        {            
            m_wb = WorkbookFactory.create(getInputStream());
            m_ssV = m_wb instanceof XSSFWorkbook ? SpreadsheetVersion.EXCEL2007 : SpreadsheetVersion.EXCEL97;  
            m_frWb = (FormulaRenderingWorkbook) m_fpWb;
        
            int noSheets = m_wb.getNumberOfSheets();
            if (noSheets <= 0)
                return;
            
            List<Table> processedTables = new ArrayList<Table>(noSheets);
            
            for (int i = 0; i < noSheets; i++) {
                Table t = parseSheet(i);
                if (t != null) {
                    processedTables.add(t);
                    t.setPersistant(true);
                }
            }

            // handle named ranges
            processNamedRegions();
            
            // handle equations
            applyDerivations();
            
            // for each table, remove empty elements (if requested) and do a final recalc
            for (Table t : processedTables) 
                pruneEmptyElements(t);
             
            // release resources
            processedTables.clear();
        }
        catch (EncryptedDocumentException e)
        {
            throw new TableIOException(e);
        }
        finally {
            close();
        }
    }

    public Table parseActiveSheet() throws IOException
    {
        try
        {
            m_wb = WorkbookFactory.create(getInputStream());
            m_ssV = m_wb instanceof XSSFWorkbook ? SpreadsheetVersion.EXCEL2007 : SpreadsheetVersion.EXCEL97;     
            int asi = m_wb.getActiveSheetIndex();
            
            Table t = parseSheet(asi);
            
            // handle named ranges
            processNamedRegions();
            
            // handle equations
            applyDerivations();
            
            pruneEmptyElements(t);
            return t;
        }
        catch (EncryptedDocumentException e)
        {
            throw new TableIOException(e);
        }
        finally {
            close();
        }
    }
    
    protected Table parseSheet(int sheetNo) throws IOException
    {
        Set<Integer> excelEmptyRows = null;
        
        // create the table scaffold
        Table t = TableFactory.createTable(getTableContext());
        List<ParsedFormula> parsedFormulas = new ArrayList<ParsedFormula>();
        try
        {
            Sheet sheet = m_wb.getSheetAt(sheetNo);             
            String sheetName = trimString(sheet.getSheetName());
            if (sheetName != null)
                t.setLabel(sheet.getSheetName());
            
            // associate this sheet with its TMS table
            m_sheetTableMap.put(sheet, t);
            m_sheetParsedFormulaMap.put(sheet, parsedFormulas);

            // keep track of empty rows, we need to
            // account for them when we map spreadsheet rows
            // to tms table rows
            if (options().isIgnoreEmptyRows()) {
                if (m_excludedRowsMap == null)
                    m_excludedRowsMap = new HashMap<Sheet, Set<Integer>>();
                
                excelEmptyRows = new LinkedHashSet<Integer>();
                m_excludedRowsMap.put(sheet, excelEmptyRows);
            }
            
            // handle column headings
            int rowNum = 0;
            if (isColumnNames()) {
                Row eR = sheet.getRow(rowNum++);
                for (short i = 0; i < eR.getLastCellNum(); i++) {
                    if (i == 0 && isRowNames())
                        continue; // skip r1c1

                    Column tC= t.addColumn(); // add the TMS column
                    Cell eC = eR.getCell(i, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
                    Object cv = fetchCellValue(eC);
                    
                    if (options().isDescriptions()) {
                        String note = fetchCellComment(eC, true);
                        if (note != null)
                            tC.setDescription(note);
                    }

                    if (cv != null)                       
                        tC.setLabel(cv.toString());
                }
            }

            // handle row data
            Set<org.tms.api.Row> emptyRows = new HashSet<org.tms.api.Row>();
            while (rowNum <= sheet.getLastRowNum()) {
                Row eR = sheet.getRow(rowNum++);
                if (eR != null && eR.getPhysicalNumberOfCells() > 0) {
                    org.tms.api.Row tR = t.addRow();
                    org.tms.api.Column tC = t.getColumn(Access.First);
                    boolean rowIsEffectivelyNull = true;
                    for (short i = 0; i < eR.getLastCellNum(); i++) {
                        Cell eC = eR.getCell(i, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
                        Object cv = fetchCellValue(eC);
                        String note = fetchCellComment(eC, true);
                        if (i == 0 && isRowNames()) {
                            if (options().isDescriptions() && note != null) {
                                tR.setDescription(note);
                                rowIsEffectivelyNull = false;
                            }

                            if (cv != null) {                   
                                tR.setLabel(cv.toString());
                                rowIsEffectivelyNull = false;
                            }
                        }
                        else {
                            if (tC == null)
                                tC = t.addColumn();

                            if (eC != null || cv != null) {
                                org.tms.api.Cell tCell = t.getCell(tR, tC);
                                if (options().isDescriptions() && note != null) {
                                    tCell.setDescription(note);
                                    rowIsEffectivelyNull = false;
                                }

                                if (cv != null) {
                                    tCell.setCellValue(cv);
                                    rowIsEffectivelyNull = false;
                                }

                                // record presence of cell formula; we will process
                                // once all of table is imported
                                if (options().isDerivations() && eC.getCellType() == CellType.FORMULA) {
                                    ParsedFormula pf = processExcelFormula(sheet, sheetNo, eC, tCell);
                                    parsedFormulas.add(pf);
                                    rowIsEffectivelyNull = false;
                                }
                            }

                            // bump column
                            tC = t.getColumn(Access.Next);
                        }
                    }
                    
                    if (rowIsEffectivelyNull)
                        emptyRows.add(tR);
                }
                else {
                    if (options().isIgnoreEmptyRows()) 
                        excelEmptyRows.add(rowNum - 1);
                    else {
                        // excel row is empty and we don't want to ignore empty rows
                        // add a new row
                        t.addRow();
                    }
                }
            }

            if (options().isIgnoreEmptyRows() && !emptyRows.isEmpty()) {
                // save empty rows, we will delete them later           
                if (m_emptyRowsMap == null)
                    m_emptyRowsMap = new HashMap<Table, Set<org.tms.api.Row>>();
                m_emptyRowsMap.put(t, emptyRows);
            }
            
        }
        catch (EncryptedDocumentException e)
        {
            throw new TableIOException(e);
        }

        return t;
    }

    protected void recalculateAllFormulas()
    {
        if (m_ssV == SpreadsheetVersion.EXCEL2007)
            XSSFFormulaEvaluator.evaluateAllFormulaCells((XSSFWorkbook) m_wb);
        else
            HSSFFormulaEvaluator.evaluateAllFormulaCells(m_wb);
    }
    
    private void pruneEmptyElements(Table t)
    {
        // prune empty rows and columns
        if (options().isIgnoreEmptyRows() && m_emptyRowsMap != null) {
            Set<org.tms.api.Row> emptyRows = m_emptyRowsMap.get(t);
            if (emptyRows != null && !emptyRows.isEmpty())
                t.delete(emptyRows.toArray(new TableElement[] {}));
        }
        
        pruneEmptyColumns(t);
        
        // and finally, recalculate table
        t.recalculate();
    }

    private void processNamedRegions()
    {
        int numNamedRegions = m_wb.getNumberOfNames();
        if (numNamedRegions > 0) {            
            for (Name namedRegion : m_wb.getAllNames()) {
                if (namedRegion != null) {
                    Sheet sheet = m_wb.getSheet(namedRegion.getSheetName());
                    
                    // if we didn't create a table corresponding to this sheet,
                    // don't bother to process the named region
                    Table t = m_sheetTableMap.get(sheet);
                    if (t == null)
                        continue;
                    
                    // don't process deleted named ranges
                    if (!namedRegion.isDeleted()) {
                        // get the region name and encoded region reference
                        String name = namedRegion.getNameName();
                        String regionRef = namedRegion.getRefersToFormula();

                        int maxRows = sheet.getLastRowNum() + 1;
                        int maxCols = getNumColumns(sheet);

                        // use the helper classes AreaRef and CellReference to
                        // decode region reference
                        // Note: we cannot rely on aref.isSingleCell 
                        // also, AreaReference can not handle non-continuous regions
                        try {
                            AreaReference aref = new AreaReference(regionRef, m_ssV);
                            CellReference[] cRefs = aref.getAllReferencedCells();
    
                            if (isSingleCell(cRefs)) {
                                org.tms.api.Cell tCell = getSingleCell(sheet, cRefs, t);
                                if (tCell != null) {
                                    tCell.setLabel(name);
                                    m_namedTableElements.put(name, tCell);
                                }
                            }  
                            else {
                                TableElement te = createTableElementFromNamedRegion(sheet, maxRows, maxCols, cRefs, t, name);
                                if (te != null)
                                    m_namedTableElements.put(name, te);
                            }
                        }
                        catch (IllegalArgumentException e) {
                            // ignore areas we can't parse
                            // TODO: log error
                        }
                    }
                }
            }
        }
    }

    /**
     * For each Excel formula discovered, create a TMS-style infix formula
     * and cache it along with the TMS cell it is associated with
     */
    private void applyDerivations()
    {
        if (!options().isDerivations())
            return;

        for (List<ParsedFormula> parsedFormulas : m_sheetParsedFormulaMap.values()) {
            m_externalFuncRefStack = EquationStack.createOpStack();
            for (ParsedFormula pf : parsedFormulas) {
                try {
                    // create an equation stack and set the primary table
                    // to the destination of the parsedFormula;
                    // this will cause formulas to generate correct refs
                    EquationStack es = EquationStack.createPostfixStack(pf.getTable());
                    
                    for (Ptg eT : pf.getTokens()) {
                        Token tmsT = excelToken2TmsToken(pf, eT);
                        if (tmsT != null) {
                            // for a few Excel formulas,
                            // we need to swap the order of
                            // the operands
                        	Operator op = tmsT.getOperator();
                        	if (op != null) {
	                            if (sf_InvertedArgs.contains(op))
	                                es.reverse(op.numArgs());
	                            else if (sf_OmitArgs.containsKey(op)) {
	                            	int omittedArgs = sf_OmitArgs.get(op);
	                            	for (; omittedArgs > 0; omittedArgs--) {
	                            		es.pop();
	                            	}
	                            }
	                            else if ((tmsT instanceof ExcelToken) && ((ExcelToken)tmsT).numArgs() > op.numArgs()) {
	                            	for (int i = op.numArgs(); i < ((ExcelToken)tmsT).numArgs(); i++) {
	                            		es.push(tmsT);
	                            	}
	                            }
                        	}
                        	
                            es.push(tmsT);
                        }
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
            }
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
                
            case "NameXPxg":
                return createOperandToken((NameXPxg)eT, pf);
        }           
        
        // if we get here, we don't support this excel token
        throw new UnimplementedException(eT.getClass().getSimpleName());            
    }

    private Token createOperandToken(NameXPxg eT, ParsedFormula pf)
    {
        String funcName = trimString(eT.getNameName());
        if (funcName != null) {
            Operator op = sf_FunctionMap.get(funcName);
            if (op != null) {               
                m_externalFuncRefStack.push(op);
                return null;
            }
        }
        
        // if we get here, we don't support this excel token
        throw new UnimplementedException(String.format("%s->%s", eT.getClass().getSimpleName(), funcName));            
    }

    private Token createOperandToken(NamePtg eT, ParsedFormula pf)
    {
        if (m_namedTableElements != null) {
        	if (m_frWb == null)
        		m_frWb = m_wb instanceof HSSFWorkbook ? 
                    HSSFEvaluationWorkbook.create((HSSFWorkbook)m_wb) : XSSFEvaluationWorkbook.create((XSSFWorkbook)m_wb);

        	String rangeName = m_frWb.getNameText(eT);
            Name namedRange = m_wb.getName(rangeName);
            
            if (namedRange != null) {
                String refName = namedRange.getNameName();
                TableElement te = m_namedTableElements.get(refName);
                if (te != null) {
                    if (te instanceof org.tms.api.Cell)
                        return new Token(TokenType.CellRef, te);
                    else if (te instanceof Subset)
                        return new Token(TokenType.SubsetRef, te);
                    else if (te instanceof org.tms.api.Row)
                        return new Token(TokenType.RowRef, te);
                    else if (te instanceof org.tms.api.Column)
                        return new Token(TokenType.ColumnRef, te);
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
        Table t = pf.getTable();
        int rngColNo = eT.getFirstColumn();
        int rngRowNo = eT.getFirstRow();
        
        int numCols = eT.getLastColumn() - rngColNo + 1;
        int numRows = eT.getLastRow() - rngRowNo + 1;
        
        int eCCol = pf.getExcelCell().getColumnIndex();
        int eCRow = pf.getExcelCell().getRowIndex();
        
        if (numCols == 1) {
            if (rngColNo == eCCol && rangeIsColumn(rngRowNo, eT.getLastRow(), rngColNo, eT, pf))
                return new Token(TokenType.ColumnRef, getTmsColumn(t, rngColNo));
            else if (rngRowNo == 0 && numRows > pf.getSheet().getLastRowNum())
                return new Token(TokenType.ColumnRef, getTmsColumn(t, rngColNo));
        }
        
        if (numRows == 1) {
            if (rngRowNo == eCRow && rangeIsRow(rngColNo, eT.getLastColumn(), rngRowNo, eT, pf))
                return new Token(TokenType.RowRef, getTmsRow(t, pf.getSheet(), rngRowNo));
            else if (rngColNo == 0 && numCols > getNumColumns(pf.getSheet()))
                return new Token(TokenType.RowRef, getTmsRow(t, pf.getSheet(), rngRowNo));
        }
        
        // create subset, if needed
        String label = "excel_" + eT.toFormulaString();
        Subset subset = t.getSubset(Access.ByLabel, label);
        
        if (subset == null) {
            subset = t.addSubset(Access.ByLabel, label);
            for (int colIdx = rngColNo; colIdx <= eT.getLastColumn(); colIdx++) {
                subset.add(getTmsColumn(t, colIdx));
            }
            
            for (int rowIdx = rngRowNo; rowIdx <= eT.getLastRow(); rowIdx++) {
                subset.add(getTmsRow(t, pf.getSheet(), rowIdx));
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
        int firstEffectiveRow = 0 + (options().isColumnLabels() ? 1 : 0);
        int lastRow = sheet.getLastRowNum();
        
        for (int i = firstEffectiveRow; i < rng1stRowNo; i++) {
            Row r = sheet.getRow(i);
            Cell cell = r.getCell(colNo, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
            if (cell == null || cell.getCellType() != CellType.FORMULA)
                return false;
        }
        
        for (int i = rngLstRowNo + 1; i < lastRow; i++) {
            Row r = sheet.getRow(i);
            Cell cell = r.getCell(colNo, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
            if (cell == null || cell.getCellType() != CellType.FORMULA || !isSameRange(cell, rng))
                return false;
        }
        
        return true;
    }

    private boolean isSameRange(Cell cell, AreaPtgBase rng)
    {
        String rngAsString = rng.toFormulaString();
        return cell.getCellFormula().indexOf(rngAsString) > -1;
    }

    /**
     * If all non-range cells in the row are formuli, return true
     * @param colNo 
     */
    private boolean rangeIsRow(int rng1stColNo, int rngLstColNo, int rowNo, AreaPtgBase rng, ParsedFormula pF)
    {
        Sheet sheet = pF.getExcelCell().getSheet();
        int firstEffectiveCol = 0 + (options().isRowLabels() ? 1 : 0);
        Row row = sheet.getRow(rowNo);
        int lastCol = row.getLastCellNum();
        
        for (int i = firstEffectiveCol; i < rng1stColNo; i++) {
            Cell cell = row.getCell(i, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
            if (cell == null || cell.getCellType() != CellType.FORMULA)
                return false;
        }
        
        for (int i = rngLstColNo + 1; i < lastCol; i++) {
            if (isExcludedRow(sheet, i))
                continue;
            Cell cell = row.getCell(i, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
            if (cell == null || cell.getCellType() != CellType.FORMULA) 
                return false;
        }
        
        return true;
    }

    private boolean isExcludedRow(Sheet sheet, int rowNo)
    {
        if (sheet != null && m_excludedRowsMap != null) {
            Set<Integer> excludedRows = m_excludedRowsMap.get(sheet);
            if (excludedRows != null)
                return excludedRows.contains(rowNo);
        }
        
        return false;
    }

    private Token createOperandToken(RefPtgBase eT, ParsedFormula pf)
    {
        // determine tms-based indices of reference
        int rowRef = getTmsRowIdx(pf.getSheet(), eT.getRow());
        int colRef = getTmsColumnIdx(eT.getColumn());
        
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
                return new ExcelToken(op, eT.getNumberOfOperands()); 
            else if ("#external#".equals(funcName) && !m_externalFuncRefStack.isEmpty())
                return m_externalFuncRefStack.pop();
        }
        
        // if we get here, we don't support this excel token
        throw new UnimplementedException(String.format("%s->%s", eT.getClass().getSimpleName(), funcName));            
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

    private int getNumColumns(Sheet activeSheet)
    {
        // use cached value, if it exists
        if (m_sheetMaxColMap.containsKey(activeSheet))
            return m_sheetMaxColMap.get(activeSheet);
        
        int numCols = 0;
        Iterator<Row> rowIter = activeSheet.rowIterator();
        while(rowIter != null && rowIter.hasNext()) {
            Row r = rowIter.next();
            int lastCol = r.getLastCellNum();
            
            if (lastCol > numCols)
                numCols = lastCol;
        }
        
        // increment to get total column count
        numCols++;
        
        // and save for later
        m_sheetMaxColMap.put(activeSheet, numCols);
        
        return numCols;
    }

    private TableElement createTableElementFromNamedRegion(Sheet sheet, int maxRows, int maxCols, 
            CellReference[] cRefs, Table t, String label)
    {
        // iterate over cell references, abstracting rows and columns
        int totalRowCnt = 0;
        int totalColCnt = 0;
        Set<TableElement> tmsRows = new HashSet<TableElement>(maxRows);
        Set<TableElement> tmsCols = new HashSet<TableElement>(maxCols);
        
        for (CellReference cRef : cRefs) {           
            int excelRowNo = cRef.getRow();
            int excelColNo = cRef.getCol();

            if (excelColNo > -1 && excelColNo < maxCols) {
                TableElement col = getTmsColumn(t, excelColNo);
                if (col != null) {
                    if (tmsCols.add(col))
                        totalColCnt++;
                }
            }

            if (excelRowNo > -1 && excelRowNo < maxRows) {
                TableElement row = getTmsRow(t, sheet, excelRowNo);
                if (row != null) {
                    if (tmsRows.add(row))
                        totalRowCnt++;
                }
            }
        }
        
        // check if we have a solitary row/col, and if so, return that
        if (totalRowCnt == 0 && totalColCnt == 1)
            return tmsCols.toArray(new TableElement[] {})[0];
        
        if (totalRowCnt == 1 && totalColCnt == 0)
            return tmsRows.toArray(new TableElement[] {})[0];
        
        // create the subset success
        Subset s = t.addSubset(Access.ByLabel, label);
        
        // xls and xlsx formats differ in how they express "whole column" and "whole row"
        // ranges; if a subset contains all columns (or rows) in the table, then only add
        // the rows (or columns), as TMS handles the concept of "all" itself...

        if (totalColCnt > 0 && totalColCnt < t.getNumColumns())
            s.add(tmsCols.toArray(new TableElement[] {}));
        
        if (totalRowCnt > 0 && totalRowCnt < t.getNumRows())
            s.add(tmsRows.toArray(new TableElement[] {}));
        
        return s;
    }

    private org.tms.api.Cell getSingleCell(Sheet sheet, CellReference[] cRefs, Table t)
    {
        // first, get Excel row and column values; they will be zero-based
        int excelRowNo = cRefs[0].getRow();
        int excelColNo = cRefs[0].getCol();

        // now, return cell, correcting for 1-based TMS row/column indexes
        return t.getCell(getTmsRow(t, sheet, excelRowNo), getTmsColumn(t, excelColNo));
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
                (!options().isColumnLabels() || cRefs[0].getRow() > 0) &&
                (!options().isRowLabels() || cRefs[0].getCol() > 0);
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
        CellType cellType = eC.getCellType();
        switch(cellType) {
            case BOOLEAN:
                return eC.getBooleanCellValue();

            case NUMERIC:
                return eC.getNumericCellValue();

            case STRING:
                return eC.getRichStringCellValue().getString();

            case FORMULA:
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
            case "TRUE()":
                return true;

            case "FALSE":
            case "FALSE()":
                return false;

            case "RAND":
            case "RAND()":
                return Math.random();

            case "NOW":
            case "NOW()":
            case "TODAY":
            case "TODAY()":
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

    private int getTmsColumnIdx(int excelColNo)
    {
        return excelColNo + 1 - (options().isRowLabels() ? 1 : 0);
    }

    private org.tms.api.Column getTmsColumn(Table t, int excelColNo)
    {
        int idx = getTmsColumnIdx(excelColNo);        
        if (idx >= 1 && idx <= t.getNumColumns())
            return t.getColumn(idx);
        else
            return null;
    }

    private int getTmsRowIdx(Sheet sheet, int excelRowNo)
    {
        int idx = excelRowNo + 1 - (options().isColumnLabels() ? 1 : 0);
        if (options().isIgnoreEmptyRows() && m_excludedRowsMap != null)
            idx -= accountForMissingElements(sheet, excelRowNo);
        
        return idx;
    }
        
    private org.tms.api.Row getTmsRow(Table t, Sheet sheet, int excelRowNo)
    {
        int idx = getTmsRowIdx(sheet, excelRowNo);
        if (idx >= 1 && idx <= t.getNumRows())
            return t.getRow(idx);
        else
            return null;
    }

    private int accountForMissingElements(Sheet sheet, int excelRowNo)
    {
        int offset = 0;
        
        if (sheet != null && m_excludedRowsMap != null) {
            Set<Integer> emptyElemIndexes = m_excludedRowsMap.get(sheet);
            
            if (emptyElemIndexes != null) {
                for (Integer emptyIdx : emptyElemIndexes) {
                    if (excelRowNo > emptyIdx)
                        offset++;
                    else if (excelRowNo < emptyIdx)
                        break;
                }
            }
        }
        
        return offset;
    }

    private ParsedFormula processExcelFormula(Sheet sheet, int asi, Cell eC, org.tms.api.Cell tCell)
    {
    	// Initialize
    	if (m_fpWb == null)
            m_fpWb = m_wb instanceof HSSFWorkbook ? 
                    HSSFEvaluationWorkbook.create((HSSFWorkbook)m_wb) : XSSFEvaluationWorkbook.create((XSSFWorkbook)m_wb);

        // parse the formula
        String formula = eC.getCellFormula();     

        Ptg [] tokens = FormulaParser.parse(formula, m_fpWb, FormulaType.NAMEDRANGE, asi);
        return new ParsedFormula(eC, tCell, tokens);
    }
    
    /**
     * Helper class to convert Excel formulas to
     * TMS Derivations.
     * 
     * Records the TMS cell to which the Excel formula 
     * will apply, as well as the parsed POI tokens
     */
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
        
        public Sheet getSheet()
        {
            return m_excelCell.getSheet();
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
    
    class ExcelToken extends Token
    {
    	private int m_numArgs;
    	
    	public ExcelToken(Operator op, int numArgs)
    	{
    		super(op);
    		m_numArgs = numArgs;
    	}
    	
    	public int numArgs()
    	{
    		return m_numArgs;
    	}
    }
}
