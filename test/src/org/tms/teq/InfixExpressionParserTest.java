package org.tms.teq;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertThat;

import org.junit.Test;

public class InfixExpressionParserTest
{

    @Test
    public final void testValidateSimpleNumericExpression()
    {
        InfixExpressionParser iep = new InfixExpressionParser("123");
        assertThat(iep, notNullValue());
        
        ParseResult pr = iep.validateExpression();
        assertThat(pr, notNullValue());
        assertThat(pr.isSuccess(), is(true));

        iep = new InfixExpressionParser("123.45");
        assertThat(iep, notNullValue());
        
        pr = iep.validateExpression();
        assertThat(pr, notNullValue());
        assertThat(pr.isSuccess(), is(true));

        iep = new InfixExpressionParser("123.45e23");
        assertThat(iep, notNullValue());
        
        pr = iep.validateExpression();
        assertThat(pr, notNullValue());
        assertThat(pr.isSuccess(), is(true));

        iep = new InfixExpressionParser("123.e23");
        assertThat(iep, notNullValue());
        
        pr = iep.validateExpression();
        assertThat(pr, notNullValue());
        assertThat(pr.isSuccess(), is(true));

        iep = new InfixExpressionParser("456.45e-23");
        assertThat(iep, notNullValue());
        
        pr = iep.validateExpression();
        assertThat(pr, notNullValue());
        assertThat(pr.isSuccess(), is(true));

        iep = new InfixExpressionParser("+123.45");
        assertThat(iep, notNullValue());
        
        pr = iep.validateExpression();
        assertThat(pr, notNullValue());
        assertThat(pr.isSuccess(), is(true));

        iep = new InfixExpressionParser("-123.45");
        assertThat(iep, notNullValue());
        
        pr = iep.validateExpression();
        assertThat(pr, notNullValue());
        assertThat(pr.isSuccess(), is(true));

        iep = new InfixExpressionParser("455 - 123.45");
        assertThat(iep, notNullValue());
        
        pr = iep.validateExpression();
        assertThat(pr, notNullValue());
        assertThat(pr.isSuccess(), is(true));

        iep = new InfixExpressionParser("455 - (123.45 * 3.5)");
        assertThat(iep, notNullValue());
        
        pr = iep.validateExpression();
        assertThat(pr, notNullValue());
        assertThat(pr.isSuccess(), is(true));

        iep = new InfixExpressionParser("2 + 3^5 - 6 % 4*3 *(2+6)");
        assertThat(iep, notNullValue());
        
        pr = iep.validateExpression();
        assertThat(pr, notNullValue());
        assertThat(pr.isSuccess(), is(true));
    }

    @Test
    public final void testInvalidNumericExpressions()
    {
        InfixExpressionParser iep = new InfixExpressionParser("123E");
        assertThat(iep, notNullValue());
        
        ParseResult pr = iep.validateExpression();
        assertThat(pr, notNullValue());
        assertThat(pr.isSuccess(), is(false));
        assertThat(pr.isFailure(), is(true));
        assertThat(pr.getParserStatusCode(), is(ParserStatusCode.InvalidNumericExpression));
        
        iep = new InfixExpressionParser("123.456.578");
        assertThat(iep, notNullValue());
        
        pr = iep.validateExpression();
        assertThat(pr, notNullValue());
        assertThat(pr.isSuccess(), is(false));
        assertThat(pr.isFailure(), is(true));
        assertThat(pr.getParserStatusCode(), is(ParserStatusCode.InvalidOperandLocation));
        
        iep = new InfixExpressionParser("123.456e+.25");
        assertThat(iep, notNullValue());
        
        pr = iep.validateExpression();
        assertThat(pr, notNullValue());
        assertThat(pr.isSuccess(), is(false));
        assertThat(pr.isFailure(), is(true));
        assertThat(pr.getParserStatusCode(), is(ParserStatusCode.InvalidNumericExpression));
        
        iep = new InfixExpressionParser("e25");
        assertThat(iep, notNullValue());
        
        pr = iep.validateExpression();
        assertThat(pr, notNullValue());
        assertThat(pr.isSuccess(), is(false));
        assertThat(pr.isFailure(), is(true));
        assertThat(pr.getParserStatusCode(), is(ParserStatusCode.NoSuchOperator));
        
        iep = new InfixExpressionParser("123e.25");
        assertThat(iep, notNullValue());
        
        pr = iep.validateExpression();
        assertThat(pr, notNullValue());
        assertThat(pr.isSuccess(), is(false));
        assertThat(pr.isFailure(), is(true));
        assertThat(pr.getParserStatusCode(), is(ParserStatusCode.InvalidNumericExpression));
    }
    
    @Test
    public final void testParseInfixExpression()
    {
        InfixExpressionParser iep = new InfixExpressionParser("123+456+789");
        assertThat(iep, notNullValue());
        
        ParseResult pr = iep.validateExpression();
        assertThat(pr, notNullValue());
        assertThat(pr.isSuccess(), is(true));
        assertThat(iep.getInfixStack().size(), is(5));
        assertThat(iep.parsedInfixExpression(), is("123.0 + 456.0 + 789.0"));

        iep = new InfixExpressionParser("123.45 * (3e6 + 4/2) - 1");
        assertThat(iep, notNullValue());
        
        pr = iep.validateExpression();
        assertThat(pr, notNullValue());
        assertThat(pr.isSuccess(), is(true));
        assertThat(iep.getInfixStack().size(), is(11));
    } 
    
    @Test
    public final void testAlaisedOperastors()
    {
        InfixExpressionParser iep = new InfixExpressionParser("5 % 2 + mod(7,5)");
        assertThat(iep, notNullValue());
        
        ParseResult pr = iep.validateExpression();
        assertThat(pr, notNullValue());
        assertThat(pr.isSuccess(), is(true));
        assertThat(iep.getInfixStack().size(), is(10));
        assertThat(iep.parsedInfixExpression(), is("5.0 % 2.0 + mod ( 7.0 , 5.0 )"));
    }  
    
    @Test
    public final void testTextParsing()
    {
        InfixExpressionParser iep = new InfixExpressionParser("'abc'");
        assertThat(iep, notNullValue());
        
        ParseResult pr = iep.validateExpression();
        assertThat(pr, notNullValue());
        assertThat(pr.isSuccess(), is(true));
        assertThat(iep.getInfixStack().size(), is(1));
        assertThat(iep.parsedInfixExpression(), is("\"abc\""));
        
        iep = new InfixExpressionParser("'abc' + 'def'");
        assertThat(iep, notNullValue());
        
        pr = iep.validateExpression();
        assertThat(pr, notNullValue());
        assertThat(pr.isSuccess(), is(true));
        assertThat(iep.getInfixStack().size(), is(3));
        assertThat(iep.parsedInfixExpression(), is("\"abc\" + \"def\""));
        
        // negative tests
        iep = new InfixExpressionParser("'abc' + 'def");
        assertThat(iep, notNullValue());
        
        pr = iep.validateExpression();
        assertThat(pr, notNullValue());
        assertThat(pr.isSuccess(), is(false));
        assertThat(pr.getParserStatusCode(), is(ParserStatusCode.SingletonQuote));
        
        // negative tests
        iep = new InfixExpressionParser("'abc' + def'");
        assertThat(iep, notNullValue());
        
        pr = iep.validateExpression();
        assertThat(pr, notNullValue());
        assertThat(pr.isSuccess(), is(false));
        assertThat(pr.getParserStatusCode(), is(ParserStatusCode.NoSuchOperator));
    }  
}
