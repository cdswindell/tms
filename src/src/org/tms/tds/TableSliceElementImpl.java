package org.tms.tds;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import org.tms.api.Access;
import org.tms.api.Cell;
import org.tms.api.Derivable;
import org.tms.api.Subset;
import org.tms.api.Row;
import org.tms.api.TableElement;
import org.tms.api.TableProperty;
import org.tms.api.TableRowColumnElement;
import org.tms.api.exceptions.IllegalTableStateException;
import org.tms.api.exceptions.NullValueException;
import org.tms.api.exceptions.ReadOnlyException;
import org.tms.teq.Derivation;
import org.tms.util.JustInTimeSet;

abstract class TableSliceElementImpl extends TableCellsElementImpl implements Derivable, TableRowColumnElement
{
    abstract protected TableSliceElementImpl insertSlice(int idx);
    abstract protected TableSliceElementImpl setCurrent();
    
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
    public void setDerivation(String expr)
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
            Derivable elem = m_deriv.getTarget();
            for (TableElement d : m_deriv.getAffectedBy()) {
                TableElementImpl tse = (TableElementImpl)d;
                tse.deregisterAffects(elem);
            }
            
            m_deriv.destroy();
            m_deriv = null;
        }        
    }
    
    @Override
    public void recalculate()
    {
        vetElement();
        if (isDerived()) {
            m_deriv.recalculateTarget();
            
            // recalculate dependent columns
            TableImpl table = getTable();
            if (table != null) 
                table.recalculateAffected(this);
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
            return ((RowImpl)this).getCellInternal((ColumnImpl)tse, false);
        else if (this instanceof ColumnImpl)
            return ((ColumnImpl)this).getCellInternal((RowImpl)tse, false);
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
    
    protected void pushCurrent()
    {
        if (getTable() != null)
            getTable().pushCurrent();
    }
    
    protected void popCurrent()
    {
        if (getTable() != null)
            getTable().popCurrent();
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
                return m_subsets.size();
                
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
                    col = (ColumnImpl) col;
                }
            }
        }     
        
        return parent.getCell(row,  col);
    }
    
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
        
        pushCurrent();       
        boolean setSome = false;
        try {
            // Clear derivation, since fill should override
            clearDerivation();
            
            boolean readOnlyExceptionEncountered = false;
            boolean nullValueExceptionEncountered = false;
            for (Cell c : cells()) {
                if (c != null) {
                    if (isDerived(c)) 
                        continue;
                    
                    try {
                        if (((CellImpl)c).setCellValue(o, true));
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
            
            if (setSome)
                this.setInUse(true); 
            else if (readOnlyExceptionEncountered)
                throw new ReadOnlyException(this, TableProperty.CellValue);
            else if (nullValueExceptionEncountered)
                throw new NullValueException(this, TableProperty.CellValue);
        }
        finally { 
            popCurrent();
        }
        
        if (setSome && parent != null)
            parent.recalculateAffected(this);
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
    
    public String toString()
    {
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
