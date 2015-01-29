package org.tms.teq;

import java.util.Iterator;
import java.util.List;

import org.tms.api.Cell;
import org.tms.api.Column;
import org.tms.api.Operator;
import org.tms.api.Row;
import org.tms.api.Table;
import org.tms.api.TableElement;
import org.tms.api.TableProperty;
import org.tms.api.TableRowColumnElement;
import org.tms.api.exceptions.IllegalTableStateException;
import org.tms.api.exceptions.UnimplementedException;
import org.tms.teq.Derivation.DerivationContext;

public class PostfixStackEvaluator 
{
	private EquationStack m_pfs;
	private Table m_table;
	private EquationStack m_opStack;
	private Iterator<Token> m_pfsIter;
	
    public PostfixStackEvaluator(String expr, Table table)
    {
        PostfixStackGenerator psg = new PostfixStackGenerator(expr, table);
        m_table = table;
        m_pfs = psg.getPostfixStack();
    }
    
    public PostfixStackEvaluator(EquationStack pfs, Table table)
    {
        if (pfs == null || pfs.getStackType() != StackType.Postfix)
            throw new IllegalTableStateException("Postfix stack required");
        
        m_table = table;
        m_pfs = pfs;
    }
    
    public PostfixStackEvaluator(Derivation deriv)
    {
        m_table = deriv.getTable();
        m_pfs = deriv.getPostfixStackInternal();
        
        if (m_pfs == null || m_pfs.getStackType() != StackType.Postfix)
            throw new IllegalTableStateException("Postfix stack required");
    }

    public Token evaluate()
    {
        return evaluate(null, null);
    }
    
    public Token evaluate(Row row, Column col)
    {
        return evaluate(row, col, null);
    }
    
    protected Token evaluate(Row row, Column col, DerivationContext dc)
    {
        m_opStack = new EquationStack(StackType.Op);
        m_pfsIter = m_pfs.descendingIterator();
        
        return reevaluate(row, col, dc);
    }
    
	public Token reevaluate(Row row, Column col)
	{
		return reevaluate(row, col, null);
	}
	
	protected Token reevaluate(Row row, Column col, DerivationContext dc)
	{
		assert m_pfs != null : "Requires Postfix Stack";
		
		if (m_opStack == null)
			m_opStack = new EquationStack(StackType.Op);
		
		if (m_pfsIter == null)
			m_pfsIter = m_pfs.descendingIterator();
		
		Table tbl = (col != null && row != null) ? col.getTable() : null;
		
		Token x;
		Token y;
		int numArgs;
		Token [] args;
		
		// walk through postfix stack from tail to head
		while(m_pfsIter.hasNext()) {
			Token t = m_pfsIter.next();
			
			TokenType tt = t.getTokenType();
			Operator oper = t.getOperator();
			Object value = t.getValue();
			
			switch (tt) {
                case RowRef:
                case ColumnRef:
                case SubsetRef:
                case TableRef:
                case CellRef:
				case Operand:
					m_opStack.push(tt, value);
					break;
					
				case UnaryOp:
				case UnaryFunc:
					x = asOperand(m_opStack.pollFirst(), tbl, row, col);
					if (x == null || !x.isOperand()) // stack is in invalid state
					    return Token.createErrorToken(x == null ? ErrorCode.StackUnderflow : ErrorCode.OperandRequired);					
					m_opStack.push(doUnaryOp(oper, x));					
					break;
					
				case BinaryOp:
				case BinaryFunc:
					y = asOperand(m_opStack.pollFirst(), tbl, row, col);
					if (y == null || !y.isOperand()) // stack is in invalid state
                        return Token.createErrorToken(y == null ? ErrorCode.StackUnderflow : ErrorCode.OperandRequired);                    
					
					x = asOperand(m_opStack.pollFirst(), tbl, row, col);
					if (x == null || !x.isOperand()) // stack is in invalid state
                        return Token.createErrorToken(x == null ? ErrorCode.StackUnderflow : ErrorCode.OperandRequired);                    

                    m_opStack.push(doBinaryOp(oper, x, y));					
					break;
					
                case GenericFunc:
                    args = null;
                    numArgs = oper.numArgs();
                    if (numArgs > 0) {
                        Class<?> [] argTypes = oper.getArgTypes();                        
                        args = new Token[numArgs];                       
                        for (int i = numArgs - 1; i >= 0; i--) {
                            x = asOperand(m_opStack.pollFirst(), tbl, row, col, argTypes[i]);
                            if (x == null) // stack is in invalid state
                                return Token.createErrorToken(x == null ? ErrorCode.StackUnderflow : ErrorCode.OperandRequired);  
                            else if (!x.isA(argTypes[i]))
                                return Token.createErrorToken(ErrorCode.OperandDataTypeMismatch);  
                            
                            args[i] = x;
                        }
                    }
                    
                    m_opStack.push(doGenericOp(oper, args));                 
                    break;
                    
                case TransformOp:
                    args = null;
                    numArgs = oper.numArgs();
                    x = null;
                    if (numArgs > 0) {
                        Class<?> [] argTypes = oper.getArgTypes();                        
                        args = new Token[numArgs];                       
                        for (int i = numArgs - 1; i >= 0; i--) {
                            x = asOperand(m_opStack.pollFirst(), tbl, row, col, argTypes[i]);
                            if (x == null) // stack is in invalid state
                                return Token.createErrorToken(x == null ? ErrorCode.StackUnderflow : ErrorCode.OperandRequired);  
                            else if (!x.isA(argTypes[i]))
                                return Token.createErrorToken(ErrorCode.OperandDataTypeMismatch);  
                            
                            args[i] = x;
                        }
                    }
                    
                    // last arg must be reference
                    if (x == null || !x.isReference()) // stack is in invalid state
                        return Token.createErrorToken(x == null ? ErrorCode.StackUnderflow : ErrorCode.ReferenceRequired);                    
                    m_opStack.push(doTransformOp(oper, tbl, row, col, dc, args));                 
                    break;
                    
                case BuiltIn:
                    m_opStack.push(doBuiltInOp(oper, row, col));                 
                    break;
                    
                case StatOp:
                    x = m_opStack.pollFirst();
                    if (x == null || !x.isReference()) // stack is in invalid state
                        return Token.createErrorToken(x == null ? ErrorCode.StackUnderflow : ErrorCode.ReferenceRequired);                    
                    m_opStack.push(doStatOp(oper, x, dc));                 
                    break;
                    
				default:
					throw new UnimplementedException(String.format("Unsupported token type: %s (%s)", tt, t));
			}
		}
		
		// opStack should only have one value at this point
		int stackSize = m_opStack.size();
        if (stackSize < 1)
            return Token.createErrorToken(ErrorCode.StackUnderflow);
        else if (stackSize > 1)
            return Token.createErrorToken(ErrorCode.StackOverflow);
        
        Token stackVal = m_opStack.pollFirst();
		Token retVal = asOperand(stackVal, tbl, row, col);
		return retVal;
	}

