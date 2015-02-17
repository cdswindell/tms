package org.tms.tds;

import java.util.Set;

import org.tms.api.Derivable;
import org.tms.api.Table;
import org.tms.api.TableContext;
import org.tms.api.TableElement;
import org.tms.api.TableProperty;
import org.tms.api.event.Observable;
import org.tms.api.event.TableElementEventType;
import org.tms.api.event.TableElementListener;
import org.tms.api.exceptions.InvalidException;
import org.tms.api.exceptions.UnimplementedException;

abstract class TableElementImpl extends BaseElementImpl implements TableElement, Observable
{
	abstract public Table getTable();
	abstract public TableContext getTableContext();
    abstract protected void delete(boolean compress);
    abstract public void fill(Object o);
    
    abstract protected void registerAffects(Derivable d);
    abstract protected void deregisterAffects(Derivable d);
    
    abstract protected boolean isDataTypeEnforced();
    
    abstract protected boolean isNullsSupported();
    abstract protected boolean isWriteProtected();
    
    abstract protected boolean add(SubsetImpl subsetImpl);
    abstract protected boolean remove(SubsetImpl subset);
    abstract protected Set<SubsetImpl> getSubsetsInternal();
    
    protected TableElementImpl(TableElementImpl e)
    {
        super();
        
        // perform base initialization
        initialize(e);
    }

    /**
     * Perform general initializations
     * @param e
     */
    protected void initialize(TableElementImpl e)
    {
        clearProperty(TableProperty.Label);
        clearProperty(TableProperty.Description);
        setEnforceDataType(false);
    }
    
    @Override
    public boolean addListener(TableElementEventType evT, TableElementListener... tels)
    {
        if (evT == null)
            throw new InvalidException("TableElementEventType required.");
        if (tels != null) {
            if (!evT.isImplementedBy(this))
                throw new UnimplementedException(String.format("%s not supported by %s", evT, this.getElementType()));
        }
        
        return false;
    }
    
    @Override
    public void delete()
    {
        delete(true);
    }
    
    @Override
    public Object getProperty(TableProperty key)
    {
        // Some properties are built into the base Table Element object
        switch (key)
        {
            case Table:
                return getTable();
                
            case Context:
                return getTableContext();
                
            case Affects:
                return getAffects();
                                
            default:
                return super.getProperty(key);
        }
    }
    
    @Override
    protected boolean initializeProperty(TableProperty tp, Object value)
    {
        if (super.initializeProperty(tp, value))
            return true;
        
        boolean initializedProperty = true; // assume success
        switch (tp) {
            default:
                initializedProperty = false;   
                break;
        }
        
        return initializedProperty;
    }

    protected BaseElementImpl getInitializationSource(TableElementImpl e)
    {
        BaseElementImpl source = null;
        if (e != null && e != this)
            source = e;
        else if (getTable() != null && getTable() != this)
            source = (BaseElementImpl) getTable();
        else if (getTableContext() != null)
            source = (BaseElementImpl) getTableContext();
        else
            source = ContextImpl.getDefaultContext();

        return source;
    }
    
    public String toString()
    {
        String label = getLabel();
        if (label != null)
            label = ": " + label;
        else
            label = "";
        
        return String.format("[%s%s]", getElementType(), label);
    }
}
