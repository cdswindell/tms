package org.tms.teq;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertThat;

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
    public final void testGetInfixStack()
    {
        PostfixStackGenerator pep = new PostfixStackGenerator("3 + 5*7", null); 
        assertThat(pep, notNullValue());
        
        EquationStack ifs = pep.getInfixStack();
        assertThat(ifs, notNullValue());
        assertThat(ifs.isEmpty(), is(false));
        assertThat(ifs.size(), is(5));
    }

    @Test
    public final void testGetPostfixStack()
    {
        PostfixStackGenerator pep = new PostfixStackGenerator("3 + 5*7 + pi", null); 
        assertThat(pep, notNullValue());
        
        EquationStack ifs = pep.getInfixStack();
        assertThat(ifs, notNullValue());
        assertThat(ifs.isEmpty(), is(false));
        assertThat(ifs.size(), is(7));
        
        EquationStack pfs = pep.getPostfixStack();
        assertThat(pfs, notNullValue());
        assertThat(pfs.size(), is(7));
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
        InfixExpressionParser iep = new InfixExpressionParser("5 % 2 + mod(7,5)");
        assertThat(iep, notNullValue());
        
        ParseResult pr = iep.validateExpression();
        assertThat(pr, notNullValue());
        assertThat(pr.isSuccess(), is(true));
        assertThat(iep.parsedInfixExpression(), is("5.0 % 2.0 + mod ( 7.0 , 5.0 )"));
        
        EquationStack ifs = iep.getInfixStack();
        assertThat(ifs, notNullValue());
        assertThat(ifs.size(), is(10));
              
        PostfixStackGenerator pfp = new PostfixStackGenerator(ifs, null);
        assertThat(pfp, notNullValue());
        
        EquationStack pfs = pfp.getPostfixStack();
        assertThat(pfs, notNullValue());        
        
        iep = new InfixExpressionParser("5 % (mod((2 + 7), 5))");
        assertThat(iep, notNullValue());
        
        pr = iep.validateExpression();
        assertThat(pr, notNullValue());
        assertThat(pr.isSuccess(), is(true));
        
        ifs = iep.getInfixStack();
        assertThat(ifs, notNullValue());
        assertThat(ifs.size(), is(14));
        assertThat(ifs.toExpression(), is("5.0 % ( mod ( ( 2.0 + 7.0 ) , 5.0 ) )"));
              
        pfp = new PostfixStackGenerator(ifs, null);
        assertThat(pfp, notNullValue());
        
        pfs = pfp.getPostfixStack();
        assertThat(pfs, notNullValue());        
    }  
}
