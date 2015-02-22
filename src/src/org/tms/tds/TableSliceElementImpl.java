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
import org.tms.api.derivables.Derivable;
import org.tms.api.event.TableElementEventType;
import org.tms.api.event.TableElementListener;
import org.tms.api.exceptions.IllegalTableStateException;
import org.tms.api.exceptions.NullValueException;
import org.tms.api.exceptions.ReadOnlyException;
import org.tms.api.utils.TableCellTransformer;
import org.tms.api.utils.TableCellValidator;
import org.tms.tds.TableImpl.CellReference;
import org.tms.teq.Derivation;
import org.tms.util.JustInTimeSet;

abstract class TableSliceElementImpl extends TableCellsElementImpl implements Derivable, TableRowColumnElement
{
    abstract protected TableSliceElementImpl insertSlice(int idx);
    abstract public TableSliceElementImpl setCurrent();
    
    private JustInTimeSet<SubsetImpl> m_subsets;
    private int m_index = -1;    
    private Derivation m_deriv;

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
    public Derivation getDerivation()
    {
        vetElement();
        return m_deriv;
    }
    
    @Override
    public Derivable setDerivation(String expr)
    {
        vetElement();
        
        // clear out any existing derivations
        if (m_deriv != null) 
            clearDerivation();
        
        if (expr != null && expr.trim().length() > 0) {
            m_deriv = Derivation.create(expr.trim(), this);
            
            // mark the rows/columns that impact the deriv, and evaluate values
            if (m_deriv != null && m_deriv.isConverted()) {
                Derivable elem = m_deriv.getTarget();
                for (TableElement d : m_deriv.getAffectedBy()) {
                    TableElementImpl tse = (TableElementImpl)d;
                    tse.registerAffects(elem);
                }
                
                setInUse(true);
                recalculate();
            }  
        }
        
        return this;
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
    public Derivable clearDerivation()
    {
        if (m_deriv != null) {
            Derivation deriv = m_deriv;
            m_deriv = null;
            
            Derivable elem = deriv.getTarget();
            for (TableElement d : deriv.getAffectedBy()) {
                TableElementImpl tse = (TableElementImpl)d;
                tse.deregisterAffects(elem);
            }
            
            deriv.destroy();
        }   
        
        return this;
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
    	m_subsets.forEach(r -> {if (r != null) r.remove(this);});
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
        
        TableImpl parent = this.getTable();
        if (parent != null) {    
            synchronized(parent) {
                if (this instanceof RowImpl) {
                    row = (RowImpl)this;
                    col = this.getTable().getColumn(mode, mda);
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
    protected boolean isWriteProtected()
    {
        return isReadOnly() ||
               (getTable() != null ? getTable().isWriteProtected() : false);
                
    }
    
    @Override
    public void clear() 
    {
        fill(null);
    }

    @Override
    public void fill(Object o) 
    {
        vetElement();
        TableImpl parent = getTable();
        assert parent != null : "Parent table required";
        
        if (this.isReadOnly())
            throw new ReadOnlyException(this, TableProperty.CellValue);
        else if (o == null && !isSupportsNull())
            throw new NullValueException(this, TableProperty.CellValue);
        
        CellReference cr = getCurrent();
        if (parent != null)
            parent.deactivateAutoRecalculate();
        boolean setSome = false;
        try {
            synchronized(parent) {
                // Clear derivation, since fill should override
                clearDerivation();
                
                boolean readOnlyExceptionEncountered = false;
                boolean nullValueExceptionEncountered = false;
                for (Cell c : cells()) {
                    if (c != null) {
                        if (isDerived(c)) 
                            continue;
                        
                        try {
                            if (((CellImpl)c).setCellValue(o, true, false));
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
            if (parent != null) {
                parent.activateAutoRecalculate(); 
                if (cr != null) 
                    cr.setCurrentCellReference(parent);
            }
        }        
        
        if (setSome && parent != null && parent.isAutoRecalculateEnabled())
            Derivation.recalculateAffected(this);
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
            Derivation.recalculateAffected(this);
        
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
            Derivation.recalculateAffected(this);        
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
