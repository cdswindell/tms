package org.tms.teq;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertThat;

import java.util.ArrayList;

import org.junit.Test;
import org.tms.api.derivables.Token;

public class BuiltinOperatorTest
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
}
