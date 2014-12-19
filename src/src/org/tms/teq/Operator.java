package org.tms.teq;

import java.util.LinkedHashSet;
import java.util.Set;

public enum Operator implements Labeled
{
    NULL_operator,

    PlusOper("+", TokenType.BinaryOp, 2),
    MinusOper("-", TokenType.BinaryOp, 2),
    MultOper("*", TokenType.BinaryOp, 4),
    DivOper("/", TokenType.BinaryOp, 4),
    PowerOper(TokenType.BinaryOp, 5, "^", "exp"),
    ModOper(TokenType.BinaryOp, 5,"%", "mod"),
    FactorialOper("!", TokenType.UnaryOp, 5),

    NegOper("neg", TokenType.UnaryOp, 6, Math.class, "negateExact"),
    AbsOper("abs", TokenType.UnaryOp, 6, Math.class),
    SqrtOper("sqrt", TokenType.UnaryOp, 6, Math.class),
    CbrtOper,
    LogOper,
    Log10Oper,

    SinOper("sin", TokenType.UnaryOp, 6, Math.class),
    CosOper("cos", TokenType.UnaryOp, 6, Math.class),
    TanOper("tan", TokenType.UnaryOp, 6, Math.class),
    ASinOper("asin", TokenType.UnaryOp, 6, Math.class),
    ACosOper("acos", TokenType.UnaryOp, 6, Math.class),
    ATanOper("atan", TokenType.UnaryOp, 6, Math.class),

    SinDOper,
    CosDOper,
    TanDOper,
    ASinDOper,
    ACosDOper,
    ATanDOper,

    FloorOper,
    CeilOper,
    FracOper,
    SignOper,
    FactOp(TokenType.UnaryOp, 5,"factorial", "fact", "!"),
    RoundOper,

    RandOper,
    SplineOper,
    BiggerOper,

    ColumnIndex,
    RowIndex,

    Column,
    Row,
    Cell,

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
    private String m_method;
    
    private Operator()
    {
        m_priority = 0;
        m_tokenTypes = new LinkedHashSet<TokenType>();
        m_aliases = new LinkedHashSet<String>();
    }
    
    private Operator(int priority)
    {
        this();
        m_priority = priority;
    }
    
    private Operator(String label, TokenType tt)
    {
        this(label, tt, 6);
    }
    
    private Operator(String label, TokenType tt, int priority)
    {
        this();
        m_label = label;
        if (label != null && label.trim().length() >= 0)
        	m_aliases.add(label.trim().toLowerCase());
        
        m_priority = priority;
        m_tokenTypes.add(tt);
    }
    
    private Operator(String label, TokenType tt, int priority, Class<? extends Object> clazz)
    {
        this(label, tt, priority, clazz, label);
    }
    
    private Operator(String label, TokenType tt, int priority, Class<? extends Object> clazz, String method)
    {
        this(label, tt, priority);
        m_clazz = clazz;
        m_method = method;
    }
    
    private Operator(int priority, TokenType... tts)
    {
        this();
        m_priority = priority;
        if (tts != null) {
            for (TokenType tt : tts) {
                m_tokenTypes.add(tt);
            }
        }
    }
    
    private Operator(TokenType tt, int priority, String... labels)
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
    
    public int getPriority()
    {
        return m_priority;
    }
    
    public String getLabel()
    {
        return m_label;
    }
    
    public Set<String> getAliases()
    {
        return m_aliases;
    }
    
    public int getLabelLength()
    {
        return m_label != null ? m_label.length() : 0;
    }
    
    public boolean isLabeled()
    { 
        return m_label != null;
    }

	public Object numArgs() 
	{
		TokenType tt = getPrimaryTokenType();
		return tt != null ? tt.numArgs() : 0;
	}
}
