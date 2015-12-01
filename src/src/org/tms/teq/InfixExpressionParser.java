package org.tms.teq;

import java.util.Iterator;

import org.tms.api.Access;
import org.tms.api.Cell;
import org.tms.api.Column;
import org.tms.api.Row;
import org.tms.api.Subset;
import org.tms.api.Table;
import org.tms.api.derivables.Derivable;
import org.tms.api.derivables.Operator;
import org.tms.api.derivables.Token;
import org.tms.api.derivables.TokenMapper;
import org.tms.api.derivables.TokenType;
import org.tms.teq.exceptions.InvalidExpressionExceptionImpl;

public class InfixExpressionParser
{
	public static final String sf_TABLE_REF = "::" ;
	
    private String m_expr;
    private Table m_table;
    private EquationStack m_ifs;
    
    public InfixExpressionParser(String expr)
    {
        this(expr, null);
    }
    
    public InfixExpressionParser(String expr, Table table)
    {
        m_expr = expr;
        m_table = table;
    }    

    public Table getTable()
    {
        return m_table;
    }
    
    public String getExpression()
    {
        return m_expr;
    }
    
    public EquationStack getInfixStack()
    {
        if (m_ifs == null) {
            ParseResult pr = parseInfixExpression();
            if (pr != null && pr.isFailure()) 
                throw new InvalidExpressionExceptionImpl(pr);
        }
        
        return m_ifs;
    }

    public String parsedInfixExpression()
    {
        return m_ifs != null ? m_ifs.toExpression(true, getTable()) : null;
    }
    
    public ParseResult validateExpression()
    {
        return validateExpression(m_table);
    }

    protected ParseResult validateExpression(Table table)
    {
        if (table == null)
            table = m_table;
        
        return parseInfixExpression(table);
    }

    public ParseResult parseInfixExpression()
    {
        m_ifs = EquationStack.createInfixStack(m_table);
        return parseInfixExpression(m_ifs, m_table);
    }
    
    public ParseResult parseInfixExpression(Derivable target)
    {
        m_ifs = EquationStack.createInfixStack(target.getTable());
        return parseInfixExpression(m_ifs, m_table, target);
    }
    
    protected ParseResult parseInfixExpression(Table table)
    {
        m_ifs = EquationStack.createInfixStack(table);
        return parseInfixExpression(m_ifs, table);
    }
    
    protected ParseResult parseInfixExpression(EquationStack ifs, Table table)
    {
        return parseInfixExpression(ifs, table, (Derivable)null);
    }
    
    protected ParseResult parseInfixExpression(EquationStack ifs, Table table, Derivable target)
    {
        if (m_expr == null || (m_expr = m_expr.trim()).length() <= 0)
            return new ParseResult(ParserStatusCode.EmptyExpression);
        
        ParseResult pr = new ParseResult(m_expr);       
        
        int curPos = 0;
        int prevPos = 0;
        int exprLen = m_expr.length();
        int [] parenCnt = {0};        
        
        if (ifs == null)
            ifs = EquationStack.createInfixStack();
        else
            ifs.clear();
        
        char [] exprChars = m_expr.toCharArray();
        TokenMapper tm = TokenMapper.fetchTokenMapper(table);
        ifs.setTokenMapper(tm);
        boolean parsingLabel = false;
        while (curPos < exprLen) {
            parsingLabel = false;
            char c = exprChars[curPos];
            if (Character.isWhitespace(c)) {
                curPos++;
                continue;
            }
            
            prevPos = curPos;
            if (Character.isLetter(c) || c == '<' || c == '>' || c == '!' || 
                                         c == '&' || c == '|' || 
                                         (c == '=' && ((curPos+1) < exprLen) && exprChars[curPos+1] == '=')) 
            {
                parsingLabel = true;
                curPos += parseLabel(exprChars, curPos, ifs, tm, table, pr);    
            }
            
            else if (Character.isDigit(c) || c == '.')
                curPos += parseNumber(exprChars, curPos, ifs, table, pr);    
            
            else if ((c == '\"') || (c == '\''))
                curPos += parseText(exprChars, curPos, ifs, pr);

            else
                curPos += parseSimpleOperator(exprChars, curPos, ifs, parenCnt, tm, table, pr);

            if (curPos <= prevPos) {
                if (pr != null) {
                    if (parsingLabel)
                        pr.addIssue(m_expr, ParserStatusCode.NoSuchOperator, curPos);
                    else
                        pr.addIssue(m_expr, ParserStatusCode.InvalidExpression, curPos);
                }
                
                return pr;
            }
        }
        
        if (pr.isSuccess())
            validateSemantics(ifs, target, pr);
        
        return pr;
    }