    public Table getTable()
	{
	    return m_table;
	}
	
    private Token asOperand(Token t, Table tbl, Row row, Column col)
    {
        return asOperand(t, tbl, row, col, null);
    }
    
    private Token asOperand(Token t, Table tbl, Row row, Column col, Class<?> requiredArgType)
    {
        if (t == null)
            return t;        
        else if (t.isOperand())
            return t;        
        else if (requiredArgType != null && requiredArgType != Object.class && t.isA(requiredArgType))
            return t;        
        else if (t.isReference()) {
            boolean haveRef = false;
            Row rowRef = null;
            Column colRef = null;
            Cell cell = null;
            
            TableElement value = t.getReferenceValue();
            if (value != null && value instanceof Column) {
                haveRef = true;
                rowRef = row;
                colRef = (Column) value;
            }
            else if (value != null && value instanceof Row) {
                haveRef = true;
                rowRef = (Row) value;
                colRef = col;
            }
            else if (value != null && value instanceof Cell) 
                cell = (Cell)value;
            
            if (haveRef) 
                cell = tbl.getCell(rowRef, colRef);
            
            if (cell != null) {
                if (cell.isNull())
                    return Token.createNullToken();
                if (cell.isErrorValue())
                    return Token.createErrorToken(cell.getErrorCode());
                else
                    return new Token(TokenType.Operand, cell.getCellValue());
            }
            else
                return t;
        }
        else
            return t;
    }
    
    private Token doGenericOp(Operator oper, Token... args)
    {
        Token t = oper.evaluate(args);
        return t;
    }

    private SingleVariableStatEngine fetchSVSE(TableElement ref, BuiltinOperator bio, DerivationContext dc) 
    {
    	SingleVariableStatEngine svse = null;
        if (dc != null) {
        	svse = dc.getCachedSVSE(ref);
        	
        	if (svse != null && bio.isRequiresRetainedDataset()  && !svse.isRetainDataset())
        		svse = null;
        }
        
        if (svse == null) {
        	List<TableElement> affectedBy = null;
        	svse = new SingleVariableStatEngine(bio.isRequiresRetainedDataset());                	
            for (Cell c : ref.cells()) {
                if (c.isNumericValue()) {
                	if (c.isDerived()) {
                		affectedBy = c.getAffectedBy();
                		if (affectedBy !=null && affectedBy.contains(ref)) {
                		    svse.exclude(c);
                			continue;
                		}
                	}
                	
                    svse.enter((Number)c.getCellValue());
                }
            }
            
            if (dc != null)
            	dc.cacheSVSE(ref, svse);
        }
        
        return svse;
	}

