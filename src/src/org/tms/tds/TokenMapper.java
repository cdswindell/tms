package org.tms.tds;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.UnaryOperator;

import org.apache.commons.text.WordUtils;
import org.tms.api.Access;
import org.tms.api.Row;
import org.tms.api.Table;
import org.tms.api.TableContext;
import org.tms.api.derivables.GenericFunc3Arg;
import org.tms.api.derivables.GenericFunc4Arg;
import org.tms.api.derivables.GenericFunc5Arg;
import org.tms.api.derivables.Operator;
import org.tms.api.derivables.Token;
import org.tms.api.derivables.TokenType;
import org.tms.api.exceptions.IllegalTableStateException;
import org.tms.api.factories.TableContextFactory;
import org.tms.api.factories.TableFactory;
import org.tms.api.io.TMSOptions;
import org.tms.teq.BuiltinOperator;
import org.tms.teq.ops.BaseOp;
import org.tms.teq.ops.ClassOp;
import org.tms.teq.ops.GroovyOp;
import org.tms.teq.ops.JythonOp;

public class TokenMapper
{
    static final private Map<String, Token> sf_BuiltInTokenMap = new HashMap<String, Token>();  
    static final private Map<Category, Set<Operator>> sf_GlobalOperatorCache = new HashMap<Category, Set<Operator>>();
    static final private Map<String, Category> sf_GlobalCategoryCache = new HashMap<String, Category>();
    
    static {
        for (BuiltinOperator o : BuiltinOperator.values()) {
            if (o.isLabeled()) {
                Set<String> labels = o.getAliases();
                for (String label: labels) {
	                Token t = new Token(label, o.getPrimaryTokenType(), o);
	                sf_BuiltInTokenMap.put(label.toLowerCase(), t);  
                }
            }
            else {
                Set<TokenType> tts = o.getTokenTypes();
                if (tts != null) {
                    for (TokenType tt : tts) {
                        if (!tt.isLabeled()) continue;
                        Set<String> labels = tt.getLabels();
                        for (String label : labels) {                            
                            Token t = new Token(label, tt, o);
                            sf_BuiltInTokenMap.put(label.toLowerCase(), t);  
                        }
                    }
                }               
            } 
            
            cacheGlobalOperatorCategories(o);
        }
    }
    
    static public TokenMapper fetchTokenMapper(Table t)
    {
        TableContext c = t != null ? t.getTableContext() : TableContextFactory.fetchDefaultTableContext();
        return fetchTokenMapper((ContextImpl)c);
    }
    
    private static final String massageCategoryLabel(String label)
    {
    	label = label.trim();
    	return WordUtils.capitalizeFully(label, ' ', '/');	
    }
    
    private static void cacheGlobalOperatorCategories(BuiltinOperator op) 
    {
		String [] cats = op.getCategories();
		if (cats != null && cats.length > 0) {
			for (String cat : cats) {
				if (cat != null && (cat = cat.trim()).length() > 0) {
					Category c = fetchGlobalCategory(massageCategoryLabel(cat), true);
					categorizeGlobalOperator(c, op);					
				}
			}
		}
		
	}

	private static void categorizeGlobalOperator(Category c, BuiltinOperator op) 
	{
		Set<Operator> ops = sf_GlobalOperatorCache.get(c);
		if (ops == null) {
			ops = new HashSet<Operator>();
			sf_GlobalOperatorCache.put(c,  ops);
		}
		
		ops.add(op);		
	}

	private static Category fetchGlobalCategory(String cat, boolean addIfMissing) 
	{
		Category c = sf_GlobalCategoryCache.get(cat);
		
		if (c == null && addIfMissing) {
			cat = cat.intern();
			c = new Category(cat);
			
			sf_GlobalCategoryCache.put(cat, c);
		}
		
		return c;
	}

	static public TokenMapper fetchTokenMapper()
    {
    	return fetchTokenMapper((ContextImpl)TableContextFactory.fetchDefaultTableContext());
    }
    
    static public TokenMapper fetchTokenMapper(ContextImpl c)
    {
        if (c == null)
            c = (ContextImpl)TableContextFactory.fetchDefaultTableContext();
            
        TokenMapper tm = c.getTokenMapper();
        if (tm != null)
            return tm;
        
        // token mapper not created, create it now
        tm = new TokenMapper(c);
        
        // fetch operators table
        File opTable = new File("Constants.tms");
        if (!opTable.exists())
        	opTable = new File("constants.tms");
        
        if (opTable.exists()) {
        	Table t = TableFactory.importFile(opTable.getName(), c, TMSOptions.Default);
        	if (t != null) {
        		try {
        			tm.setConstantsTable(t);
        		}
        		catch (Exception e) {};
        	}
        }
        
        return tm;
    }
       
