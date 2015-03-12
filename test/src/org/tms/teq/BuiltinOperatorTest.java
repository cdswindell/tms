package org.tms.teq;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertThat;

import java.util.ArrayList;

import org.junit.Test;
import org.tms.BaseTest;
import org.tms.api.derivables.Token;

public class BuiltinOperatorTest extends BaseTest
{

    @Test
    public final void testStringFunctions() 
            throws PendingDerivationException, BlockedDerivationException
    {
        PostfixStackEvaluator pse = new PostfixStackEvaluator("reverse('abcdefghi')", null);
        assertThat(pse, notNullValue());

        Token t = pse.evaluate();
        assertThat(t, notNullValue());
        assertThat(t.isNumeric(), is(false));
        assertThat(t.isString(), is(true));
        assertThat(t.getStringValue(), is("ihgfedcba"));

        pse = new PostfixStackEvaluator("len('abcdefghi')", null);
        assertThat(pse, notNullValue());

        t = pse.evaluate();
        assertThat(t, notNullValue());
        assertThat(t.isNumeric(), is(true));
        assertThat(t.isString(), is(false));
        assertThat(t.getValue(), is((double)"abcdefghi".length()));

        pse = new PostfixStackEvaluator("toUpper('abcdefghi')", null);
        assertThat(pse, notNullValue());

        t = pse.evaluate();
        assertThat(t, notNullValue());
        assertThat(t.isNumeric(), is(false));
        assertThat(t.isString(), is(true));
        assertThat(t.getValue(), is("ABCDEFGHI"));

        pse = new PostfixStackEvaluator("toLower('ABCDEFGHI')", null);
        assertThat(pse, notNullValue());

        t = pse.evaluate();
        assertThat(t, notNullValue());
        assertThat(t.isNumeric(), is(false));
        assertThat(t.isString(), is(true));
        assertThat(t.getValue(), is("ABCDEFGHI".toLowerCase()));

        pse = new PostfixStackEvaluator("trim('  abcd efghi    ')", null);
        assertThat(pse, notNullValue());

        t = pse.evaluate();
        assertThat(t, notNullValue());
        assertThat(t.isNumeric(), is(false));
        assertThat(t.isString(), is(true));
        assertThat(t.getValue(), is("abcd efghi"));

        pse = new PostfixStackEvaluator("toString(15)", null);
        assertThat(pse, notNullValue());

        t = pse.evaluate();
        assertThat(t, notNullValue());
        assertThat(t.isNumeric(), is(false));
        assertThat(t.isString(), is(true));
        assertThat(t.getValue(), is("15.0"));

        pse = new PostfixStackEvaluator("toString(15/3)", null);
        assertThat(pse, notNullValue());

        t = pse.evaluate();
        assertThat(t, notNullValue());
        assertThat(t.isNumeric(), is(false));
        assertThat(t.isString(), is(true));
        assertThat(t.getValue(), is("5.0"));

        pse = new PostfixStackEvaluator("isPrime(7.0)", null);
        assertThat(pse, notNullValue());

        t = pse.evaluate();
        assertThat(t, notNullValue());
        assertThat(t.isNumeric(), is(false));
        assertThat(t.isBoolean(), is(true));
        assertThat(t.getValue(), is(true));

        pse = new PostfixStackEvaluator("toString(15/3)", null);
        assertThat(pse, notNullValue());

        t = pse.evaluate();
        assertThat(t, notNullValue());
        assertThat(t.isNumeric(), is(false));
        assertThat(t.isString(), is(true));
        assertThat(t.getValue(), is("5.0"));
    }