    private Token doStatOp(Operator oper, Token x, DerivationContext dc)
    {
        Token result = null;
        BuiltinOperator bio = oper.getBuiltinOperator();
        if (bio != null) {
            TableElement ref = x.getReferenceValue();           
            if (ref != null) {
                SingleVariableStatEngine svse = fetchSVSE(ref, bio, dc);
                try {
                    double value = svse.calcStatistic(bio);
                    result = new Token(TokenType.Operand, value);
                }
                catch (UnimplementedException ue) {
                    result = Token.createErrorToken(ErrorCode.UnimplementedStatistic);
                }
            }
            else
                result = Token.createNullToken();            
        }
        else
        	result = oper.evaluate(x);
        
        return result;
    }

	private Token doTransformOp(Operator oper, Table tbl, Row curRow, Column curCol, DerivationContext dc, Token... args)
    {        
        Token result = null;
        Cell cell = null;

        // error checking of args has been done in main loop        
        Token x = args[0];
        BuiltinOperator bio = oper.getBuiltinOperator();
        if (bio != null) {
            TableElement ref = null;
            TableRowColumnElement excludeCheckElement = null;
            if (x.getRowValue() != null) {
                ref = x.getRowValue();                
                cell = tbl.getCell((Row)ref, curCol);
                excludeCheckElement = curCol;
            }
            else if (x.getColumnValue() != null) {
                ref = x.getColumnValue();
                cell = tbl.getCell(curRow, (Column)ref);
                excludeCheckElement = curRow;
            }
            
            if (ref != null) {
                SingleVariableStatEngine svse = fetchSVSE(ref, bio, dc); 
                
                if (excludeCheckElement != null && svse.isExcluded(excludeCheckElement))
                    return Token.createNullToken();
                
                if (cell == null || cell.isNull())
                    return Token.createNullToken();
                else if (cell.isErrorValue())
                    return Token.createErrorToken(cell.getErrorCode());
                else if (!cell.isNumericValue())
                    return Token.createErrorToken(ErrorCode.OperandDataTypeMismatch);
                
                try {
                    double mean;
                    double stDev;
                    double min, sMin;
                    double max, sMax;
                    double rScale, rSource;
                    double value = ((Number)cell.getCellValue()).doubleValue();              
                    
                    switch (bio) {
                        case MeanCenterOper:
                            mean = svse.calcStatistic(BuiltinOperator.MeanOper);
                            value = value - mean;
                            result = new Token(TokenType.Operand, value);
                            break;
                            
                        case NormalizeOper:
                            mean = svse.calcStatistic(BuiltinOperator.MeanOper);
                            stDev = svse.calcStatistic(BuiltinOperator.StDevSampleOper);
                            if (stDev == 0.0)
                                return Token.createErrorToken(ErrorCode.DivideByZero);
                            value = (value - mean)/stDev;
                            result = new Token(TokenType.Operand, value);
                            break;
                        
                        case ScaleOper:
                            sMin = args[1].getNumericValue();
                            sMax = args[2].getNumericValue();
                            rScale = sMax - sMin;
                            if (rScale == 0.0)
                                return Token.createErrorToken(ErrorCode.DivideByZero);
                            
                            min = svse.calcStatistic(BuiltinOperator.MinOper);
                            max = svse.calcStatistic(BuiltinOperator.MaxOper);
                            rSource = max - min;
                            
                            value = ((value - min)*rScale/rSource) + sMin;
                            result = new Token(TokenType.Operand, value);
                            break;
                            
                        default:
                            break;
                    }
                }
                catch (UnimplementedException ue) {
                    result = Token.createErrorToken(ErrorCode.UnimplementedStatistic);
                }
                
                // if we do
                if (result == null) {
                	Token y = new Token(TokenType.CellRef, cell);
                	result = oper.evaluate(x, y);
                }
            }
            else
                result = Token.createNullToken();            
        }
        
        return result;
    }

	private Token doBuiltInOp(Operator oper, Row row, Column col)
    {
	    assert oper.numArgs() == 0 : "Too many arguments";
	    
	    Token result = null;
        BuiltinOperator bio = oper.getBuiltinOperator();
        if (bio != null) {
            switch (bio) {
                case RowIndex:
                    if (row == null)
                        result = Token.createErrorToken(ErrorCode.InvalidTableOperand);
                    else
                        result = new Token(row.getPropertyInt(TableProperty.Index));
                    break;
                    
                case ColumnIndex:
                    if (col == null)
                        result = Token.createErrorToken(ErrorCode.InvalidTableOperand);
                    else
                        result = new Token(col.getPropertyInt(TableProperty.Index));
                    break;
                    
                default:
                    break;
            }
        }
        
        if (result != null)
            return result;
        
        // evaluate the result
        result = oper.evaluate();
        
        return result;
    }

