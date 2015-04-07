package org.tms.teq;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.tms.BaseTest;
import org.tms.api.derivables.Token;

public class InfixToPostfixConversionTest extends BaseTest
{
    @Test
    public final void testOperatorAssociations() 
    throws PendingDerivationException, BlockedDerivationException
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
        
        PostfixStackEvaluator pse = new PostfixStackEvaluator(psg);
        assertThat(pse, notNullValue());
        
        Token t = pse.evaluate();
        assertThat(t, notNullValue());
        assertThat(t.isNumeric(), is(true));
        assertThat(t.getNumericValue(), is(1.0));
        
        iep = new InfixExpressionParser("5*2^3+7");
        assertThat(iep, notNullValue());
        
        pr = iep.validateExpression();
        assertThat(pr, notNullValue());
        assertThat(pr.isSuccess(), is(true));
        
        psg = new PostfixStackGenerator(iep);
        assertThat(psg, notNullValue());
        
        pr = psg.convertInfixToPostfix();
        assertThat(pr, notNullValue());
        assertThat(pr.isSuccess(), is(true));
                
        pse = new PostfixStackEvaluator(psg);
        assertThat(pse, notNullValue());
        
        t = pse.evaluate();
        assertThat(t, notNullValue());
        assertThat(t.isNumeric(), is(true));
        assertThat(t.getNumericValue(), is(47.0));

        iep = new InfixExpressionParser("5*2^3*7");
        assertThat(iep, notNullValue());
        
        pr = iep.validateExpression();
        assertThat(pr, notNullValue());
        assertThat(pr.isSuccess(), is(true));
        
        psg = new PostfixStackGenerator(iep);
        assertThat(psg, notNullValue());
        
        pr = psg.convertInfixToPostfix();
        assertThat(pr, notNullValue());
        assertThat(pr.isSuccess(), is(true));
        
        pse = new PostfixStackEvaluator(psg);
        assertThat(pse, notNullValue());
        
        t = pse.evaluate();
        assertThat(t, notNullValue());
        assertThat(t.isNumeric(), is(true));
        assertThat(t.getNumericValue(), is(280.0));

        iep = new InfixExpressionParser("4^3^2");
        assertThat(iep, notNullValue());
        
        pr = iep.validateExpression();
        assertThat(pr, notNullValue());
        assertThat(pr.isSuccess(), is(true));
        
        psg = new PostfixStackGenerator(iep);
        assertThat(psg, notNullValue());
        
        pr = psg.convertInfixToPostfix();
        assertThat(pr, notNullValue());
        assertThat(pr.isSuccess(), is(true));
        
        pse = new PostfixStackEvaluator(psg);
        assertThat(pse, notNullValue());
        
        t = pse.evaluate();
        assertThat(t, notNullValue());
        assertThat(t.isNumeric(), is(true));
        assertThat(t.getNumericValue(), is(262144.0));

        iep = new InfixExpressionParser("4^(3^2)");
        assertThat(iep, notNullValue());
        
        pr = iep.validateExpression();
        assertThat(pr, notNullValue());
        assertThat(pr.isSuccess(), is(true));
        
        psg = new PostfixStackGenerator(iep);
        assertThat(psg, notNullValue());
        
        pr = psg.convertInfixToPostfix();
        assertThat(pr, notNullValue());
        assertThat(pr.isSuccess(), is(true));
        
        pse = new PostfixStackEvaluator(psg);
        assertThat(pse, notNullValue());
        
        t = pse.evaluate();
        assertThat(t, notNullValue());
        assertThat(t.isNumeric(), is(true));
        assertThat(t.getNumericValue(), is(262144.0));

        iep = new InfixExpressionParser("(4^3)^2");
        assertThat(iep, notNullValue());
        
        pr = iep.validateExpression();
        assertThat(pr, notNullValue());
        assertThat(pr.isSuccess(), is(true));
        
        psg = new PostfixStackGenerator(iep);
        assertThat(psg, notNullValue());
        
        pr = psg.convertInfixToPostfix();
        assertThat(pr, notNullValue());
        assertThat(pr.isSuccess(), is(true));
        
        pse = new PostfixStackEvaluator(psg);
        assertThat(pse, notNullValue());
        
        t = pse.evaluate();
        assertThat(t, notNullValue());
        assertThat(t.isNumeric(), is(true));
        assertThat(t.getNumericValue(), is(4096.0));

        iep = new InfixExpressionParser("3^2*(5+3*(8/2+3))-8/4");
        assertThat(iep, notNullValue());
        
        pr = iep.validateExpression();
        assertThat(pr, notNullValue());
        assertThat(pr.isSuccess(), is(true));
        
        psg = new PostfixStackGenerator(iep);
        assertThat(psg, notNullValue());
        
        pr = psg.convertInfixToPostfix();
        assertThat(pr, notNullValue());
        assertThat(pr.isSuccess(), is(true));
        
        pse = new PostfixStackEvaluator(psg);
        assertThat(pse, notNullValue());
        
        t = pse.evaluate();
        assertThat(t, notNullValue());
        assertThat(t.isNumeric(), is(true));
        assertThat(t.getNumericValue(), is(232.0));

        iep = new InfixExpressionParser("((3^2)*(5+(3*((8/2)+3))))-(8/4)");
        assertThat(iep, notNullValue());
        
        pr = iep.validateExpression();
        assertThat(pr, notNullValue());
        assertThat(pr.isSuccess(), is(true));
        
        psg = new PostfixStackGenerator(iep);
        assertThat(psg, notNullValue());
        
        pr = psg.convertInfixToPostfix();
        assertThat(pr, notNullValue());
        assertThat(pr.isSuccess(), is(true));
        
        pse = new PostfixStackEvaluator(psg);
        assertThat(pse, notNullValue());
        
        t = pse.evaluate();
        assertThat(t, notNullValue());
        assertThat(t.isNumeric(), is(true));
        assertThat(t.getNumericValue(), is(232.0));

        iep = new InfixExpressionParser("11 % 6 % 3");
        assertThat(iep, notNullValue());
        
        pr = iep.validateExpression();
        assertThat(pr, notNullValue());
        assertThat(pr.isSuccess(), is(true));
        
        psg = new PostfixStackGenerator(iep);
        assertThat(psg, notNullValue());
        
        pr = psg.convertInfixToPostfix();
        assertThat(pr, notNullValue());
        assertThat(pr.isSuccess(), is(true));
        
        pse = new PostfixStackEvaluator(psg);
        assertThat(pse, notNullValue());
        
        t = pse.evaluate();
        assertThat(t, notNullValue());
        assertThat(t.isNumeric(), is(true));
        assertThat(t.getNumericValue(), is(2.0));
    }     
}