    @SuppressWarnings("unchecked")
    @Test
    public final void testUnaryMathFunctions() 
            throws PendingDerivationException, BlockedDerivationException
    {
        PostfixStackEvaluator pse = new PostfixStackEvaluator("reverse('abcdefghi')", null);
        assertThat(pse, notNullValue());

        Token t = pse.evaluate();
        assertThat(t, notNullValue());
        assertThat(t.isNumeric(), is(false));
        assertThat(t.isString(), is(true));
        assertThat(t.getStringValue(), is("ihgfedcba"));

        pse = new PostfixStackEvaluator("isPrime(7.0)", null);
        assertThat(pse, notNullValue());

        t = pse.evaluate();
        assertThat(t, notNullValue());
        assertThat(t.isNumeric(), is(false));
        assertThat(t.isBoolean(), is(true));
        assertThat(t.getValue(), is(true));

        pse = new PostfixStackEvaluator("nextPrime(14)", null);
        assertThat(pse, notNullValue());

        t = pse.evaluate();
        assertThat(t, notNullValue());
        assertThat(t.isNumeric(), is(true));
        assertThat(t.isString(), is(false));
        assertThat(t.getValue(), is(17.0));

        pse = new PostfixStackEvaluator("nextPrime(37)", null);
        assertThat(pse, notNullValue());

        t = pse.evaluate();
        assertThat(t, notNullValue());
        assertThat(t.isNumeric(), is(true));
        assertThat(t.isString(), is(false));
        assertThat(t.getValue(), is(37.0));

        pse = new PostfixStackEvaluator("primeFactors(45)", null);
        assertThat(pse, notNullValue());

        t = pse.evaluate();
        assertThat(t, notNullValue());
        assertThat(t.isNumeric(), is(false));
        assertThat(t.isString(), is(false));
        assertThat(t.isA(ArrayList.class), is(true));
        assertThat(((ArrayList<Integer>)t.getValue()).toArray(), is(new int[] {3, 3, 5}));

        pse = new PostfixStackEvaluator("isPowerOfTwo(1023)", null);
        assertThat(pse, notNullValue());

        t = pse.evaluate();
        assertThat(t, notNullValue());
        assertThat(t.isNumeric(), is(false));
        assertThat(t.isBoolean(), is(true));
        assertThat(t.getValue(), is(false));

        pse = new PostfixStackEvaluator("isPowerOfTwo(1024)", null);
        assertThat(pse, notNullValue());

        t = pse.evaluate();
        assertThat(t, notNullValue());
        assertThat(t.isNumeric(), is(false));
        assertThat(t.isBoolean(), is(true));
        assertThat(t.getValue(), is(true));

        pse = new PostfixStackEvaluator("isNumber(123)", null);
        assertThat(pse, notNullValue());

        t = pse.evaluate();
        assertThat(t, notNullValue());
        assertThat(t.isNumeric(), is(false));
        assertThat(t.isBoolean(), is(true));
        assertThat(t.getValue(), is(true));

        pse = new PostfixStackEvaluator("isNumber('abc')", null);
        assertThat(pse, notNullValue());

        t = pse.evaluate();
        assertThat(t, notNullValue());
        assertThat(t.isNumeric(), is(false));
        assertThat(t.isBoolean(), is(true));
        assertThat(t.getValue(), is(false));

        pse = new PostfixStackEvaluator("isNumber(123.5)", null);
        assertThat(pse, notNullValue());

        t = pse.evaluate();
        assertThat(t, notNullValue());
        assertThat(t.isNumeric(), is(false));
        assertThat(t.isBoolean(), is(true));
        assertThat(t.getValue(), is(true));

        pse = new PostfixStackEvaluator("isEven(123.4)", null);
        assertThat(pse, notNullValue());

        t = pse.evaluate();
        assertThat(t, notNullValue());
        assertThat(t.isNumeric(), is(false));
        assertThat(t.isBoolean(), is(true));
        assertThat(t.getValue(), is(false));

        pse = new PostfixStackEvaluator("isEven(123.6)", null);
        assertThat(pse, notNullValue());

        t = pse.evaluate();
        assertThat(t, notNullValue());
        assertThat(t.isNumeric(), is(false));
        assertThat(t.isBoolean(), is(true));
        assertThat(t.getValue(), is(true));

        pse = new PostfixStackEvaluator("isEven(124)", null);
        assertThat(pse, notNullValue());

        t = pse.evaluate();
        assertThat(t, notNullValue());
        assertThat(t.isNumeric(), is(false));
        assertThat(t.isBoolean(), is(true));
        assertThat(t.getValue(), is(true));

        pse = new PostfixStackEvaluator("isText(124)", null);
        assertThat(pse, notNullValue());

        t = pse.evaluate();
        assertThat(t, notNullValue());
        assertThat(t.isNumeric(), is(false));
        assertThat(t.isBoolean(), is(true));
        assertThat(t.getValue(), is(false));

        pse = new PostfixStackEvaluator("isText('124')", null);
        assertThat(pse, notNullValue());

        t = pse.evaluate();
        assertThat(t, notNullValue());
        assertThat(t.isNumeric(), is(false));
        assertThat(t.isBoolean(), is(true));
        assertThat(t.getValue(), is(true));

        pse = new PostfixStackEvaluator("isLogical(isText('124'))", null);
        assertThat(pse, notNullValue());

        t = pse.evaluate();
        assertThat(t, notNullValue());
        assertThat(t.isNumeric(), is(false));
        assertThat(t.isBoolean(), is(true));
        assertThat(t.getValue(), is(true));

        pse = new PostfixStackEvaluator("isLogical('124')", null);
        assertThat(pse, notNullValue());

        t = pse.evaluate();
        assertThat(t, notNullValue());
        assertThat(t.isNumeric(), is(false));
        assertThat(t.isBoolean(), is(true));
        assertThat(t.getValue(), is(false));

        pse = new PostfixStackEvaluator("isError(3*0)", null);
        assertThat(pse, notNullValue());

        t = pse.evaluate();
        assertThat(t, notNullValue());
        assertThat(t.isNumeric(), is(false));
        assertThat(t.isBoolean(), is(true));
        assertThat(t.getValue(), is(false));

        pse = new PostfixStackEvaluator("isError(3/0)", null);
        assertThat(pse, notNullValue());

        t = pse.evaluate();
        assertThat(t, notNullValue());
        assertThat(t.isNumeric(), is(false));
        assertThat(t.isBoolean(), is(true));
        assertThat(t.getValue(), is(true));
    }

