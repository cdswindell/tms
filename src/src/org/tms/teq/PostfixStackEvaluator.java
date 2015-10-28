package org.tms.teq;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.tms.api.Access;
import org.tms.api.Cell;
import org.tms.api.Column;
import org.tms.api.Row;
import org.tms.api.Table;
import org.tms.api.TableElement;
import org.tms.api.TableProperty;
import org.tms.api.TableRowColumnElement;
import org.tms.api.derivables.ErrorCode;
import org.tms.api.derivables.Operator;
import org.tms.api.derivables.Token;
import org.tms.api.derivables.TokenMapper;
import org.tms.api.derivables.TokenType;
import org.tms.api.exceptions.IllegalTableStateException;
import org.tms.api.exceptions.UnimplementedException;
import org.tms.teq.DerivationImpl.DerivationContext;
import org.tms.teq.PendingState.AwaitingState;
import org.tms.teq.PendingState.BlockedState;

public class PostfixStackEvaluator 
{
	private EquationStack m_pfs;
	private Table m_table;
	private EquationStack m_opStack;
	private Token[] m_pfsArray;
	private int m_pfsIdx;
    private DerivationImpl m_derivationImpl;
	
    public PostfixStackEvaluator(String expr, Table table)
    {
        PostfixStackGenerator psg = new PostfixStackGenerator(expr, table);
        m_table = table;
        m_pfs = psg.getPostfixStack();
        m_pfsIdx = -1;
    }
    
    public PostfixStackEvaluator(DerivationImpl deriv)
    {
        m_pfs = deriv.getPostfixStackInternal();        
        if (m_pfs == null || m_pfs.getStackType() != StackType.Postfix)
            throw new IllegalTableStateException("Postfix stack required");
        
        m_derivationImpl = deriv;
        m_table = deriv.getTable();
        m_pfsIdx = -1;
    }

    public PostfixStackEvaluator(PostfixStackGenerator psg)
    {
        m_pfs = psg.getPostfixStack();        
        if (m_pfs == null || m_pfs.getStackType() != StackType.Postfix)
            throw new IllegalTableStateException("Postfix stack required");
        
        m_pfsIdx = -1;
    }

    protected DerivationImpl getDerivation()
    {
        return m_derivationImpl;
    }
    
    public Token evaluate() 
    throws PendingDerivationException, BlockedDerivationException
    {
        return evaluate(null, null, null);
    }
    
    public Token evaluate(Row row, Column col) 
    throws PendingDerivationException, BlockedDerivationException
    {
        return evaluate(row, col, null);
    }
    
    protected Token evaluate(Row row, Column col, DerivationContext dc) 
    throws PendingDerivationException, BlockedDerivationException
    {
        // reset state variables to allow evaluation from stack tail
        if (m_opStack != null)
            m_opStack.clear();
        
        if (m_pfsArray != null)
            m_pfsIdx = m_pfsArray.length - 1;
        
        return reevaluate(row, col, dc);
    }
    
	public Token reevaluate(Row row, Column col) 
	throws PendingDerivationException, BlockedDerivationException
	{
		return reevaluate(row, col, null);
	}
	
