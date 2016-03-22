package org.tms.tds;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.tms.api.Access;
import org.tms.api.Cell;
import org.tms.api.Row;
import org.tms.api.Subset;
import org.tms.api.TableElement;
import org.tms.api.TableProperty;
import org.tms.api.TableRowColumnElement;
import org.tms.api.derivables.BasicFormula;
import org.tms.api.derivables.Derivable;
import org.tms.api.derivables.Derivation;
import org.tms.api.derivables.TimeSeries;
import org.tms.api.derivables.TimeSeriesable;
import org.tms.api.events.TableElementEventType;
import org.tms.api.events.TableElementListener;
import org.tms.api.exceptions.IllegalTableStateException;
import org.tms.api.exceptions.NullValueException;
import org.tms.api.exceptions.ReadOnlyException;
import org.tms.api.exceptions.UnimplementedException;
import org.tms.api.utils.TableCellTransformer;
import org.tms.api.utils.TableCellValidator;
import org.tms.tds.TableImpl.CellReference;
import org.tms.teq.DerivationImpl;
import org.tms.util.JustInTimeSet;

public abstract class TableSliceElementImpl extends TableCellsElementImpl implements Derivable, TimeSeriesable, TableRowColumnElement
{
    abstract protected TableSliceElementImpl insertSlice(int idx);
    abstract public TableSliceElementImpl setCurrent();
    
    private JustInTimeSet<SubsetImpl> m_subsets;
    private int m_index = -1;    
    private DerivationImpl m_deriv;
    private DerivationImpl m_timeSeries;

    public TableSliceElementImpl(TableElementImpl e)
    {
        super(e);
    }

    /*
     * Field getters/setters
     */   
    boolean isInUse()
    {
        return isSet(sf_IN_USE_FLAG);
    }
    
    void setInUse(boolean inUse)
    {
        if (inUse)
            vetElement();
        set(sf_IN_USE_FLAG, inUse);
    }
    
    @Override
    protected boolean isDataTypeEnforced()
    {
        if (getTable() != null && getTable().isDataTypeEnforced())
            return true;
        else
            return this.isEnforceDataType();
    }

    @Override
    public boolean isNullsSupported()
    {
        return (getTable() != null ? getTable().isNullsSupported() : false) && isSupportsNull();
    }
    
    @Override
    public int getNumSubsets()
    {
        return m_subsets.size();
    }
    
    @Override
    public List<Subset> getSubsets()
    {
        vetElement();
        return Collections.unmodifiableList(new ArrayList<Subset>(m_subsets.clone()));
    } 
    
    @Override
    synchronized public Iterable<Subset> subsets()
    {
        vetElement();
        return new BaseElementIterable<Subset>(m_subsets);
    }
    
    protected Set<SubsetImpl> getSubsetsInternal()
    {
        return m_subsets;
    } 
    
    @Override 
    public List<TableElementListener> removeAllListeners(TableElementEventType... evTs )
    {
        List<TableElementListener> tblListeners = super.removeAllListeners(evTs);
        
        TableImpl t = getTable();
        if (t != null) {
            synchronized(t) {
                // remove listeners from all rows and columns
                getSubsetsInternal().forEach(s -> {if (s != null) s.removeAllListeners(evTs); });
            }
        }
        
        // return listeners on the this table
        return tblListeners;
    }
    
    @Override
    public boolean isDerived()
    {
        vetElement();
        return m_deriv != null;       
    }
    
    @Override
    public DerivationImpl getDerivation()
    {
        vetElement();
        return m_deriv;
    }
    
    @Override
    public Derivation setDerivation(String expr)
    {
        return setDerivation(expr, true);
    }
    
    protected DerivationImpl setDerivation(String expr, boolean doRecalc)
    {
        vetElement();
        
        // clear out any existing derivations
        if (m_deriv != null) 
            clearDerivation();
        
        m_deriv = createDerivation(expr, Derivation.class);
        
        if (doRecalc && m_deriv != null)
        	recalculate();
        
        return m_deriv;
    }
    
