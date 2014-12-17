package org.tms.teq;

import java.util.LinkedHashSet;
import java.util.Set;

public class ParseResult
{
    private ParserStatusCode m_parserStatusCode;
    private String m_expression;
    private Set<ParseIssue> m_parseIssues;

    /** 
     * Default constructor, leaves ParseResult in the "success" state
     */
    ParseResult()
    {
        
    }
    
    ParseResult(ParserStatusCode status)
    {
        m_parserStatusCode = status;
    }
    
    ParseResult(String expr)
    {
        m_expression = expr;
        m_parserStatusCode = null;
    }
    
    ParseResult(String expr, ParserStatusCode status)
    {
        m_expression = expr;
        m_parserStatusCode = status;
    }
    
    public ParserStatusCode getParserStatusCode()
    {
        if (m_parserStatusCode == null)
            return ParserStatusCode.Success;
        else
            return m_parserStatusCode;
    }

    void setParserStatusCode(ParserStatusCode parserStatusCode)
    {
        m_parserStatusCode = parserStatusCode;
    }
    
    public String getExpression()
    {
        return m_expression;
    }
    
    public boolean isSuccess()
    {
        return getParserStatusCode().isSuccess();
    }
    public boolean isFailure()
    {
        return !isSuccess();
    }
    
    public ParseResult addIssue(ParserStatusCode status, int pos)
    {
        return addIssue(null, status, pos);
    }
    
    public ParseResult addIssue(String expr, ParserStatusCode status, int pos)
    {
        if (m_expression == null && expr != null)
            m_expression = expr;
        
        if (m_parseIssues == null)
            m_parseIssues = new LinkedHashSet<ParseIssue>();
        
        if (m_parserStatusCode == null)
            m_parserStatusCode = status;
        
        m_parseIssues.add(new ParseIssue(status, pos, null));
        
        return this;
    }
    
    public String toString()
    {
        return getParserStatusCode().toString();
    }
    
    public class ParseIssue
    {
        private ParserStatusCode m_issueStatusCode;
        private int m_occuredAtPos;
        private String m_term;
        
        public ParseIssue(ParserStatusCode status, int pos, String term)
        {
            m_issueStatusCode = status;
            m_occuredAtPos = pos;
            m_term = term != null ? term : 
                    (m_expression != null && m_expression.length() > pos ? m_expression.substring(pos) : null) ;
        }

        public int getOccuredAtPos()
        {
            return m_occuredAtPos;
        }

        public ParserStatusCode getIssueStatusCode()
        {
            return m_issueStatusCode;
        }

        public String getTerm()
        {
            return m_term;
        }
        
        public String toString()
        {
            return String.format("%s at position %d", getIssueStatusCode(), getOccuredAtPos());
        }
    }
}
