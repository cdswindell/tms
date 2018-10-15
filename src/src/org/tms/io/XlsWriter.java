package org.tms.io;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.SpreadsheetVersion;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.ClientAnchor;
import org.apache.poi.ss.usermodel.Comment;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Drawing;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.FormulaError;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Name;
import org.apache.poi.ss.usermodel.RichTextString;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.AreaReference;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.tms.api.Column;
import org.tms.api.Subset;
import org.tms.api.Table;
import org.tms.api.TableElement;
import org.tms.api.TableRowColumnElement;
import org.tms.api.derivables.Derivable;
import org.tms.api.derivables.Derivation;
import org.tms.api.derivables.Operator;
import org.tms.api.derivables.Token;
import org.tms.api.derivables.TokenType;
import org.tms.api.exceptions.UnimplementedException;
import org.tms.api.io.XLSOptions;
import org.tms.api.utils.AbstractOperator;
import org.tms.teq.BuiltinOperator;
import org.tms.teq.EquationStack;
import org.tms.teq.InfixExpressionParser;
import org.tms.teq.PostfixStackGenerator;
import org.tms.teq.StackType;
import org.tms.util.Tuple;

public class XlsWriter extends LabeledWriter<XLSOptions>
{
    static final Map<Operator, String> sf_tmsToExcelFunctionMap = new HashMap<Operator, String>();
    static final Map<BuiltinOperator, Operator> sf_builtInToExcelMap = new HashMap<BuiltinOperator, Operator>();
    static final Set<Operator> sf_specialOps = new HashSet<Operator>();
    
    static {
        for (Map.Entry<String, Operator> e : XlsReader.sf_FunctionMap.entrySet()) {
            Operator oper = e.getValue();
            String excelFunc = e.getKey().toLowerCase();
            
            if (!sf_tmsToExcelFunctionMap.containsKey(oper))
                sf_tmsToExcelFunctionMap.put(oper, excelFunc);
        }
        
        ExcelOp eOp = new ExcelOp(BuiltinOperator.AndOper, "and");
        sf_builtInToExcelMap.put((BuiltinOperator)eOp.getTmsOp(), eOp);
        
        eOp = new ExcelOp(BuiltinOperator.OrOper, "or");
        sf_builtInToExcelMap.put((BuiltinOperator)eOp.getTmsOp(), eOp);

        eOp = new ExcelOp(BuiltinOperator.NotOper, "not");
        sf_builtInToExcelMap.put((BuiltinOperator)eOp.getTmsOp(), eOp);

        eOp = new ExcelOp(BuiltinOperator.XorOper, "xor");
        sf_builtInToExcelMap.put((BuiltinOperator)eOp.getTmsOp(), eOp);
        
        eOp = new ExcelOp(BuiltinOperator.PowerOper, "power");
        sf_builtInToExcelMap.put((BuiltinOperator)eOp.getTmsOp(), eOp);
        
        eOp = new ExcelOp(BuiltinOperator.FactOper, "fact");
        sf_builtInToExcelMap.put((BuiltinOperator)eOp.getTmsOp(), eOp);
        
        eOp = new ExcelOp(BuiltinOperator.PlusOper, "&", TokenType.BinaryOp, String.class);
        sf_builtInToExcelMap.put((BuiltinOperator)eOp.getTmsOp(), eOp);
        
        eOp = new ExcelOp(BuiltinOperator.MultOper, "rept", String.class);
        sf_builtInToExcelMap.put((BuiltinOperator)eOp.getTmsOp(), eOp);
        
        eOp = new ExcelOp(BuiltinOperator.toStringOper, "", String.class );
        sf_builtInToExcelMap.put((BuiltinOperator)eOp.getTmsOp(), eOp);
        
        // some additional items
        sf_tmsToExcelFunctionMap.put(BuiltinOperator.SumOper, "sum");
        sf_tmsToExcelFunctionMap.put(BuiltinOperator.PercentOper, "%");
        
        sf_tmsToExcelFunctionMap.put(BuiltinOperator.PiOper, "pi()");
        sf_tmsToExcelFunctionMap.put(BuiltinOperator.RandOper, "rand()");
        sf_tmsToExcelFunctionMap.put(BuiltinOperator.TrueOper, "true()");
        sf_tmsToExcelFunctionMap.put(BuiltinOperator.FalseOper, "false()");
        sf_tmsToExcelFunctionMap.put(BuiltinOperator.EOper, String.valueOf(Math.E));
        sf_tmsToExcelFunctionMap.put(BuiltinOperator.ColumnIndexOper, "column()");
        sf_tmsToExcelFunctionMap.put(BuiltinOperator.RowIndexOper, "row()");
        
        sf_tmsToExcelFunctionMap.put(BuiltinOperator.EqOper, "=");
        sf_tmsToExcelFunctionMap.put(BuiltinOperator.NEqOper, "<>");
        sf_tmsToExcelFunctionMap.put(BuiltinOperator.GtEOper, ">=");
        sf_tmsToExcelFunctionMap.put(BuiltinOperator.LtEOper, "<=");
        sf_tmsToExcelFunctionMap.put(BuiltinOperator.GtOper, ">");
        sf_tmsToExcelFunctionMap.put(BuiltinOperator.LtOper, "<");
        
        sf_specialOps.add(BuiltinOperator.PlusOper);
        sf_specialOps.add(BuiltinOperator.MultOper);
        sf_specialOps.add(BuiltinOperator.MinusOper);
        sf_specialOps.add(BuiltinOperator.DivOper);
    }