    private DerivationImpl createDerivation(String expr, Class<? extends BasicFormula> type) 
    {
    	DerivationImpl deriv = null;
        if (expr != null && expr.trim().length() > 0) {
            deriv = DerivationImpl.create(expr.trim(), this);
            
            // if the parent table is based on a dbms or log file, make sure UUIDs
            // are assigned to affectedBy references
            boolean assignUuids = getTable() != null && getTable() instanceof ExternalDependenceTableElement;
            
            // mark the rows/columns that impact the deriv, and evaluate values
            if (deriv != null && deriv.isConverted()) {
                Derivable elem = deriv.getTarget();
                for (TableElement d : deriv.getAffectedBy()) {
                    TableElementImpl tse = (TableElementImpl)d;
                    if (type == Derivation.class)
                    	tse.registerAffects(elem);
                    else if (type == TimeSeries.class)
                    	tse.addListeners(TableElementEventType.OnBeforeDelete, deriv);
                    
                    // Assign a UUID, if needed
                    if (assignUuids)
                    	tse.getUuid();
                }
                
                if (assignUuids) 
                	m_deriv.resetAsEnteredExpression();
                
                setInUse(true);
            }  
        }   
        
        return deriv;
    }
    
    @Override
    public List<TableElement> getAffectedBy()
    {
        if (m_deriv != null)
            return Collections.unmodifiableList(m_deriv.getAffectedBy());
        else
            return null;
    }
    
    @Override
    public void clearDerivation()
    {
        if (m_deriv != null) {
        	DerivationImpl deriv = m_deriv;
            Derivable elem = deriv.getTarget();
            for (TableElement d : deriv.getAffectedBy()) {
                TableElementImpl tse = (TableElementImpl)d;
                tse.deregisterAffects(elem);
            }
            
            deriv.destroy();
            
        	m_deriv = null;
        }   
    }
    
    @Override
    public void recalculate()
    {
        vetElement();
        if (isDerived()) {
            m_deriv.recalculateTarget();
            
            fireEvents(this, TableElementEventType.OnRecalculate);
        }
    }
       
    @Override
    public List<Derivable> getDerivedElements()
    {
        vetElement();
        Set<Derivable> derived = new LinkedHashSet<Derivable>();
        
        CellReference cr = getCurrent();
        try {
            // if the element itself is derived, add it to the list
            if (isDerived())
                derived.add(this);
            
            // get any derived cells associated to the parent table in this slice
            TableImpl parent = getTable();
            if (parent != null) {
                for (Cell c : parent.derivedCells()) {
                    if (c.getColumn() == this || c.getRow() == this)
                        derived.add(c);
                }
            }
            
            return Collections.unmodifiableList(new ArrayList<Derivable>(derived));
        }
        finally {
            if (cr != null) cr.setCurrentCellReference(getTable());
        }       
    }
    
    @Override
    public boolean isTimeSeries()
    {
        vetElement();
        return m_timeSeries != null;       
    }
    
    @Override
    public DerivationImpl getTimeSeries()
    {
        vetElement();
        return m_timeSeries;
    }
    
    @Override
    public void clearTimeSeries()
    {
        if (m_timeSeries != null) {
        	DerivationImpl deriv = m_timeSeries;
            for (TableElement d : deriv.getAffectedBy()) {
            	d.removeListeners(TableElementEventType.OnBeforeDelete, deriv);
            }
            
            deriv.destroy();
            
            m_timeSeries = null;
        }   
    }   
    
    @Override
    public DerivationImpl setTimeSeries(String expr)
    {
        vetElement();
        
        // clear out any existing time series
        if (m_timeSeries != null) 
            clearTimeSeries();
        
        m_timeSeries = createDerivation(expr, TimeSeries.class);
        
        return m_timeSeries;
    }
    
    @Override
    public void sort() 
    {
        vetElement();
        TableImpl parent = getTable();
        if (parent == null)
            throw new IllegalTableStateException("Table Required");
        
        parent.sort(this);
    }

    @Override
    public void sort(Comparator<Cell> cellSorter) 
    {
        vetElement();
        TableImpl parent = getTable();
        if (parent == null)
            throw new IllegalTableStateException("Table Required");
        
        parent.sort(this, cellSorter);
    }
        
    CellImpl getCellInternal(TableSliceElementImpl tse)
    {
        if (this instanceof RowImpl)
            return ((RowImpl)this).getCellInternal((ColumnImpl)tse, false, false);
        else if (this instanceof ColumnImpl)
            return ((ColumnImpl)this).getCellInternal((RowImpl)tse, false, false);
        else
            throw new IllegalTableStateException("Table Slice ELement Required");
    }
    
    protected boolean add(SubsetImpl r)
    {
        vetElement();
        if (r != null) {
            /*
             *  if the subset doesn't contain the row, use the subset method to do all the work
             *  TableSliceElementImpl.add will be called recursively to finish up
             */
            if (!r.contains(this))
                return r.add(this);
            
            return m_subsets.add(r);
        }
        
        return false;
    }

