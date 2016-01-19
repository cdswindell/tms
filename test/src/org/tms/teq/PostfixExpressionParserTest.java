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
    public final void testConvertInfixToPostfixWithNeg()
    {
        PostfixStackGenerator pep = new PostfixStackGenerator("randomBetween(-5.0, 20.0)", null); 
        assertThat(pep, notNullValue());
        
        EquationStack ifs = pep.getInfixStack();
        assertThat(ifs, notNullValue());
        assertThat(ifs.isEmpty(), is(false));
        assertThat(ifs.size(), is(7));
        
        ParseResult pr = pep.convertInfixToPostfix();
        assertThat(pr, notNullValue());
        assertThat(pr.isSuccess(), is(true));
        
        EquationStack pfs = pep.getPostfixStack();
        assertThat(pfs, notNullValue());
        assertThat(pfs.isEmpty(), is(false));
        assertThat(pfs.size(), is(4));
        assertThat(pfs.toExpression(), is("5.0 - 20.0 randomBetween"));
    }    
        
    
    @Test
    public final void testConvertInfixToPostfixEquationStackEquationStack()
    {
        InfixExpressionParser iep = new InfixExpressionParser("mod(5, 2) + mod(7,5)");
        assertThat(iep, notNullValue());
        
        ParseResult pr = iep.validateExpression();
        assertThat(pr, notNullValue());
        assertThat(pr.isSuccess(), is(true));
        assertThat(iep.parsedInfixExpression(), is("mod ( 5.0 , 2.0 ) + mod ( 7.0 , 5.0 )"));
        
        EquationStack ifs = iep.getInfixStack();
        assertThat(ifs, notNullValue());
        assertThat(ifs.size(), is(13));
              
        PostfixStackGenerator pfp = new PostfixStackGenerator(ifs, null);
        assertThat(pfp, notNullValue());
        
        EquationStack pfs = pfp.getPostfixStack();
        assertThat(pfs, notNullValue());        
        assertThat(pfs.toExpression(), is("5.0 2.0 mod 7.0 5.0 mod +"));
        assertThat(pfs, notNullValue());   
        
        String infixExpr = pfs.toExpression(StackType.Infix);
        assertThat(infixExpr, is("mod(5.0, 2.0) + mod(7.0, 5.0)") );
        
        iep = new InfixExpressionParser("mod(5, mod(2 + 7, 5))");
        assertThat(iep, notNullValue());
        
        pr = iep.validateExpression();
        assertThat(pr, notNullValue());
        assertThat(pr.isSuccess(), is(true));
        
        ifs = iep.getInfixStack();
        assertThat(ifs, notNullValue());
        assertThat(ifs.size(), is(13));
        assertThat(ifs.toExpression(true, false, null), is("mod ( 5.0 , mod ( 2.0 + 7.0 , 5.0 ) )"));
        assertThat(ifs.toExpression(), is("mod(5.0, mod(2.0 + 7.0, 5.0))"));
              
        pfp = new PostfixStackGenerator(ifs, null);
        assertThat(pfp, notNullValue());
        
        pfs = pfp.getPostfixStack();
        assertThat(pfs.toExpression(), is("5.0 2.0 7.0 + 5.0 mod mod"));
        assertThat(pfs, notNullValue());   
        
        infixExpr = pfs.toExpression(StackType.Infix);
        assertThat(infixExpr, is("mod(5.0, (mod((2.0 + 7.0), 5.0)))") );
        
        iep = new InfixExpressionParser("5 * 3 + 2 / 7");
        assertThat(iep, notNullValue());
        
        pr = iep.validateExpression();
        assertThat(pr, notNullValue());
        assertThat(pr.isSuccess(), is(true));
        
        ifs = iep.getInfixStack();
        assertThat(ifs, notNullValue());
        assertThat(ifs.toExpression(), is("5.0 * 3.0 + 2.0 / 7.0"));
              
        pfp = new PostfixStackGenerator(ifs, null);
        assertThat(pfp, notNullValue());
        
        pfs = pfp.getPostfixStack();
        assertThat(pfs.toExpression(), is("5.0 3.0 * 2.0 7.0 / +"));
        assertThat(pfs, notNullValue());   
        
        infixExpr = pfs.toExpression(StackType.Infix);
        assertThat(infixExpr, is("5.0 * 3.0 + 2.0 / 7.0") );
        
        iep = new InfixExpressionParser("(5 + 3) * (2 - 7)");
        assertThat(iep, notNullValue());
        
        pr = iep.validateExpression();
        assertThat(pr, notNullValue());
        assertThat(pr.isSuccess(), is(true));
        
        ifs = iep.getInfixStack();
        assertThat(ifs, notNullValue());
        assertThat(ifs.toExpression(true, false, null), is("( 5.0 + 3.0 ) * ( 2.0 - 7.0 )"));
        assertThat(ifs.toExpression(), is("(5.0 + 3.0) * (2.0 - 7.0)"));
              
        pfp = new PostfixStackGenerator(ifs, null);
        assertThat(pfp, notNullValue());
        
        pfs = pfp.getPostfixStack();
        assertThat(pfs.toExpression(), is("5.0 3.0 + 2.0 7.0 - *"));
        assertThat(pfs, notNullValue());   
        
        infixExpr = pfs.toExpression(StackType.Infix);
        assertThat(infixExpr, is("(5.0 + 3.0) * (2.0 - 7.0)") );
        
        iep = new InfixExpressionParser("(-5 + 3) * (2 - 7)");
        assertThat(iep, notNullValue());
        
        pr = iep.validateExpression();
        assertThat(pr, notNullValue());
        assertThat(pr.isSuccess(), is(true));
        
        ifs = iep.getInfixStack();
        assertThat(ifs, notNullValue());
        assertThat(ifs.toExpression(true, false, null), is("( - 5.0 + 3.0 ) * ( 2.0 - 7.0 )"));
        assertThat(ifs.toExpression(), is("(-5.0 + 3.0) * (2.0 - 7.0)"));
              
        pfp = new PostfixStackGenerator(ifs, null);
        assertThat(pfp, notNullValue());
        
        pfs = pfp.getPostfixStack();
        assertThat(pfs.toExpression(), is("5.0 - 3.0 + 2.0 7.0 - *"));
        assertThat(pfs, notNullValue());   
        
        infixExpr = pfs.toExpression(StackType.Infix);
        assertThat(infixExpr, is("(-5.0 + 3.0) * (2.0 - 7.0)") );
    }  
}
