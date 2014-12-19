package org.tms.teq;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import org.junit.Test;

public class PostfixExpressionParserTest
{

    @Test
    public final void testPostfixExpressionParserStringTable()
    {
        PostfixStackGenerator pep = new PostfixStackGenerator("3 + 5", null); 
        assertThat(pep, notNullValue());
        
        EquationStack ifs = pep.getInfixStack();
        assertThat(ifs, notNullValue());
        assertThat(ifs.isEmpty(), is(false));
        assertThat(ifs.size(), is(3));
    }

    @Test
    public final void testPostfixExpressionParserInfixExpressionParser()
    {
        fail("Not yet implemented"); // TODO
    }

    @Test
    public final void testGetTable()
    {
        fail("Not yet implemented"); // TODO
    }

    @Test
    public final void testGetInfixStack()
    {
        fail("Not yet implemented"); // TODO
    }

    @Test
    public final void testGetPostfixStack()
    {
        fail("Not yet implemented"); // TODO
    }

    @Test
    public final void testConvertInfixToPostfix()
    {
        PostfixStackGenerator pep = new PostfixStackGenerator("3 + 5", null); 
        assertThat(pep, notNullValue());
        
        EquationStack ifs = pep.getInfixStack();
        assertThat(ifs, notNullValue());
        assertThat(ifs.isEmpty(), is(false));
        assertThat(ifs.size(), is(3));
        
        ParseResult pr = pep.convertInfixToPostfix();
        assertThat(pr, notNullValue());
        assertThat(pr.isSuccess(), is(true));
        
        EquationStack pfs = pep.getPostfixStack();
        assertThat(pfs, notNullValue());
        assertThat(pfs.isEmpty(), is(false));
        assertThat(pfs.size(), is(3));
        
        
        pep = new PostfixStackGenerator("3^2 * (5+3*(8/2+3)) -8/4", null); 
        assertThat(pep, notNullValue());
        
        ifs = pep.getInfixStack();
        assertThat(ifs, notNullValue());
        assertThat(ifs.isEmpty(), is(false));
        assertThat(ifs.size(), is(21));
        
        pr = pep.convertInfixToPostfix();
        assertThat(pr, notNullValue());
        assertThat(pr.isSuccess(), is(true));
        
        pfs = pep.getPostfixStack();
        assertThat(pfs, notNullValue());
        assertThat(pfs.isEmpty(), is(false));
        assertThat(pfs.size(), is(17));
        
    }

    @Test
    public final void testConvertInfixToPostfixEquationStackEquationStack()
    {
        fail("Not yet implemented"); // TODO
    }

}
