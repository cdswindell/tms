package org.tms.teq;

import org.tms.api.Table;

public class InfixExpressionParser
{
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

    EquationStack getExpressionStack()
    {
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

    public ParseResult validateExpression(Table table)
    {
        if (table == null)
            table = m_table;
        
        return parseInfixExpression(table);
    }

    public ParseResult parseInfixExpression(Table table)
    {
        m_ifs = Token.createTokenStack();
        return parseInfixExpression(m_ifs, table);
    }
    
    public ParseResult parseInfixExpression(EquationStack ifs, Table table)
    {
        if (m_expr == null || (m_expr = m_expr.trim()).length() <= 0)
            return new ParseResult(ParserStatusCode.EmptyExpression);
        
        ParseResult pr = new ParseResult(m_expr);       
        
        int curPos = 0;
        int prevPos = 0;
        int exprLen = m_expr.length();
        int [] parenCnt = {0};        
        
        if (ifs == null)
            ifs = Token.createTokenStack();
        else
            ifs.clear();
        
        char [] exprChars = m_expr.toCharArray();
        TokenMapper tm = new TokenMapper(table != null ? table.getTableContext() : null);
        while (curPos < exprLen) {
            char c = exprChars[curPos];
            if (Character.isWhitespace(c)) {
                curPos++;
                continue;
            }
            
            prevPos = curPos;
            if (Character.isLetter(c))
                curPos += parseLabel(exprChars, curPos, ifs, table, pr);    
            
            else if (Character.isDigit(c))
                curPos += parseNumber(exprChars, curPos, ifs, table, pr);    
            
            else if ((c == '\"') || (c == '\''))
                curPos += parseText(exprChars, curPos, ifs);

            else
                curPos += parseOperator(exprChars, curPos, ifs, parenCnt, tm, table, pr);

            if (curPos <= prevPos) {
                if (pr.isSuccess())
                    pr.addIssue(m_expr, ParserStatusCode.InvalidExpression, curPos);
                return pr;
            }
        }
        
        return pr;
    }

    private int parseOperator(char[] exprChars, int curPos, EquationStack ifs, int[] parenCnt, 
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
                    if (pr != null)
                        pr.addIssue(ParserStatusCode.ParenMismatch, curPos);
                    return curPos;
                    
                case BinaryOp:
                    switch (oper) {
                        case PlusOper: // ignore
                            break;

                        case MinusOper:     /* negation operator */
                            ifs.push(Operator.NegOper);
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
        // TODO Auto-generated method stub
        return false;
    }

    private int parseText(char[] exprChars, int curPos, EquationStack ifs)
    {
        // TODO Auto-generated method stub
        return 0;
    }

    private int parseNumber(char[] exprChars, int curPos, EquationStack ifs, 
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
                pr.addIssue(ParserStatusCode.InvalidConstantLocation, curPos);
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
                           EquationStack ifs, Table table,
                           ParseResult pr)
    {
        // TODO Auto-generated method stub
        return 0;
    }
    
    public String toString()
    {
        return String.format("[ %s ]", m_expr != null ? m_expr : "<null>");
    }
<<<<<<< HEAD
=======

>>>>>>> branch 'master' of https://github.com/cdswindell/tms.git
}
