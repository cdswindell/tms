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
                    ifs.push(tType, oper);
                    break;
            } // of switch tType
        }
        
        // if everything is ok, return the operator length
        return oper.getLabelLength();
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
                continue; /* digits are ok */
            }

            if ((c == '.') && !foundDP && !eWasLast) 
                foundDP = true;
            else if ((c == 'e') && !foundE) 
                eWasLast = foundE = true;
            else if ((c == 'E') && !foundE)
                eWasLast = foundE = true;
            else if (foundE && (c == '-') && eWasLast )
                eWasLast = false;
            else if (foundE && (c == '+') && eWasLast )
                eWasLast = true;
            else {
                sb.deleteCharAt(i);
                break;
            }
        } /* of for loop */

        // decode the number
        value = Double.parseDouble(sb.toString());
        
        ifs.push(TokenType.Operand, value);
        return i;

    } /* of TEQ_ParseNumber */
    
    private int parseLabel(char[] exprChars, int curPos, 
                           EquationStack ifs, Table table,
                           ParseResult pr)
    {
        // TODO Auto-generated method stub
        return 0;
    }

}