	protected Token reevaluate(Row row, Column col, DerivationContext dc) 
	throws PendingDerivationException, BlockedDerivationException
	{
		assert m_pfs != null : "Requires Postfix Stack";
		
        if (m_pfsArray == null) 
            m_pfsArray = m_pfs.toArray(new Token [] {});
        
        if (m_pfsIdx >= m_pfsArray.length)
            m_pfsIdx = m_pfsArray.length - 1;
        
		if (m_opStack == null) 
			m_opStack = new EquationStack(StackType.Op);
		
		// another edge case, where the pending operation was the
		// first operation
	    if (m_pfsIdx < 0) {
	        if (m_opStack.isEmpty())
	            m_pfsIdx = m_pfsArray.length - 1;
	    }
		
		Table tbl = (col != null && row != null) ? col.getTable() : null;
		
		Token x;
		Token y;
		int numArgs;
		Token [] args;
		EquationStack rewind = new EquationStack(StackType.Op);
		
		// Assign a unique transaction ID to this calculation; 
		// the transaction id is stored in ThreadLocal storage
		DerivationImpl.assignTransactionID();
		
		// walk through postfix stack from tail to head
		while(m_pfsIdx >= 0) {
            Token t = m_pfsArray[m_pfsIdx--];         
			TokenType tt = t.getTokenType();
			Operator oper = t.getOperator();
			Object value = t.getValue();
			
			rewind.clear();
            Class<?> [] argTypes = oper != null ? oper.getArgTypes() : null;                        

			switch (tt) {
                case RowRef:
                case ColumnRef:
                case SubsetRef:
                case TableRef:
                case CellRef:
				case Operand:
					m_opStack.push(tt, value);
					break;
					
                case BuiltIn:
                    m_opStack.push(doBuiltInOp(oper, row, col));                 
                    break;
                    
                case UnaryOp:
                case UnaryFunc:
                case UnaryTrailingOp:
					x = asOperand(rewind, tbl, row, col);
					if (x == null || (!x.isOperand() && oper != BuiltinOperator.IsErrorOper)) // stack is in invalid state
					    return Token.createErrorToken(x == null ? ErrorCode.StackUnderflow : ErrorCode.OperandRequired);					
					m_opStack.push(doUnaryOp(oper, x));					
					break;
					
                case BinaryOp:
				case BinaryFunc:
					y = asOperand(rewind, tbl, row, col, null);
					if (y == null || !y.isOperand()) // stack is in invalid state
                        return Token.createErrorToken(y == null ? ErrorCode.StackUnderflow : ErrorCode.OperandRequired);                    
					
					x = asOperand(rewind, tbl, row, col, null);
					if (x == null || !x.isOperand()) // stack is in invalid state
                        return Token.createErrorToken(x == null ? ErrorCode.StackUnderflow : ErrorCode.OperandRequired);                    

                    m_opStack.push(doBinaryOp(oper, x, y));					
					break;
					
                case GenericFunc:
                    args = null;
                    numArgs = oper.numArgs();
                    if (numArgs > 0) {
                        args = new Token[numArgs];                       
                        for (int i = numArgs - 1; i >= 0; i--) {
                            x = asOperand(rewind, tbl, row, col, argTypes[i]);
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
                        args = new Token[numArgs];                       
                        for (int i = numArgs - 1; i >= 0; i--) {
                            x = asOperand(rewind, tbl, row, col, argTypes[i]);
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
                    
                    try {
                        m_opStack.push(doTransformOp(oper, tbl, row, col, dc, args));                 
                    }
                    catch (BlockingSetDerivationException e) {
                        signalBlockedDerivation(rewind, tbl, row, col, oper, e);                        
                        // if signalBlockedDerivation doesn't rethrow calculation, retry
                    }
                    catch (BlockedDerivationException e) {
                        // pendingCellState should be locked at this point
                        PendingState pendingCellState = e.getPendingState();
                        pendingCellState.lock();
                        try {
                            signalBlockedDerivation(rewind, tbl, row, col, pendingCellState, false);
                        }
                        finally {
                            pendingCellState.unlock();
                        }
                    }
                    break;
                    
                case StatOp:
                    args = null;
                    numArgs = oper.numArgs();
                    x = null;
                    if (numArgs > 0) {
                        args = new Token[numArgs];                       
                        for (int i = numArgs - 1; i >= 0; i--) {
                            x = asOperand(rewind, tbl, row, col, argTypes[i]);
                            if (x == null) // stack is in invalid state
                                return Token.createErrorToken(x == null ? ErrorCode.StackUnderflow : ErrorCode.OperandRequired);  
                            else if (!x.isA(argTypes[i]))
                                return Token.createErrorToken(ErrorCode.OperandDataTypeMismatch);  
                            
                            args[i] = x;
                        }
                    }
                    
                    // last arg must be a reference
                    if (x == null || !x.isReference()) // stack is in invalid state
                        return Token.createErrorToken(x == null ? ErrorCode.StackUnderflow : ErrorCode.ReferenceRequired);    
                    
                    try {
                        m_opStack.push(doStatOp(oper, dc, args));
                    }
                    catch (BlockingSetDerivationException e) {
                        signalBlockedDerivation(rewind, tbl, row, col, oper, e);                                               
                        // if signalBlockedDerivation doesn't rethrow exception, retry
                    }
                    catch (BlockedDerivationException e) {
                        PendingState pendingCellState = e.getPendingState();
                        pendingCellState.lock();
                        try {
                            signalBlockedDerivation(rewind, tbl, row, col, pendingCellState, false);
                        }
                        finally {
                            pendingCellState.unlock();
                        }
                    }
                    break;
                    
				default:
					throw new UnimplementedException(String.format("Unsupported token type: %s (%s)", tt, t));
			}
			
			// check if the last token is pending, and if so, suspend calculation
			Token pendingToken = m_opStack.peekFirst();
			if (pendingToken != null && pendingToken.isPending()) {
			    m_pfsArray = null; // conserve a bit of memory
			    AwaitingState pendingState = new AwaitingState(this, row, col, pendingToken);
			    pendingToken.setValue(pendingState);
                if (tbl != null) 
                    tbl.setCellValue(row,  col, pendingToken);
			    throw new PendingDerivationException(pendingState);
			}
		}
		
		// opStack should only have one value at this point
		int stackSize = m_opStack.size();
        if (stackSize < 1)
            return Token.createErrorToken(ErrorCode.StackUnderflow);
        else if (stackSize > 1)
            return Token.createErrorToken(ErrorCode.StackOverflow);
        
		Token retVal = asOperand(rewind, tbl, row, col);
		
		// free some memory 
		m_pfsArray = null;
		
		// return value
		return retVal;
	}

    public Table getTable()
	{
	    return m_table;
	}
	
    private Token asOperand(EquationStack rewind, Table tbl, Row row, Column col) 
    throws BlockedDerivationException
    {
        return asOperand(rewind, tbl, row, col, null);
    }
    
    private Token asOperand(EquationStack rewind, Table tbl, Row row, Column col, Class<?> requiredArgType) 
    throws BlockedDerivationException
    {
        Token t = m_opStack.pollFirst();
        rewind.push(t);
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
            
            if (haveRef) {
                if (tbl.isCellDefined(rowRef, colRef))
                    cell = tbl.getCell(rowRef, colRef);
            }
            
            if (cell != null) {
                if (cell.isPendings()) {
                    PendingState ps = getPendingState(cell);
                    if (ps != null) {
                        ps.lock();
                        try {
                            if (!cell.isPendings()) {
                                m_opStack.push(rewind.pollFirst());
                                return asOperand(rewind, tbl, row, col, requiredArgType);
                            }
                            
                            signalBlockedDerivation(rewind, tbl, row, col, ps, true);
                        }
                        finally {
                            ps.unlock();
                        }
                    }
                }
                
                if (cell.isNull())
                    return Token.createNullToken();
                if (cell.isErrorValue())
                    return Token.createErrorToken(cell.getErrorCode());
                else
                    return new Token(TokenType.Operand, cell.getCellValue());
            }
            else
                return Token.createNullToken();
        }
        else
            return t;
    }

    private Token doGenericOp(Operator oper, Token... args)
    {
        // process builtins first
        BuiltinOperator biOper = oper instanceof BuiltinOperator ? (BuiltinOperator)oper : null;
        if (biOper != null) {
            switch (biOper) {
                case IfOper:
                    if ((Boolean)args[0].getValue())
                        return args[1];
                    else
                        return args[2];
                 
                case ColRefOper:
                case RowRefOper:
                    return doRefOp(biOper, args[0]);
                    
                default:
                    break;
            }
        }
        
        Token t = oper.evaluate(args);
        return t;
    }

    private Token doRefOp(BuiltinOperator bio, Token token)
    {
        TableRowColumnElement ref = null;
        TokenType refType = null;
        
        if (token == null || token.isNull())
            return Token.createErrorToken(ErrorCode.OperandRequired);
        else if (token.isNumeric()) {
            int idx = token.getNumericValue().intValue();
            if (idx > 0) {                 
                if (bio == BuiltinOperator.ColRefOper) {
                    ref = getTable().getColumn(Access.ByIndex, idx);
                    refType = TokenType.ColumnRef;
                }
                else if (bio == BuiltinOperator.RowRefOper) {
                    ref = getTable().getRow(Access.ByIndex, idx);
                    refType = TokenType.RowRef;
                }
                    
                if (ref != null && refType != null)
                    return new Token(refType, ref);
            }
        }
        else if (token.isString()) {
            String label = token.getStringValue();
            if (label != null && (label = label.trim()).length() > 0) {
                if (bio == BuiltinOperator.ColRefOper) {
                    ref = getTable().getColumn(Access.ByLabel, label);
                    refType = TokenType.ColumnRef;
                }
                else if (bio == BuiltinOperator.RowRefOper) {
                    ref = getTable().getRow(Access.ByLabel, label);
                    refType = TokenType.RowRef;
                }
                    
                if (ref != null && refType != null)
                    return new Token(refType, ref);
            }
        }
        
        // if we get here, we have an invalid argument
        return Token.createErrorToken(ErrorCode.InvalidOperand);
    }        

    private PendingState getPendingState(Cell c)
    {
        if (c != null) {
            Object v = c.getCellValue();
            if (v == null || !(v instanceof PendingState))
                return null;
            PendingState ps = (PendingState)v;
            if (ps != null) {
                ps.lock();
                try {
                    if (ps.isBlocked()) {
                        PendingState rootPendingState = ps.getRootPendingState();
                        return rootPendingState;
                    }
                }
                finally {
                    ps.unlock();
                }           
            }
            
            return ps;
        }
        else
            return null;
    }

    private void signalBlockedDerivation(EquationStack rewind, Table tbl, Row row, Column col, PendingState ps, boolean doForce) 
    throws BlockedDerivationException
    {
        // reset opstack queue
        while (!rewind.isEmpty())
            m_opStack.push(rewind.pollFirst());
        
        m_pfsIdx++; // reset index to reevaluate this token
        
        if (doForce || ps.isStillPending()) {
            m_pfsArray = null; // conserve a bit of memory
            
            Token sourcePending = Token.createPendingToken(ps);
            PendingState pendingState = new BlockedState(this, row, col, sourcePending);
            
            ps.getDerivation().registerBlockingCell(ps.getPendingCell(), pendingState);
            if (tbl != null) {
                Token pt = Token.createPendingToken(pendingState);
                tbl.setCellValue(row,  col, pt);
            }
    
            throw new BlockedDerivationException(pendingState);
        }
        
        // if the state isn't pending any longer, the calculation will continue
    }

    private void signalBlockedDerivation(EquationStack rewind, Table tbl, Row row, Column col, 
                                         Operator oper, BlockingSetDerivationException e) 
    throws BlockedDerivationException
    {
        PendingStatistic pendingStat = e.getPendingStatistic();
        if (pendingStat == null) 
            pendingStat = new PendingStatistic(getDerivation(), oper, e.getReferenceElement(), e.getBlockingSet());        

        pendingStat.lock();
        try {
            // reset opstack queue
            while (!rewind.isEmpty())
                m_opStack.push(rewind.pollFirst());
                
            m_pfsIdx++; // reset index to reevaluate this token
            
            if (pendingStat.isValid()) {
                m_pfsArray = null; // conserve a bit of memory
                Token t = Token.createPendingToken((PendingState)null);
                BlockedState bs = new BlockedState(this, row, col, t);
                t.setValue(bs);
                
                if (tbl != null) 
                    tbl.setCellValue(row,  col, t);
                    
                pendingStat.registerBlockedDerivation(bs);
                
                throw new BlockingSetDerivationException(pendingStat);
            }
            
            // if the state isn't pending any longer, the calculation will continue
        }
        finally {
            pendingStat.unlock();
        }
    }

    private SingleVariableStatEngine fetchSVSE(TableElement ref, BuiltinOperator bio, DerivationContext dc) 
    throws BlockingSetDerivationException
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
            
            boolean arePendings = false;
            Set<PendingState> blockingSet = new LinkedHashSet<PendingState>();
            DerivationImpl d = getDerivation();
            if (d == null)
                throw new IllegalTableStateException("DerivationImpl is required");
            
            for (Cell c : ref.cells()) {
                if (c == null)
                    continue;
                
                // pass over cells dependent on this calculation
                if (c.isDerived()) {
                    affectedBy = c.getAffectedBy();
                    if (affectedBy !=null && affectedBy.contains(ref)) {
                        svse.exclude(c);
                        continue;
                    }
                }
                
                // check for pendings
                if (c.isPendings()) {
                    // if we've already recorded this statistic, don't reprocess
                    PendingStatistic pendingStat = d.getPendingStatistic(ref);
                    if (pendingStat != null)
                        throw new BlockingSetDerivationException(pendingStat);
                    
                    PendingState ps = getPendingState(c);
                    if (ps != null) {
                        arePendings = true;
                        blockingSet.add(ps);
                        continue;
                    }
                }
                else if (c.isNumericValue()) {                 
                    svse.enter((Number)c.getCellValue());
                }
            }
            
            if (arePendings)
                throw new BlockingSetDerivationException(blockingSet, ref);
            
            if (dc != null)
                dc.cacheSVSE(ref, svse);
        }
        
        return svse;
    }

