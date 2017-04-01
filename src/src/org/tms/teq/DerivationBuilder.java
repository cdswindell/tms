package org.tms.teq;

import org.tms.api.Cell;
import org.tms.api.Column;
import org.tms.api.Row;
import org.tms.api.Subset;
import org.tms.api.Table;
import org.tms.api.TableElement;
import org.tms.api.derivables.Operator;
import org.tms.api.derivables.Token;
import org.tms.api.derivables.TokenType;
import org.tms.tds.TokenMapper;

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
	
	static final public String algebraic(final Table table, final Object... params) 
	{
		DerivationBuilder db = new DerivationBuilder(table);
		
		for (Object p : params) {
			db.add(p);
		}
			
		return db.toExpression();
	}
	
	private EquationStack m_stack;
	private Table m_table;
	private TokenMapper m_tm;
	
	private DerivationBuilder(final Table table)
	{
		m_table = table;
		
		if (table != null)
			m_tm = TokenMapper.fetchTokenMapper(table);
		else
			m_tm = TokenMapper.fetchTokenMapper();			
	}
	
	final private void push(final Object p) 
    {
		if (m_stack == null) {
	        m_stack = EquationStack.createPostfixStack(m_table);
	        m_stack.setTokenMapper(m_tm);
		}
		
		// create the appropriate token, depending on the argument type
		Token t = toToken(p);
		
		m_stack.push(t);		
	}

	final private void add(final Object p) 
    {
		if (m_stack == null) {
	        m_stack = EquationStack.createInfixStack(m_table);
	        m_stack.setTokenMapper(m_tm);
		}
		
		// create the appropriate token, depending on the argument type
		Token t = toToken(p);
		
		m_stack.push(t);		
	}

	final private Token toToken(final Object p) {
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
				if (t == null) {
					if (str.equals("("))
						t = new Token(TokenType.LeftParen);
					else if (str.equals(")"))
						t = new Token(TokenType.RightParen);
					else if (str.equals(","))
						t = new Token(TokenType.Comma);
				}
			}
			else if (p instanceof Character) {
				t = m_tm.lookUpToken((char)p);
				if (t == null) {
					switch ((char)p) {
						case '(':
							t = new Token(TokenType.LeftParen);
							break;
						case ')':
							t = new Token(TokenType.RightParen);
							break;
						case ',':
							t = new Token(TokenType.Comma);
							break;
					}
				}
			}
			
			// if t is null, no token found
			if (t == null)
				t = Token.createOperandToken(p);
		}
		return t;
	}

	final public String toExpression()
    {
    	if (m_stack != null) {
    		switch (m_stack.getStackType()) {
				case Infix:
		    		return m_stack.toExpression();
				case Postfix:
		    		return m_stack.toExpression(StackType.Infix);
				default:
					break;
    		}
    	}
    	
     	return null;
    }
}
