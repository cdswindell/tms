package org.tms.api.derivables;

import org.tms.api.Cell;
import org.tms.api.Column;
import org.tms.api.Row;
import org.tms.api.Subset;
import org.tms.api.Table;
import org.tms.api.TableElement;
import org.tms.tds.TokenMapper;
import org.tms.teq.EquationStack;
import org.tms.teq.StackType;

public class DerivationBuilder 
{
	static final public String rpn(final Table table, final Object... params) 
	{
		DerivationBuilder db = new DerivationBuilder(table);
		
		for (Object p : params) {
			db.push(p);
		}
			
		return db.toExpression();
	}
	
	private EquationStack m_ifs;
    private EquationStack m_pfs;
	private Table m_table;
	private TokenMapper m_tm;
	
	public DerivationBuilder(final Table table)
	{
		m_table = table;
		
		if (table != null)
			m_tm = TokenMapper.fetchTokenMapper(table);
		else
			m_tm = TokenMapper.fetchTokenMapper();			
	}
	
    private void push(final Object p) 
    {
		if (m_pfs == null) {
	        m_pfs = EquationStack.createPostfixStack(m_table);
	        m_pfs.setTokenMapper(m_tm);
		}
		
		// create the appropriate token, depending on the argument type
		Token t = null;
		if (p == null)
			t = Token.createNullToken();
		else if (p instanceof Operator)
			t = new Token((Operator)p);
		else if (p instanceof TableElement) {
			if (p instanceof Column)
				t = new Token(TokenType.ColumnRef, p);
			else if (p instanceof Row)
				t = new Token(TokenType.RowRef, p);
			else if (p instanceof Subset)
				t = new Token(TokenType.SubsetRef, p);
			else if (p instanceof Cell)
				t = new Token(TokenType.CellRef, p);
			else if (p instanceof Table)
				t = new Token(TokenType.TableRef, p);
			else
				throw new UnsupportedOperationException("Unsuported Table Element: " + ((TableElement)p).getElementType());
		}
		else {
			if (p instanceof String) {
				String str = ((String)p).trim();
				
				t = m_tm.lookUpToken(str);
			}
			
			// if t is null, no token found
			if (t == null)
				t = Token.createOperandToken(p);
		}
		
		m_pfs.push(t);		
	}

    public String toExpression()
    {
    	if (m_ifs != null)
    		return m_ifs.toExpression();
    	else if (m_pfs != null)
    		return m_pfs.toExpression(StackType.Infix);
    	else
    		return null;
    }
}
