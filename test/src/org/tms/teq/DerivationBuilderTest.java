package org.tms.teq;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.tms.BaseTest;
import org.tms.api.Column;
import org.tms.api.Table;
import org.tms.api.derivables.DerivationBuilder;
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
		
		dStr = DerivationBuilder.rpn(t, c1, "mean");
		assertNotNull(dStr);
		assertThat(dStr, is("mean(col 1)"));		
	}
}
