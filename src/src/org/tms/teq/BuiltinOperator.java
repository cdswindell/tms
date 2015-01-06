package org.tms.teq;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

import org.tms.api.Operator;

public enum BuiltinOperator implements Labeled, Operator
{
    NULL_operator,

    // Basic math operators, implementation supported for numbers and strings (+ & -)
    PlusOper("+", TokenType.BinaryOp, 2),
    MinusOper("-", TokenType.BinaryOp, 2),
    MultOper("*", TokenType.BinaryOp, 4),
    DivOper("/", TokenType.BinaryOp, 4),
    
    // Special math operators, implemented in Java Math class
    ModOper(TokenType.BinaryOp, 5, Math.class, "IEEEremainder", "%"),  
    PowerOper(TokenType.BinaryOp, 5, Math.class, "pow", "^"),
    
    // Factorial operator, implemented in code
    FactOper("!", TokenType.UnaryOp, 5),
    
    // Unary functions, mostly supported in Java Math
    FracOper("frac", TokenType.UnaryFunc, 6),
    NegOper("neg", TokenType.UnaryFunc, 6),
    
    AbsOper("abs", TokenType.UnaryFunc, 6, Math.class),
    SqrtOper("sqrt", TokenType.UnaryFunc, 6, Math.class),
    CbrtOper("cbrt", TokenType.UnaryFunc, 6, Math.class),
    ExpOper(TokenType.UnaryFunc, 6, Math.class, "exp", "e", "exp"),
    LogOper(TokenType.UnaryFunc, 6, Math.class, "log", "loge"),
    Log10Oper(TokenType.UnaryFunc, 6, Math.class, "log10", "log", "log10"),
    RandIntOper(TokenType.UnaryFunc, 6, "randomInt", "randInt"),

    toDegreesOper("toDegrees", TokenType.UnaryFunc, 6, Math.class),
    toRadiansOper("toRadians", TokenType.UnaryFunc, 6, Math.class),
    
    SinOper("sin", TokenType.UnaryFunc, 6, Math.class),
    CosOper("cos", TokenType.UnaryFunc, 6, Math.class),
    TanOper("tan", TokenType.UnaryFunc, 6, Math.class),
    ASinOper("asin", TokenType.UnaryFunc, 6, Math.class),
    ACosOper("acos", TokenType.UnaryFunc, 6, Math.class),
    ATanOper("atan", TokenType.UnaryFunc, 6, Math.class),

    SinDOper,
    CosDOper,
    TanDOper,
    ASinDOper,
    ACosDOper,
    ATanDOper,

    FactFunc(TokenType.UnaryFunc, 5, "factorial", "fact"),
    FloorOper("floor", TokenType.UnaryFunc, 6, Math.class),
    CeilOper("ceil", TokenType.UnaryFunc, 6, Math.class),
    SignOper(TokenType.UnaryFunc, 6, Math.class, "signum", "sign", "signum"),
    RoundOper("round", TokenType.UnaryFunc, 6, Math.class),
    
    // Binary functions, mostly supported in Java Math
    ModFunc(TokenType.BinaryFunc, 5, Math.class, "IEEEremainder", "mod"),
    PowerFunc(TokenType.BinaryFunc, 5, Math.class, "pow", "pow", "power"),

    BiggerOper(TokenType.BinaryFunc, 5, Math.class, "max", "bigger"),
    SmallerOper(TokenType.BinaryFunc, 5, Math.class, "min", "smaller"),
    HypotOper(TokenType.BinaryFunc, 5, Math.class, "hypot"),

    // Builtin functions
    RandOper(TokenType.BuiltIn, 6, Math.class, "random"),
    ColumnIndex(TokenType.BuiltIn, 6, "ColumnIndex", "cidx"),
    RowIndex(TokenType.BuiltIn, 6, "RowIndex", "ridx"),

    Column,
    Row,
    Cell,

    SplineOper,
    SumOper,
    MeanOper,
    MedianOper,
    StdevOper,
    VarOper,
    MinOper,
    MaxOper,
    RangeOper,
    KurtOper,
    SkewOper,
    CountOper,
    MeanCenterOper,
    NormalizeOper,
    ScaleOper,

    Paren(6, TokenType.LeftParen, TokenType.RightParen),
    NOP(0, TokenType.Comma, TokenType.ColumnRef, TokenType.RowRef, TokenType.RangeRef, TokenType.CellRef),

    LAST_operator;
    
    static final public int MAX_PRIORITY = 6;
    
    private String m_label;
    private Set<String> m_aliases;
    private Set<TokenType> m_tokenTypes;
    private int m_priority;
    private Class<? extends Object> m_clazz;
    private String m_methodName;
    private Method m_method;
    private Class<?>[] m_methodArgs;
    
    private BuiltinOperator()
    {
        m_priority = 0;
        m_tokenTypes = new LinkedHashSet<TokenType>();
        m_aliases = new LinkedHashSet<String>();
    }
    
    private BuiltinOperator(int priority)
    {
        this();
        m_priority = priority;
    }
    
    private BuiltinOperator(String label, TokenType tt)
    {
        this(label, tt, 6);
    }
    
    private BuiltinOperator(String label, TokenType tt, int priority)
    {
        this();
        m_label = label;
        if (label != null && label.trim().length() >= 0)
        	m_aliases.add(label.trim().toLowerCase());
        
        m_priority = priority;
        m_tokenTypes.add(tt);
    }
    