    public static TokenMapper cloneTokenMapper(TokenMapper source, TableContext c)
    {
        if (c == null)
            c = TableContextFactory.fetchDefaultTableContext();
            
        // token mapper not created, create it now
        TokenMapper tm = new TokenMapper(c);
        
        // copy the user content
        for (Entry<String, Token> e: source.m_userTokenMap.entrySet()) {
            tm.m_userTokenMap.put(e.getKey(), e.getValue());
        }
        
        for (Entry<String, Category> e : source.m_userCategoryCache.entrySet()) {
        	tm.m_userCategoryCache.put(e.getKey(), e.getValue());
        }
        
        for (Entry<Category, Set<Operator>> e : source.m_userOperatorCache.entrySet()) {
        	tm.m_userOperatorCache.put(e.getKey(), new HashSet<Operator>(e.getValue()));
        }
        
        tm.m_operTable = source.m_operTable;
        
        return tm;
    }
    
    private Map<String, Token> m_userTokenMap = new HashMap<String, Token>();
    private Map<OverloadKey, Token> m_userOverloadedOps = new HashMap<OverloadKey, Token>();
    private Table m_operTable;
    private TableContext m_context;
    private Map<Category, Set<Operator>> m_userOperatorCache = new HashMap<Category, Set<Operator>>();
    private Map<String, Category> m_userCategoryCache = new HashMap<String, Category>();
    
    private TokenMapper(Table operTable)
    {
        m_operTable = operTable;
    }
   
    private TokenMapper(TableContext context)
    {
        if (context == null)
            throw new IllegalTableStateException("Table TableContext Required");
            
        m_operTable = null;
        m_context = context;
    }
   
    Category fetchCategory(String cat)
    {
        return fetchCategory(cat, true);
    }
    
	public Category fetchCategory(String category, boolean createIfMissing) 
	{
        if (category != null && (category = category.trim()).length() > 0) {
        	category = massageCategoryLabel(category);
        	
        	Category tObj = TokenMapper.fetchGlobalCategory(category, false);      	
        	if (tObj == null) {
	            tObj = m_userCategoryCache.get(category);
	            if (tObj == null && createIfMissing) {
	                // minimize object creation
	            	category = category.intern();
	                
	                tObj = new Category(category);
	                m_userCategoryCache.put(category, tObj);
	            }
        	}
            
            return tObj;
        }
        else
            return null;
	}
	
    public boolean hasOperatorTable()
    {
    	return m_operTable != null;
    }
    
    public Table getConstantsTable()
    {
    	return m_operTable;
    }
    
    public void setConstantsTable(Table t)
    {
    	if (t != null && t.getNumColumns() < 1)
    		throw new IllegalArgumentException("Operator table must have at least one column");
    	m_operTable = t;
    }
    
    public Token lookUpToken(char label)
    {
        return lookUpToken(Character.toString(label), m_operTable);
    }
    
    public Token lookUpToken(String label)
    {
        return lookUpToken(label, m_operTable);
    }
    
    public Token lookUpToken(char label, Table operTable)
    {
        return lookUpToken(Character.toString(label), operTable);
    }
    
    public Token lookUpToken(String label, Table operTable)
    {
        if (label == null || (label = label.trim()).length() <= 0)
            return null;
        
        Token t = m_userTokenMap.get(label.toLowerCase());
        
        if (t == null && operTable != null) {
            Row r = operTable.getRow(Access.ByLabel, label);
            if (r != null) {
            	Object value = operTable.getCellValue(r, operTable.getColumn(1));
            	if (value != null)
            		t = new Token(TokenType.Constant, value, label);
            }
        }
        
        if (t == null)
        	t = sf_BuiltInTokenMap.get(label.toLowerCase());
        
        return t;        
    }  
    
    public TableContext getTableContext()
    {
        return m_context;
    }
    
    public void registerGroovyOperator(String label, Class<?>[] pTypes, Class<?> resultType, String fileName)
    {
        registerGroovyOperator(label, pTypes, resultType, fileName, label);
    }
    
