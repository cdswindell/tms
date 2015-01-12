package org.tms.teq;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.tms.api.BaseElement;
import org.tms.api.Cell;
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
        // create the derivation structure and save the as-entered exprerssion
        Derivation deriv = new Derivation();
        deriv.m_asEntered = new String(expr);
        
        // parse the expression
        Table t = elem.getTable();
        InfixExpressionParser ifParser = new InfixExpressionParser(expr, t);
        
        ParseResult pr = ifParser.parseInfixExpression();
        if (pr != null && pr.isFailure())
            throw new InvalidExpressionException(pr);

        // otherwise, harvest the infix stack
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
        
        // note cols/rows that affect this derivation
        Iterator<Token> iter = deriv.m_ifs.iterator();
        while(iter != null && iter.hasNext()) {
            Token tk = iter.next();
            TokenType tt = tk.getTokenType();
            
            switch (tt) {
                case RowRef:
                case ColumnRef:
                    if (tk.getDerivableValue() != null) 
                        deriv.m_affectedBy.add(tk.getDerivableValue());
                    break;
                    
                default:
                    break;
            }            
        }
        
        // finally, retain the derivable element and return the derivation
        deriv.m_target = elem;
        return deriv;
    }
    
    private String m_asEntered;
    private EquationStack m_ifs;
    private EquationStack m_pfs;
    private PostfixStackEvaluator m_pfe;
    private Set<Derivable> m_affectedBy;
    private boolean m_parsed;
    private boolean m_converted;
    private Derivable m_target;
    
    private Derivation()
    {
        m_affectedBy = new LinkedHashSet<Derivable>();
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
            return m_pfs.toExpression();
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
        
        // currently support derived rows, columns, and cells, 
        // dispatch to correct handler
        if (m_target instanceof Column)
            recalculateTargetColumn();
        else if (m_target instanceof Row)
            recalculateTargetRow();
        else if (m_target instanceof Cell)
            recalculateTargetCell();
        
        t.popCurrent();      
    }
    
    private void recalculateTargetCell()
    {
        Cell cell = (Cell)m_target;
        
    }

    private void recalculateTargetRow()
    {
        Row row = (Row)m_target;
        Table tbl = row.getTable();
        for (Column col : tbl.columnIterable()) {
            Token t = m_pfe.evaluate(row, col);
            tbl.setCellValue(row, col, t);
        }        
    }

    private void recalculateTargetColumn()
    {
        Column col = (Column)m_target;
        Table tbl = col.getTable();
        for (Row row : tbl.rowIterable()) {
            Token t = m_pfe.evaluate(row, col);
            tbl.setCellValue(row, col, t);
        }       
    }

    public List<Derivable> getAffectedBy()
    {
        List<Derivable> affectedBy = new ArrayList<Derivable>();
        
        if (m_affectedBy != null) {
            for (Derivable d : m_affectedBy) {
                affectedBy.add(d);
            }
        }
        
        return affectedBy;       
    }
    

    public Derivable getTarget()
    {
        return m_target;
    }
    
    public void destroy()
    {
        // TODO Auto-generated method stub
        
    }
    
    public String toString()
    {
        return String.format("%s [%s %s]", 
                this.getAsEnteredExpression(),
                isParsed() ? "parsed" : "", isConverted() ? "converted" : "");
    }
}
