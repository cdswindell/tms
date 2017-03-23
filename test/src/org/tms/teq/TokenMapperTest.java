package org.tms.teq;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNot.not;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.util.List;

import org.junit.AfterClass;
import org.junit.Test;
import org.tms.BaseTest;
import org.tms.api.Table;
import org.tms.api.TableContext;
import org.tms.api.derivables.InvalidExpressionException;
import org.tms.api.derivables.Operator;
import org.tms.api.derivables.Token;
import org.tms.api.derivables.TokenType;
import org.tms.api.factories.TableContextFactory;
import org.tms.api.utils.AbstractOperator;
import org.tms.tds.ContextImpl;
import org.tms.tds.TokenMapper;

public class TokenMapperTest extends BaseTest
{

    @AfterClass
    static public void cleanup()
    {
        TokenMapper tm = TokenMapper.fetchTokenMapper((Table) null);
        tm.deregisterAllOperators();
    }
    
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
        TokenMapper tm = TokenMapper.fetchTokenMapper((Table) null);
        assertThat(tm, notNullValue());
        assertThat(tm.getTableContext(), is(TableContextFactory.fetchDefaultTableContext())); 
        
        TableContext c = TableContextFactory.createTableContext();
        assertThat(c, notNullValue());
        assertThat(c, not(TableContextFactory.fetchDefaultTableContext()));
        
