package org.tms.api.events;

import java.util.HashSet;
import java.util.Set;

import org.tms.api.ElementType;
import org.tms.api.TableElement;
import org.tms.api.exceptions.TableErrorClass;
import org.tms.api.exceptions.TableException;

public enum TableElementEventType
{
    OnBeforeCreate(true, false, ElementType.Table, ElementType.Subset, ElementType.Row, ElementType.Column),
    OnBeforeDelete(true, true, ElementType.Table, ElementType.Subset, ElementType.Row, ElementType.Column),
    OnBeforeNewValue(true, true, ElementType.Table, ElementType.Row, ElementType.Column, ElementType.Cell),
    
    OnNewValue(false, true, ElementType.Table, ElementType.Row, ElementType.Column, ElementType.Cell),
    OnCreate(false, true, ElementType.Table, ElementType.Subset, ElementType.Row, ElementType.Column),
    OnDelete(false, true, ElementType.Table, ElementType.Subset, ElementType.Row, ElementType.Column),
    
    OnPendings(false, true, ElementType.Table, ElementType.Row, ElementType.Column, ElementType.Cell),
    OnNoPendings(false, true, ElementType.Table, ElementType.Row, ElementType.Column, ElementType.Cell),
    
    OnRecalculate(false, false, ElementType.Table, ElementType.Row, ElementType.Column, ElementType.Cell),
    ;
    
    private Set<ElementType> m_implementedBy = new HashSet<ElementType>();
    private boolean m_notifyInSameThread;
    private boolean m_alertParent;
    
    private TableElementEventType(boolean notifyInSameThread, boolean alertParent, ElementType... implementedBy)
    {
        if (implementedBy != null)
        {
            for (ElementType t : implementedBy)
                if (!m_implementedBy.add(t))
                    throw new TableException(String.format("TableElementEventType: %s Duplicate BaseElementType: %s", this, t), TableErrorClass.Invalid);
        }  
        
        m_notifyInSameThread = notifyInSameThread;
        m_alertParent = alertParent;
    }
    
    public boolean isAlertContainer()
    {
        return m_alertParent;
    }
    
    public boolean isNotifyInSameThread()
    {
        return m_notifyInSameThread;
    }
    
    public boolean isImplementedBy(TableElement te)
    {
        return isImplementedBy(te.getElementType());
    }
    
    public boolean isImplementedBy(Listenable te)
    {
        if (te == null)
            return false;
        else
            return isImplementedBy((TableElement)te);
    }
    
    public boolean isImplementedBy(ElementType et)
    {
        if (et == null)
            return false;
        else
            return m_implementedBy.contains(et);
    }   
}