    private BuiltinOperator(String label, TokenType tt, int priority, Class<? extends Object> clazz)
    {
        this(label, tt, priority, clazz, label);
    }
    
    private BuiltinOperator(String label, TokenType tt, int priority, Class<? extends Object> clazz, String methodName)
    {
        this(label, tt, priority);
        m_clazz = clazz;
        m_methodName = methodName;
    }
    
    private BuiltinOperator(TokenType tt, int priority, Class<? extends Object> clazz, String methodName, String...labels)
    {
        this();
        m_priority = priority;
        m_tokenTypes.add(tt);
        if (labels != null && labels.length > 0) {
            for (String label : labels) {
                if (m_label == null)
                    m_label = label;
                m_aliases.add(label.toLowerCase());
            }
        }
        else {
            m_label = methodName;
            m_aliases.add(methodName.toLowerCase());
        }
        
        m_clazz = clazz;
        m_methodName = methodName;
    }
    
    private BuiltinOperator(int priority, TokenType... tts)
    {
        this();
        m_priority = priority;
        if (tts != null) {
            for (TokenType tt : tts) {
                m_tokenTypes.add(tt);
            }
        }
    }
    
    private BuiltinOperator(TokenType tt, int priority, String... labels)
    {
        this();
        m_priority = priority;
        m_tokenTypes.add(tt);
        if (labels != null) {
            for (String label : labels) {
            	if (m_label == null)
            		m_label = label;
            	m_aliases.add(label.toLowerCase());
            }
        }
    }
    
    public TokenType getPrimaryTokenType()
    {
        if (m_tokenTypes != null && m_tokenTypes.size() == 1) 
            return m_tokenTypes.toArray(new TokenType [] {})[0];
        else
            return null;
    }
    
    public Set<TokenType> getTokenTypes()
    {
        return m_tokenTypes;
    }
    
    Method getMethod()
    {
        if (m_method == null && this.m_clazz != null) {
            try
            {
                m_method = m_clazz.getMethod(m_methodName, getArgTypes());
            }
            catch (NoSuchMethodException | SecurityException e)
            {
                e.printStackTrace();
            }
        }
        
        return m_method;
    }
    
    @Override
    public TokenType getTokenType()
    {
        return getPrimaryTokenType();
    }
    
    @Override
    public int getPriority()
    {
        return m_priority;
    }
    
    @Override
    public String getLabel()
    {
        return m_label;
    }
    
    public Set<String> getAliases()
    {
        return m_aliases;
    }
    
    @Override
    public int getLabelLength()
    {
        return m_label != null ? m_label.length() : 0;
    }
    
    @Override
    public boolean isLabeled()
    { 
        return m_label != null;
    }

	@Override
    public int numArgs() 
	{
		TokenType tt = getPrimaryTokenType();
		return tt != null ? tt.numArgs() : 0;
	}	

    @Override
    public Class<?>[] getArgTypes()
    {
        // allocate array and set all elements to Double
        if (m_methodArgs == null) {
            int numArgs = numArgs();
            if (numArgs > 0) {
                m_methodArgs = new Class<?>[numArgs];
                Arrays.fill(m_methodArgs, double.class);
            }
        }
        
        return m_methodArgs;
    }
    
	@Override
    public BuiltinOperator getBuiltinOperator()
	{
	    return this;
	}

    @Override
    public Token evaluate(Token... args)
    {
        assert numArgs() <= args.length : "Insufficent number of tokens suppled: " + this.toString();
        
        // handle some special cases
        Token retVal = null;
        Token x = null;
        
        // handle certain special cases that are not implemented
        // in the Java Math class; all other builtins are handled
        // via invoking the appropriate class method
        switch (this) {
            case NegOper:
                x = args[0];
                if (x.isNumeric())
                    retVal = new Token(-x.getNumericValue());
                break;
                
            case FracOper:
                x = args[0];
                if (x.isNumeric()) {
                    double d = x.getNumericValue();
                    retVal = new Token(d - Math.floor(d));
                }
                break;
                
            case FactOper:
            case FactFunc:
                x = args[0];
                if (x.isNumeric()) {
                    double d = x.getNumericValue();
                    double rVal = 1;
                    if (d > 1.0) {
                        long dl = Math.round(d);
                        for (long i = dl; i > 1; i--) {
                            rVal *= i;
                        }
                    }
                    
                    retVal = new Token(rVal);
                }                
                break;
                
            case RandIntOper:
                x = args[0];
                if (x.isNumeric()) {
                    double d = Math.ceil(Math.abs(x.getNumericValue()));
                    retVal = new Token(1 + Math.floor(d * Math.random()));
                }
                break;
                                   
            default: 
                break;
        }
        
        if (retVal != null)
            return retVal;
        
        assert getMethod() != null : "No method available: " + this.toString();       
        
        int numArgs = numArgs();
        Object [] params = new Object[numArgs];
        for (int i = 0; i < numArgs; i++)
        {
            params[i] = args[i] != null ? args[i].getValue() : null;
        }
        
        try
        {
            Object val = getMethod().invoke(null, params);
            retVal = new Token(val);
        }
        catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
        return retVal;
    }
}
