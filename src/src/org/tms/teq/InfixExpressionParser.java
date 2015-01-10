package org.tms.teq;

import java.util.Iterator;

import org.tms.api.Operator;
import org.tms.api.Table;
import org.tms.api.exceptions.InvalidExpressionException;

public class InfixExpressionParser
{
	private static final String sf_TABLE_REF = "::" ;
	
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
    
    EquationStack getInfixStack()
    {
        if (m_ifs == null) {
            ParseResult pr = parseInfixExpression();
            if (pr != null && pr.isFailure()) 
                throw new InvalidExpressionException(pr);
        }
        
        return m_ifs;
    }

    public String parsedInfixExpression()
    {
        return m_ifs != null ? m_ifs.toExpression() : null;
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
        m_ifs = EquationStack.createInfixStack();
        return parseInfixExpression(m_ifs, m_table);
    }
    
    protected ParseResult parseInfixExpression(Table table)
    {
        m_ifs = EquationStack.createInfixStack();
        return parseInfixExpression(m_ifs, table);
    }
    
    protected ParseResult parseInfixExpression(EquationStack ifs, Table table)
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
            if (Character.isLetter(c)) {
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
        
        return pr;
    }

    private int parseSimpleOperator(char[] exprChars, int curPos, EquationStack ifs, int[] parenCnt, 
                              TokenMapper tm, Table table, ParseResult pr)
    {
        Token t = tm.lookUpToken(exprChars[curPos]);
        if (t == null) {
            if (pr != null)
                pr.addIssue(ParserStatusCode.NoSuchOperator, curPos);
            return curPos;
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
                    return curPos;
                    
                case BinaryOp:
                    switch (oper.getBuiltinOperator()) {
                        case PlusOper: // ignore
                            break;

                        case MinusOper:     /* negation operator */
                            ifs.push(BuiltinOperator.NegOper);
                            break;

                        default:
                            if (pr != null)
                                pr.addIssue(ParserStatusCode.InvalidOperatorLocation, curPos);
                            return curPos;
                    }
                    break;
                
                case Comma:
                    if (pr != null)
                        pr.addIssue(ParserStatusCode.InvalidCommaLocation, curPos);
                    return curPos;
                
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
                    if (isOkForComma(ifs))
                        ifs.push(tType, oper);
                    else {
                        if (pr != null)
                            pr.addIssue(ParserStatusCode.InvalidCommaLocation, curPos);
                        return curPos;
                    }
                    break;
                    
                case UnaryOp:
                    ifs.push(tType, oper);
                    break;
                    
                default:
                    if (pr != null)
                        pr.addIssue(ParserStatusCode.InvalidExpression, curPos);
                    return curPos;
            } // of switch
        } // if not leading
        
        if (tType == TokenType.LeftParen)
            parenCnt[0]++;
        else if (tType == TokenType.RightParen) {
            parenCnt[0]--;
            if (parenCnt[0] < 0) {
                if (pr != null)
                    pr.addIssue(ParserStatusCode.ParenMismatch, curPos);
                return curPos;
            }
        }
        
        // if everything is ok, return the operator length
        return t.getLabelLength();
    }

    private boolean isOkForComma(EquationStack ifs)
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
                case Operand:
                case BuiltIn:
                case Constant:
                case Variable: 
                    if (parenCnt == 0)
                        argCnt++;
                    break;
                
                case RangeOp: // handled as expression arg
                case StatOp:  // handled as expression arg
                case BinaryStatOp:  // handled as expression arg
                    break;
                    
                case Comma: // skip these elements
                    if (!processedFirstToken)
                        commaIsOk = false;
                    break;
                    
                case GenericFunc:
                case BinaryFunc:
                    if (parenCnt == 0) {
                        foundFunc = true;
                        if (oper != null && argCnt < oper.numArgs()) 
                            return true; // comma is ok   
                        else
                            commaIsOk = false;
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
        for (int i = curPos; i < maxPos; i++) {
        	char c = exprChars[i];
        	if (Character.isLetterOrDigit(c) || c == '#' || c == '_' || c == ':') {
        		sb.append(c);
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
        	
        	// handle Row/Column/Range references
        	if (oper.getBuiltinOperator() == BuiltinOperator.Column) {
        		additionalCharsParsed = parseColumnReference(exprChars, curPos + charsParsed, table, t);
        		if (additionalCharsParsed > 0 && (value = t.getValue()) != null) 
        			charsParsed += additionalCharsParsed;
        		else {
        			if (pr != null)
        				pr.addIssue(ParserStatusCode.InvalidColumnReferemce, curPos + charsParsed);
        			return 0;
        		}
        	}
        }
        else {
        	// TODO: handle other tricks in teq_parse
        }
        
        if (ifs.isLeading()) {
        	if (tt == TokenType.BinaryOp) {
        		if (pr != null)
        			pr.addIssue(ParserStatusCode.InvalidOperatorLocation, curPos, tt.toString());
        		return 0;
        	}
        	
        	ifs.push(tt, oper, value);
        }
        else {
        	if (tt != TokenType.BinaryOp) {
        		if (pr != null)
        			pr.addIssue(ParserStatusCode.InvalidOperatorLocation, curPos, tt.toString());
        		return 0;
        	}
        	
        	ifs.push(tt, oper, value);        	
        }
        
        return charsParsed;
    }
    
    private int parseColumnReference(char[] exprChars, int curPos, Table table, Token t) 
    {
		// TODO Auto-generated method stub
		return 0;
	}

	public String toString()
    {
        return String.format("[ %s ]", m_expr != null ? m_expr : "<null>");
    }
}