    /**
     * Remove the reference from the specified SubsetImpl to this TableSliceElementImpl, removing
     * this TableSliceElementImple from the specified subset, if it has not already been removed.
     * 
     * Returns true if the specified SubsetImpl was successfully removed
     * 
     * @param r SubsetImpl
     */
    @Override
    protected boolean remove(SubsetImpl r)
    {
        if (r != null) {
            /*
             * if the subset contains the element, use the subset method to do all the work
             * TableSliceElementImpl.remove will be called again to finish up
             */
        	if (r.contains(this)) 
        		r.remove(this);
        	
        	return m_subsets.remove(r);
        }
        
        return false;
    }
    
    protected void removeFromAllSubsets()
    {
    	// remove this table slice element from all subsets
        if (!m_subsets.isEmpty()) {
            List<SubsetImpl> tmp = new ArrayList<SubsetImpl>(m_subsets);
            m_subsets.clear();
            tmp.forEach(r -> {if (r != null) r.remove(this);});
        }
    }
    
    protected CellReference getCurrent()
    {
        if (getTable() != null)
            return getTable().getCurrent();
        else
            return null;
    }
    
    /*
     * Overridden methods
     */
    @Override
    protected void initialize(TableElementImpl e)
    {
        super.initialize(e);
        
        BaseElementImpl source = getInitializationSource(e);        
        for (TableProperty tp : this.getInitializableProperties()) {
            Object value = source.getProperty(tp);
            
            if (super.initializeProperty(tp, value)) continue;
            
            switch (tp) {                    
                default:
                    if (!tp.isOptional())
                        throw new IllegalStateException("No initialization available for " + 
                                                        this.getClass().getSimpleName() +" Property: " + tp);                       
            }
        }
        
        // initialize other member fields
        m_subsets = new JustInTimeSet<SubsetImpl>();
        setIndex(-1);
        setInUse(false);
    } 

    @Override
    public Object getProperty(TableProperty key)
    {
        switch(key)
        {
            case numSubsets:
                return getNumSubsets();
                
            case Subsets:
                return getSubsets();
                
            case isInUse:
                return isInUse();
                
            case Derivation:
                return getDerivation();
                
            case Cells:
                return getCells();
                
            case Index:
                return getIndex();
                               
            default:
                return super.getProperty(key);
        }
    }   

    @Override
    public String getUnits()
    {
        return (String)getProperty(TableProperty.Units);
    }

    @Override
    public void setUnits(String units)
    {
        if (units == null || (units = units.trim()).length() == 0)
            clearProperty(TableProperty.Units);
        else
            setProperty(TableProperty.Units, units);
    }

    @Override
    public String getDisplayFormat()
    {
        return (String)getProperty(TableProperty.DisplayFormat);
    }
    
    @Override
    public void setDisplayFormat(String value)
    {
        if (value != null && (value = value.trim()).length() > 0)
            setProperty(TableProperty.DisplayFormat, value);
        else
            this.clearProperty(TableProperty.DisplayFormat);        
    }

    @Override
    public TableCellValidator getValidator()
    {
        if (isSet(sf_HAS_CELL_VALIDATOR_FLAG))
            return (TableCellValidator)getProperty(TableProperty.Validator);
        else
            return null;
    }

    @Override
    public void setValidator(TableCellValidator validator)
    {
        if (validator == null)
            clearProperty(TableProperty.Validator);
        else
            setProperty(TableProperty.Validator, validator);
        
        // accelerator to minimize map lookups
        set(sf_HAS_CELL_VALIDATOR_FLAG, validator != null);
    }

    @Override
    public void setTransformer(TableCellTransformer transformer)
    {
        setValidator(transformer);
    }
    
    @Override
    public Cell getCell(Access mode, Object... mda)
    {
        RowImpl row = null;
        ColumnImpl col = null;
        
        TableImpl parent = getTable();
        if (parent != null) {    
            synchronized(parent) {
                if (this instanceof RowImpl) {
                    row = (RowImpl)this;
                    col = parent.getColumn(mode, mda);
                }
                else if (this instanceof ColumnImpl) {
                    row = parent.getRow(mode, mda);
                    col = (ColumnImpl) this;
                }
            }
        }     
        
        return parent.getCell(row,  col);
    }
    
    @Override
    public int getIndex()
    {
        return m_index ;
    }
    
    void setIndex(int idx)
    {
        m_index = idx;
    }
    
    protected List<CellImpl> getCells()
    {
        int numCells = getNumCells();
        if (numCells == 0)
            return Collections.emptyList();
        
        List<CellImpl> cells = new ArrayList<CellImpl>(numCells);
        for (Cell c : cells()) {
            cells.add((CellImpl)c);
        }
        
        return cells;
    }
    