    private TwoVariableStatEngine fetchTVSE(TableRowColumnElement ref1, TableRowColumnElement ref2, 
            BuiltinOperator bio, DerivationContext dc)
    throws BlockingSetDerivationException 
    {
        TwoVariableStatEngine tvse = null;
        if (dc != null)
            tvse = dc.getCachedTVSE(ref1, ref2);
        
        if (tvse == null) {
            tvse = new TwoVariableStatEngine();  
            
            Cell ref1Cell = ref1.getCell(Access.First);
            Cell ref2Cell = ref2.getCell(Access.First);
            
            // we only track pendings in one or the other ref at a time
            // future development goal would be to handle both at the same time 
            TableRowColumnElement pendingsIn = null;
            boolean arePendings = false;
            Set<PendingState> blockingSet = new LinkedHashSet<PendingState>();
            DerivationImpl d = getDerivation();
            if (d == null)
                throw new IllegalTableStateException("DerivationImpl is required");
            
            List<TableElement> affectedBy = null;
            int idx = 1;
            while (ref1Cell != null && ref2Cell != null) {  
                // want to force one iteration of loop, allowing several escapes
                do {
                    if (ref1Cell.isPendings() && (pendingsIn == null || pendingsIn == ref1)) {
                        if (pendingsIn == null)
                            pendingsIn = ref1;
                        
                        // if we've already recorded this statistic, don't reprocess
                        PendingStatistic pendingStat = d.getPendingStatistic(pendingsIn);
                        if (pendingStat != null)
                            throw new BlockingSetDerivationException(pendingStat);
                        
                        PendingState ps = getPendingState(ref1Cell);
                        if (ps != null) {
                            arePendings = true;
                            blockingSet.add(ps);
                            continue;
                        }
                    }                    
                    else if (ref2Cell.isPendings() && (pendingsIn == null || pendingsIn == ref2)) {
                        if (pendingsIn == null)
                            pendingsIn = ref2;
                        
                        // if we've already recorded this statistic, don't reprocess
                        PendingStatistic pendingStat = d.getPendingStatistic(pendingsIn);
                        if (pendingStat != null)
                            throw new BlockingSetDerivationException(pendingStat);
                        
                        PendingState ps = getPendingState(ref2Cell);
                        if (ps != null) {
                            arePendings = true;
                            blockingSet.add(ps);
                            continue;
                        }
                    }
                    
                    Number x = (Number)ref1Cell.getCellValue();
                    Number y = (Number)ref2Cell.getCellValue();
                    
                    // exclude cells that contain derivations that effect the reference
                    if (ref1Cell.isDerived()) {
                        affectedBy = ref1Cell.getAffectedBy();
                        if (affectedBy !=null && affectedBy.contains(ref1))
                            x = null;
                    }
                    
                    if (ref2Cell.isDerived()) {
                        affectedBy = ref2Cell.getAffectedBy();
                        if (affectedBy !=null && affectedBy.contains(ref2))
                            y = null;
                    }
                    
                    tvse.enter(x != null ? x : Double.MIN_VALUE, y != null ? y : Double.MIN_VALUE);
                } while (false);
                
                // set up to process the cells in the next row/column
                idx++;
                ref1Cell = ref1.getCell(Access.ByIndex, idx);
                ref2Cell = ref2.getCell(Access.ByIndex, idx);
            }
                        
            if (arePendings && pendingsIn != null) {
                //System.out.println(String.format("fetchTVSE: %s %s %s %d", bio, ref1, ref2, blockingSet.size()));
                throw new BlockingSetDerivationException(blockingSet, pendingsIn);
            }
            
            if (dc != null)
                dc.cacheTVSE(ref1, ref2, tvse);
        }
        
        return tvse;
    }
    