        tm = ((ContextImpl)c).getTokenMapper();
        assertThat(tm, notNullValue());
        assertThat(tm, not(((ContextImpl)TableContextFactory.fetchDefaultTableContext()).getTokenMapper()));
    }

    @Test
    public final void testRegisterOperator()
    throws PendingDerivationException, BlockedDerivationException
    {
    	TableContext tc = TableContextFactory.fetchDefaultTableContext();
        assertThat(tc, notNullValue());
        
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
        tc.registerOperator(sOp);
        
        // reparse expression
        pse = new PostfixStackEvaluator("square(-6)", null);
        assertThat(pse, notNullValue());
        
        Token t = pse.evaluate();
        assertThat(t, notNullValue());
        assertThat(t.isNumeric(), is(true));
        assertThat(t.getValue(), is(36.0));
        
        tc.deregisterOperator(sOp);
        
        // register new operator
        Add3 add3 = new Add3();
        tc.registerOperator(add3);
        
        // reparse expression
        pse = new PostfixStackEvaluator("add3(1,2,3)", null);
        assertThat(pse, notNullValue());
        
        t = pse.evaluate();
        assertThat(t, notNullValue());
        assertThat(t.isNumeric(), is(true));
        assertThat(t.getValue(), is(6.0));
        
        tc.deregisterAllOperators();
    }
    
    @Test
    public final void testLamdaRegisterOperator()
    throws PendingDerivationException, BlockedDerivationException
    {
    	TableContext tc = TableContextFactory.fetchDefaultTableContext();
        assertThat(tc, notNullValue());
        
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
        
        // register new operators
        tc.registerNumericOperator("square", (x)->x*x);
        tc.registerNumericOperator("cube", (x)->x*x*x);
        
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
        tc.deregisterAllOperators();
        try {
            pse = new PostfixStackEvaluator("square(-6)", null);
            fail("square operator resolved");
        }
        catch (InvalidExpressionException e) {
            ParseResult pr = e.getParseResult();
            assertThat(pr, notNullValue());
            assertThat(pr.getParserStatusCode(), is(ParserStatusCode.NoSuchOperator));
        }
        
        // try again with unary function (Function) lambda expression
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
        
        tc.deregisterAllOperators();
        
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
        
    	TableContext tc = TableContextFactory.fetchDefaultTableContext();
        assertThat(tc, notNullValue());
        
        // register new operator
		AddStringNum plusOverload = new AddStringNum();
        tc.overloadOperator("+", plusOverload);
        
        pse = new PostfixStackEvaluator("'5' + 6 * 2 + ('2' + 1)", null);
        assertThat(pse, notNullValue());
        
        t = pse.evaluate();
        assertThat(t, notNullValue());
        assertThat(t.isNumeric(), is(true));
        assertThat(t.getValue(), is(20.0));
        
        tc.unOverloadOperator("+", plusOverload);       
        
        // overload operator using Lambda
        TokenMapper tm = TokenMapper.fetchTokenMapper((Table) null);
        assertThat(tm, notNullValue());
        assertThat(tm.getTableContext(), is(tc)); 
        
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
    	TableContext tc = TableContextFactory.fetchDefaultTableContext();
        assertThat(tc, notNullValue());
        
        // register new operator
        tc.registerGroovyOperator("greet", new Class<?>[]{String.class}, String.class, qualifiedFileName("person.groovy"));
        
        PostfixStackEvaluator pse = new PostfixStackEvaluator("greet(\"Dave\")", null);
        assertThat(pse, notNullValue());
        
        Token t = pse.evaluate();
        assertThat(t, notNullValue());
        assertThat(t.isNumeric(), is(false));
        assertThat(t.isString(), is(true));
        assertThat(t.getValue(), is("Hello Dave!!!")); 
        
        // register new operator
        tc.registerGroovyOperators(qualifiedFileName("geomFuncs.groovy"));
        
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
        
        tc.registerGroovyOperators(qualifiedFileName("factors.groovy"));

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
        
        tc.registerGroovyOperators("class myMath { double addIt(double x, double y, double z){x + y + z}\n double subIt(double x, double y){x - y} }");

        pse = new PostfixStackEvaluator("addIt(3, 4, 2) + subIt(5, 7)", null);
        assertThat(pse, notNullValue());
        
        t = pse.evaluate();
        assertThat(t, notNullValue());
        assertThat(t.isNumeric(), is(true));
        assertThat(t.isString(), is(false));
        assertThat(t.getValue(), is(7.0));       
        
        tc.deregisterAllOperators();
    }
        
    @Test
    public final void testJythonOperator()
    throws PendingDerivationException, BlockedDerivationException
    {
    	TableContext tc = TableContextFactory.fetchDefaultTableContext();
        assertThat(tc, notNullValue());
        
        // register new operator, class implements Operator
        tc.registerJythonOperators(qualifiedFileName("dayChg.jy"));
        
        PostfixStackEvaluator pse = new PostfixStackEvaluator("dayChg('ctct')", null);
        assertThat(pse, notNullValue());
        
        // register new operator, class consists of methods
        tc.registerJythonOperators(qualifiedFileName("factors.jy"));
        
        pse = new PostfixStackEvaluator("firstFactor(81)", null);
        assertThat(pse, notNullValue());
        
        Token resToken = pse.evaluate();
        assertThat(resToken, notNullValue());
        assertThat(3.0, is(resToken.getNumericValue()));
        
        // register new operator, class consists of methods
        tc.registerJythonOperators(qualifiedFileName("volumeOpAsync.jy"));
        
        pse = new PostfixStackEvaluator("volumeAsync(2, 3, 4)", null);
        assertThat(pse, notNullValue());
        
        try {
        	resToken = pse.evaluate();
        	fail("Calculation not asyncronous");
        }
        catch (PendingDerivationException pe) {
        	assertThat(pe, notNullValue());
        	assertThat(pe.getAwaitingState().getPendingIntermediate(), notNullValue());
        }
        
        // register new operator, class consists of methods
        tc.registerJythonOperators(qualifiedFileName("volumeOp.jy"));
        
        pse = new PostfixStackEvaluator("volume(3, 4, 5)", null);
        assertThat(pse, notNullValue());
        
        resToken = pse.evaluate();
        assertThat(resToken, notNullValue());
        assertThat(60.0, is(resToken.getNumericValue()));
        
        // register new operator, file consists of functions
        tc.registerJythonOperators(qualifiedFileName("geomFuncs.jy"));
        
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
        
        tc.deregisterAllOperators();
    }
    
    @Test
    public void testGetCategories()
    {
    	TableContext tc = TableContextFactory.fetchDefaultTableContext();
        assertThat(tc, notNullValue());
        
        List<String> cats = tc.getOperatorCategories();
        assertNotNull(cats);
        assertThat(false, is(cats.isEmpty()));
        assertThat(true, is(cats.contains("Math")));
        assertThat(true, is(cats.contains("Financial")));
    }
    
    @Test
    public void testRegisteredOpCategories()
    {
    	TableContext tc = TableContextFactory.fetchDefaultTableContext();
        assertThat(tc, notNullValue());
        
        List<String> cats = tc.getOperatorCategories();
        assertNotNull(cats);
        assertThat(false, is(cats.isEmpty()));
        assertThat(false, is(cats.contains("Special Math")));
        
        Operator op = new Square();
        tc.registerOperator(op);
        
        cats = tc.getOperatorCategories();
        assertNotNull(cats);
        assertThat(false, is(cats.isEmpty()));
        assertThat(true, is(cats.contains("Special Math")));
        
        tc.deregisterOperator(op);
        cats = tc.getOperatorCategories();
        assertNotNull(cats);
        assertThat(false, is(cats.isEmpty()));
        assertThat(false, is(cats.contains("Special Math")));   
    }
    
    @Test
    public void testRegisteredFetchByCategory()
    {
    	TableContext tc = TableContextFactory.fetchDefaultTableContext();
        assertThat(tc, notNullValue());
        
        List<String> cats = tc.getOperatorCategories();
        assertNotNull(cats);
        assertThat(false, is(cats.isEmpty()));
        assertThat(false, is(cats.contains("Special Math")));
        
        Operator op = new Square();
        tc.registerOperator(op);
        
        cats = tc.getOperatorCategories();
        assertNotNull(cats);
        assertThat(false, is(cats.isEmpty()));
        assertThat(true, is(cats.contains("Special Math")));
        
        List<Operator> ops = tc.getOperatorsForCategory("special math");
        assertNotNull(ops);
        assertThat(false, is(ops.isEmpty()));
        assertThat(true, is(ops.contains(op)));
        
        tc.deregisterOperator(op);
    }
    
    @Test
    public void testRegisteredFetchByMathCategory()
    {
    	TableContext tc = TableContextFactory.fetchDefaultTableContext();
        assertThat(tc, notNullValue());
        
        List<Operator> ops = tc.getOperatorsForCategory("math");
        assertNotNull(ops);
        assertThat(false, is(ops.isEmpty()));
        
        for (Operator op : BuiltinOperator.values()) {
        	if (isCategorized(op, "math")) {
                assertThat(true, is(ops.contains(op)));
                ops.remove(op);
        	}
        }
        
        assertThat(true, is(ops.isEmpty()));
    }
    
    private boolean isCategorized(Operator op, String cat) 
    {
		if (op == null || op.getCategories() == null || op.getCategories().length <= 0)
			return false;
		
		for (String s : op.getCategories()) {
			if (cat.equalsIgnoreCase(s))
				return true;
		}
		
		return false;
	}

	public class Square extends AbstractOperator
    {
		public Square()
		{
			super("square", new Class<?>[] {double.class}, double.class);
		}

        @Override
        public TokenType getTokenType()
        {
            return TokenType.UnaryFunc;
        }

        @Override
        public Token evaluate(Token... args)
        {
            assert args != null && args.length == 1;
            
            double d = args[0].getNumericValue();
            
            return new Token(d * d);
        }
        
        @Override
        public String[] getCategories()
        {
        	return new String [] {"Special Math"};
        }
    }
    
    public class Add3 extends AbstractOperator
    {
    	public Add3()
    	{
    		super("add3", new Class<?>[] {double.class, double.class, double.class}, double.class);
    	}
    	
        @Override
        public TokenType getTokenType()
        {
            return TokenType.GenericFunc;
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
    }
    
    public class AddStringNum extends AbstractOperator
    {
    	public AddStringNum()
    	{
    		super("+", new Class<?>[] {String.class, Double.class}, double.class);
    	}
    	
        @Override
        public TokenType getTokenType()
        {
            return TokenType.BinaryOp;
        }

        @Override
        public Token evaluate(Token... args)
        {
            assert args != null && args.length == 2;
            
            String s1 = args[0].getStringValue();
            double d2 = args[1].getNumericValue();
            
            return new Token(Double.valueOf(s1) + d2);
        }
    }
}