    private void validateSemantics(EquationStack ifs, Derivable target, ParseResult pr)
    {
        // check that the target of the derivation is appropriate
        // only Row, Column, and Cell can be targets of a derivation
        // Cells do not support transform functions
        if (target != null && target instanceof Cell) {
            Iterator<Token> tIter = ifs.descendingIterator();
            while(tIter != null && tIter.hasNext()) {
                Token t = tIter.next();
                if (!t.isFunction()) continue;
                
                TokenType tt = t.getTokenType();
                if (tt.isTransform()) {
                    pr.addIssue(ParserStatusCode.InvalidFunctionTarget, t.getOperator().getLabel());
                    return;
                }
            }
        }
    }
    
    private int parseSimpleOperator(char[] exprChars, int curPos, EquationStack ifs, int[] parenCnt, 
                                    TokenMapper tm, Table table, ParseResult pr)
    {
        Token t = tm.lookUpToken(exprChars[curPos]);
        if (t == null) {
            if (pr != null)
                pr.addIssue(ParserStatusCode.NoSuchOperator, curPos);
            return 0;
        }
        
        TokenType tType = t.getTokenType();
        Operator oper = t.getOperator();
        
        // if leading expression, check for valid operators
        if (ifs.isLeading()) {
            switch (tType) {
                case RightParen:
                    // the construct x() is allowed
                    Token tmpT = ifs.peek();
                    if (t != null && tmpT.isLeftParen()) {
                        ifs.push(tType, oper);
                        break;
                    }
                    
                    if (pr != null)
                        pr.addIssue(ParserStatusCode.ParenMismatch, curPos);
                    return 0;
                    
                case BinaryOp:
                    if (oper instanceof BuiltinOperator) {
                        BuiltinOperator bOper = (BuiltinOperator)oper;
                        if (bOper == BuiltinOperator.PlusOper)
                            break;
                        else if (bOper == BuiltinOperator.MinusOper) {
                            ifs.push(BuiltinOperator.NegOper);
                            break;
                        }
                    }
                    
                    // otherwise, this is an invalid location for the oper
                    if (pr != null)
                        pr.addIssue(ParserStatusCode.InvalidOperatorLocation, curPos);
                    return 0;
                
                case Comma:
                    if (pr != null)
                        pr.addIssue(ParserStatusCode.InvalidCommaLocation, curPos);
                    return 0;
                
                default:
                    // valid token detected, push it onto the stack
                    ifs.push(tType, oper);
                    break;
            } // of switch tType
        } // if if leading
        else {
            switch (tType) {
                case RightParen:
                case BinaryOp:
                    ifs.push(tType, oper);
                    break;
                    
                case Comma:
                    if (isOkForComma(ifs, pr, curPos))
                        ifs.push(tType, oper);
                    else {
                        if (pr != null)
                            pr.addIssue(ParserStatusCode.InvalidCommaLocation, curPos);
                        return 0;
                    }
                    break;
                    
                case UnaryTrailingOp:
                    ifs.push(tType, oper);
                    break;
                    
                case LeftParen:
                    Token tmpT = ifs.peek();
                    if (t != null && tmpT.isBuiltIn()) {
                        ifs.push(tType, oper);
                        break;
                    }
                    
                default:
                    if (pr != null)
                        pr.addIssue(ParserStatusCode.InvalidExpression, curPos);
                    return 0;
            } // of switch
        } // if not leading
        
        if (tType == TokenType.LeftParen)
            parenCnt[0]++;
        else if (tType == TokenType.RightParen) {
            parenCnt[0]--;
            if (parenCnt[0] < 0) {
                if (pr != null)
                    pr.addIssue(ParserStatusCode.ParenMismatch, curPos);
                return 0;
            }
        }
        
        // if everything is ok, return the operator length
        return t.getLabelLength();
    }

