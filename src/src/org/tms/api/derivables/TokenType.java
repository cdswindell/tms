package org.tms.api.derivables;

import java.util.LinkedHashSet;
import java.util.Set;

public enum TokenType implements Labeled
{
    NULL_TokenType(false),
    
    NullValue(false, "Null", "Empty"),
    EvaluationError(false, "Error"),
    Pending(false),
    
    TableRef(false, "Table", "Tbl", "T"),
    ColumnRef(false, "Column", "Col", "C"),
    RowRef(false, "Row","R"),
    CellRef(false, "Cell"),
    SubsetRef(false, "subset", "set", "group"),
    
    Constant(false),
    BuiltIn(false),
    Variable(false),
    Operand(false),
    
    Expression(false), // Special case, used to convert postfix expressions to infix
    
    StatOp(true, 1),
    TransformOp(true, 1),
    BinaryOp(true, 2),
    BinaryFunc(true, 2),
    UnaryOp(true, 1),
    UnaryFunc(true, 1), 
    UnaryTrailingOp(false, 1),
    GenericFunc(true),
    
    Comma(true, ","),
    LeftParen(true, "("),
    RightParen(false, ")"),
    
    OperandDataType(false),
       
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

    public boolean isOperator()
    {
        return isFunction() || isTransform() || (this == BinaryOp) || (this == UnaryOp) || (this == UnaryTrailingOp);
    }
    
    public boolean isBasicOperator()
    {
        switch (this) {
            case BinaryOp:
                return true;

            default:
                return false;
        }
    }
    
    public boolean isFunction()
    {
        switch (this) {
            case StatOp:
            case TransformOp:
            case BinaryFunc:
            case UnaryFunc: 
            case GenericFunc:
                return true;
             
            default:
                return false;
        }
    }
    
    public boolean isTransform()
    {
        switch (this) {
            case TransformOp:
                return true;
             
            default:
                return false;
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
        return this == Operand || this == Expression;
	}

    public boolean isBuiltIn()
    {
        return this == BuiltIn;
    }
    
	public int numArgs() 
	{
		return m_numArgs;
	}

    public boolean isReference()
    {
        return this == ColumnRef || this == RowRef || this == CellRef || this == SubsetRef || this == TableRef;
    }

    public boolean isNull()
    {
        return this == NullValue;
    }

    public boolean isPending()
    {
        return this == Pending;
    }

    public static TokenType numArgsToTokenType(int argCnt)
    {
        switch(argCnt) {
            case 0:
                return BuiltIn;
                
            case 1: 
                return UnaryFunc;
                
            case 2:
                return BinaryFunc;
                
            default:
                return GenericFunc;
        }
    }
}
