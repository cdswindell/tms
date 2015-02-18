package org.tms.api.event;

import java.util.HashSet;
import java.util.Set;

import org.tms.api.ElementType;
import org.tms.api.TableElement;
import org.tms.api.exceptions.TableErrorClass;
import org.tms.api.exceptions.TableException;

public enum TableElementEventType
{
    OnBeforeCreate(true, false, ElementType.Table, ElementType.Row, ElementType.Column, ElementType.Subset),
    OnBeforeDelete(true, false, ElementType.Table, ElementType.Row, ElementType.Column, ElementType.Subset),
    OnBeforeNewValue(true, true, ElementType.Table, ElementType.Row, ElementType.Column, ElementType.Subset, ElementType.Cell),
    
    OnNewValue(false, true, ElementType.Table, ElementType.Row, ElementType.Column, ElementType.Subset, ElementType.Cell),
    OnCreate(false, false, ElementType.Table, ElementType.Row, ElementType.Column, ElementType.Subset),
    OnDelete(false, false, ElementType.Table, ElementType.Row, ElementType.Column, ElementType.Subset),
    
    OnPendings(false, true, ElementType.Table, ElementType.Row, ElementType.Column, ElementType.Cell, ElementType.Subset),
    OnNoPendings(false, true, ElementType.Table, ElementType.Row, ElementType.Column, ElementType.Cell, ElementType.Subset),
    ;
    
    private Set<ElementType> m_implementedBy = new HashSet<ElementType>();
    private boolean m_throwExceptions;
    private boolean m_alertParent;
    
    private TableElementEventType(boolean throwExceptions, boolean alertParent, ElementType... implementedBy)
    {
        if (implementedBy != null)
        {
            for (ElementType t : implementedBy)
                if (!m_implementedBy.add(t))
                    throw new TableException(String.format("TableElementEventType: %s Duplicate BaseElementType: %s", this, t), TableErrorClass.Invalid);
        }  
        
        m_throwExceptions = throwExceptions;
        m_alertParent = alertParent;
    }
    
    public boolean isAlertParent()
    {
        return m_alertParent;
    }
    
    public boolean isThrowExceptions()
    {
        return m_throwExceptions;
    }
    
    public boolean isImplementedBy(TableElement te)
    {
        if (te == null)
            return false;
        else
            return isImplementedBy(te.getElementType());
    }
    
    public boolean isImplementedBy(ElementType et)
    {
        if (et == null)
            return false;
        else
            return m_implementedBy.contains(et);
    }   
}