    @Override
    public boolean isWriteProtected()
    {
        return isReadOnly() ||
               (getTable() != null ? getTable().isWriteProtected() : false);
                
    }
    
    @Override
    public boolean clear() 
    {
        return fill(null);
    }

    @Override
    public boolean fill(Object o) 
    {
        return fill(o, true, false, true);
    }
    
    boolean fill(Object o, boolean preserveCurrent, boolean preserveDerivedCells, boolean fireEvents) 
    {
        vetElement();
        TableImpl parent = getTable();
        assert parent != null : "Parent table required";
        
        if (this.isReadOnly())
            throw new ReadOnlyException(this, TableProperty.CellValue);
        else if (o == null && !isSupportsNull())
            throw new NullValueException(this, TableProperty.CellValue);
        
        CellReference cr = null;
        if (preserveCurrent)
            getCurrent();
        
        boolean reactivateAutoRecalc = false;
        if (parent != null) {
            if (!parent.isSet(sf_AUTO_RECALCULATE_DISABLED_FLAG)) {
                parent.deactivateAutoRecalculate();
                reactivateAutoRecalc = true;
            }
        }
        
        boolean setSome = false;
        try {
            synchronized(parent) {
                // Clear derivation, since fill should override
                clearDerivation();
                
                boolean readOnlyExceptionEncountered = false;
                boolean nullValueExceptionEncountered = false;
                for (Cell c : cells()) {
                    if (c != null) {
                        if (preserveDerivedCells && isDerived(c)) 
                            continue;
                        else
                        	c.clearDerivation();
                        
                        try {
                            if (((CellImpl)c).setCellValue(o, true, false))
                                setSome = true;
                        }
                        catch (ReadOnlyException e) {
                            readOnlyExceptionEncountered = true;
                        }
                        catch (NullValueException e) {
                            nullValueExceptionEncountered = true;
                        }
                    }
                }
                
                if (setSome) {
                    setInUse(true); 
                    
                    if (fireEvents)
                        fireEvents(this, TableElementEventType.OnNewValue,  o);
                }
                else if (readOnlyExceptionEncountered)
                    throw new ReadOnlyException(this, TableProperty.CellValue);
                else if (nullValueExceptionEncountered)
                    throw new NullValueException(this, TableProperty.CellValue);
           }
        }
        finally { 
            if (cr != null) 
                cr.setCurrentCellReference(parent);
            
            if (reactivateAutoRecalc)
                parent.activateAutoRecalculate(); 
        }        
        
        if (setSome && parent != null && parent.isAutoRecalculateEnabled())
            DerivationImpl.recalculateAffected(this.getTable());
        
        return setSome;
    }

    @Override
    public void fill(Object o, int n, Access mode, Object... mda) 
    {
        vetElement();
        TableImpl parent = getTable();
        assert parent != null : "Parent table required";
        
        if (this.isReadOnly())
            throw new ReadOnlyException(this, TableProperty.CellValue);
        else if (o == null && !isSupportsNull())
            throw new NullValueException(this, TableProperty.CellValue);
        
        // we do not push/pop current cell state in this routine, as
        // the use case is it will be called several times in succession,
        // and we want the table to adopt the new current cell state
        if (parent != null)
            parent.deactivateAutoRecalculate();
        boolean setSome = false;
        try {
            synchronized(parent) {
                CellImpl c = (CellImpl) this.getCell(mode, mda);
                boolean readOnlyExceptionEncountered = false;
                boolean nullValueExceptionEncountered = false;
                while (c != null && n > 0) {                    
                    try {
                        if (c.isDerived())
                            continue;
                        
                        if (c.setCellValue(o))
                            setSome = true;
                    }
                    catch (ReadOnlyException e) {
                        readOnlyExceptionEncountered = true;
                    }
                    catch (NullValueException e) {
                        nullValueExceptionEncountered = true;
                    }
                    finally {
                        n--;
                        if (n < 1) break; // leave the table at the last row/col modified
                        c = (CellImpl) getCell(Access.Next);
                    }
                }
                
                if (setSome) {
                    this.setInUse(true); 
                    fireEvents(this, TableElementEventType.OnNewValue,  o);
                }
                else if (readOnlyExceptionEncountered)
                    throw new ReadOnlyException(this, TableProperty.CellValue);
                else if (nullValueExceptionEncountered)
                    throw new NullValueException(this, TableProperty.CellValue);
            }
        }
        finally { 
            if (parent != null)
                parent.activateAutoRecalculate();
        }        
        
        if (setSome && parent != null && parent.isAutoRecalculateEnabled())
            DerivationImpl.recalculateAffected(this.getTable());
        
    }
    