    private boolean isOkForComma(EquationStack ifs, ParseResult pr, int curPos)
    {
        /* 
         * Commas are used to separate arguments in function calls.
         * The number of arguments a function takes is determined
         * by the numArgs method on the Operator element of the 
         * function token. In the simple case, when we encounter a 
         * comma, we count the arguments we've encountered since the
         * containing function call, and as long as the number of 
         * commas is less than the arg count, we are ok.
         * 
         * Complicating matters is, of course, that individual args
         * can themselves be self-contained expressions. We will impose 
         * the restriction that expression arguments must be contained 
         * within parenthesis.
         * 
         * the following code implements this rule checker 
         */

        if (ifs.isEmpty())
            return false;
        
        int argCnt = 0;
        int parenCnt = 0;
        boolean foundFuncOpenParen = false;
        boolean foundFunc = false;
        
        boolean commaIsOk = true;
        boolean processedFirstToken = false;
        
        Iterator<Token> iter = ifs.iterator();
        while (iter != null && iter.hasNext()) {
            Token t = iter.next();
            TokenType tt = t.getTokenType();
            Operator oper = t.getOperator();
            
            if (oper == BuiltinOperator.NegOper)
                continue; // allow these almost anywhere
            
            switch (tt) {
                case RightParen:
                    if (foundFuncOpenParen) 
                        commaIsOk = false;
                    else
                        parenCnt++;
                    break;
                    
                case LeftParen:
                    if (parenCnt > 0) {
                        parenCnt--;
                        if (parenCnt == 0)
                            argCnt++;
                    }
                    else
                        foundFuncOpenParen = true;
                    break;
                    
                case CellRef:
                case ColumnRef:
                case RowRef:
                case SubsetRef:
                case Operand:
                case BuiltIn:
                case Constant:
                case Variable: 
                    if (parenCnt == 0)
                        argCnt++;
                    break;
                
                case Comma: // skip these elements
                    if (!processedFirstToken)
                        commaIsOk = false;
                    break;
                
                case UnaryFunc:
                case StatOp:
                case TransformOp:
                case GenericFunc:
                case BinaryFunc:
                case BinaryOp:
                    if (parenCnt == 0) {
                        foundFunc = true;
                        if (oper != null && argCnt < oper.numArgs()) 
                            return true; // comma is ok   
                        else {
                            commaIsOk = false;
                            if (pr != null)
                                pr.addIssue(ParserStatusCode.ArgumentCountMismatch, curPos);
                        }
                    }
                    break;
                    
                default:
                    if (parenCnt == 0)
                        commaIsOk = false;
                    break;
            }
            
            processedFirstToken = true;
            
            if (!commaIsOk)
                break;
        }
        
        // if we get here and did not find a function, comma is misplaced
        if (!foundFunc)
            commaIsOk = false;
        
        // return final status
        return commaIsOk;
    }

    private int parseText(char[] exprChars, int curPos, EquationStack ifs, ParseResult pr)
    {
        char delim = exprChars[curPos];
        StringBuffer text = new StringBuffer();
        
        int exprLen = exprChars.length;
        // special case boundary test; if quote is last character of expression, this is an error
        if (curPos == exprLen - 1) {
            pr.addIssue(ParserStatusCode.SingletonQuote, curPos);
            return 0;
        }
          
        boolean foundTrailingDelim = false;
        for (int i = curPos + 1; i < exprLen; i++) {
            char c = exprChars[i];
            if (c == delim) {
                foundTrailingDelim = true;
                break;       
            }
            
            text.append(c);
        }
        
        if (!foundTrailingDelim) {
            pr.addIssue(ParserStatusCode.SingletonQuote, curPos);
            return 0;
        }
        
        // add the text to the stack as an operand
        ifs.push(TokenType.Operand, text.toString());
        
        // return the number of consumed characters
        return text.length() + 2;
    }

