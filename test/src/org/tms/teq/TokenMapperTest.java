package org.tms.teq;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNot.not;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.util.List;

import org.junit.Test;
import org.tms.BaseTest;
import org.tms.api.Table;
import org.tms.api.TableContext;
import org.tms.api.derivables.Operator;
import org.tms.api.derivables.Token;
import org.tms.api.derivables.TokenMapper;
import org.tms.api.derivables.TokenType;
import org.tms.api.factories.TableContextFactory;
import org.tms.teq.exceptions.InvalidExpressionExceptionImpl;

public class TokenMapperTest extends BaseTest
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
    throws PendingDerivationException, BlockedDerivationException
    {
        TokenMapper tm = TokenMapper.fetchTokenMapper((TableContext) null);
        assertThat(tm, notNullValue());
        assertThat(tm.getTableContext(), is(TableContextFactory.fetchDefaultTableContext())); 
        
        PostfixStackEvaluator pse = null;
        try {
            pse = new PostfixStackEvaluator("square(-6)", null);
            fail("square operator resolved");
        }
        catch (InvalidExpressionExceptionImpl e) {
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
        
        tm.deregisterOperator(sOp);
        
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
        
        tm.deregisterAllOperators();
    }
    
    @Test
    public final void testLamdaRegisterOperator()
    throws PendingDerivationException, BlockedDerivationException
    {
        TokenMapper tm = TokenMapper.fetchTokenMapper((TableContext) null);
        assertThat(tm, notNullValue());
        assertThat(tm.getTableContext(), is(TableContextFactory.fetchDefaultTableContext())); 
        
        PostfixStackEvaluator pse = null;
        try {
            pse = new PostfixStackEvaluator("square(-6)", null);
            fail("square operator resolved");
        }
        catch (InvalidExpressionExceptionImpl e) {
            ParseResult pr = e.getParseResult();
            assertThat(pr, notNullValue());
            assertThat(pr.getParserStatusCode(), is(ParserStatusCode.NoSuchOperator));
        }
        
        // register new operators
        tm.registerNumericOperator("square", (x)->x*x);
        tm.registerNumericOperator("cube", (x)->x*x*x);
        
        // reparse expression
        pse = new PostfixStackEvaluator("square(-6)", null);
        assertThat(pse, notNullValue());
        
        Token t = pse.evaluate();
        assertThat(t, notNullValue());
        assertThat(t.isNumeric(), is(true));
        assertThat(t.getValue(), is(36.0));
        
        // try cube
        pse = new PostfixStackEvaluator("cube(-3)", null);
        assertThat(pse, notNullValue());
        
        t = pse.evaluate();
        assertThat(t, notNullValue());
        assertThat(t.isNumeric(), is(true));
        assertThat(t.getValue(), is(-27.0));
        
        // remove operator and reparse
        tm.deregisterAllOperators();
        try {
            pse = new PostfixStackEvaluator("square(-6)", null);
            fail("square operator resolved");
        }
        catch (InvalidExpressionExceptionImpl e) {
            ParseResult pr = e.getParseResult();
            assertThat(pr, notNullValue());
            assertThat(pr.getParserStatusCode(), is(ParserStatusCode.NoSuchOperator));
        }
        
        // try again with unary function (Function) lambda expression
        TableContext tc = tm.getTableContext();
        tc.registerOperator("square", Double.class, Double.class, (Double x)->String.valueOf(x*x));
        
        pse = new PostfixStackEvaluator("square(-6)", null);
        assertThat(pse, notNullValue());
        
        t = pse.evaluate();
        assertThat(t, notNullValue());
        assertThat(t.isString(), is(true));
        assertThat(t.getValue(), is("36.0"));
        
        // Test binary function (BiFunction) lambda expression
        tc.registerOperator("multStr", String.class, String.class, Double.class, (String x, String y)->Double.valueOf(x) * Double.valueOf(y));
        
        pse = new PostfixStackEvaluator("multStr('20', '3')", null);
        assertThat(pse, notNullValue());
        
        t = pse.evaluate();
        assertThat(t, notNullValue());
        assertThat(t.isNumeric(), is(true));
        assertThat(t.getValue(), is(60.0));
        
        tm.deregisterAllOperators();
        
        // reevaluate again, should succeed
        t = pse.evaluate();
        assertThat(t, notNullValue());
        assertThat(t.isNumeric(), is(true));
        assertThat(t.getValue(), is(60.0));
    }
    
    @Test
    public final void testOverloadOperator()
    throws PendingDerivationException, BlockedDerivationException
    {
        PostfixStackEvaluator pse = new PostfixStackEvaluator("'5' + 6 * 2 + ('2' + 1)", null);
        assertThat(pse, notNullValue());
        
        Token t = pse.evaluate();
        assertThat(t, notNullValue());
        assertThat(t.isNumeric(), is(false));
        assertThat(t.getValue(), is("512.021.0"));
        
        TokenMapper tm = TokenMapper.fetchTokenMapper((TableContext) null);
        assertThat(tm, notNullValue());
        assertThat(tm.getTableContext(), is(TableContextFactory.fetchDefaultTableContext())); 
        
        // register new operator
		AddStringNum plusOverload = new AddStringNum();
        tm.overloadOperator("+", plusOverload);
        
        pse = new PostfixStackEvaluator("'5' + 6 * 2 + ('2' + 1)", null);
        assertThat(pse, notNullValue());
        
        t = pse.evaluate();
        assertThat(t, notNullValue());
        assertThat(t.isNumeric(), is(true));
        assertThat(t.getValue(), is(20.0));
        
        // overload operator using Lambda
        tm.unOverloadOperator("+", plusOverload);
        tm.overloadOperator("+", String.class, Double.class, Double.class,
                (String x, Double y)->Double.valueOf(x) + y);
        
        pse = new PostfixStackEvaluator("'5' + 6 * 2 + ('2' + 1)", null);       
        assertThat(pse, notNullValue());
        
        t = pse.evaluate();
        assertThat(t, notNullValue());
        assertThat(t.isNumeric(), is(true));
        assertThat(t.getValue(), is(20.0));
    }
        
    @Test
    public final void testGroovyOperator()
    throws PendingDerivationException, BlockedDerivationException
    {
        TokenMapper tm = TokenMapper.fetchTokenMapper((TableContext) null);
        assertThat(tm, notNullValue());
        assertThat(tm.getTableContext(), is(TableContextFactory.fetchDefaultTableContext())); 
        
        // register new operator
        tm.registerGroovyOperator("greet", new Class<?>[]{String.class}, String.class, qualifiedFileName("person.groovy"));
        
        PostfixStackEvaluator pse = new PostfixStackEvaluator("greet(\"Dave\")", null);
        assertThat(pse, notNullValue());
        
        Token t = pse.evaluate();
        assertThat(t, notNullValue());
        assertThat(t.isNumeric(), is(false));
        assertThat(t.isString(), is(true));
        assertThat(t.getValue(), is("Hello Dave!!!")); 
        
        // register new operator
        tm.registerGroovyOperators(qualifiedFileName("geomFuncs.groovy"));
        
        pse = new PostfixStackEvaluator("volume(2, 3, 4)", null);
        assertThat(pse, notNullValue());
        
        t = pse.evaluate();
        assertThat(t, notNullValue());
        assertThat(t.isNumeric(), is(true));
        assertThat(t.isString(), is(false));
        assertThat(t.getValue(), is(24.0));        
        
        pse = new PostfixStackEvaluator("area(2, 3)", null);
        assertThat(pse, notNullValue());
        
        t = pse.evaluate();
        assertThat(t, notNullValue());
        assertThat(t.isNumeric(), is(true));
        assertThat(t.isString(), is(false));
        assertThat(t.getValue(), is(6.0));        
        
        pse = new PostfixStackEvaluator("perimeter(2, 3)", null);
        assertThat(pse, notNullValue());
        
        t = pse.evaluate();
        assertThat(t, notNullValue());
        assertThat(t.isNumeric(), is(true));
        assertThat(t.isString(), is(false));
        assertThat(t.getValue(), is(10.0));        
        
        pse = new PostfixStackEvaluator("circumference(2)", null);
        assertThat(pse, notNullValue());
        
        t = pse.evaluate();
        assertThat(t, notNullValue());
        assertThat(t.isNumeric(), is(true));
        assertThat(t.isString(), is(false));
        assertThat(t.getValue(), is(2 * Math.PI));             
        
        tm.registerGroovyOperators(qualifiedFileName("factors.groovy"));

        pse = new PostfixStackEvaluator("firstFactor(91)", null);
        assertThat(pse, notNullValue());
        
        t = pse.evaluate();
        assertThat(t, notNullValue());
        assertThat(t.isNumeric(), is(true));
        assertThat(t.isString(), is(false));
        assertThat(t.getValue(), is(7.0));        

        pse = new PostfixStackEvaluator("lastFactor(91)", null);
        assertThat(pse, notNullValue());
        
        t = pse.evaluate();
        assertThat(t, notNullValue());
        assertThat(t.isNumeric(), is(true));
        assertThat(t.isString(), is(false));
        assertThat(t.getValue(), is(13.0));
        
        pse = new PostfixStackEvaluator("allFactors(91)", null);
        assertThat(pse, notNullValue());
        
        t = pse.evaluate();
        assertThat(t, notNullValue());
        assertThat(t.isA(List.class), is(true));
        
        @SuppressWarnings("unchecked")
        List<Integer> list = (List<Integer>)t.getValue();
        assertThat(list.size(), is(2));
        assertThat(list.contains(7), is(true));
        assertThat(list.contains(13), is(true));
        
        tm.registerGroovyOperators("class myMath { double addIt(double x, double y, double z){x + y + z}\n double subIt(double x, double y){x - y} }");

        pse = new PostfixStackEvaluator("addIt(3, 4, 2) + subIt(5, 7)", null);
        assertThat(pse, notNullValue());
        
        t = pse.evaluate();
        assertThat(t, notNullValue());
        assertThat(t.isNumeric(), is(true));
        assertThat(t.isString(), is(false));
        assertThat(t.getValue(), is(7.0));        
    }
        
    @Test
    public final void testJythonOperator()
    throws PendingDerivationException, BlockedDerivationException
    {
        TokenMapper tm = TokenMapper.fetchTokenMapper((TableContext) null);
        assertThat(tm, notNullValue());
        assertThat(tm.getTableContext(), is(TableContextFactory.fetchDefaultTableContext())); 
        
        // register new operator, class implements Operator
        tm.registerJythonOperators(qualifiedFileName("dayChg.jy"));
        
        PostfixStackEvaluator pse = new PostfixStackEvaluator("dayChg('ctct')", null);
        assertThat(pse, notNullValue());
        
        // register new operator, class consists of methods
        tm.registerJythonOperators(qualifiedFileName("factors.jy"));
        
        pse = new PostfixStackEvaluator("firstFactor(81)", null);
        assertThat(pse, notNullValue());
        
        Token resToken = pse.evaluate();
        assertThat(resToken, notNullValue());
        assertThat(3.0, is(resToken.getNumericValue()));
        
        // register new operator, class consists of methods
        tm.registerJythonOperators(qualifiedFileName("volumeOp.jy"));
        
        pse = new PostfixStackEvaluator("volume(3, 4, 5)", null);
        assertThat(pse, notNullValue());
        
        resToken = pse.evaluate();
        assertThat(resToken, notNullValue());
        assertThat(60.0, is(resToken.getNumericValue()));
        
        // register new operator, file consists of functions
        tm.registerJythonOperators(qualifiedFileName("geomFuncs.jy"));
        
        pse = new PostfixStackEvaluator("area(5, 4)", null);
        assertThat(pse, notNullValue());
        
        resToken = pse.evaluate();
        assertThat(resToken, notNullValue());
        assertThat(20.0, is(resToken.getNumericValue()));
        
        pse = new PostfixStackEvaluator("perimeter(5, 4)", null);
        assertThat(pse, notNullValue());
        
        resToken = pse.evaluate();
        assertThat(resToken, notNullValue());
        assertThat(18.0, is(resToken.getNumericValue()));
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