    public void registerGroovyOperator(String label, Class<?>[] pTypes, Class<?> resultType, String fileName, String methodName)
    {
        Operator op = new GroovyOp(label, pTypes, resultType, fileName, methodName);
        registerOperator(op);
    }
    
    public void registerGroovyOverload(String label, Class<?> pType1, Class<?> pType2, Class<?> resultType, String fileName, String methodName)
    {
        Operator op = new GroovyOp(label, TokenType.BinaryOp, new Class<?>[] {pType1, pType2}, resultType, fileName, methodName);
        overloadOperator(label, op);
    }
    
    public void registerGroovyOperators(String fileName)
    {
        GroovyOp.registerAllOps(this, fileName);
    }
    
    public void registerJythonOperators(String fileName)
    {
        JythonOp.registerAllOps(this, fileName);
    }
    
    public void registerJythonOperators(String fileName, String className)
    {
        JythonOp.registerAllOps(this, fileName, className);
    }
    
    public void registerOperators(Class<?> clazz)
    {
    	ClassOp.processClass(this, clazz);
    }
    
    public <T, R> void registerOperator(String label, Class<?> p1Type, Class<?> resultType, Function<T, R> uniOp)
    {
        Operator op = new UnaryFunc1ArgOp<T, R>(label, TokenType.UnaryFunc, p1Type, resultType, uniOp);
        registerOperator(op);
    }
    
    public <T, U, R> void registerOperator(String label, Class<?> p1Type, Class<?> p2Type, Class<?> resultType, BiFunction<T, U, R> biOp)
    {
        Operator op = new BinaryFunc2ArgOp<T, U, R>(label, TokenType.BinaryFunc, new Class<?> [] {p1Type, p2Type}, resultType, biOp);
        registerOperator(op);
    }
    
    public <T, U, V, R> void registerOperator(String label, 
                                Class<?> p1Type, Class<?> p2Type, Class<?> p3Type, Class<?> resultType, 
                                GenericFunc3Arg<T, U, V, R> gfOp)
    {
        Operator op = new GenericFunc3ArgOp<T, U, V, R>(label, new Class<?> [] {p1Type, p2Type, p3Type}, resultType, gfOp);
        registerOperator(op);
    }
    
    public <T, U, V, W, R> void registerOperator(String label, 
                                    Class<?> p1Type, Class<?> p2Type, Class<?> p3Type, Class<?> p4Type, Class<?> resultType, 
                                    GenericFunc4Arg<T, U, V, W, R> gfOp)
    {
        Operator op = new GenericFunc4ArgOp<T, U, V, W, R>(label, new Class<?> [] {p1Type, p2Type, p3Type, p4Type}, resultType, gfOp);
        registerOperator(op);
    }

    public <T, U, V, W, X, R> void registerOperator(String label, 
            Class<?> p1Type, Class<?> p2Type, Class<?> p3Type, Class<?> p4Type, Class<?> p5Type, Class<?> resultType, 
            GenericFunc5Arg<T, U, V, W, X, R> gfOp)
    {
        Operator op = new GenericFunc5ArgOp<T, U, V, W, X, R>(label, new Class<?> [] {p1Type, p2Type, p3Type, p4Type, p5Type}, resultType, gfOp);
        registerOperator(op);
    }

    public void registerNumericOperator(String label, UnaryOperator<Double> uniOp)
    {
        Operator op = new UnaryFunc1ArgOp<Double, Double>(label, TokenType.UnaryFunc, double.class, double.class, uniOp);
        registerOperator(op);
    }
    
    public void registerNumericOperator(String label, BinaryOperator<Double> biOp)
    {
        Operator op = new BinaryFunc2ArgOp<Double, Double, Double>(label, TokenType.BinaryFunc, 
                            new Class<?> [] {double.class, double.class}, double.class, biOp);
        registerOperator(op);
    }
    
    public void registerOperator(Operator oper)
    {
        if (oper == null)
            throw new IllegalTableStateException("Operator required");
        
        TokenType tt = oper.getTokenType();
        if (tt == null)
            throw new IllegalTableStateException("Operator TokenType required");
        
        String label = oper.getLabel();
        if (label == null || label.trim().length() == 0)
            throw new IllegalTableStateException("Labeled operator required");
        
        switch(tt) {
        	case BuiltIn:
        	case UnaryFunc:
            case BinaryFunc:
            case GenericFunc:
                break;
                
            default:
                throw new IllegalTableStateException("TokenType not supported: " + tt);
        }
        
        Token t = new Token(tt, oper);
        m_userTokenMap.put(label.trim().toLowerCase(),  t);
        
        cacheOperatorCategories(oper);
    }
    
