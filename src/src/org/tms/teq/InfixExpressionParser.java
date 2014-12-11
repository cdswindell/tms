package org.tms.teq;

import java.util.Deque;

import org.tms.api.Table;

public class InfixExpressionParser
{
    public ParseResult validateExpression(String expr, Table table)
    {
        Deque<Token> ifs = Token.createTokenStack();
        return parseInfixExpression(expr, ifs, table);
    }

    public ParseResult parseInfixExpression(String expr, Deque<Token> ifs, Table table)
    {
        if (expr == null || (expr = expr.trim()).length() <= 0)
            return new ParseResult(ParserStatusCode.EmptyExpression);
        
        ParseResult pr = new ParseResult();
        
        
        int curPos = 0;
        int prevPos = 0;
        int exprLen = expr.length();
        int parenCnt = 0;        
        
        char [] exprChars = expr.toCharArray();
        while (curPos < exprLen) {
            char c = exprChars[curPos];
            if (Character.isWhitespace(c)) {
                curPos++;
                continue;
            }
            
            prevPos = curPos;
            if (Character.isLetter(c))
                curPos += parseLabel(exprChars, curPos, ifs, table, pr);    
            
            if (curPos <= prevPos) {
                pr.addIssue(expr, ParserStatusCode.InvalidExpression, curPos);
                return pr;
            }
        }
        
        return pr;
    }

    private int parseLabel(char[] exprChars, int curPos, 
                           Deque<Token> ifs, Table table,
                           ParseResult pr)
    {
        // TODO Auto-generated method stub
        return 0;
    }

}