    private Map<String, CellStyle> m_styleCache;
    private Map<Sheet, Map<TableRowColumnElement, Integer>> m_rowMap;
    private Map<Sheet, Map<TableRowColumnElement, Integer>> m_colMap;
    private Map<Cell, CachedDerivation> m_cachedDerivations;
    private Map<TableElement, String> m_cachedRangeRef;
    private Map<Derivation, EquationStack> m_infixCache;
    
    private CreationHelper m_wbHelper = null;
	private Workbook m_wb;

    public static void export(TableExportAdapter tea, OutputStream output, XLSOptions options) 
            throws IOException
    {
        XlsWriter writer = new XlsWriter(tea, output, options);
        writer.export();
    }

    private XlsWriter(TableExportAdapter tea, OutputStream out, XLSOptions options)
    {
        super(tea, out, options);        
        m_styleCache = new HashMap<String, CellStyle>();
        m_rowMap = new HashMap<Sheet, Map<TableRowColumnElement, Integer>>();
        m_colMap = new HashMap<Sheet, Map<TableRowColumnElement, Integer>>();
        m_cachedDerivations = new LinkedHashMap<Cell, CachedDerivation>();
        m_infixCache = new HashMap<Derivation, EquationStack>();
        m_cachedRangeRef = new HashMap<TableElement, String>();
    }

    @SuppressWarnings("resource")
	@Override
    protected void export() throws IOException
    {
    	try {
	        m_wb = options().isXlsXFormat() ? new XSSFWorkbook() : new HSSFWorkbook(); 
	        m_wbHelper = m_wb.getCreationHelper();
	        
	        // perform the export
	        export(getTable());
	        
	        // write the output stream
	        m_wb.write(getOutputStream());  
    	}
    	finally {
    		if (m_wb != null)
    			m_wb.close();
    	}
    }
    