    private void cacheOperatorCategories(Operator op) 
    {
		String [] cats = op.getCategories();
		if (cats != null && cats.length > 0) {
			for (String cat : cats) {
				if (cat != null && (cat = cat.trim()).length() > 0) {
					Category c = fetchCategory(massageCategoryLabel(cat), true);
					if (c != null) {
						Set<Operator> ops = m_userOperatorCache.get(c);
					    if (ops == null) {
					    	ops = new HashSet<Operator>();
					    	m_userOperatorCache.put(c, ops);
					    }
					    
						ops.add(op);
					}
				}
			}
		}		
	}

    private void removeCachedOperator(Operator op) 
    {
		String [] cats = op.getCategories();
		if (cats != null && cats.length > 0) {
			for (String cat : cats) {
				if (cat != null && (cat = cat.trim()).length() > 0) {
					Category c = fetchCategory(massageCategoryLabel(cat), false);
					if (c != null) {
						Set<Operator> ops = m_userOperatorCache.get(c);
					    if (ops != null) {
					    	if (ops.remove(op) && ops.isEmpty()) {
					    		m_userOperatorCache.remove(c);
					    		this.m_userCategoryCache.remove(c.getLabel());
					    	}
					    }
					}
				}
			}
		}		
    }
    
    public boolean deregisterOperator(Operator oper)
    {
        if (oper == null)
            throw new IllegalTableStateException("Operator required");
        
        String label = oper.getLabel();
        
        if (label != null && (label = label.trim()).length() > 0) {
        	removeCachedOperator(oper);
        	return deregisterOperator(label);
        }
        else
        	return false;
    }
    
    protected boolean deregisterOperator(String label)
    {
        if (label == null || label.trim().length() == 0)
            throw new IllegalTableStateException("Labeled operator required");
        
        Token t = m_userTokenMap.remove(label.trim().toLowerCase()); 
        return t != null;
    }
    
    public void deregisterAllOperators()
    {
    	m_userTokenMap.clear();
    	m_userOperatorCache.clear();
    	m_userCategoryCache.clear();
    }
   
    public List<String> getOperatorCategories()
    {
    	Set<String> catNamesSet = new HashSet<String>();
    	
    	for (String catName : sf_GlobalCategoryCache.keySet()) {
    		catNamesSet.add(catName);
    	}
    	
    	for (String catName : m_userCategoryCache.keySet()) {
    		catNamesSet.add(catName);
    	}
    	
    	List<String> catNames = new ArrayList<String>(catNamesSet);
    	Collections.sort(catNames);
    	
    	return catNames;
    } 

	public List<Operator> getOperatorsForCategory(String label) 
	{
		Category c = fetchCategory(massageCategoryLabel(label), false);
		if (c == null)
			return Collections.emptyList();
		
		Set<Operator> opSet = new HashSet<Operator>();
		
		Set<Operator> ops = m_userOperatorCache.get(c);
		if (ops != null)
			opSet.addAll(ops);
		
		ops = sf_GlobalOperatorCache.get(c);
		if (ops != null)
			opSet.addAll(ops);
		
		// turn set into a list
		List<Operator> opsList = new ArrayList<Operator>(opSet);
		
		// sort the list by operator name
		if (!opsList.isEmpty()) {
			Comparator<Operator> byLabel = (Operator o1, Operator o2)->o1.getLabel().compareToIgnoreCase(o2.getLabel());
			Collections.sort(opsList, byLabel);
	    }
		
		// and return it
		return opsList;		
	}
	
    public <T, R> void overloadOperator(String label, Class<?> p1Type, Class<?> resultType, Function<T, R> unOp)
    {
        Operator op = new UnaryFunc1ArgOp<T, R>(label, TokenType.UnaryOp, p1Type, resultType, unOp);
        overloadOperator(label, op);
    }
        
    public <T, U, R> void overloadOperator(String label, Class<?> p1Type, Class<?> p2Type, Class<?> resultType, BiFunction<T, U, R> biOp)
    {
        Operator op = new BinaryFunc2ArgOp<T, U, R>(label, TokenType.BinaryOp, new Class<?> [] {p1Type, p2Type}, resultType, biOp);
        overloadOperator(label, op);
    }
        