    private Token doStatOp(Operator oper, DerivationContext dc, Token... args)
    throws BlockedDerivationException
    {
        Token result = null;
        BuiltinOperator bio = oper instanceof BuiltinOperator ? (BuiltinOperator)oper : null;
        if (bio != null) {
            Token [] params = null;
            TableElement ref1 = args[0].getReferenceValue();           
            if (args.length >= 2 && args[0].isReference() && args[1].isReference()) {
                TableElement ref2 = args[1].getReferenceValue();           
                if (ref1 != null && ref2 != null) {
                    TwoVariableStatEngine tvse = fetchTVSE((TableRowColumnElement)ref1, (TableRowColumnElement)ref2, bio, dc);
                    try {
                        if (args.length > 2)
                            params = Arrays.copyOfRange(args, 2, args.length);
                        Object value = tvse.calcStatistic(bio, params);
                        result = new Token(TokenType.Operand, value);
                    }
                    catch (UnimplementedException ue) {
                        result = Token.createErrorToken(ErrorCode.UnimplementedStatistic);
                    }
                }
                else
                    result = Token.createNullToken();                
            }
            else if (args.length >= 1 && args[0].isReference()) {
                if (ref1 != null) {                    
                    SingleVariableStatEngine svse = fetchSVSE(ref1, bio, dc);
                    try {
                        if (args.length > 1)
                            params = Arrays.copyOfRange(args, 1, args.length);
                        Object value = svse.calcStatistic(bio, params);
                        result = new Token(TokenType.Operand, value);
                    }
                    catch (UnimplementedException ue) {
                        result = Token.createErrorToken(ErrorCode.UnimplementedStatistic);
                    }
                }
                else
                    result = Token.createNullToken();
            }
        }
        else
        	result = oper.evaluate(args);
        
        return result;
    }

