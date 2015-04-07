package org.tms.teq;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertThat;

import org.junit.Test;

public class InfixToPostfixConversionTest
{
    @Test
    public final void testParseInfixExpression()
    {
        InfixExpressionParser iep = new InfixExpressionParser("5 % mod((2 + 7), 5)");
        assertThat(iep, notNullValue());
        
        ParseResult pr = iep.validateExpression();
        assertThat(pr, notNullValue());
        assertThat(pr.isSuccess(), is(true));
        
        PostfixStackGenerator psg = new PostfixStackGenerator(iep);
        assertThat(psg, notNullValue());
        
        pr = psg.convertInfixToPostfix();
        assertThat(pr, notNullValue());
        assertThat(pr.isSuccess(), is(true));
    }     
}