    public void overloadOperator(String theOp, Operator oper)
    {
        validateOverload(theOp, oper);
        
        TokenType tt = oper.getTokenType();
        if (tt == null)
            throw new IllegalTableStateException("Operator TokenType required");
                
        switch(tt) {
            case UnaryOp:
            case BinaryOp:
            case BinaryFunc:
                break;
                
            default:
                throw new IllegalTableStateException("Overload: TokenType not supported");
        }
        
        Token t = new Token(theOp.trim(), tt, oper);
        OverloadKey key = new OverloadKey(theOp, oper.getArgTypes());
        
        m_userOverloadedOps.put(key, t);
    }
    
    public boolean unOverloadOperator(String theOp, Operator oper)
    {
        validateOverload(theOp, oper);
    	OverloadKey key = new OverloadKey(theOp, oper.getArgTypes());

    	Token t =  m_userOverloadedOps.remove(key);
    	return t != null;
    }
    
    public void unOverloadAllOperators()
    {
    	m_userOverloadedOps.clear();
    }

    public Operator fetchOverload(String theOp, Class<?>... paramTypes) 
    {
    	validateOverload(theOp, paramTypes);
    	
    	OverloadKey key = new OverloadKey(theOp, paramTypes);
    	Token t = m_userOverloadedOps.get(key);
    	
    	if (t != null)
    		return t.getOperator();
    	else
    		return null;   	
    }

    protected void validateOverload(String theOp, Class<?>... paramTypes)
    {
        validateOverload(theOp, (TokenType) null);
        
    	if (paramTypes == null)
    		throw new IllegalTableStateException("Parameter type(s) required");

    	if (paramTypes.length < 1 || paramTypes.length > 2)
    		throw new IllegalTableStateException("Must specify 1 or 2 parameter types");
    }
    
    private void validateOverload(String theOp, Operator oper)
    {
        if (oper == null)
            throw new IllegalTableStateException("Operator required");

        TokenType tt = oper.getTokenType();
        validateOverload(theOp, tt);
        
        if (tt == TokenType.UnaryOp && oper.numArgs() != 1)
            throw new IllegalTableStateException("Operator must take exactly 1 argument");
        else if (tt == TokenType.BinaryOp && oper.numArgs() != 2)
            throw new IllegalTableStateException("Operator must take exactly 2 arguments");
    }

    protected void validateOverload(String theOp, TokenType tt)
    {
        if (theOp == null || (theOp = theOp.trim()).length() == 0)
            throw new IllegalTableStateException("Valid " + (tt == null ? "UnaryOp or BinaryOp" : tt) + " required");

        if (tt == TokenType.UnaryOp && !BuiltinOperator.isValidUnaryOp(theOp))
            throw new IllegalTableStateException("UnaryOp required");
        else if (tt == TokenType.BinaryOp && !BuiltinOperator.isValidBinaryOp(theOp))
            throw new IllegalTableStateException("+, -, *, or / required");
        else if (!(BuiltinOperator.isValidBinaryOp(theOp) || BuiltinOperator.isValidUnaryOp(theOp)))
            throw new IllegalTableStateException("+, -, *, or / required");
    }
    
    static private class OverloadKey 
    {
    	private String m_op;
    	private List<Class<?>> m_argTypes;
    	
    	private OverloadKey(String theOp, Class<?>... paramTypes)
    	{
    		m_op = theOp.trim();
    		m_argTypes = new ArrayList<Class<?>>();
    		
    		if (paramTypes != null) {
    			for (Class<?> pType : paramTypes) {
    				m_argTypes.add(pType);
    			}
    		}    		
    	}

		@Override
		public int hashCode() 
		{
			final int prime = 31;
			int result = 1;
			result = prime * result +  m_op.hashCode();
			
			if (m_argTypes != null) {
				for (Class<?> pType : m_argTypes) {
					result = prime * result + pType.hashCode();
				}
			}
			
			return result;
		}

		@Override
		public boolean equals(Object obj) 
		{
			if (this == obj)
				return true;
			
			if (obj == null)
				return false;
			
			if (getClass() != obj.getClass())
				return false;
			
			OverloadKey other = (OverloadKey) obj;
			if (!m_op.equals(other.m_op))
				return false;
			
			int numArgs = m_argTypes.size();
			if (numArgs != other.m_argTypes.size())
				return false;
			
			for (int i = 0; i < numArgs; i++) {
				if (m_argTypes.get(i) != other.m_argTypes.get(i))
					return false;
			}
			
			return true;
		}
    }

