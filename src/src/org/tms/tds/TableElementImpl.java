package org.tms.tds;

import java.util.Map;
import java.util.Set;

import org.tms.api.ElementType;
import org.tms.api.TableContext;
import org.tms.api.TableElement;
import org.tms.api.TableProperty;
import org.tms.api.derivables.Derivable;
import org.tms.api.events.Listenable;
import org.tms.api.exceptions.NotUniqueException;

abstract class TableElementImpl extends BaseElementImpl implements TableElement, Listenable
{
	abstract public TableImpl getTable();
	abstract public TableContext getTableContext();
    abstract protected void delete(boolean compress);
    abstract public boolean fill(Object o);
    
    abstract protected void registerAffects(Derivable d);
    abstract protected void deregisterAffects(Derivable d);
    
    abstract protected boolean isDataTypeEnforced();
    
    abstract protected boolean isNullsSupported();
    abstract public boolean isWriteProtected();
    
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
    
    @Override
    public void setLabel(String label)
    {
        if (isLabelIndexed()) {
            ElementType et = getElementType();
            Map<String, TableElementImpl> elemIndex = getTable().getElementIndex(et);
            
            assert elemIndex != null : et + " Label Index is null";
            
            synchronized (elemIndex) {
                String curLabel = getLabel();
                TableElementImpl curElem = null;
                if (curLabel != null) {
                    curElem = elemIndex.remove(curLabel.trim().toLowerCase());
                    assert curElem == null || curElem == this : et + " label index inconsistent";
                }
                
                String newLabelKey = null;
                if (label == null || (newLabelKey = label.trim().toLowerCase()).length() == 0) 
                    ; // noop, we have already removed the current label
                else {
                    // check if key is unique
                    if (elemIndex.containsKey(newLabelKey)) {
                        // this means the label isn't unique, reindex the original label
                        // and throw a NotUniqueException
                        if (curLabel != null) 
                            elemIndex.put(curLabel.trim().toLowerCase(), this);
                        throw new NotUniqueException(this, TableProperty.Label, label);
                    }
                    else
                        elemIndex.put(newLabelKey, this);
                }
                
                // set the actual label
                super.setLabel(label);
            } // of sync block
        }
        else
            super.setLabel(label);
    }
    
    public String toString()
    {
        if (isInvalid())
            return String.format("[Deleted %s]", getElementType());
        
        String label = getLabel();
        if (label != null)
            label = ": " + label;
        else
            label = "";
        
        return String.format("[%s%s]", getElementType(), label);
    }
}