    @Override
    public void fill(Object[] o, Access mode, Object... mda) 
    {
        vetElement();
        
        if (o == null || o.length == 0)
            return;
        else if (this.isReadOnly())
            throw new ReadOnlyException(this, TableProperty.CellValue);
        
        TableImpl parent = getTable();
        assert parent != null : "Parent table required";
        
        // we do not push/pop current cell state in this routine, as
        // the use case is it will be called several times in succession,
        // and we want the table to adopt the new current cell state
        if (parent != null)
            parent.deactivateAutoRecalculate();
        boolean setSome = false;
        try {
            int n = o.length;
            int idx = 0;
            synchronized(parent) {
                CellImpl c = (CellImpl) this.getCell(mode, mda);
                boolean readOnlyExceptionEncountered = false;
                boolean nullValueExceptionEncountered = false;
                while (c != null && idx < n) {                    
                    try {
                        if (c.isDerived())
                            continue;
                        
                        if (c.setCellValue(o[idx++]))
                            setSome = true;
                    }
                    catch (ReadOnlyException e) {
                        readOnlyExceptionEncountered = true;
                    }
                    catch (NullValueException e) {
                        nullValueExceptionEncountered = true;
                    }
                    finally {
                        if (idx >= n) break; // leave the table at the last row/col modified
                        c = (CellImpl) getCell(Access.Next);
                    }
                }
                
                if (setSome) {
                    this.setInUse(true); 
                    fireEvents(this, TableElementEventType.OnNewValue);
                }
                else if (readOnlyExceptionEncountered)
                    throw new ReadOnlyException(this, TableProperty.CellValue);
                else if (nullValueExceptionEncountered)
                    throw new NullValueException(this, TableProperty.CellValue);
            }
        }
        finally { 
            if (parent != null)
                parent.activateAutoRecalculate();
        }        
        
        if (setSome && parent != null && parent.isAutoRecalculateEnabled())
            DerivationImpl.recalculateAffected(this.getTable());        
    }
    
	private boolean isDerived(Cell c)
    {
        if (c.isDerived())
            return true;
        
        Derivable d = null;
        if (this instanceof Row) 
            d = c.getColumn();
        else 
            d = c.getRow();
        
        if (d != null && d.isDerived())
            return true;
        
        return false;
    }
	
    @Override
	public boolean isNull() 
	{
		return getNumCells() == 0;
	}   
    
    @Override
    public Object [] toArray()
    {
        vetElement();       
        List<Object> l = new ArrayList<Object>(size());
        for (Cell c : cells()) {
        	l.add(c.getCellValue());
        }
        
    	return l.toArray();
    }
    
    @Override
    @SuppressWarnings("unchecked")
	public <T> T[] toArray(T[] template) 
    {
        vetElement();
        Class<?> clazz = template.getClass().getComponentType();
        boolean clazzIsNumeric = Number.class.isAssignableFrom(clazz);
        List<T> l = new ArrayList<T>(size());
        for (Cell c : cells()) {
        	Object cv = c.getCellValue();
        	if (cv != null && clazz.isAssignableFrom(cv.getClass()))
        		l.add((T)cv);
        	else if (cv != null && c.isNumericValue() && clazzIsNumeric)
        		l.add((T)convertNumericValue((Number)cv, clazz));
        	else if (cv == null)
        		l.add(null);
        }
        
    	return l.toArray(template);
    }
    
    private Object convertNumericValue(Number cv, Class<?> clazz) 
    {
		if (clazz == Double.class)
			return cv.doubleValue();
		else if (clazz == Float.class)
			return cv.floatValue();
		else if (clazz == Long.class)
			return cv.longValue();
		else if (clazz == Integer.class)
			return cv.intValue();
		else if (clazz == Short.class)
			return cv.shortValue();
		else if (clazz == Byte.class)
			return cv.byteValue();
		return null;
	}
    
	public int size()
    {
    	if (this instanceof RowImpl)
    		return getTable().getNumColumns();
    	else if (this instanceof ColumnImpl)
    		return getTable().getNumRows();
    	else
    		throw new UnimplementedException("No support for: " + this.getClass().getSimpleName());
    }
    
    @Override
    public String toString()
    {
        if (isInvalid())
            return String.format("[Deleted %s]", getElementType());
        
        String label = (String)getProperty(TableProperty.Label);
        if (label != null)
            label = ": " + label;
        else
            label = "";
        
        int idx = getIndex();
        
        if (idx > 0)
            return String.format("[%s %d%s]", getElementType(), idx, label);
        else
            return String.format("[%s%s]", getElementType(), label);
    }
}
