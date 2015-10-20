package org.tms.teq;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.tms.api.Access;
import org.tms.api.Column;
import org.tms.api.Row;
import org.tms.api.Table;
import org.tms.api.factories.TableFactory;

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

        iep = new InfixExpressionParser("2 + 3^5 - mod(6, 4*3 *(2+6))");
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
        InfixExpressionParser iep = new InfixExpressionParser("mod(5, 2) + mod(7,5)");
        assertThat(iep, notNullValue());
        
        ParseResult pr = iep.validateExpression();
        assertThat(pr, notNullValue());
        assertThat(pr.isSuccess(), is(true));
        assertThat(iep.getInfixStack().size(), is(13));
        assertThat(iep.parsedInfixExpression(), is("mod ( 5.0 , 2.0 ) + mod ( 7.0 , 5.0 )"));
        assertThat(iep.getInfixStack().toExpression(false), is("mod(5.0, 2.0) + mod(7.0, 5.0)"));
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
        
        // negative test
        iep = new InfixExpressionParser("sin('abcdef' - 'def')", null);
        assertThat(iep, notNullValue());

        pr = iep.validateExpression();
        assertThat(pr, notNullValue());
        assertThat(pr.isSuccess(), is(true));
        
        PostfixStackGenerator psg = new PostfixStackGenerator(iep);
        assertThat(psg, notNullValue());
        
        pr = psg.convertInfixToPostfix();
        assertThat(pr, notNullValue());
        assertThat(pr.getParserStatusCode(), is(ParserStatusCode.ArgumentTypeMismatch));
    }  
    
    @Test
    public void testColumnReferenceParsing()
    {
        Table t = TableFactory.createTable(10, 10);
        assertThat(t, notNullValue());           
        assertThat(t.getNumColumns(), is(0));
        assertThat(t.getNumRows(), is(0));
        
        Row r10 = t.addRow(Access.ByIndex, 10);
        assertThat(t.getNumRows(), is(10));
        
        Column c1 = t.addColumn(Access.Next);
        assertThat(c1, notNullValue());
        
        Column c2 = t.addColumn(Access.Next);
        assertThat(c2, notNullValue());
        
        Column c3 = t.addColumn(Access.Next);
        assertThat(c3, notNullValue());
        c3.fill(3);
        
        Column c4 = t.addColumn(Access.Next);
        assertThat(c4, notNullValue());
        c4.fill(4);
        
        assertThat(t.getNumColumns(), is(4));       
        assertThat(t.getNumRows(), is(10));   
        
        // test column index references
        c2.setDerivation("col 3 + col 4");
        assertThat(c2.isDerived(), is(true));       
        assertThat(t.getCellValue(r10, c2), is(7.0));  	
    }
    
    @Test
    public final void testCommaParsing()
    {
        // hypot operator
        InfixExpressionParser iep = new InfixExpressionParser("hypot(1 + 2 + 3, 4)");
        assertThat(iep, notNullValue());

        ParseResult pr = iep.validateExpression();
        assertThat(pr, notNullValue());
        assertThat(pr.isSuccess(), is(true));
        
        // positive test 
        iep = new InfixExpressionParser("hypot(4, (1 + 2))");
        assertThat(iep, notNullValue());

        pr = iep.validateExpression();
        assertThat(pr, notNullValue());
        assertThat(pr.isSuccess(), is(true));
        
        // negative test (too many args)
        iep = new InfixExpressionParser("hypot((1 + 2), 4, 7)");
        assertThat(iep, notNullValue());

        pr = iep.validateExpression();
        assertThat(pr, notNullValue());
        assertThat(pr.isSuccess(), is(false));
        assertThat(pr.getParserStatusCode(), is(ParserStatusCode.ArgumentCountMismatch));
        
        // negative test (too many args)
        iep = new InfixExpressionParser("hypot(4, 7, (1 + 2))");
        assertThat(iep, notNullValue());

        pr = iep.validateExpression();
        assertThat(pr, notNullValue());
        assertThat(pr.isSuccess(), is(false));
        assertThat(pr.getParserStatusCode(), is(ParserStatusCode.ArgumentCountMismatch));
        
        
        // negative test (too few args)
        iep = new InfixExpressionParser("3 + hypot(3, hypot(4))");
        assertThat(iep, notNullValue());

        pr = iep.validateExpression();
        assertThat(pr, notNullValue());
        assertThat(pr.isSuccess(), is(true));
        
        PostfixStackGenerator psg = new PostfixStackGenerator(iep);
        assertThat(psg, notNullValue());
        
        pr = psg.convertInfixToPostfix();
        assertThat(pr, notNullValue());
        assertThat(pr.getParserStatusCode(), is(ParserStatusCode.ArgumentCountMismatch));
    }
    
    @Test
    public final void testArgumentTypeChecking()
    {
        // mean operator, too many arguments
        InfixExpressionParser iep = new InfixExpressionParser("mean((1 + 2), 4)");
        assertThat(iep, notNullValue());

        ParseResult pr = iep.validateExpression();
        assertThat(pr, notNullValue());
        assertThat(pr.isSuccess(), is(false));
        assertThat(pr.getParserStatusCode(), is(ParserStatusCode.ArgumentCountMismatch));
        
        // mean operator, wrong argument type
        iep = new InfixExpressionParser("mean(4)");
        assertThat(iep, notNullValue());

        pr = iep.validateExpression();
        assertThat(pr, notNullValue());
        assertThat(pr.isSuccess(), is(true));
        
        PostfixStackGenerator psg = new PostfixStackGenerator(iep);
        assertThat(psg, notNullValue());
        
        pr = psg.convertInfixToPostfix();
        assertThat(pr, notNullValue());
        assertThat(pr.getParserStatusCode(), is(ParserStatusCode.ArgumentTypeMismatch));
        
        // mean operator, wrong argument type
        iep = new InfixExpressionParser("mean('abc')");
        assertThat(iep, notNullValue());

        pr = iep.validateExpression();
        assertThat(pr, notNullValue());
        assertThat(pr.isSuccess(), is(true));
        
        psg = new PostfixStackGenerator(iep);
        assertThat(psg, notNullValue());
        
        pr = psg.convertInfixToPostfix();
        assertThat(pr, notNullValue());
        assertThat(pr.getParserStatusCode(), is(ParserStatusCode.ArgumentTypeMismatch));
    }
}