    protected void export(Table t) throws IOException
    {
        String tableLabel = trimString(t.getLabel());
        Sheet sheet = tableLabel != null ? m_wb.createSheet(tableLabel) : m_wb.createSheet();

        int cellFontSize = options().getDefaultFontSize();
        int commentFontSize = options().getDefaultFontSize() - 3;
        int headerFontSize = options().getHeadingFontSize();
        
        int colWidthPx = options().getDefaultColumnWidth();
        int colWidth = colWidthPx > 0 ? (int)((colWidthPx - 5)/6.0) : 8;        
        sheet.setDefaultColumnWidth(colWidth);
        
        CellStyle cellStyle = getCachedCellStyle("default", m_wb, cellFontSize, false);
        CellStyle headingStyle = getCachedCellStyle("heading", m_wb, headerFontSize, false);
        
        // make the comment styles, we may or may not need them, but this 
        // saves us from having to cache the comment font size
        getCachedCellStyle("comment", m_wb, commentFontSize, false);
        getCachedCellStyle("author", m_wb, commentFontSize, true);
               
        int firstActiveRow = 0;
        int firstActiveCol = 0;
        
        int rowNum = 0;
        int maxExcelCol = 0;
        if (options().isColumnLabels()) {
            firstActiveRow = 1;
            Row headerRow = sheet.createRow(rowNum++);
            headerRow.setHeightInPoints(30);
            Cell headerCell = null;

            short colCnt = (short)0;
            if (options().isRowLabels()) {
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
            
            if ((colCnt - 1) > maxExcelCol)
                maxExcelCol = colCnt - 1;
            
            // set print headings
            sheet.setRepeatingRows(CellRangeAddress.valueOf("$1:$1"));
        }

        if (options().isRowLabels()) {
            firstActiveCol = 1;
            int rnColWidthPx = options().getRowLabelColumnWidth();
            int rnColWidth = rnColWidthPx > 0 ? (int)(0.5 + (((rnColWidthPx)/6.0) * 256)) : 10 * 256;        
            sheet.setColumnWidth(0, rnColWidth);
            
            // set print headings
            sheet.setRepeatingColumns(CellRangeAddress.valueOf("$A:$A"));
        }
        
        // Fill data cells
        boolean processedAllColumns = false;
        for (org.tms.api.Row tr : this.getActiveRows()) {
            short colCnt = 0;
            Row r = sheet.createRow(rowNum++);
            cacheRowAssociation(sheet, tr, r);
            if (options().isRowLabels()) {
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
                if (!processedAllColumns) {
                    sheet.setDefaultColumnStyle(colCnt, cellStyle);
                    cacheColAssociation(sheet, tc, colCnt);
                }
                
                org.tms.api.Cell tCell = t.isCellDefined(tr, tc) ? t.getCell(tr, tc) : null;
                if (tCell != null) {
                    Cell excelC  = r.createCell(colCnt);
                    if (tCell.isErrorValue()) 
                        excelC.setCellErrorValue(toExcelErrorValue(tCell));
                    else if (!tCell.isNull()) {
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
                    else 
                        excelC.setCellType(CellType.BLANK);
                    
                    String label = trimString(tCell.getLabel());
                    if (label != null) 
                        applyName(tCell, label, m_wb, sheet, excelC);
                    
                    if (options().isDescriptions()) {
                        String desc = trimString(tCell.getDescription());
                        if (desc != null) 
                            applyComment(excelC, desc);
                    }
                    
                    if (options().isDerivations()) {
                        Derivation deriv = getDerivation(tCell);
                        if (deriv != null) {
                            CachedDerivation cd = new CachedDerivation(tCell, deriv, excelC);
                            m_cachedDerivations.put(excelC, cd);
                        }
                    }
                }

                colCnt++;   
            } // of columns
            
            processedAllColumns = true;
            if ((colCnt - 1) > maxExcelCol)
                maxExcelCol = colCnt - 1;
        } // of rows
        
        // process TMS subsets
        processSubsets(t, m_wb, sheet, maxExcelCol);
        
        // process derivations
        processDerivations(t, m_wb, sheet, maxExcelCol);

        // Freeze pains        
        if (options().isRowLabels() && options().isColumnLabels())
            sheet.createFreezePane( 1, 1, 1, 1 );
        else if (options().isColumnLabels())
            sheet.createFreezePane( 0, 1, 0, 1 );
        else if (options().isRowLabels())
            sheet.createFreezePane( 1, 0, 1, 0 );

        // set active cell
        Row r = sheet.getRow(firstActiveRow);
        Cell activeCell = r.getCell(firstActiveCol);
        if (activeCell != null)
            activeCell.setAsActiveCell();        
    }

    private void processDerivations(Table t, Workbook wb, Sheet sheet, int maxExcelCol)
    {
       for (Map.Entry<Cell, CachedDerivation> e : m_cachedDerivations.entrySet()) {
           Cell eCell = e.getKey();
           CachedDerivation cd = e.getValue();
           
           String formula = trimString(derivationToFormula(wb, sheet, maxExcelCol, cd));
           if (formula != null)
               eCell.setCellFormula(formula);
       }       
    }

    private String derivationToFormula(Workbook wb, Sheet sheet, int maxCol, CachedDerivation cd)
    {
        try {
            EquationStack pfs = cd.getPostfixEquationStack();
            return equationStackToFormula(pfs, cd, wb, sheet, maxCol);
        }
        catch (Exception e) {
            return null;
        }
    }

    private String equationStackToFormula(EquationStack es, CachedDerivation cd, Workbook wb, Sheet sheet, int maxCol)
    {
        // translate tokens to Excel form
        EquationStack excelPfs = EquationStack.createPostfixStack(cd.getTmsTable());
        Iterator<Token> iter = es.descendingIterator();
        while (iter != null && iter.hasNext()) {
            Token t = iter.next();           
            TokenType tt = t.getTokenType();
            String eRef = null;
            
            switch (tt) {
                case RowRef:
                case CellRef:
                case ColumnRef:
                    eRef = xlateTmsRef(t, cd, sheet, maxCol);
                    if (eRef == null)
                        throw new UnimplementedException("Reference: " + t);
                    excelPfs.push(new Token(TokenType.Expression, eRef));
                    break;
                 
                case StatOp:
                case TransformOp:
                case BinaryFunc:
                case UnaryFunc:
                case GenericFunc:
                case BuiltIn:
                    if (!isValidExcelOp(t))
                        throw new UnimplementedException("Reference: " + t);
                    
                default:
                    excelPfs.push(t);
                    break;
            }
        }
        
        String excelFormula = excelPfs.toExpression(StackType.Infix);
        return excelFormula;
    }

    private boolean isValidExcelOp(Token t)
    {
        Operator op = t.getOperator();
        if (op != null) {
            if (op instanceof ExcelOp)
                return true;
            else
                return sf_tmsToExcelFunctionMap.containsKey(op);
        }
        else
            return false;
    }

    private String xlateTmsRef(Token t, CachedDerivation cd, Sheet sheet, int maxCol)
    {
        Cell cell = cd.getExcelCell();
        int targetRowNum = cell.getRowIndex();
        int targetColNum = cell.getColumnIndex();
        
        TableElement te = t.getReferenceValue();
        int xlatedRefRowNum = -1;
        int xlatedRefColNum = -1;
        
        if (te instanceof org.tms.api.Cell) {
            xlatedRefRowNum = m_rowMap.get(sheet).get(((org.tms.api.Cell)te).getRow());
            xlatedRefColNum = m_colMap.get(sheet).get(((org.tms.api.Cell)te).getColumn());
        }        
        else if (te instanceof org.tms.api.Row) {
            if (t instanceof ReferenceToken)
                return tmsRowAsRange(te, sheet, maxCol);
            xlatedRefRowNum = m_rowMap.get(sheet).get(te);
            xlatedRefColNum = targetColNum;
        }
        else if (te instanceof org.tms.api.Column) {
            if (t instanceof ReferenceToken)
                return tmsColAsRange(te, sheet);
            xlatedRefRowNum = targetRowNum;
            xlatedRefColNum = m_colMap.get(sheet).get(te);
        }  
        
        CellReference cr = new CellReference(xlatedRefRowNum, xlatedRefColNum, false, false);
        return cr.formatAsString();
    }

    private String tmsColAsRange(TableElement te, Sheet sheet)
    {
        // try from cache
        String ref = m_cachedRangeRef.get(te);
        if (ref == null) {
            Map<TableRowColumnElement, Integer> rowMap = m_rowMap.get(sheet);
            Map<TableRowColumnElement, Integer> colMap = m_colMap.get(sheet);

            Column c = (Column)te;
            Integer colIdx = colMap.get(c);
            if (colIdx == null)
                return null;

            Set<Derivable> affects = new HashSet<Derivable>(c.getAffects());
            List<Integer> rangeCells = new ArrayList<Integer>(te.getTable().getNumColumns());

            for (org.tms.api.Cell tCell : c.cells()) {
                if (affects.contains(tCell))
                    continue;

                Integer rowNum = rowMap.get(tCell.getRow());
                if (rowNum != null)
                    rangeCells.add(rowNum); 
            }

            if (rangeCells.isEmpty())
                return null;

            // we need the references to be ascending
            Collections.sort(rangeCells);

            // now build range formula; it may not be continuous
            Integer firstIdx = null;
            Integer prevIdx = null;
            StringBuffer sb = new StringBuffer();
            boolean isContinuous = true;
            for (int i : rangeCells) {
                if (firstIdx == null)
                    firstIdx = i;
                else {
                    if (i > prevIdx + 1) {
                        if (!isContinuous)
                            sb.append(',');
                        sb.append(makeAreaRef(firstIdx, colIdx, prevIdx, colIdx, false).formatAsString());
                        firstIdx = i;
                        isContinuous = false;
                    }
                }

                // always save the last element idx, it is used to look for gaps
                prevIdx = i;
            }

            // write out last reference
            if (!isContinuous)
                sb.append(',');
            sb.append(makeAreaRef(firstIdx, colIdx, prevIdx, colIdx, false).formatAsString());        

            ref = sb.toString();
            m_cachedRangeRef.put(te, ref);    
        }
        
        return ref;
    }

    private String tmsRowAsRange(TableElement te, Sheet sheet, int maxCol)
    {
        String ref = m_cachedRangeRef.get(te);
        if (ref == null) {
            Map<TableRowColumnElement, Integer> rowMap = m_rowMap.get(sheet);
            Map<TableRowColumnElement, Integer> colMap = m_colMap.get(sheet);

            org.tms.api.Row r = (org.tms.api.Row)te;
            Integer rowIdx = rowMap.get(r);
            if (rowIdx == null)
                return null;

            Set<Derivable> affects = new HashSet<Derivable>(r.getAffects());
            List<Integer> rangeCells = new ArrayList<Integer>(te.getTable().getNumRows());

            for (org.tms.api.Cell tCell : r.cells()) {
                if (affects.contains(tCell))
                    continue;

                Integer colNum = colMap.get(tCell.getColumn());
                if (colNum != null)
                    rangeCells.add(colNum); 
            }

            if (rangeCells.isEmpty())
                return null;

            // we need the references to be ascending
            Collections.sort(rangeCells);

            // now build range formula; it may not be continuous
            Integer firstIdx = null;
            Integer prevIdx = null;
            StringBuffer sb = new StringBuffer();
            boolean isContinuous = true;
            for (int i : rangeCells) {
                if (firstIdx == null)
                    firstIdx = i;
                else {
                    if (i > prevIdx + 1) {
                        if (!isContinuous)
                            sb.append(',');
                        sb.append(makeAreaRef(rowIdx, firstIdx, rowIdx, prevIdx, false).formatAsString());
                        firstIdx = i;
                        isContinuous = false;
                    }
                }

                // always save the last element idx, it is used to look for gaps
                prevIdx = i;
            }

            // write out last reference
            if (!isContinuous)
                sb.append(',');
            sb.append(makeAreaRef(rowIdx, firstIdx, rowIdx, prevIdx, false).formatAsString());

            ref = sb.toString();
            m_cachedRangeRef.put(te, ref);    
        }
        
        return ref;
    }

    private AreaReference makeAreaRef(int trIdx, int lcIdx, int brIdx, int rcIdx, boolean isRelative)
    {
        CellReference topLeft = new CellReference(trIdx, lcIdx, isRelative, isRelative);
        CellReference bottomRight = new CellReference(brIdx, rcIdx, isRelative, isRelative);
        
        AreaReference ar = new AreaReference(topLeft, bottomRight, getExcelVersion());
        return ar;
    }

    private void processSubsets(Table t, Workbook wb, Sheet sheet, int maxExcelCol)
    {
        // We only handle at the moment continuous ranges, eg, col 1 - 3
        // or col 2-5, row 3 - 7
        int subsetIdx = 0;
        for (Subset s : t.subsets()) {
            subsetIdx++;
            AreaReference ar = xlateSubsetToAreaRef(s, sheet, maxExcelCol);
            if (ar != null) {
                String label = trimString(s.getLabel());
                if (label == null)
                    label = String.format("TMS_Subset_%d", subsetIdx);
                
                Name namedRange = wb.createName();
                namedRange.setNameName(toExcelName(label));
                
                StringBuffer sb = new StringBuffer();
                sb.append(applySheetName(sheet));
                sb.append('!');
                sb.append(ar.formatAsString());
    
                namedRange.setRefersToFormula(sb.toString());
            }
        }        
    }

    private AreaReference xlateSubsetToAreaRef(Subset s, Sheet sheet, int maxExcelCol)
    {
        int numRows = s.getNumRows();
        int numCols = s.getNumColumns();
        boolean processable = numRows > 0 || numCols > 0;
        if (processable) {
            // now check for contiguous range
            Tuple<TableRowColumnElement> colBounds = null;
            if (numCols > 1) {
                Map<TableRowColumnElement, Integer> usedColsMap = m_colMap.get(sheet);
                if (usedColsMap == null)
                    return null;                
                
                List<org.tms.api.Column> subsetCols = s.getColumns();                
                colBounds = isContinuousElems(subsetCols, usedColsMap);
                if (colBounds == null)
                    numCols = 0;
                else if (colBounds.getSecondElement() == null)
                    return null; // indicates non-contiguous region
            }
            
            Tuple<TableRowColumnElement> rowBounds = null;
            if (numRows > 1) {
                Map<TableRowColumnElement, Integer> usedRowsMap = m_rowMap.get(sheet);
                if (usedRowsMap == null)
                    return null;                
                
                List<org.tms.api.Row> subsetRows = s.getRows();                
                rowBounds = isContinuousElems(subsetRows, usedRowsMap);
                if (rowBounds == null)
                    numRows = 0;
                else if (rowBounds.getSecondElement() == null)
                    return null; // indicates non-contiguous region
            }
                       
            // repeat calculation, as we may have found that subset consisted of 
            // only excluded rows and cols
            if (rowBounds != null || colBounds != null) {
                Map<TableRowColumnElement, Integer> rowMap = m_rowMap.get(sheet);
                Map<TableRowColumnElement, Integer> colMap = m_colMap.get(sheet);
                
                int topLeftRowNum = rowBounds != null ? rowMap.get(rowBounds.getFirstElement()) : options().isColumnLabels() ? 1 : 0;
                int topLeftColNum = colBounds != null ? colMap.get(colBounds.getFirstElement()) : options().isRowLabels() ? 1 : 0;
                CellReference topLeft = new CellReference(topLeftRowNum, topLeftColNum, true, true);

                
                int bottomRightRowNum = rowBounds != null ? rowMap.get(rowBounds.getSecondElement()) : sheet.getLastRowNum();
                int bottomRightColNum = colBounds != null ? colMap.get(colBounds.getSecondElement()) : maxExcelCol;
                CellReference bottomRight = new CellReference(bottomRightRowNum, bottomRightColNum, true, true);
                
                AreaReference ar = new AreaReference(topLeft, bottomRight, getExcelVersion());
                return ar;
            }
        }
        
        return null;
    }

    private Tuple<TableRowColumnElement> isContinuousElems(List<? extends TableRowColumnElement> subsetElems, 
                                 Map<TableRowColumnElement, ? extends Object> usedMap)
    {
        // Columns have to be in ascending order; use Java 8
        // closure syntax to specify index comparator
        Collections.sort(subsetElems, 
                (TableRowColumnElement c1, TableRowColumnElement c2) -> (c1.getIndex() > c2.getIndex() ? 1 : c1.getIndex() < c2.getIndex() ? -1 : 0));
        int lastElemIdx = 0;
        boolean isRow = false;
        TableRowColumnElement firstElem = null;
        TableRowColumnElement lastElem = null;
        for (TableRowColumnElement te : subsetElems) {
            if (usedMap.containsKey(te)) {
                if (firstElem == null) {
                    firstElem = te;
                    
                    if (te instanceof org.tms.api.Row)
                        isRow = true;
                }
            }
            
            if (lastElemIdx == 0) 
                lastElemIdx = te.getIndex();
            else {
                // is there a break in continuity?
                if (te.getIndex() > lastElemIdx + 1) {
                    for (int i = lastElemIdx + 1; i < te.getIndex(); i++) {
                        TableRowColumnElement excluded = isRow ? getTable().getRow(i) : getTable().getColumn(i);
                        if (te == null || usedMap.containsKey(excluded))
                            return new Tuple<TableRowColumnElement>(firstElem, null);
                    }
                }
                
                lastElemIdx = te.getIndex();
                lastElem = te;
            }
        }
        
        if (firstElem != null && lastElem != null)
            return new Tuple<TableRowColumnElement>(firstElem, lastElem);
        else
            return null;
    }

    private void applyName(org.tms.api.Cell tCell, String label, Workbook wb, Sheet sheet, Cell excelC)
    {
        CellReference cr = new CellReference(excelC.getRowIndex(), excelC.getColumnIndex(), true, true) ;
        
        Name namedCell = wb.createName();
        namedCell.setNameName(toExcelName(label));
        
        StringBuffer sb = new StringBuffer();
        sb.append(applySheetName(sheet));
        sb.append('!');
        
        sb.append(cr.formatAsString()); // area reference
        namedCell.setRefersToFormula(sb.toString());      
    }

    private StringBuffer applySheetName(Sheet sheet)
    {
        StringBuffer sb = new StringBuffer();
        String sn = sheet.getSheetName();
        boolean snNeedsQuote = sn.indexOf(' ') > -1 || sn.indexOf("'") > -1;
        
        if (snNeedsQuote)
            sb.append("'");
        sb.append(sn);
        if (snNeedsQuote)
            sb.append("'");
        
        return sb;
    }

    private void applyComment(Cell excelC, String comment)
    {
        comment = trimString(comment);
        if (comment != null) {
            Sheet sheet = excelC.getSheet();
            Drawing<?> dp = sheet.createDrawingPatriarch();
            
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
            int commentFontIdx  = this.getCachedCellFontIndex("comment");
            Font commentFont = m_wb.getFontAt(commentFontIdx);
            str.applyFont(commentFont);
            
            if (authorLen > -1) {
                int authorFontIdx  = this.getCachedCellFontIndex("author");
                Font authorFont = m_wb.getFontAt(authorFontIdx);
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
        Map<TableRowColumnElement, Integer> rowMap = m_rowMap.get(sheet);
        if (rowMap == null) {
            rowMap = new HashMap<TableRowColumnElement, Integer>(tmsR.getTable().getNumRows());
            m_rowMap.put(sheet,  rowMap);
        }
        
        rowMap.put(tmsR, excelR.getRowNum());
    }

    private void cacheColAssociation(Sheet sheet, org.tms.api.Column tmsC, int excelC)
    {
        Map<TableRowColumnElement, Integer> colMap = m_colMap.get(sheet);
        if (colMap == null) {
            colMap = new HashMap<TableRowColumnElement, Integer>(tmsC.getTable().getNumColumns());
            m_colMap.put(sheet,  colMap);
        }
        
        colMap.put(tmsC, excelC);
    }

    private int getCachedCellFontIndex(String styleName)
    {
        CellStyle cs = m_styleCache.get(styleName);
        if (cs != null)
            return cs.getFontIndexAsInt();
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
        
        headingStyle.setAlignment(HorizontalAlignment.CENTER); 
        headingStyle.setVerticalAlignment(VerticalAlignment.CENTER); 
        headingStyle.setFillForegroundColor(IndexedColors.GREY_50_PERCENT.getIndex());
        headingStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND); 
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
        cellStyle.setVerticalAlignment(VerticalAlignment.CENTER); 
        
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
    
    /**
     * Translate EMS names into allowable Excel names
     * (translate any non alpha or digit character to "_")
     * @param label
     * @return
     */
    private String toExcelName(String label) 
    {
        StringBuffer sb = new StringBuffer();
        
        boolean isFirstChar = true;
        for (char c : label.toCharArray()) {
            if (isFirstChar) {
                // first character has to be a letter, _, or \
                if (isValidExcelNameChar(c) && (Character.isDigit(c) || c == '.'))
                    sb.append('_');
            }
            
            if (isValidExcelNameChar(c))
                sb.append(c);
            else
                sb.append('_');
            
            isFirstChar = false;
        }
        
        return sb.toString();
    }
    
    private boolean isValidExcelNameChar(char c)
    {
        return Character.isLetterOrDigit(c) || c == '.' || c == '_' || c == '\\';
    }
    
    private SpreadsheetVersion getExcelVersion()
    {
    	if (options().isXlsXFormat())
    		return SpreadsheetVersion.EXCEL2007;
    	else if(options().isXlsFormat())
    		return SpreadsheetVersion.EXCEL97;
    	else 
    		return null;
    }
    
    class CachedDerivation
    {
        private org.tms.api.Cell m_tmsCell;
        private Cell m_excelCell;
        private Derivation m_derivation;
        
        CachedDerivation(org.tms.api.Cell tmsCell, Derivation deriv, Cell excelCell)
        {
            m_tmsCell = tmsCell;
            m_derivation = deriv;
            m_excelCell = excelCell;
        }

        public EquationStack getPostfixEquationStack()
        {
            EquationStack es = XlsWriter.this.m_infixCache.get(getDerivation());
            if (es == null) {
                String exp = getDerivation().getExpression();
                
                InfixExpressionParser iep = new InfixExpressionParser(exp, getTmsTable()); 
                PostfixStackGenerator psg = new PostfixStackGenerator(iep);
                EquationStack pfs = psg.getPostfixStack();
                
                // traverse the postfix stack and translate operators
                // that need to be modified to work with excel
                Token [] tokens = pfs.toArray(new Token[] {});
                for (int i = tokens.length - 1; i >= 0; i--) {
                    Token t = tokens[i];
                    Operator op = t.getOperator();
                    if (op != null && op != BuiltinOperator.NOP) {
                        Operator eOp = sf_specialOps.contains(op) ? null : sf_builtInToExcelMap.get(op);
                        if (eOp != null) {
                            t.setOperator(eOp);
                            t.setTokenType(eOp.getTokenType());
                        }
                        
                        // replace token with ExcelOpToken
                        // this will allow us to map
                        // function names into excel space
                        if (sf_specialOps.contains(op)) {
                            if (op == BuiltinOperator.PlusOper) {
                                // check for string concatenation
                                if (tokens[i + 1].isEvaluatesToString() || tokens[i + 2].isEvaluatesToString()) {
                                    eOp = sf_builtInToExcelMap.get(op);
                                    t.setOperator(eOp);
                                    t.setTokenType(eOp.getTokenType());
                                }
                            }
                            else if (op == BuiltinOperator.MultOper) {
                                if (tokens[i + 2].isEvaluatesToString()) {
                                    eOp = sf_builtInToExcelMap.get(op);
                                    t.setOperator(eOp);
                                    t.setTokenType(eOp.getTokenType());
                                }
                            }
                        }
                        else {
                            tokens[i] = new ExcelOpToken(t);
                        
                            // do some processing for stat ops
                            if (t.hasReferenceArg()) {
                                int numArgs = t.getOperator().numArgs();
                                for (int argIdx = 1; argIdx <= numArgs; argIdx++) {
                                    if (tokens[i + argIdx].isReference())
                                        tokens[i + argIdx] = new ReferenceToken(tokens[i + argIdx]);
                                }
                            }
                        }
                    }
                }
                
                // Save the modified postfix stack               
                es = EquationStack.createPostfixStack(getTmsTable());
                for (Token t : tokens) {
                    es.add(t);
                }
                
                XlsWriter.this.m_infixCache.put(getDerivation(), es);
            }
            
            return es;
        }

        org.tms.api.Cell getTmsCell()
        {
            return m_tmsCell;
        }

        Table getTmsTable()
        {
            return m_tmsCell.getTable();
        }

        Cell getExcelCell()
        {
            return m_excelCell;
        }
        
        Derivation getDerivation()
        {
            return m_derivation;
        }
    }
    
    static class ReferenceToken extends Token
    {
        ReferenceToken(Token t) 
        {
            super(t);
        }
    }
    
    static class ExcelOpToken extends Token
    {
        private Operator m_excelOp;
        
        ExcelOpToken(Token t) 
        {
            super(t);
        }
        
        @Override
        public String getLabel()
        {
            Operator op = getOperator();
            if (op instanceof ExcelOp)
                return getOperator().getLabel();
            else if (sf_tmsToExcelFunctionMap.containsKey(op))
                return sf_tmsToExcelFunctionMap.get(op).toLowerCase();
            else
                return super.getLabel();
        }
        
        @Override
        public Operator getOperator()
        {
            if (m_excelOp == null) {
                Operator superOp = super.getOperator();
                if (sf_tmsToExcelFunctionMap.containsKey(super.getOperator()))
                    m_excelOp = new ExcelOp(superOp, sf_tmsToExcelFunctionMap.get(superOp).toLowerCase());
                else
                    m_excelOp = superOp;
            }
            
            return m_excelOp;
        }
    }
    
    static class ExcelOp extends AbstractOperator
    {
        private Operator m_tmsOp;
        private TokenType m_tokenType;
        
        ExcelOp(Operator op)
        {
        	this(op, op.getLabel());
        }
        
        ExcelOp(Operator op, String label)
        {
            this(op, label, null, op.getResultType());
         }
        
        ExcelOp(Operator op, String label, Class<?> resultType)
        {
            this(op, label, null, resultType);
        }
        
        ExcelOp(Operator op, String label, TokenType tt, Class<?> resultType)
        {
        	super(label, op.getArgTypes(), resultType != null ? resultType : op.getResultType() );
        	m_tmsOp = op;
        	m_tokenType = tt;
        }
        
        ExcelOp(Operator op, String label, TokenType tt)
        {
            this(op, label);
            m_tokenType = tt;
        }
        
        Operator getTmsOp()
        {
            return m_tmsOp;
        }
        
        @Override
        public TokenType getTokenType()
        {
            if (m_tokenType != null)
                return m_tokenType;
            
            switch (m_tmsOp.getTokenType()) {
                case BinaryOp:
                    return TokenType.BinaryFunc;
                    
                case UnaryOp:
                case UnaryTrailingOp:
                    return TokenType.UnaryFunc;
                    
                default:
                    return m_tmsOp.getTokenType();
            }
        }

        @Override
        public Token evaluate(Token... args)
        {
            return null;
        }        
    }
}