    private int parseNumber(char[] exprChars, 
    						int curPos, 
    						EquationStack ifs, 
                            Table table,
                            ParseResult pr)
    {
        int i;
        double   value = 0.0;
        boolean foundDP   = false;
        boolean foundE    = false;
        boolean foundP    = false;
        boolean eWasLast = false;

        /* if we are not at a leading expression, punt */
        if (!ifs.isLeading()) {
            if (pr != null)
                pr.addIssue(ParserStatusCode.InvalidOperandLocation, curPos);
            return 0;
        }

        /* parse the number */
        StringBuffer sb = new StringBuffer();
        int exprLen = exprChars.length;
        for (i = curPos; i < exprLen; i++) {
            char c = exprChars[i];
            sb.append(c);
            if (Character.isDigit(c)) {
                eWasLast = false;
                
                // if we found the Exponent signal (e or E), indicate we now have digits 
                // for the exponent power
                if (foundE)
                    foundP = true;
                continue; /* digits are ok */
            }

            if ((c == '.') && !foundDP && !foundE) 
                foundDP = true; // decimal not allowed after exponent
            else if ((c == 'e') && !foundE) 
                eWasLast = foundE = true;
            else if ((c == 'E') && !foundE)
                eWasLast = foundE = true;
            else if (foundE && eWasLast && (c == '-'))
                eWasLast = false;
            else if (foundE && eWasLast && (c == '+'))
                eWasLast = false;
            else {
                sb.deleteCharAt(sb.length() - 1);
                break;
            }
        } /* of for loop */

        // check for invalid exponent
        if (foundE && !foundP) {
            if (pr != null)
                pr.addIssue(ParserStatusCode.InvalidNumericExpression, curPos);
            return curPos;
        }
            
        // decode the number
        try {
            value = Double.parseDouble(sb.toString());   
        }
        catch (NumberFormatException e) {
            if (pr != null)
                pr.addIssue(ParserStatusCode.InvalidNumericExpression, curPos);
            return curPos;
        }
        
        ifs.push(value);
        return sb.length();

    } /* of TEQ_ParseNumber */
    
