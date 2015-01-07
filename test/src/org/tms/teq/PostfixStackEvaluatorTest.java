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
        pse = new PostfixStackEvaluator("sind(390)", null);
        assertThat(pse, notNullValue());

        t = pse.evaluate();
        assertThat(t, notNullValue());
        assertThat(t.isNumeric(), is(true));
        assertThat(t.isError(), is(false));
        assertThat(t.getErrorCode(), is(ErrorCode.NoError));
        assertThat(Math.abs(0.5 - t.getNumericValue()) < 0.0001, is(true));
        
        // tan (degrees arg)
        pse = new PostfixStackEvaluator("tand(90)", null);
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
        pse = new PostfixStackEvaluator("asind(.5)", null);
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
    }
    
    @Test
    public final void testConstants()
    {
        // hypot operator
        PostfixStackEvaluator pse = new PostfixStackEvaluator("pi", null);
        assertThat(pse, notNullValue());

        Token t = pse.evaluate();
        assertThat(t, notNullValue());
        assertThat(t.isNumeric(), is(true));
        assertThat(t.isError(), is(false));
        assertThat(t.getErrorCode(), is(ErrorCode.NoError));
        assertThat(t.getNumericValue(), is(Math.PI));

        // e
        pse = new PostfixStackEvaluator("e", null);
        assertThat(pse, notNullValue());

        t = pse.evaluate();
        assertThat(t, notNullValue());
        assertThat(t.isNumeric(), is(true));
        assertThat(t.isError(), is(false));
        assertThat(t.getErrorCode(), is(ErrorCode.NoError));
        assertThat(t.getNumericValue(), is(Math.E));

        // e
        pse = new PostfixStackEvaluator("e + pi", null);
        assertThat(pse, notNullValue());

        t = pse.evaluate();
        assertThat(t, notNullValue());
        assertThat(t.isNumeric(), is(true));
        assertThat(t.isError(), is(false));
        assertThat(t.getErrorCode(), is(ErrorCode.NoError));
        assertThat(t.getNumericValue(), is(Math.E + Math.PI));
    }
    
    @Test
    public final void testHypot()
    {
        // hypot operator
        PostfixStackEvaluator pse = new PostfixStackEvaluator("hypot((1 + 2), 4)", null);
        assertThat(pse, notNullValue());

        Token t = pse.evaluate();
        assertThat(t, notNullValue());
        assertThat(t.isNumeric(), is(true));
        assertThat(t.isError(), is(false));
        assertThat(t.getErrorCode(), is(ErrorCode.NoError));
        assertThat(t.getNumericValue(), is(5.0));
    }
    
    @Test
    public final void testBinaryFunc()
    {
        // bigger operator
        PostfixStackEvaluator pse = new PostfixStackEvaluator("bigger((1 + 2), 4)", null);
        assertThat(pse, notNullValue());

        Token t = pse.evaluate();
        assertThat(t, notNullValue());
        assertThat(t.isNumeric(), is(true));
        assertThat(t.isError(), is(false));
        assertThat(t.getErrorCode(), is(ErrorCode.NoError));
        assertThat(t.getNumericValue(), is(4.0));
        
        // smaller operator
        pse = new PostfixStackEvaluator("smaller((1 + 2), 4)", null);
        assertThat(pse, notNullValue());

        t = pse.evaluate();
        assertThat(t, notNullValue());
        assertThat(t.isNumeric(), is(true));
        assertThat(t.isError(), is(false));
        assertThat(t.getErrorCode(), is(ErrorCode.NoError));
        assertThat(t.getNumericValue(), is(3.0));
        
        // smaller operator
        pse = new PostfixStackEvaluator("smaller(smaller(1, (1 + 1)), 4)", null);
        assertThat(pse, notNullValue());

        t = pse.evaluate();
        assertThat(t, notNullValue());
        assertThat(t.isNumeric(), is(true));
        assertThat(t.isError(), is(false));
        assertThat(t.getErrorCode(), is(ErrorCode.NoError));
        assertThat(t.getNumericValue(), is(1.0));
        
        // smaller operator
        pse = new PostfixStackEvaluator("smaller(bigger(1, (1 + 1)), 4)", null);
        assertThat(pse, notNullValue());

        t = pse.evaluate();
        assertThat(t, notNullValue());
        assertThat(t.isNumeric(), is(true));
        assertThat(t.isError(), is(false));
        assertThat(t.getErrorCode(), is(ErrorCode.NoError));
        assertThat(t.getNumericValue(), is(2.0));
        
        // power operator
        pse = new PostfixStackEvaluator("pow((1 + 2), bigger(4,3))", null);
        assertThat(pse, notNullValue());

        t = pse.evaluate();
        assertThat(t, notNullValue());
        assertThat(t.isNumeric(), is(true));
        assertThat(t.isError(), is(false));
        assertThat(t.getErrorCode(), is(ErrorCode.NoError));
        assertThat(t.getNumericValue(), is(81.0));
    }
    
    @Test
    public void testComplexExpression()
    {
        // smaller operator
        PostfixStackEvaluator pse = new PostfixStackEvaluator("smaller(bigger(1, smaller((1 + 1), 3)), 4)", null);
        assertThat(pse, notNullValue());

        Token t = pse.evaluate();
        assertThat(t, notNullValue());
        assertThat(t.isNumeric(), is(true));
        assertThat(t.isError(), is(false));
        assertThat(t.getErrorCode(), is(ErrorCode.NoError));
        assertThat(t.getNumericValue(), is(2.0));
    }
    
    @Test
    public void testTextExpression()
    {
        // plus operator
        PostfixStackEvaluator pse = new PostfixStackEvaluator("'abc' + 'def'", null);
        assertThat(pse, notNullValue());

        Token t = pse.evaluate();
        assertThat(t, notNullValue());
        assertThat(t.isNumeric(), is(false));
        assertThat(t.isString(), is(true));
        assertThat(t.isError(), is(false));
        assertThat(t.getErrorCode(), is(ErrorCode.NoError));
        assertThat(t.getStringValue(), is("abcdef"));
        
        // minus operator
        pse = new PostfixStackEvaluator("'abcdef' - 'def'", null);
        assertThat(pse, notNullValue());

        t = pse.evaluate();
        assertThat(t, notNullValue());
        assertThat(t.isNumeric(), is(false));
        assertThat(t.isString(), is(true));
        assertThat(t.isError(), is(false));
        assertThat(t.getErrorCode(), is(ErrorCode.NoError));
        assertThat(t.getStringValue(), is("abc"));
        
        // negative test
        pse = new PostfixStackEvaluator("sin('abcdef' - 'def')", null);
        assertThat(pse, notNullValue());

        t = pse.evaluate();
        assertThat(t, notNullValue());
        assertThat(t.isNumeric(), is(false));
        assertThat(t.isString(), is(false));
        assertThat(t.isError(), is(true));
        assertThat(t.getErrorCode(), is(ErrorCode.OperandDataTypeMismatch));
        
        // function operator
        pse = new PostfixStackEvaluator("len('abcdef' - 'def')", null);
        assertThat(pse, notNullValue());

        t = pse.evaluate();
        assertThat(t, notNullValue());
        assertThat(t.isNumeric(), is(true));
        assertThat(t.isString(), is(false));
        assertThat(t.isError(), is(false));
        assertThat(t.getErrorCode(), is(ErrorCode.NoError));
        assertThat(t.getNumericValue(), is(3.0));
        
        // function operator
        pse = new PostfixStackEvaluator("toNumber('54' + '6') + toNumber(4)", null);
        assertThat(pse, notNullValue());

        t = pse.evaluate();
        assertThat(t, notNullValue());
        assertThat(t.isNumeric(), is(true));
        assertThat(t.isString(), is(false));
        assertThat(t.isError(), is(false));
        assertThat(t.getErrorCode(), is(ErrorCode.NoError));
        assertThat(t.getNumericValue(), is(550.0));
    }
}
