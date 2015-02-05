package org.tms.teq;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.*;

import org.junit.Test;

public class BuiltinOperatorTest
{

    @Test
    public final void testStringFunctions() 
    throws PendingDerivationException
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
    }
}
