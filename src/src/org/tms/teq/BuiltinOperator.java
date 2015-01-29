package org.tms.teq;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

import org.tms.api.Operator;
import org.tms.api.TableElement;
import org.tms.api.TableRowColumnElement;

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
    FactOper(TokenType.UnaryOp, 5, MathUtil.class, "fact", "!"),
    
    // Unary functions, mostly supported in Java Math
    FracOper("frac", TokenType.UnaryFunc, 5, MathUtil.class),
    NegOper("neg", TokenType.UnaryFunc, 5, MathUtil.class),
    
    AbsOper("abs", TokenType.UnaryFunc, 5, Math.class),
    SqrtOper("sqrt", TokenType.UnaryFunc, 5, Math.class),
    CbrtOper("cbrt", TokenType.UnaryFunc, 5, Math.class),
    ExpOper(TokenType.UnaryFunc, 5, Math.class, "exp"),
    LogOper(TokenType.UnaryFunc, 5, Math.class, "log", "ln", "loge"),
    Log10Oper(TokenType.UnaryFunc, 5, Math.class, "log10", "log", "log10"),
    RandIntOper(TokenType.UnaryFunc, 5, MathUtil.class, "randomInt", "randomInt", "randInt"),

    toDegreesOper("toDegrees", TokenType.UnaryFunc, 5, Math.class),
    toRadiansOper("toRadians", TokenType.UnaryFunc, 5, Math.class),
    
    // trig functions, radians
    SinOper("sin", TokenType.UnaryFunc, 5, Math.class),
    CosOper("cos", TokenType.UnaryFunc, 5, Math.class),
    TanOper("tan", TokenType.UnaryFunc, 5, Math.class),
    ASinOper("asin", TokenType.UnaryFunc, 5, Math.class),
    ACosOper("acos", TokenType.UnaryFunc, 5, Math.class),
    ATanOper("atan", TokenType.UnaryFunc, 5, Math.class),

    SinDOper("sinD", TokenType.UnaryFunc, 5, MathUtil.class),
    CosDOper("cosD", TokenType.UnaryFunc, 5, MathUtil.class),
    TanDOper("tanD", TokenType.UnaryFunc, 5, MathUtil.class),
    ASinDOper("asinD", TokenType.UnaryFunc, 5, MathUtil.class),
    ACosDOper("acosD", TokenType.UnaryFunc, 5, MathUtil.class),
    ATanDOper("atanD", TokenType.UnaryFunc, 5, MathUtil.class),

    SinHOper("sinh", TokenType.UnaryFunc, 5, Math.class),
    CosHOper("cosh", TokenType.UnaryFunc, 5, Math.class),
    TanHOper("tanh", TokenType.UnaryFunc, 5, Math.class),

    FactFunc(TokenType.UnaryFunc, 5, MathUtil.class, "fact", "factorial"),
    FloorOper("floor", TokenType.UnaryFunc, 5, Math.class),
    CeilOper("ceil", TokenType.UnaryFunc, 5, Math.class),
    SignOper(TokenType.UnaryFunc, 5, Math.class, "signum", "sign", "signum"),
    RoundOper("round", TokenType.UnaryFunc, 5, Math.class),
    
    // String functions
    LenOper("len", TokenType.UnaryFunc, 5, MathUtil.class, "length", String.class),
    toLowerOper("toLower", TokenType.UnaryFunc, 5, MathUtil.class, "toLower", String.class),
    toUpperOper("toUpper", TokenType.UnaryFunc, 5, MathUtil.class, "toUpper", String.class),
    trimOper("trim", TokenType.UnaryFunc, 5, MathUtil.class, "trim", String.class),
    reverseOper("reverse", TokenType.UnaryFunc, 5, MathUtil.class, "reverse", String.class),
    toStringOper("toString", TokenType.UnaryFunc, 5, MathUtil.class, "toString", Object.class),
    toNumberOper("toNumber", TokenType.UnaryFunc, 5, MathUtil.class, "toNumber", Object.class),
   
    // Binary functions, mostly supported in Java Math
    ModFunc(TokenType.BinaryFunc, 5, Math.class, "IEEEremainder", "mod"),
    PowerFunc(TokenType.BinaryFunc, 5, Math.class, "pow", "pow", "power"),

    BiggerOper(TokenType.BinaryFunc, 5, Math.class, "max", "bigger"),
    SmallerOper(TokenType.BinaryFunc, 5, Math.class, "min", "smaller"),
    HypotOper(TokenType.BinaryFunc, 5, Math.class, "hypot"),
    numberOfOper("numberOf", TokenType.GenericFunc, 5, MathUtil.class, "numberOf", TableElement.class, Object.class),    

    // Builtin functions
    PiOper(TokenType.BuiltIn, 5, MathUtil.class, "pi"),
    EOper(TokenType.BuiltIn, 5, MathUtil.class, "e"),
    RandOper(TokenType.BuiltIn, 5, Math.class, "random"),
    ColumnIndex(TokenType.BuiltIn, 5, "ColumnIndex", "cidx"),
    RowIndex(TokenType.BuiltIn, 5, "RowIndex", "ridx"),

    // Single Variable Stat Functions 
    SumOper(TokenType.StatOp, 5, "sum"),
    Sum2Oper(TokenType.StatOp, 5, "sumOfSquares"),
    MeanOper(TokenType.StatOp, 5, "mean", "ave", "average"),
    MedianOper(TokenType.StatOp, 5, "median"),
    ModeOper(TokenType.StatOp, 5, "mode"),
    StDevPopulationOper(TokenType.StatOp, 5, "stDevPopulation", "stDevOfPopulation", "stDev.p"),
    StDevSampleOper(TokenType.StatOp, 5, "stDevSample", "stDevOfSample", "stDev", "stDev.s"),
    VarPopulationOper(TokenType.StatOp, 5, "variancePopulation", "varianceOfPopulation", "var.p"),
    VarSampleOper(TokenType.StatOp, 5, "varianceSample", "varianceOfSample", "var", "var.s", "variance"),
    MinOper(TokenType.StatOp, 5, "min", "minimum"),
    MaxOper(TokenType.StatOp, 5, "max", "maximum"),
    RangeOper(TokenType.StatOp, 5, "range", "spread"),
    CountOper(TokenType.StatOp, 5, "count", "cnt"),
    SkewOper(TokenType.StatOp, 5, "skewness", "skew"),
    KurtOper(TokenType.StatOp, 5),
    
    // Two Variable Stat Functions
    LinearSlopeOper(TokenType.BinaryStatOp, 5),
    LinearInterceptOper(TokenType.BinaryStatOp, 5),
    LinearCorrelationOper(TokenType.BinaryStatOp, 5),
    
    // Transformation Functions
    SplineOper,
    MeanCenterOper(TokenType.TransformOp, 5, "meanCenter"),
    NormalizeOper(TokenType.TransformOp, 5, "normalize", "standardize"),    
    ScaleOper("scale", TokenType.TransformOp, 5, MathUtil.class, "scale", TableElement.class, double.class, double.class),

    Paren(6, TokenType.LeftParen, TokenType.RightParen),
    NOP(0, TokenType.Comma, TokenType.ColumnRef, TokenType.RowRef, TokenType.SubsetRef, TokenType.CellRef, TokenType.TableRef),

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
    
    private BuiltinOperator(String label, TokenType tt, int priority, Class<? extends Object> clazz, String methodName, Class<?>... argTypes)
    {
        this(label, tt, priority);
        m_clazz = clazz;
        m_methodName = methodName;
        m_methodArgs = argTypes;
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
    
    /*
     * Class methods
     */
    public boolean isRequiresRetainedDataset()
    {
        switch(this) {
            case ModeOper:
            case SkewOper:
            case MedianOper:
                return true;
                
            default:
                return false;
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
	    if (m_methodArgs != null)
	        return m_methodArgs.length;
	    else {
    		TokenType tt = getPrimaryTokenType();
    		return tt != null ? tt.numArgs() : 0;
	    }
	}	

    @Override
    public Class<?>[] getArgTypes()
    {
        // allocate array and set all elements to Double
        if (m_methodArgs == null) {
            int numArgs = numArgs();
            TokenType tt = getPrimaryTokenType();
            if (numArgs > 0 && tt != null) {
                m_methodArgs = new Class<?>[numArgs];
                switch (tt) {
                    case StatOp:
                        m_methodArgs[0] = TableElement.class;
                        break;
                        
                    case BinaryStatOp:
                        m_methodArgs[0] = m_methodArgs[1] = TableElement.class;
                        break;
                        
                    case TransformOp:
                        m_methodArgs[0] = TableRowColumnElement.class;
                        break;
                        
                    default:
                        Arrays.fill(m_methodArgs, double.class);
                        break;
                }
            }
        }
        
        return m_methodArgs;
    }
    
	@Override
    public BuiltinOperator getBuiltinOperator()
	{
	    return this;
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
    public Token evaluate(Token... args)
    {
        assert numArgs() <= args.length : "Insufficent number of tokens suppled: " + this.toString();
        assert getMethod() != null : "No method available: " + this.toString();       
        
        Token retVal = null;
        int numArgs = numArgs();
        Object [] params = new Object[numArgs];
        Class<?> [] argTypes = getArgTypes();
        for (int i = 0; i < numArgs; i++)
        {
            if (args[i] == null || args[i].isNull())
                return Token.createNullToken();
            else if (!args[i].isA(argTypes[i]))
                return Token.createErrorToken(ErrorCode.OperandDataTypeMismatch);
                        
            params[i] = args[i].getValue();
        }
        
        try
        {
            Object val = getMethod().invoke(null, params);
            retVal = new Token(val);
        }
        catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e)
        {
            return Token.createErrorToken(e.getMessage());
        }
        
        return retVal;
    }
}
