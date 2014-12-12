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
    PowerOper("^", TokenType.BinaryOp, 6),
    ModOper("%", TokenType.BinaryOp, 4),
    FactorialOper("!", TokenType.UnaryOp, 4),

    NegOper("neg", TokenType.UnaryFunc, 6),
    ExpOper,
    AbsOper("abs", TokenType.UnaryFunc),
    SqrtOper,
    CbrtOper,
    LogOper,
    Log10Oper,

    SinOper,
    CosOper,
    TanOper,
    ASinOper,
    ACosOper,
    ATanOper,

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
    FactOper,
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

    Paren(8, TokenType.LeftParen, TokenType.RightParen),
    NOP(0, TokenType.Comma, TokenType.ColumnRef, TokenType.RowRef, TokenType.RangeRef, TokenType.CellRef),

    LAST_operator;
    
    private String m_label;
    private Set<TokenType> m_tokenTypes;
    private int m_priority;
    
    private Operator()
    {
        m_priority = 0;
        m_tokenTypes = new LinkedHashSet<TokenType>();
    }
    
    private Operator(int priority)
    {
        this();
        m_priority = priority;
    }
    
    private Operator(String label, TokenType tt)
    {
        this(label, tt, 0);
    }
    
    private Operator(String label, TokenType tt, int priority)
    {
        this();
        m_label = label;
        m_priority = priority;
        m_tokenTypes.add(tt);
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
    
    public int getLabelLength()
    {
        return m_label != null ? m_label.length() : 0;
    }
    
    public boolean isLabeled()
    { 
        return m_label != null;
    }
}