    public String toString()
    {
        return String.format("[Tokens Built in: %d, User: %d, Overloads: %d]", 
                sf_BuiltInTokenMap.size(), m_userTokenMap.size(), m_userOverloadedOps.size());
    }
      
    static private class UnaryFunc1ArgOp<T, R> extends BaseOp
    {
        private Function<T, R> m_uniOp;
        
        private UnaryFunc1ArgOp(String label, TokenType tt, Class<?> p1Type, Class<?> resultType, Function<T, R> uniOp)
        {
            super(label, tt, new Class<?>[] {p1Type}, resultType);
            m_uniOp = uniOp;
        }

        @SuppressWarnings("unchecked")
        @Override
        public Token evaluate(Token... args)
        {
            T x = (T)args[0].getValue();
            R result = m_uniOp.apply(x);
            
            return new Token(TokenType.Operand, result);
        }
    }
    
    static private class BinaryFunc2ArgOp<T, S, R> extends BaseOp
    {
        BiFunction<T, S, R> m_biOp;
        
        private BinaryFunc2ArgOp(String label, TokenType tt, Class<?>[] argTypes, Class<?> resultType, BiFunction<T, S, R> biOp)
        {
            super(label, tt, argTypes, resultType);
            m_biOp = biOp;
        }

        @SuppressWarnings("unchecked")
        @Override
        public Token evaluate(Token... args)
        {
            T x = (T)args[0].getValue();
            S y = (S)args[1].getValue();
            R result = m_biOp.apply(x, y);
            
            return new Token(TokenType.Operand, result);
        }
    }
    
    static private class GenericFunc3ArgOp<T, U, V, R>extends BaseOp
    {
        GenericFunc3Arg<T, U, V, R> m_gfOp;
        
        private GenericFunc3ArgOp(String label, Class<?>[] argTypes, Class<?> resultType, GenericFunc3Arg<T, U, V, R> gfOp)
        {
            super(label, TokenType.GenericFunc, argTypes, resultType);
            m_gfOp = gfOp;
        }

        @SuppressWarnings("unchecked")
        @Override
        public Token evaluate(Token... args)
        {
            T x = (T)args[0].getValue();
            U y = (U)args[1].getValue();
            V z = (V)args[2].getValue();
            R result = m_gfOp.apply(x, y, z);
            
            return new Token(TokenType.Operand, result);
        }
    }
    
    static private class GenericFunc4ArgOp<T, U, V, W, R>extends BaseOp
    {
        GenericFunc4Arg<T, U, V, W, R> m_gfOp;
        
        private GenericFunc4ArgOp(String label, Class<?>[] argTypes, Class<?> resultType, GenericFunc4Arg<T, U, V, W, R> gfOp)
        {
            super(label, TokenType.GenericFunc, argTypes, resultType);
            m_gfOp = gfOp;
        }

        @SuppressWarnings("unchecked")
        @Override
        public Token evaluate(Token... args)
        {
            T x = (T)args[0].getValue();
            U y = (U)args[1].getValue();
            V z = (V)args[2].getValue();
            W w = (W)args[3].getValue();
            R result = m_gfOp.apply(x, y, z, w);
            
            return new Token(TokenType.Operand, result);
        }
    }
    
    static private class GenericFunc5ArgOp<T, U, V, W, X, R>extends BaseOp
    {
        GenericFunc5Arg<T, U, V, W, X, R> m_gfOp;
        
        private GenericFunc5ArgOp(String label, Class<?>[] argTypes, Class<?> resultType, GenericFunc5Arg<T, U, V, W, X, R> gfOp)
        {
            super(label, TokenType.GenericFunc, argTypes, resultType);
            m_gfOp = gfOp;
        }

        @SuppressWarnings("unchecked")
        @Override
        public Token evaluate(Token... args)
        {
            T p1 = (T)args[0].getValue();
            U p2 = (U)args[1].getValue();
            V p3 = (V)args[2].getValue();
            W p4 = (W)args[3].getValue();
            X p5 = (X)args[4].getValue();
            R result = m_gfOp.apply(p1, p2, p3, p4, p5);
            
            return new Token(TokenType.Operand, result);
        }
    }
}