	private Token doTransformOp(Operator oper, Table tbl, Row curRow, Column curCol, DerivationContext dc, Token... args) 
	throws BlockedDerivationException
    {        
        Token result = null;
        Cell cell = null;

        // error checking of args has been done in main loop        
        Token x = args[0];
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
        
        BuiltinOperator bio = oper instanceof BuiltinOperator ? (BuiltinOperator)oper : null;
        if (bio != null) {   
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
                            mean = (double)svse.calcStatistic(BuiltinOperator.MeanOper);
                            value = value - mean;
                            result = new Token(TokenType.Operand, value);
                            break;
                            
                        case NormalizeOper:
                            mean = (double)svse.calcStatistic(BuiltinOperator.MeanOper);
                            stDev = (double)svse.calcStatistic(BuiltinOperator.StDevSampleOper);
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
                            
                            min = (double)svse.calcStatistic(BuiltinOperator.MinOper);
                            max = (double)svse.calcStatistic(BuiltinOperator.MaxOper);
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
            }
            else
                result = Token.createNullToken();            
        }
        
        // if result is null, call evaluate method on operation
        if (result == null) {
            List<Token> params = new ArrayList<Token>(Arrays.asList(args));
            Token y = new Token(TokenType.CellRef, cell);
            params.add(y);
            
            result = oper.evaluate(params.toArray(new Token[] {}));
        }
        
