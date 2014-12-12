package org.tms.teq;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import org.junit.Test;

public class InfixExpressionParserTest
{

    @Test
    public final void testValidateSimpleNumericExpression()
    {
        InfixExpressionParser iep = new InfixExpressionParser("123");
        assertThat(iep, notNullValue());
        
        ParseResult pr = iep.validateExpression();
        assertThat(pr, notNullValue());
        assertThat(pr.isSuccess(), is(true));

        iep = new InfixExpressionParser("123.45");
        assertThat(iep, notNullValue());
        
        pr = iep.validateExpression();
        assertThat(pr, notNullValue());
        assertThat(pr.isSuccess(), is(true));

        iep = new InfixExpressionParser("123.45e23");
        assertThat(iep, notNullValue());
        
        pr = iep.validateExpression();
        assertThat(pr, notNullValue());
        assertThat(pr.isSuccess(), is(true));

        iep = new InfixExpressionParser("456.45e-23");
        assertThat(iep, notNullValue());
        
        pr = iep.validateExpression();
        assertThat(pr, notNullValue());
        assertThat(pr.isSuccess(), is(true));

        iep = new InfixExpressionParser("+123.45");
        assertThat(iep, notNullValue());
        
        pr = iep.validateExpression();
        assertThat(pr, notNullValue());
        assertThat(pr.isSuccess(), is(true));

        iep = new InfixExpressionParser("-123.45");
        assertThat(iep, notNullValue());
        
        pr = iep.validateExpression();
        assertThat(pr, notNullValue());
        assertThat(pr.isSuccess(), is(true));
    }

    @Test
    public final void testParseInfixExpression()
    {
        fail("Not yet implemented"); // TODO
    }

}