    private int parseLabel(char[] exprChars, int curPos, 
                           EquationStack ifs, TokenMapper tm, Table table,
                           ParseResult pr)
    {
    	int charsParsed = 0; // assume the worst
    	
        // find a "word", as defined by letters, digits, #'s, _'s, and :'s     	
    	StringBuffer sb = new StringBuffer();
    	int maxPos = exprChars.length;
    	boolean foundLabel = false;
    	int labelCharsParsed = 0;
    	boolean firstCharLetterOrDigit = false;
        for (int i = curPos; i < maxPos; i++) {
        	char c = exprChars[i];
        	// need to allow for !=, <>, <=, & >=
        	if (Character.isLetterOrDigit(c) || c == '#' || c == '_' || c == ':'|| c == '.') {
        		sb.append(c);
        		
                if (labelCharsParsed == 0)
                    firstCharLetterOrDigit = true;
                
        		labelCharsParsed++;        		
        		continue;
        	}
        	// logical & comparison operators support, =, ==,  >, <, >=, <=, &&, ||
        	else if ((labelCharsParsed == 0 && (c == '<' || c == '>' || c == '!' || c == '&') || c == '|' || c == '=') ||
        	         (labelCharsParsed == 1 && ((!firstCharLetterOrDigit && c == '=') || c == '&' || c == '|'))) 
        	{
                sb.append(c);
                labelCharsParsed++;
                
                // special case a few comparison operators              
                switch (sb.toString()) {
                    case "==":
                    case "!=":
                    case "<>":
                    case "<=":
                    case ">=":
                    case "&&":
                    case "||":
                        foundLabel = true;
                        break;
                    
                    default:
                        break;
                }
                if (foundLabel)
                    break;
                continue;
        	}
        	else
        		break;
        }

        /* check if the word is an operator */
        Token t = tm.lookUpToken(sb.toString());
        TokenType tt = null;
        Operator oper = null;
        Object value = null;
        if (t != null) {
        	charsParsed = sb.length();
        	tt = t.getTokenType();
        	oper = t.getOperator();
        	int additionalCharsParsed = 0;
        	
        	// handle Row/Column/Subset references
        	Token tmpToken = new Token(tt, oper);        	
            if (tt == TokenType.ColumnRef) {
                additionalCharsParsed = parseColumnReference(exprChars, curPos + charsParsed, table, tmpToken);
                if (additionalCharsParsed > 0 && (value = tmpToken.getValue()) != null) 
                    charsParsed += additionalCharsParsed;
                else {
                    if (pr != null)
                        pr.addIssue(ParserStatusCode.InvalidColumnReference, curPos + charsParsed);
                    return 0;
                }
            }
            else if (tt == TokenType.RowRef) {
                additionalCharsParsed = parseRowReference(exprChars, curPos + charsParsed, table, tmpToken);
                if (additionalCharsParsed > 0 && (value = tmpToken.getValue()) != null) 
                    charsParsed += additionalCharsParsed;
                else {
                    if (pr != null)
                        pr.addIssue(ParserStatusCode.InvalidRowReference, curPos + charsParsed);
                    return 0;
                }
            }
            else if (tt == TokenType.SubsetRef) {
                additionalCharsParsed = parseSubsetReference(exprChars, curPos + charsParsed, table, tmpToken);
                if (additionalCharsParsed > 0 && (value = tmpToken.getValue()) != null) 
                    charsParsed += additionalCharsParsed;
                else {
                    if (pr != null)
                        pr.addIssue(ParserStatusCode.InvalidSubsetReference, curPos + charsParsed);
                    return 0;
                }
            }
            else if (tt == TokenType.TableRef) {
                additionalCharsParsed = parseTableReference(exprChars, curPos + charsParsed, table, tmpToken);
                if (additionalCharsParsed > 0 && (value = tmpToken.getValue()) != null) 
                    charsParsed += additionalCharsParsed;
                else {
                    if (pr != null)
                        pr.addIssue(ParserStatusCode.InvalidTableReference, curPos + charsParsed);
                    return 0;
                }
            }
            else if (tt == TokenType.CellRef) {
                additionalCharsParsed = parseCellReference(exprChars, curPos + charsParsed, table, tmpToken);
                if (additionalCharsParsed > 0 && (value = tmpToken.getValue()) != null) 
                    charsParsed += additionalCharsParsed;
                else {
                    if (pr != null)
                        pr.addIssue(ParserStatusCode.InvalidCellReference, curPos + charsParsed);
                    return 0;
                }
            }
        }
        else {
            /*
             * token not found, do a few more tricks to decode the label string
             */
            
            // look for named cell, in this table or in
            // a referenced table
            if (table != null) {
                String label = sb.toString();
                Cell cell = table.getCell(Access.ByLabel, label);
                
                int tblRefIdx = 0;
                if (cell == null && table.getTableContext() != null && 
                        (tblRefIdx = label.indexOf(sf_TABLE_REF)) > 0 &&
                        (tblRefIdx + sf_TABLE_REF.length() < label.length())) 
                {
                    String tableName = label.substring(0, tblRefIdx);
                    Table refTable = table.getTableContext().getTable(Access.ByLabel, tableName);
                    if (refTable != null) {
                        label = label.substring(tblRefIdx + sf_TABLE_REF.length());
                        cell = refTable.getCell(Access.ByLabel, label);
                    }
                }
                
                if (cell != null) {
                    value = cell;
                    tt = TokenType.CellRef;
                    oper = BuiltinOperator.NOP;
                    charsParsed = sb.length();
                }
            }

            // if still no luck, assume a term in the constants table
            if (tt == null && value == null) {
                
            }
            
        	// TODO: handle other tricks in teq_parse
        }
        
        if (ifs.isLeading()) {
        	if (tt == TokenType.BinaryOp ) {
        		if (pr != null)
        			pr.addIssue(ParserStatusCode.InvalidOperatorLocation, curPos, tt.toString());
        		return 0;
        	}
        	
        	ifs.push(tt, oper, value);
        }
        else {
        	if (!(tt == TokenType.BinaryOp || (tt == TokenType.UnaryTrailingOp)) ) {
        		if (pr != null)
        			pr.addIssue(ParserStatusCode.InvalidOperatorLocation, curPos, tt.toString());
        		return 0;
        	}
        	
        	ifs.push(tt, oper, value);        	
        }
        
        return charsParsed;
    }
    
