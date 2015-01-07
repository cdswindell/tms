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
import org.tms.api.TableContextFactory;
import org.tms.api.exceptions.InvalidExpressionException;

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
        tm.registerOperator(TokenType.UnaryFunc, sOp);
        
        // reparse expression
        pse = new PostfixStackEvaluator("square(-6)", null);
        assertThat(pse, notNullValue());
        
        Token t = pse.evaluate();
        assertThat(t, notNullValue());
        assertThat(t.isNumeric(), is(true));
        assertThat(t.getValue(), is(36.0));
        
        // register new operator
        Add3 add3 = new Add3();
        tm.registerOperator(TokenType.GenericFunc, add3);
        
        // reparse expression
        pse = new PostfixStackEvaluator("add3(1,2,3)", null);
        assertThat(pse, notNullValue());
        
        t = pse.evaluate();
        assertThat(t, notNullValue());
        assertThat(t.isNumeric(), is(true));
        assertThat(t.getValue(), is(6.0));
    }
    
    public class Square implements Operator
    {

        @Override
        public TokenType getTokenType()
        {
            return TokenType.UnaryFunc;
        }

        @Override
        public int getPriority()
        {
            return 5;
        }

        @Override
        public String getLabel()
        {
            return "square";
        }

        @Override
        public int numArgs()
        {
            return 1;
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
    }

    
    public class Add3 implements Operator
    {

        @Override
        public TokenType getTokenType()
        {
            return TokenType.GenericFunc;
        }

        @Override
        public int getPriority()
        {
            return 5;
        }

        @Override
        public String getLabel()
        {
            return "add3";
        }

        @Override
        public int numArgs()
        {
            return 3;
        }

        @Override
        public Class<?>[] getArgTypes()
        {
            return new Class<?>[] {double.class, double.class, double.class};
        }

        @Override
        public Token evaluate(Token... args)
        {
            assert args != null && args.length == 1;
            
            double d1 = args[0].getNumericValue();
            double d2 = args[1].getNumericValue();
            double d3 = args[2].getNumericValue();
            
            return new Token(d1 + d2 + d3);
        }       
    }

}
