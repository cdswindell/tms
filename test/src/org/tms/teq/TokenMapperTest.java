package org.tms.teq;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNot.not;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import org.junit.Test;
import org.tms.api.Operator;
import org.tms.api.Table;
import org.tms.api.TableContext;
import org.tms.api.exceptions.InvalidExpressionException;
import org.tms.api.factories.TableContextFactory;

public class TokenMapperTest
{

    @Test
    public final void testFetchTokenMapperTable()
    {
        TokenMapper tm = TokenMapper.fetchTokenMapper((Table) null);
        assertThat(tm, notNullValue());
        assertThat(tm.getTableContext(), is(TableContextFactory.fetchDefaultTableContext()));       
    }

    @Test
    public final void testFetchTokenMapperTableContext()
    {
        TokenMapper tm = TokenMapper.fetchTokenMapper((TableContext) null);
        assertThat(tm, notNullValue());
        assertThat(tm.getTableContext(), is(TableContextFactory.fetchDefaultTableContext())); 
        
        TableContext c = TableContextFactory.createTableContext();
        assertThat(c, notNullValue());
        assertThat(c, not(TableContextFactory.fetchDefaultTableContext()));
        
        tm = c.getTokenMapper();
        assertThat(tm, notNullValue());
        assertThat(tm, not(TableContextFactory.fetchDefaultTableContext().getTokenMapper()));
    }

    @Test
    public final void testRegisterOperator()
    throws PendingDerivationException
    {
        TokenMapper tm = TokenMapper.fetchTokenMapper((TableContext) null);
        assertThat(tm, notNullValue());
        assertThat(tm.getTableContext(), is(TableContextFactory.fetchDefaultTableContext())); 
        
        PostfixStackEvaluator pse = null;
        try {
            pse = new PostfixStackEvaluator("square(-6)", null);
            fail("square operator resolved");
        }
        catch (InvalidExpressionException e) {
            ParseResult pr = e.getParseResult();
            assertThat(pr, notNullValue());
            assertThat(pr.getParserStatusCode(), is(ParserStatusCode.NoSuchOperator));
        }
        
        // register new operator
        Square sOp = new Square();
        tm.registerOperator(sOp);
        
        // reparse expression
        pse = new PostfixStackEvaluator("square(-6)", null);
        assertThat(pse, notNullValue());
        
        Token t = pse.evaluate();
        assertThat(t, notNullValue());
        assertThat(t.isNumeric(), is(true));
        assertThat(t.getValue(), is(36.0));
        
        // register new operator
        Add3 add3 = new Add3();
        tm.registerOperator(add3);
        
        // reparse expression
        pse = new PostfixStackEvaluator("add3(1,2,3)", null);
        assertThat(pse, notNullValue());
        
        t = pse.evaluate();
        assertThat(t, notNullValue());
        assertThat(t.isNumeric(), is(true));
        assertThat(t.getValue(), is(6.0));
    }
    
    @Test
    public final void testOverloadOperator()
    throws PendingDerivationException
    {
        TokenMapper tm = TokenMapper.fetchTokenMapper((TableContext) null);
        assertThat(tm, notNullValue());
        assertThat(tm.getTableContext(), is(TableContextFactory.fetchDefaultTableContext())); 
        
        // register new operator
		AddStringNum plusOverload = new AddStringNum();
        tm.overloadOperator("+", plusOverload);
        
        PostfixStackEvaluator pse = new PostfixStackEvaluator("'5' + 6 * 2 + ('2' + 1)", null);
        assertThat(pse, notNullValue());
        
        Token t = pse.evaluate();
        assertThat(t, notNullValue());
        assertThat(t.isNumeric(), is(true));
        assertThat(t.getValue(), is(20.0));
    }
        
    public class Square implements Operator
    {

        @Override
        public TokenType getTokenType()
        {
            return TokenType.UnaryFunc;
        }

        @Override
        public String getLabel()
        {
            return "square";
        }

        @Override
        public Class<?>[] getArgTypes()
        {
            return new Class<?>[] {double.class};
        }

        @Override
        public Token evaluate(Token... args)
        {
            assert args != null && args.length == 1;
            
            double d = args[0].getNumericValue();
            
            return new Token(d * d);
        }

        public Class<?> getResultType()
        {
            return double.class;
        }       
    }
    
    public class Add3 implements Operator
    {
        @Override
        public TokenType getTokenType()
        {
            return TokenType.GenericFunc;
        }

        @Override
        public String getLabel()
        {
            return "add3";
        }

        @Override
        public Class<?>[] getArgTypes()
        {
            return new Class<?>[] {double.class, double.class, double.class};
        }

        @Override
        public Token evaluate(Token... args)
        {
            assert args != null && args.length == 3;
            
            double d1 = args[0].getNumericValue();
            double d2 = args[1].getNumericValue();
            double d3 = args[2].getNumericValue();
            
            return new Token(d1 + d2 + d3);
        }

        public Class<?> getResultType()
        {
            return double.class;
        }       
    }
    
    public class AddStringNum implements Operator
    {
        @Override
        public TokenType getTokenType()
        {
            return TokenType.BinaryOp;
        }

        @Override
        public String getLabel()
        {
            return "+";
        }

        @Override
        public Class<?>[] getArgTypes()
        {
            return new Class<?>[] {String.class, Double.class};
        }

        @Override
        public Token evaluate(Token... args)
        {
            assert args != null && args.length == 2;
            
            String s1 = args[0].getStringValue();
            double d2 = args[1].getNumericValue();
            
            return new Token(Double.valueOf(s1) + d2);
        }

        public Class<?> getResultType()
        {
            return double.class;
        }       
    }
}
