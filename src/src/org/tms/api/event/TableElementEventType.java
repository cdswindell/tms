package org.tms.api.event;

import java.util.HashSet;
import java.util.Set;

import org.tms.api.ElementType;
import org.tms.api.exceptions.TableErrorClass;
import org.tms.api.exceptions.TableException;
import org.tms.tds.BaseElementImpl;

public enum TableElementEventType
{
    OnBeforeCreate(ElementType.Table, ElementType.Row, ElementType.Column, ElementType.Subset),
    OnBeforeDelete(ElementType.Table, ElementType.Row, ElementType.Column, ElementType.Subset),
    OnBeforeNewValue(ElementType.Cell),
    
    OnNewValue(ElementType.Cell),
    OnCreate(ElementType.Table, ElementType.Row, ElementType.Column, ElementType.Subset),
    OnDelete(ElementType.Table, ElementType.Row, ElementType.Column, ElementType.Subset),
    
    OnPendings(ElementType.Table, ElementType.Row, ElementType.Column, ElementType.Cell, ElementType.Subset),
    OnNoPendings(ElementType.Table, ElementType.Row, ElementType.Column, ElementType.Cell, ElementType.Subset),
    ;
    
    private Set<ElementType> m_implementedBy = new HashSet<ElementType>();
    
    private TableElementEventType(ElementType... implementedBy)
    {
        if (implementedBy != null)
        {
            for (ElementType t : implementedBy)
                if (!m_implementedBy.add(t))
                    throw new TableException(String.format("TableElementEventType: %s Duplicate BaseElementType: %s", this, t), TableErrorClass.Invalid);
        }        
    }
    public boolean isImplementedBy(BaseElementImpl te)
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