    private int parseTableReference(char[] exprChars, int curPos, Table table, Token t)
    {
        ElementReference er = parseElementReference(exprChars, curPos);
        if (er.foundToken()) {
            Table refTable = null;
            String label = er.getLabel();
            if (table.getTableContext() != null)
                refTable = table.getTableContext().getTable(Access.ByLabel, label);
            
            // if we found a table, save it in the token and return the consumed chars
            if (refTable != null) {
                t.setValue(refTable);
                return er.getCharsParsed();
            }
        }
        
        // failure
        return 0;
    }

    private int parseCellReference(char[] exprChars, int curPos, Table table, Token t)
    {
        ElementReference er = parseElementReference(exprChars, curPos);
        if (er.foundToken()) {
            Cell cell = null;
            String label = er.getLabel();
            cell = table.getCell(Access.ByLabel, label);
            
            int tblRefIdx = 0;
            if (cell == null && table.getTableContext() != null && 
                    (tblRefIdx = label.indexOf(sf_TABLE_REF)) > 0 &&
                    (tblRefIdx + sf_TABLE_REF.length() < label.length())) 
            {
                String tableName = label.substring(0, tblRefIdx);
                Table refTable = table.getTableContext().getTable(Access.ByLabel, tableName);
                if (refTable != null) {
                    label = label.substring(tblRefIdx + sf_TABLE_REF.length());
                    cell = refTable.getCell(Access.ByLabel, label);
                }
            }
            
            // if we found a cell, save it in the token and return the consumed chars
            if (cell != null) {
                t.setValue(cell);
                return er.getCharsParsed();
            }
        }
        
        // failure
        return 0;
    }

    private int parseColumnReference(char[] exprChars, int curPos, Table table, Token t) 
    {
        ElementReference er = parseElementReference(exprChars, curPos);
        if (er.foundToken()) {
            Column col = null;
            if (er.isIndex()) 
                col = table.getColumn(Access.ByIndex, er.getIndex());
            else {
                String label = er.getLabel();
                col = table.getColumn(Access.ByLabel, label);
                
                int tblRefIdx = 0;
                if (col == null && table.getTableContext() != null && 
                        (tblRefIdx = label.indexOf(sf_TABLE_REF)) > 0 &&
                        (tblRefIdx + sf_TABLE_REF.length() < label.length())) 
                {
                    String tableName = label.substring(0, tblRefIdx);
                    Table refTable = table.getTableContext().getTable(Access.ByLabel, tableName);
                    if (refTable != null) {
                        label = label.substring(tblRefIdx + sf_TABLE_REF.length());
                        col = refTable.getColumn(Access.ByLabel, label);
                        
                        // One last attempt; is the label a column index?
                        int colIdx = -1;
                        if (col == null && (colIdx = labelToIdx(label)) > 0)
                            col = refTable.getColumn(colIdx);
                    }
                }
            }
            
            // if we found a column, save it in the token and return the consumed chars
            if (col != null) {
                t.setValue(col);
                return er.getCharsParsed();
            }
        }
        
        // failure
        return 0;
    }

    private int labelToIdx(String label)
    {
        try {
            return Integer.parseInt(label.trim());
        }
        catch (Exception e) {
            return 0;
        }
    }

    private int parseRowReference(char[] exprChars, int curPos, Table table, Token t) 
    {
        ElementReference er = parseElementReference(exprChars, curPos);
        if (er.foundToken()) {
            Row row = null;
            if (er.isIndex()) 
                row = table.getRow(Access.ByIndex, er.getIndex());
            else {
                String label = er.getLabel();
                row = table.getRow(Access.ByLabel, label);
                
                int tblRefIdx = 0;
                if (row == null && table.getTableContext() != null && 
                        (tblRefIdx = label.indexOf(sf_TABLE_REF)) > 0 &&
                        (tblRefIdx + sf_TABLE_REF.length() < label.length())) 
                {
                    String tableName = label.substring(0, tblRefIdx);
                    Table refTable = table.getTableContext().getTable(Access.ByLabel, tableName);
                    if (refTable != null) {
                        label = label.substring(tblRefIdx + sf_TABLE_REF.length());
                        row = refTable.getRow(Access.ByLabel, label);
                        
                        // One last attempt; is the label a column index?
                        int rowIdx = -1;
                        if (row == null && (rowIdx = labelToIdx(label)) > 0)
                            row = refTable.getRow(rowIdx);
                    }
                }
            }
            
            // if we found a row, save it in the token and return the consumed chars
            if (row != null) {
                t.setValue(row);
                return er.getCharsParsed();
            }
        }
        
        // failure
        return 0;
    }

