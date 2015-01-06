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
    
    @Test
    public final void testBuiltIns()
    {
        // Factorial edge cases
        PostfixStackEvaluator pse = new PostfixStackEvaluator("Random", null);
        assertThat(pse, notNullValue());

        Token t = pse.evaluate();
        assertThat(t, notNullValue());
        assertThat(t.isNumeric(), is(true));
        assertThat(t.getNumericValue() >= 0 && t.getNumericValue() < 1.0, is(true));
    }    
    
    @Test
    public final void testRandom()
    {
        // Factorial edge cases
        PostfixStackEvaluator pse = new PostfixStackEvaluator("RandInt(10)", null);
        assertThat(pse, notNullValue());

        Token t = pse.evaluate();
        assertThat(t, notNullValue());
        assertThat(t.isNumeric(), is(true));
        assertThat(t.getNumericValue() >= 1 && t.getNumericValue() <= 10.0, is(true));
        
        // Alternate Spelling
        pse = new PostfixStackEvaluator("RandomInt(10)", null);
        assertThat(pse, notNullValue());

        t = pse.evaluate();
        assertThat(t, notNullValue());
        assertThat(t.isNumeric(), is(true));
        assertThat(t.getNumericValue() >= 1 && t.getNumericValue() <= 10.0, is(true));
        
        // check distribution
        int [] vals = new int[] {0,0,0,0,0,0,0,0,0,0,0};

        for (int i = 0; i < 1000; i++) {
            t = pse.evaluate();
            assertThat(t, notNullValue());
            assertThat(t.isNumeric(), is(true));
            assertThat(t.getNumericValue() >= 1 && t.getNumericValue() <= 10.0, is(true));
            
            int rVal = t.getNumericValue().intValue();
            vals[rVal]++;
        }
        
        assertThat(vals, notNullValue());
        
        // expression
        pse = new PostfixStackEvaluator("RandomInt(4 + 3)", null);
        assertThat(pse, notNullValue());

        t = pse.evaluate();
        assertThat(t, notNullValue());
        assertThat(t.isNumeric(), is(true));
        assertThat(t.getNumericValue() >= 1 && t.getNumericValue() <= 7.0, is(true));      
        
        // expression
        pse = new PostfixStackEvaluator("RandomInt(4 + 3*2)", null);
        assertThat(pse, notNullValue());

        t = pse.evaluate();
        assertThat(t, notNullValue());
        assertThat(t.isNumeric(), is(true));
        assertThat(t.getNumericValue() >= 1 && t.getNumericValue() <= 10.0, is(true));      
    } 
    
    @Test
    public final void testDivideByZero()
    {
        // Factorial edge cases
        PostfixStackEvaluator pse = new PostfixStackEvaluator("5/0", null);
        assertThat(pse, notNullValue());

        Token t = pse.evaluate();
        assertThat(t, notNullValue());
        assertThat(t.isNumeric(), is(false));
        assertThat(t.isError(), is(true));
        assertThat(t.getErrorCode(), is(ErrorCode.DivideByZero));
    }
    
    @Test
    public final void testTrig()
    {
        // sin (radians arg)
        PostfixStackEvaluator pse = new PostfixStackEvaluator("sin(toRadians(30))", null);
        assertThat(pse, notNullValue());

        Token t = pse.evaluate();
        assertThat(t, notNullValue());
        assertThat(t.isNumeric(), is(true));
        assertThat(t.isError(), is(false));
        assertThat(t.getErrorCode(), is(ErrorCode.NoError));
        assertThat(Math.abs(0.5 - t.getNumericValue()) < 0.0001, is(true));
        
        // sin (degrees arg)
        pse = new PostfixStackEvaluator("sinD(30)", null);
        assertThat(pse, notNullValue());

        t = pse.evaluate();
        assertThat(t, notNullValue());
        assertThat(t.isNumeric(), is(true));
        assertThat(t.isError(), is(false));
        assertThat(t.getErrorCode(), is(ErrorCode.NoError));
        assertThat(Math.abs(0.5 - t.getNumericValue()) < 0.0001, is(true));
        
        // sin (degrees arg)
        pse = new PostfixStackEvaluator("sinD(390)", null);
        assertThat(pse, notNullValue());

        t = pse.evaluate();
        assertThat(t, notNullValue());
        assertThat(t.isNumeric(), is(true));
        assertThat(t.isError(), is(false));
        assertThat(t.getErrorCode(), is(ErrorCode.NoError));
        assertThat(Math.abs(0.5 - t.getNumericValue()) < 0.0001, is(true));
        
        // tan (degrees arg)
        pse = new PostfixStackEvaluator("tanD(90)", null);
        assertThat(pse, notNullValue());

        t = pse.evaluate();
        assertThat(t, notNullValue());
        assertThat(t.isNumeric(), is(true));
        assertThat(t.isError(), is(false));
        assertThat(t.getErrorCode(), is(ErrorCode.NoError));
        assertThat(t.getNumericValue(), is(Double.POSITIVE_INFINITY));
        
        // tan (degrees arg)
        pse = new PostfixStackEvaluator("tanD(450)", null);
        assertThat(pse, notNullValue());

        t = pse.evaluate();
        assertThat(t, notNullValue());
        assertThat(t.isNumeric(), is(true));
        assertThat(t.isError(), is(false));
        assertThat(t.getErrorCode(), is(ErrorCode.NoError));
        assertThat(t.getNumericValue(), is(Double.POSITIVE_INFINITY));
        
        // tan (degrees arg)
        pse = new PostfixStackEvaluator("tanD(270)", null);
        assertThat(pse, notNullValue());

        t = pse.evaluate();
        assertThat(t, notNullValue());
        assertThat(t.isNumeric(), is(true));
        assertThat(t.isError(), is(false));
        assertThat(t.getErrorCode(), is(ErrorCode.NoError));
        assertThat(t.getNumericValue(), is(Double.NEGATIVE_INFINITY));
        
        // aSinD
        pse = new PostfixStackEvaluator("asinD(.5)", null);
        assertThat(pse, notNullValue());

        t = pse.evaluate();
        assertThat(t, notNullValue());
        assertThat(t.isNumeric(), is(true));
        assertThat(t.isError(), is(false));
        assertThat(t.getErrorCode(), is(ErrorCode.NoError));
        assertThat(Math.abs(30.0 - t.getNumericValue()) < 0.00001, is(true));
        
        // aTanD
        pse = new PostfixStackEvaluator("atanD(tanD(90.0))", null);
        assertThat(pse, notNullValue());

        t = pse.evaluate();
        assertThat(t, notNullValue());
        assertThat(t.isNumeric(), is(true));
        assertThat(t.isError(), is(false));
        assertThat(t.getErrorCode(), is(ErrorCode.NoError));
        assertThat(t.getNumericValue(), is(90.0));
        
        // pi
        pse = new PostfixStackEvaluator("pi", null);
        assertThat(pse, notNullValue());

        t = pse.evaluate();
        assertThat(t, notNullValue());
        assertThat(t.isNumeric(), is(true));
        assertThat(t.isError(), is(false));
        assertThat(t.getErrorCode(), is(ErrorCode.NoError));
        assertThat(t.getNumericValue(), is(Math.PI));
    }
}
