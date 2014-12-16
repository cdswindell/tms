package org.tms.teq;

import org.tms.api.Table;
import org.tms.api.exceptions.InvalidExpressionException;

public class PostfixExpressionParser
{
    private EquationStack m_ifs;
    private EquationStack m_pfs;
    private Table m_table;
    
    public PostfixExpressionParser(String infixExpr, Table table)
    {
        InfixExpressionParser ifp = new InfixExpressionParser(infixExpr);
        ParseResult pr = ifp.parseInfixExpression(table);
        if (pr != null && pr.isFailure())
            throw new InvalidExpressionException(pr);
        
        m_ifs = ifp.getInfixStack();
        m_table = table;
    }

    public PostfixExpressionParser(InfixExpressionParser ife)
    {
       if (ife.getInfixStack() == null || ife.getInfixStack().isEmpty()) {
           ParseResult pr = ife.parseInfixExpression();
           if (pr != null && pr.isFailure())
               throw new InvalidExpressionException(pr);
       }
       
       m_ifs = ife.getInfixStack();
       m_table = ife.getTable();
    }

    public PostfixExpressionParser(EquationStack ifs, Table table)
    {
        m_ifs = ifs;
        m_table = table;
    }
    
    public Table getTable()
    {
        return m_table;
    }
    
    public EquationStack getInfixStack()
    {
        return m_ifs;
    }
    
    public EquationStack getPostfixStack()
    {
        if (m_pfs == null) {
            
        }
        
        return m_pfs;
    }
    
    protected ParseResult convertInfixToPostfix()
    {
        m_pfs = new EquationStack();
        return convertInfixToPostfix(m_ifs, m_pfs);
    }
    
    protected ParseResult convertInfixToPostfix(EquationStack ifs, EquationStack pfs)
    {
        ParseResult pr = null;
        
        return pr;
    }
}