    private int parseSubsetReference(char[] exprChars, int curPos, Table table, Token t) 
    {
        ElementReference er = parseElementReference(exprChars, curPos);
        if (er.foundToken()) {
            Subset subset = null;
            String label = er.getLabel();
            subset = table.getSubset(Access.ByLabel, label);
            
            int tblRefIdx = 0;
            if (subset == null && table.getTableContext() != null && 
                    (tblRefIdx = label.indexOf(sf_TABLE_REF)) > 0 &&
                    (tblRefIdx + sf_TABLE_REF.length() < label.length())) 
            {
                String tableName = label.substring(0, tblRefIdx);
                Table refTable = table.getTableContext().getTable(Access.ByLabel, tableName);
                if (refTable != null) {
                    label = label.substring(tblRefIdx + sf_TABLE_REF.length());
                    subset = refTable.getSubset(Access.ByLabel, label);
                }
            }
            
            // if we found a subset, save it in the token and return the consumed chars
            if (subset != null) {
                t.setValue(subset);
                return er.getCharsParsed();
            }
        }
        
        // failure
        return 0;
    }

	private ElementReference parseElementReference(char[] exprChars, int curPos) 
	{
    	int charsParsed = 0; // assume the worst
    	
        /*
         * Find a parsable token, as defined as:
         *	-- an integer
         *  -- an unquoted word
         *  -- a quoted string
         */
    	StringBuffer sb = new StringBuffer();
    	int maxPos = exprChars.length;
    	char quoteChar = 0;
    	boolean foundToken = false;
    	boolean foundNonDigit = false;
    	boolean foundDigit = false;
    	for (int i = curPos; i < maxPos; i++) {
    		char c = exprChars[i];
    		charsParsed++;
    		
    		// handle whitespace
    		if (Character.isWhitespace(c)) {
    			if (quoteChar != 0) // include whitespace in labels
    				;
    			else if (foundToken) // if we've found a token, white space terminates it
    				break;
    			else
    				continue;
    		}
    		
    		// found terminating quote?
    		if (quoteChar != 0 && c == quoteChar) 
    			break;
    		
            // comma or right paren also terminate
    		if ((c == ')' || c == ',') && quoteChar == 0 ) {
    		    // don't consume character
    		    charsParsed--;
    		    break;
    		}
    		
    		// handle parsing of comparitor operators, so we can do things like
    		// col 12!=col 6
    		if (foundDigit && !foundNonDigit && (c == '=' || c == '!' || c == '<' || c == '>')) {
                charsParsed--;
                break;
    		}
    		
    		// found leading quote?
    		if (!foundToken && quoteChar == 0 && (c == '"' || c == '\'')) {
    			quoteChar = c;
    			continue;
    		}
    		
    		if (!Character.isDigit(c))
    			foundNonDigit = true;
    		else 
    		    foundDigit = true;
    		
    		// add the token character to the buffer
    		foundToken = true;
    		sb.append(c);
    	}
    	
    	// if all characters are digits, parse them
    	String label = sb.toString().trim();
    	int idx = 0;
    	if (quoteChar == 0 && !foundNonDigit)
    		idx = Integer.valueOf(label);
    	
    	ElementReference er = new ElementReference(foundToken ? charsParsed : 0, label, idx);
    	
		return er;
	}

	public String toString()
    {
        return String.format("[ %s ]", m_expr != null ? m_expr : "<null>");
    }
	
	private static class ElementReference
	{
		private int m_charsParsed;
		private int m_index;
		private String m_label;
		
		public ElementReference(int charsParsed, String label, int idx)
		{
			m_charsParsed = charsParsed;
			m_label = label;
			m_index = idx;
		}
		
		public boolean foundToken()
		{
			return m_charsParsed > 0;
		}
		
		public int getCharsParsed()
		{
			return m_charsParsed;
		}
		
		public int getIndex()
		{
			return m_index;
		}
		
		public boolean isIndex()
		{
			return m_index > 0;
		}
		
		public String getLabel()
		{
			return m_label;
		}
	}
}