    private Token doBinaryOp(Operator oper, Token x, Token y) 
	{
        if (x.isNull() || y.isNull())
            return Token.createNullToken();
        
		Token result = null;	
		BuiltinOperator bio = oper.getBuiltinOperator();
		if (bio != null) {
    		switch (bio) {
    			case PlusOper:
                case MinusOper:
                case MultOper:
                case DivOper:
                    result = doBuiltInOp(bio, x, y);
                    break;
                    
    			default:
    			    break;
    		}
    		
    		if (result != null)
    		    return result;
		}
		
		// evaluate the result
		result = oper.evaluate(x, y);
		
		return result;
	}

	private Token doBuiltInOp(BuiltinOperator bio, Token x, Token y)
    {
        if (x.isNumeric() && y.isNumeric())
            return doBuiltInOp(bio, x.getNumericValue(), y.getNumericValue());        
        else if (x.isString() && y.isString())
            return doBuiltInOp(bio, x.getStringValue(), y.getStringValue());
        else if (x.isString() && y.isNumeric())
            return doBuiltInOp(bio, x.getStringValue(), y.getNumericValue());
        
        // if a token mapper exists, look for a supporting overload
        TokenMapper tm = m_pfs.getTokenMapper();
        if (tm != null) {
        	Operator oper = tm.fetchOverload(bio.getLabel(), new Class<?>[] {x.getDataType(), y.getDataType()});
        	if (oper != null) {
        		Token t = oper.evaluate(x, y);
        		return t;
        	}
        }
        
        throw new UnimplementedException(String.format("Unimplemented built in operator: %s (%s, %s)", 
                bio, 
                x.getDataType() != null ? x.getDataType().getSimpleName() : "null",
                y.getDataType() != null ? y.getDataType().getSimpleName() : "null"));    
    }

    private Token doBuiltInOp(BuiltinOperator bio, String s1, Double n2)
    {
        switch (bio) {
            case MultOper:
                if (n2 >= 0.5) {
                    StringBuffer sb = new StringBuffer();
                    long max = Math.round(n2);
                    for (int i = 0; i < max; i++) {
                        sb.append(s1);
                    }
                    
                    return new Token(TokenType.Operand, sb.toString());
                }
                else
                   return Token.createErrorToken(ErrorCode.InvalidOperand);
                
            default:
                // if a token mapper exists, look for a supporting overload
                TokenMapper tm = m_pfs.getTokenMapper();
                if (tm != null) {
                	Token x = new Token(TokenType.Operand, s1);
                	Token y = new Token(TokenType.Operand, n2);
                	Operator oper = tm.fetchOverload(bio.getLabel(), new Class<?>[] {x.getDataType(), y.getDataType()});
                	if (oper != null) {
                		Token t = oper.evaluate(x, y);
                		return t;
                	}
                }
                throw new UnimplementedException("Unimplemented built in String/Numeric operator: " + bio);    
        }               
    }

    private Token doBuiltInOp(BuiltinOperator bio, String s1, String s2)
    {
        switch (bio) {
            case PlusOper:
                return new Token(TokenType.Operand, s1.concat(s2));
                
            case MinusOper:
                return new Token(TokenType.Operand, s1.replace(s2, ""));
                
            default:
                // if a token mapper exists, look for a supporting overload
                TokenMapper tm = m_pfs.getTokenMapper();
                if (tm != null) {
                	Token x = new Token(TokenType.Operand, s1);
                	Token y = new Token(TokenType.Operand, s2);
                	Operator oper = tm.fetchOverload(bio.getLabel(), new Class<?>[] {x.getDataType(), y.getDataType()});
                	if (oper != null) {
                		Token t = oper.evaluate(x, y);
                		return t;
                	}
                }
                throw new UnimplementedException("Unimplemented built in String operator: " + bio);    
        }               
    }

    private Token doBuiltInOp(BuiltinOperator bio, double x, double y)
    {
        switch (bio) {
            case PlusOper:
                return new Token(x + y);
                
            case MinusOper:
                return new Token(x - y);
                
            case MultOper:
                return new Token(x * y);
                
            case DivOper:
                if (y == 0.0)
                    return Token.createErrorToken(ErrorCode.DivideByZero);
                else
                    return new Token(x / y);
            default:
                throw new UnimplementedException("Unimplemented built in operator: " + bio);    
        }
    }

    private Token doUnaryOp(Operator oper, Token x) 
	{
		if (x.isNull())
			return Token.createNullToken();
		
		Token val = oper.evaluate(x);
		
		return val;
	}
}