        return result;
    }

	private Token doBuiltInOp(Operator oper, Row row, Column col)
    {
	    if (oper.numArgs() != 0)
            return Token.createErrorToken(ErrorCode.StackOverflow);
	    
	    Token result = null;
        BuiltinOperator bio = oper instanceof BuiltinOperator ? (BuiltinOperator)oper : null;
        if (bio != null) {
            switch (bio) {
                case RowIndexOper:
                    if (row == null)
                        result = Token.createErrorToken(ErrorCode.InvalidTableOperand);
                    else
                        result = new Token(row.getPropertyInt(TableProperty.Index));
                    break;
                    
                case ColumnIndexOper:
                    if (col == null)
                        result = Token.createErrorToken(ErrorCode.InvalidTableOperand);
                    else
                        result = new Token(col.getPropertyInt(TableProperty.Index));
                    break;
                    
                case NullOper:
                    result = Token.createNullToken();
                    break;
                    
                case TrueOper:
                    result = new Token(true);
                    break;
                    
                case FalseOper:
                    result = new Token(false);
                    break;
                    
                case NowOper:
                    result = new Token(new Date());
                    break;
                    
                default:
                    break;
            }
        }
        
        if (result != null)
            return result;
        
        // evaluate the result
        return oper.evaluate();
    }

    private Token doBinaryOp(Operator oper, Token x, Token y) 
	{
        // special case equals
        if (oper == BuiltinOperator.EqOper)
            return isEqual(x, y);
        else if (oper == BuiltinOperator.NEqOper)
            return isNotEqual(x, y);
        else if (x.isNull() || y.isNull())
            return Token.createNullToken();
        
		Token result = null;	
        BuiltinOperator bio = oper instanceof BuiltinOperator ? (BuiltinOperator)oper : null;
		if (bio != null) {
    		switch (bio) {
    			case PlusOper:
                case MinusOper:
                case MultOper:
                case DivOper:
                    
                case GtOper:
                case LtOper:
                case GtEOper:
                case LtEOper:
                    
                case AndOper:
                case OrOper:
                case XorOper:
                    result = doBuiltInBinaryOp(bio, x, y);
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

    /**
     * Implements EqOper
     * @param x
     * @param y
     * @return Token containing True or False
     */
    private Token isEqual(Token x, Token y)
    {
        boolean isEqual = false;
        Object xV = x.getValue();
        Object yV = y.getValue();
        
        // object equality and both values are null
        if (xV == yV)
            isEqual = true;
        else if (xV != null)
            isEqual = xV.equals(yV);
        else
            isEqual = yV.equals(xV); // yV can't be null at this point!
        
        if (!isEqual && (x.isString() || y.isString()) && (x.isNumeric() || y.isNumeric())) {
            // x is a number and y is a string or visa versa
            if (x.isNumeric()) {
                try {
                    isEqual = xV.equals(Double.parseDouble(y.getStringValue()));
                }
                catch (NumberFormatException nfe) { } // noop
            }
            else if (y.isNumeric()) {
                try {
                    isEqual = yV.equals(Double.parseDouble(x.getStringValue()));
                }
                catch (NumberFormatException nfe) { } // noop
            }
        }       
        else if (!isEqual && (x.isString() || y.isString()) && (x.isBoolean() || y.isBoolean())) {
            // x is a number and y is a string or visa versa
            if (x.isBoolean()) {
                try {
                    isEqual = xV.equals(Boolean.parseBoolean(y.getStringValue()));
                }
                catch (NumberFormatException nfe) { } // noop
            }
            else if (y.isBoolean()) {
                try {
                    isEqual = yV.equals(Boolean.parseBoolean(x.getStringValue()));
                }
                catch (NumberFormatException nfe) { } // noop
            }
        }
        
        return new Token(isEqual);
    }

    @SuppressWarnings("unchecked")
    private Token doCompareTo(BuiltinOperator bio, Token x, Token y)
    {
        Comparable<Object> xC = (Comparable<Object>)x.getValue();
        Comparable<Object> yC = (Comparable<Object>)y.getValue();
        
        int compareResult = xC.compareTo(yC);
        boolean result = false;
        
        switch(bio) {
            case GtOper:
                result = compareResult > 0;
                break;
                
            case LtOper:
                result = compareResult < 0;
                break;
                
            case GtEOper:
                result = compareResult >= 0;
                break;
                
            case LtEOper:
                result = compareResult <= 0;
                break;
                
            default:
                break;
        }
        
        return new Token(result);
    }

    private Token isNotEqual(Token x, Token y)
    {
        Token t = isEqual(x, y);
        if (!t.isNull())
            t.setValue(!(Boolean)t.getValue());
        
        return t;
    }
    
	private Token doBuiltInBinaryOp(BuiltinOperator bio, Token x, Token y)
    {
	    switch (bio) {
	        case GtOper:
	        case LtOper:
	        case GtEOper:
	        case LtEOper:
	            if ((x.isA(y) || y.isA(x)) && x.isA(Comparable.class)) 
	                return doCompareTo(bio, x, y);
	            
	            // Note: there is no "break" stmt here, as we want to fall through to see if there is a
	            // custom operator registered if the tokens are not comparators
	            
	        default:
                // if a token mapper exists, look for a supporting overload
                TokenMapper tm = m_pfs.getTokenMapper();
                if (tm != null) {
                    Operator oper = tm.fetchOverload(bio.getLabel(), new Class<?>[] {x.getDataType(), y.getDataType()});
                    if (oper != null) {
                        Token t = oper.evaluate(x, y);
                        return t;
                    }
                }
                
                // otherwise, handle the built-in support we have
	            if (x.isNumeric() && y.isNumeric())
	                return doBuiltInOp(bio, x.getNumericValue(), y.getNumericValue());        
	            else if (x.isString() && y.isString())
	                return doBuiltInOp(bio, x.getStringValue(), y.getStringValue());       
                else if (x.isString() && y.isNumeric())
                    return doBuiltInOp(bio, x.getStringValue(), y.getNumericValue());
                else if (x.isNumeric() && y.isString())
                    return doBuiltInOp(bio, x.getNumericValue(), y.getStringValue());
                else if (x.isBoolean() && y.isBoolean())
                    return doBuiltInOp(bio, x.getBooleanValue(), y.getBooleanValue());
	            
	            switch (bio) {
	                case PlusOper:
	                case MinusOper:
	                case MultOper:
	                case DivOper:
	                case AndOper:
	                case OrOper:
	                case XorOper:
	                    return Token.createNullToken();
	                    
	                default:	                    
	                    throw new UnimplementedException(String.format("Unimplemented built in operator: %s (%s, %s)", 
	                            bio, 
	                            x.getDataType() != null ? x.getDataType().getSimpleName() : "null",
	                            y.getDataType() != null ? y.getDataType().getSimpleName() : "null"));    
	            }
	    }
    }

    private Token doBuiltInOp(BuiltinOperator bio, boolean xV, boolean yV)
    {
        switch (bio) {
            case AndOper:
                return new Token(TokenType.Operand, xV && yV);
                
            case OrOper:
                return new Token(TokenType.Operand, xV || yV);
                
            case XorOper:
                return new Token(TokenType.Operand, xV ^ yV);
                
            default:
                throw new UnimplementedException("Unimplemented built in Boolean/Boolean operator: " + bio);    
        }               
    }

    private Token doBuiltInOp(BuiltinOperator bio, String s1, Double n2)
    {
        switch (bio) {
            case PlusOper:
            case MinusOper:
                return doBuiltInOp(bio, s1, String.valueOf(n2));
                
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
                throw new UnimplementedException("Unimplemented built in String/Numeric operator: " + bio);    
        }               
    }

    private Token doBuiltInOp(BuiltinOperator bio, Double n1, String s2)
    {
        switch (bio) {
            case PlusOper:
                return doBuiltInOp(bio, String.valueOf(n1), s2);
                
            default:
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
                
            case AndOper:
                return new Token(Math.round(x) & Math.round(y));
                
            case OrOper:
                return new Token(Math.round(x) | Math.round(y));
                
            case XorOper:
                return new Token(Math.round(x) ^ Math.round(y));
                
            default:
                throw new UnimplementedException("Unimplemented built in operator: " + bio);    
        }
    }

    private Token doUnaryOp(Operator oper, Token x) 
	{
        // special case IsNullOper
        if (oper == BuiltinOperator.IsNullOper) {
            if (x.isNull())
                return new Token(true);
            else
                return new Token(false);
        }
        
		if (x.isNull())
			return Token.createNullToken();
		
        Token result = null;    
        BuiltinOperator bio = oper instanceof BuiltinOperator ? (BuiltinOperator)oper : null;
        if (bio != null) {
            switch (bio) {
                case IsEvenOper:
                case IsOddOper:
                case IsNumberOper:
                case IsTextOper:
                case IsLogicalOper:
                case IsErrorOper:
                case NotOper:
                    result = doBuiltInUnaryOp(bio, x);
                    break;
                    
                default:
                    break;
            }
            
            if (result != null)
                return result;
        }
        
		Token val = oper.evaluate(x);
		
		return val;
	}

    private Token doBuiltInUnaryOp(BuiltinOperator bio, Token x)
    {
        Token result = null;
        
        // if a token mapper exists, look for a supporting overload
        TokenMapper tm = m_pfs.getTokenMapper();
        TokenType tt = bio.getTokenType();
        if (tm != null && (tt == TokenType.UnaryOp || tt == TokenType.UnaryTrailingOp)) {
            Operator oper = tm.fetchOverload(bio.getLabel(), new Class<?>[] {x.getDataType()});
            if (oper != null) {
                result = oper.evaluate(x);
                return result;
            }
        }
        
        if (x.isNumeric())
            result = doBuiltInOp(bio, x.getNumericValue());        
        else if (x.isBoolean())
            result = doBuiltInOp(bio, x.getBooleanValue());
        
        if (result != null)
            return result;
        
        switch (bio) {
            case IsErrorOper:
                return new Token(x.isError()); 
                
            case IsLogicalOper:
                return new Token(x.isBoolean()); 
                
            case IsTextOper:
                return new Token(x.isString()); 
                
            case IsNumberOper:
                return new Token(x.isNumeric()); 
                
            case NotOper:
                return Token.createNullToken();
                
            default:                        
                throw new UnimplementedException(
                    String.format("Unimplemented built in operator: %s (%s)", 
                    bio, 
                    x.getDataType() != null ? x.getDataType().getSimpleName() : "null"));    
        }
    }

    private Token doBuiltInOp(BuiltinOperator bio, double dv)
    {
        switch(bio) {
            case NotOper:
                return new Token(~Math.round(dv));
                
            case IsEvenOper:
                return new Token(Math.round(dv) % 2 == 0);
                
            case IsOddOper:
                return new Token(Math.round(dv) % 2 == 1);
                
            default:
                return null;                
        }
    }   

    private Token doBuiltInOp(BuiltinOperator bio, Boolean bv)
    {
        switch(bio) {
            case NotOper:
                return new Token(!bv);
                
            default:
                return null;                
        }
    }  
}
