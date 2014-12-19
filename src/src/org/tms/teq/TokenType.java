package org.tms.teq;

import java.util.LinkedHashSet;
import java.util.Set;

public enum TokenType implements Labeled
{
    NULL_TokenType(false),
    
    TableRef(false, "Table"),
    ColumnRef(false, "Column", "Col", "C"),
    RowRef(false, "Row","R"),
    CellRef(false, "Cell"),
    RangeRef(false, "Range"),
    
    Constant(false),
    BuiltIn(false),
    Variable(false),
    Operand(false),
    String(false),
    RangeOp(true, 1),
    StatOp(true, 1),
    BinaryOp(true, 2),
    BinaryFunc(true, 2),
    UnaryOp(true, 1),
    
    Comma(true, ","),
    LeftParen(true, "("),
    RightParen(false, ")"),
    
    GenericOp(true),
    GenericUnaryOp(true, 1),
    GenericBinaryFunc(true, 2),
    GenericBinaryOp(true, 2),
    
    NullOpValue(false),
    LAST_TokenType(false);
    
    private boolean m_leading;
    private Set<String> m_labels;
    private int m_numArgs;
    
    private TokenType(boolean isLeading)
    {
        this(isLeading, (String [])null);
    }

    private TokenType(boolean isLeading, int numArgs)
    {
        this(isLeading, (String [])null);
        m_numArgs = numArgs;
    }

    private TokenType(boolean isLeading, String... labels)
    {
        m_leading = isLeading;
        
        m_labels = new LinkedHashSet<String>();
        if (labels != null) {
            for (String label : labels) {
                m_labels.add(label);
            }
        }
    }

    public boolean isLeading()
    {
        return m_leading;
    }
    
    public String getLabel()
    {
        return m_labels.isEmpty() ? null : m_labels.toArray(new String [] {})[0];
    }
    
    public int getLabelLength()
    {
        return getLabel() != null ? getLabel().length() : 0;
    }
        
    public boolean isLabeled()
    { 
        return getLabel() != null;
    }
    
    public Set<String> getLabels()
    {
        return m_labels;
    }

    public boolean isLeftParen()
    {
        return this == LeftParen;
    }

    public boolean isRightParen()
    {
        return this == RightParen;
    }

	public boolean isOperand() 
	{
        return this == Operand;
	}

	public int numArgs() 
	{
		return m_numArgs;
	}
}
