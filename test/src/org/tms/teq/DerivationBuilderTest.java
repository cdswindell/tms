package org.tms.teq;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.tms.BaseTest;
import org.tms.api.Column;
import org.tms.api.Table;
import org.tms.api.factories.TableFactory;

public class DerivationBuilderTest extends BaseTest 
{
	@Test
	public void testDerivationBuilderRpn()
	{
		String dStr = DerivationBuilder.rpn((Table)null, 3, 5, "+");
		assertNotNull(dStr);
		assertThat(dStr, is("3.0 + 5.0"));
		
		dStr = DerivationBuilder.rpn((Table)null, 3, 5, "+",4,6,"+","*");
		assertNotNull(dStr);
		assertThat(dStr, is("(3.0 + 5.0) * (4.0 + 6.0)"));		
		
		dStr = DerivationBuilder.rpn((Table)null, 3, 5, "+",4,6,"+","randBetween");
		assertNotNull(dStr);
		assertThat(dStr, is("randomBetween((3.0 + 5.0), (4.0 + 6.0))"));
		
		// test with table
		Table t = TableFactory.createTable();
		Column c1 = t.addColumn();
		Column c2 = t.addColumn();
		
		dStr = (c2.setDerivation(c1, "mean")).getExpression();
		assertNotNull(dStr);
		assertThat(dStr, is("mean(col 1)"));		
	}
	
	@Test
	public void testDerivationBuilderAlgebraic()
	{
		String dStr = DerivationBuilder.algebraic((Table)null, 3, "+", 5);
		assertNotNull(dStr);
		assertThat(dStr, is("3.0 + 5.0"));
		
		dStr = DerivationBuilder.algebraic((Table)null, "(", 3, "+", 5, ")", "*", "(",4,"+", 6,")");
		assertNotNull(dStr);
		assertThat(dStr, is("(3.0 + 5.0) * (4.0 + 6.0)"));		
		
		dStr = DerivationBuilder.algebraic((Table)null,"randBetween", '(', 3, '+', 5, ',',4,'+',6, ')');
		assertNotNull(dStr);
		assertThat(dStr, is("randbetween(3.0 + 5.0, 4.0 + 6.0)"));
		
		dStr = DerivationBuilder.algebraic((Table)null,"randBetween", "(", 3, "+", 5, ",",4,"+",6, ")");
		assertNotNull(dStr);
		assertThat(dStr, is("randbetween(3.0 + 5.0, 4.0 + 6.0)"));
		
		// test with table
		Table t = TableFactory.createTable();
		t.setLabel("t");
		Column c1 = t.addColumn();
		
		dStr = DerivationBuilder.algebraic(t, "mean", "(", c1, ")");
		assertNotNull(dStr);
		assertThat(dStr, is("mean(col 1)"));		
		
		Table t2 = TableFactory.createTable();
		dStr = DerivationBuilder.algebraic(t2, "mean", "(", c1, ")");
		assertNotNull(dStr);
		assertThat(dStr, is("mean(col \"t::1\")"));		
	}
}