    @Test
    public final void testBinaryMathFunctions() 
            throws PendingDerivationException, BlockedDerivationException
    {
        PostfixStackEvaluator pse = new PostfixStackEvaluator("lcm(330, 65)", null);
        assertThat(pse, notNullValue());

        Token t = pse.evaluate();
        assertThat(t, notNullValue());
        assertThat(t.isNumeric(), is(true));
        assertThat(t.isString(), is(false));
        assertThat(t.getValue(), is(4290.0));

        pse = new PostfixStackEvaluator("gcd(36, 18)", null);
        assertThat(pse, notNullValue());

        t = pse.evaluate();
        assertThat(t, notNullValue());
        assertThat(t.isNumeric(), is(true));
        assertThat(t.isString(), is(false));
        assertThat(t.getValue(), is(18.0));

        pse = new PostfixStackEvaluator("gcd(81, 45)", null);
        assertThat(pse, notNullValue());

        t = pse.evaluate();
        assertThat(t, notNullValue());
        assertThat(t.isNumeric(), is(true));
        assertThat(t.isString(), is(false));
        assertThat(t.getValue(), is(9.0));
    }
    
    @Test
    public final void testGenericMathFunctions() 
            throws PendingDerivationException, BlockedDerivationException
    {
        PostfixStackEvaluator pse = new PostfixStackEvaluator("pmt((10/12/100), (10*12), 0, 10000)", null);
        assertThat(pse, notNullValue());

        Token t = pse.evaluate();
        assertThat(t, notNullValue());
        assertThat(t.isNumeric(), is(true));
        assertThat(t.isString(), is(false));
        assertThat(closeTo(t.getValue(), -48.82, 0.01), is(true));

        pse = new PostfixStackEvaluator("pmt((10/12/100), (10*12), 5000, 10000)", null);
        assertThat(pse, notNullValue());

        t = pse.evaluate();
        assertThat(t, notNullValue());
        assertThat(t.isNumeric(), is(true));
        assertThat(t.isString(), is(false));
        assertThat(closeTo(t.getValue(), -114.89, 0.01), is(true));

        pse = new PostfixStackEvaluator("pmt((10/12/100), (10*12), (-5000), 10000)", null);
        assertThat(pse, notNullValue());

        t = pse.evaluate();
        assertThat(t, notNullValue());
        assertThat(t.isNumeric(), is(true));
        assertThat(t.isString(), is(false));
        assertThat(closeTo(t.getValue(), 17.26, 0.01), is(true));

        pse = new PostfixStackEvaluator("fv((6/12/100), (5*12), 100, 0)", null);
        assertThat(pse, notNullValue());

        t = pse.evaluate();
        assertThat(t, notNullValue());
        assertThat(t.isNumeric(), is(true));
        assertThat(t.isString(), is(false));
        assertThat(closeTo(t.getValue(), -6977, 0.01), is(true));

        pse = new PostfixStackEvaluator("fv((6/12/100), (5*12), 100, 1000)", null);
        assertThat(pse, notNullValue());

        t = pse.evaluate();
        assertThat(t, notNullValue());
        assertThat(t.isNumeric(), is(true));
        assertThat(t.isString(), is(false));
        assertThat(closeTo(t.getValue(), -8325.85, 0.01), is(true));

        pse = new PostfixStackEvaluator("fv((6/12/100), (5*12), 0, 1000)", null);
        assertThat(pse, notNullValue());

        t = pse.evaluate();
        assertThat(t, notNullValue());
        assertThat(t.isNumeric(), is(true));
        assertThat(t.isString(), is(false));
        assertThat(closeTo(t.getValue(), -1348.85, 0.01), is(true));

        pse = new PostfixStackEvaluator("pv((8/12/100), (20*12), 500, 0)", null);
        assertThat(pse, notNullValue());

        t = pse.evaluate();
        assertThat(t, notNullValue());
        assertThat(t.isNumeric(), is(true));
        assertThat(t.isString(), is(false));
        assertThat(closeTo(t.getValue(), -59777.15, 0.01), is(true));

        pse = new PostfixStackEvaluator("pv(0.10, 3, 0, 100)", null);
        assertThat(pse, notNullValue());

        t = pse.evaluate();
        assertThat(t, notNullValue());
        assertThat(t.isNumeric(), is(true));
        assertThat(t.isString(), is(false));
        assertThat(closeTo(t.getValue(), -75.13, 0.01), is(true));

        pse = new PostfixStackEvaluator("nper((12/12/100), (-100), (-1000), 10000)", null);
        assertThat(pse, notNullValue());

        t = pse.evaluate();
        assertThat(t, notNullValue());
        assertThat(t.isNumeric(), is(true));
        assertThat(t.isString(), is(false));
        assertThat(closeTo(t.getValue(), 60.08, 0.01), is(true));

        pse = new PostfixStackEvaluator("nper((12/12/100), (-100), (-1000), 0)", null);
        assertThat(pse, notNullValue());

        t = pse.evaluate();
        assertThat(t, notNullValue());
        assertThat(t.isNumeric(), is(true));
        assertThat(t.isString(), is(false));
        assertThat(closeTo(t.getValue(), -9.5786, 0.0001), is(true));

        pse = new PostfixStackEvaluator("rate((4*12), (-200), 8000, 0)", null);
        assertThat(pse, notNullValue());

        t = pse.evaluate();
        assertThat(t, notNullValue());
        assertThat(t.isNumeric(), is(true));
        assertThat(t.isString(), is(false));
        assertThat(closeTo(t.getValue(), 0.0077, 0.0001), is(true));
        assertThat(closeTo(12.0 * (double)t.getValue(), 0.09242, 0.00001), is(true));

        pse = new PostfixStackEvaluator("rate((365*24*60*60), (-0.01), 0, 331667.006690776891780341908435)", null);
        assertThat(pse, notNullValue());

        t = pse.evaluate();
        assertThat(t, notNullValue());
        assertThat(t.isNumeric(), is(true));
        assertThat(t.isString(), is(false));
        assertThat(closeTo(31536000 * (double)t.getValue(), 0.10, 0.00001), is(true));
    }
}
