package org.tms.teq;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertThat;

import org.junit.Test;

public class PostfixStackEvaluatorTest
{

    @Test
    public final void testPostfixStackEvaluator()
    {
        PostfixStackEvaluator pse = new PostfixStackEvaluator("abs(-6)", null);
        assertThat(pse, notNullValue());
        
        Token t = pse.evaluate();
        assertThat(t, notNullValue());
        assertThat(t.isNumeric(), is(true));
        assertThat(t.getValue(), is(6.0));
    }

    @Test
    public final void testEvaluate()
    {
        PostfixStackEvaluator pse = new PostfixStackEvaluator("abs(-6)", null);
        assertThat(pse, notNullValue());
        
        // simple unary function
        Token t = pse.evaluate();
        assertThat(t, notNullValue());
        assertThat(t.isNumeric(), is(true));
        assertThat(t.getValue(), is(6.0));
        
        // simple Binary op (^)
        pse = new PostfixStackEvaluator("3^2", null);
        assertThat(pse, notNullValue());

        t = pse.evaluate();
        assertThat(t, notNullValue());
        assertThat(t.isNumeric(), is(true));
        assertThat(t.getValue(), is(9.0));
    }
    
    @Test
    public final void testFactorials()
    {
        PostfixStackEvaluator pse = new PostfixStackEvaluator("6!", null);
        assertThat(pse, notNullValue());
        
        // simple unary function
        Token t = pse.evaluate();
        assertThat(t, notNullValue());
        assertThat(t.isNumeric(), is(true));
        assertThat(t.getValue(), is(720.0));
        
        // simple unary operator
        pse = new PostfixStackEvaluator("6!", null);
        assertThat(pse, notNullValue());

        t = pse.evaluate();
        assertThat(t, notNullValue());
        assertThat(t.isNumeric(), is(true));
        assertThat(t.getValue(), is(720.0));
        
        // Factorial rounding
        pse = new PostfixStackEvaluator("6.25!", null);
        assertThat(pse, notNullValue());

        t = pse.evaluate();
        assertThat(t, notNullValue());
        assertThat(t.isNumeric(), is(true));
        assertThat(t.getValue(), is(720.0));
        
        // Factorial rounding
        pse = new PostfixStackEvaluator("5.99!", null);
        assertThat(pse, notNullValue());

        t = pse.evaluate();
        assertThat(t, notNullValue());
        assertThat(t.isNumeric(), is(true));
        assertThat(t.getValue(), is(720.0));
        
        // Factorial op (!)
        pse = new PostfixStackEvaluator("101.25!", null);
        assertThat(pse, notNullValue());

        t = pse.evaluate();
        assertThat(t, notNullValue());
        assertThat(t.isNumeric(), is(true));
        assertThat(t.getValue(), is(9.42594775983836E159));
        
        // Factorial edge cases
        pse = new PostfixStackEvaluator("1!", null);
        assertThat(pse, notNullValue());

        t = pse.evaluate();
        assertThat(t, notNullValue());
        assertThat(t.isNumeric(), is(true));
        assertThat(t.getValue(), is(1.0));
        
        // Factorial edge cases
        pse = new PostfixStackEvaluator("0!", null);
        assertThat(pse, notNullValue());

        t = pse.evaluate();
        assertThat(t, notNullValue());
        assertThat(t.isNumeric(), is(true));
        assertThat(t.getValue(), is(1.0));
        
        // Factorial edge cases
        pse = new PostfixStackEvaluator("-1!", null);
        assertThat(pse, notNullValue());

        t = pse.evaluate();
        assertThat(t, notNullValue());
        assertThat(t.isNumeric(), is(true));
        assertThat(t.getValue(), is(1.0));
    }   

}
