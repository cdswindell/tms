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

}
