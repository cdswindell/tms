package org.tms.teq;

import java.util.LinkedHashSet;
import java.util.Set;

import org.tms.api.BaseElement;
import org.tms.api.Column;
import org.tms.api.Derivable;
import org.tms.api.Row;
import org.tms.api.Table;
import org.tms.api.TableProperty;
import org.tms.api.exceptions.InvalidExpressionException;
import org.tms.api.exceptions.ReadOnlyException;

public class Derivation  
{

    public static Derivation create(String expr, Derivable elem)
    {
        // parse the expression
        Table t = elem.getTable();
        InfixExpressionParser ifParser = new InfixExpressionParser(expr, t);
        
        ParseResult pr = ifParser.parseInfixExpression();
        if (pr != null && pr.isFailure())
            throw new InvalidExpressionException(pr);

        // otherwise, harvest the infix stack
        Derivation deriv = new Derivation();
        deriv.m_ifs = ifParser.getInfixStack();
        deriv.m_parsed = true;
        
        // convert infix to postfix
        PostfixStackGenerator pfg = new PostfixStackGenerator(deriv.m_ifs, t);
        pr =  pfg.convertInfixToPostfix();
        if (pr != null && pr.isFailure())
            throw new InvalidExpressionException(pr);
        
        // harvest the postfix stack
        deriv.m_pfs = pfg.getPostfixStack();
        deriv.m_converted = true;
        
        // create an evaluator
        PostfixStackEvaluator pfe = new PostfixStackEvaluator(deriv.m_pfs, t);
        deriv.m_pfe = pfe;
        
        // finally, retain the derivable element and return the derivation
        deriv.m_target = elem;
        return deriv;
    }
    
    private String m_asEntered;
    private EquationStack m_ifs;
    private EquationStack m_pfs;
    private PostfixStackEvaluator m_pfe;
    private Set<Row> m_affectedByRows;
    private Set<Column> m_affectedByCols;
    private boolean m_parsed;
    private boolean m_converted;
    private Derivable m_target;
    
    private Derivation()
    {
        m_affectedByRows = new LinkedHashSet<Row>();
        m_affectedByCols = new LinkedHashSet<Column>();
    }
    
    public boolean isParsed()
    {
        return m_parsed;
    }

    public boolean isConverted()
    {
        return m_converted;
    }
    
    public String getInfixExpression()
    {
        if (m_ifs != null)
            return m_ifs.toExpression();
        else
            return null;
    }
    
    public String getPostfixExpression()
    {
        if (m_pfs != null)
            return m_ifs.toExpression();
        else
            return null;
    }
    
    public String getAsEnteredExpression()
    {
        if (m_asEntered != null)
            return m_asEntered;
        else if (m_ifs != null)
            return m_ifs.toExpression();
        else
            return null;
    }

    public void recalculateTarget()
    {
        if (m_target.isReadOnly())
            throw new ReadOnlyException((BaseElement)m_target, TableProperty.Derivation);
        
        Table t = m_target.getTable();
        t.pushCurrent();
        
        t.popCurrent();      
    }
    
    public void destroy()
    {
        // TODO Auto-generated method stub
        
    }

}
