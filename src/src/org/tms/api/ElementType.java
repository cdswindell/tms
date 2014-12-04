package org.tms.api;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public enum ElementType
{
    Context,
    Table,
    Row,
    Column,
    Cell,
    Range,
    Derivation;
    
    
    public List<TableProperty> getProperties()
    {
        Set<TableProperty> properties = new HashSet<TableProperty>();
        
        for (TableProperty tp : TableProperty.values()) {
            if (tp.isImplementedBy(this) )
                properties.add(tp);
        }
        
        return toSortedList(properties);
    }

    public List<TableProperty> getOptionalProperties()
    {
        Set<TableProperty> properties = new HashSet<TableProperty>();
        
        for (TableProperty tp : TableProperty.values()) {
            if (tp.isImplementedBy(this) && tp.isOptional())
                properties.add(tp);
        }
        
        return toSortedList(properties);
    }
    
    public List<TableProperty> getNonOptionalProperties()
    {
        Set<TableProperty> properties = new HashSet<TableProperty>();
        
        for (TableProperty tp : TableProperty.values()) {
            if (tp.isImplementedBy(this) && !tp.isOptional())
                properties.add(tp);
        }
        
        return toSortedList(properties);
    }
    
    public List<TableProperty> getInitializableProperties()
    {
        Set<TableProperty> properties = new HashSet<TableProperty>();
        
        for (TableProperty tp : TableProperty.values()) {
            if (tp.isImplementedBy(this) && tp.isInitializable())
                properties.add(tp);
        }
        
        return toSortedList(properties);
    }
    
    public List<TableProperty> getReadOnlyProperties()
    {
        Set<TableProperty> properties = new HashSet<TableProperty>();
        
        for (TableProperty tp : TableProperty.values()) {
            if (tp.isImplementedBy(this) && tp.isReadOnly())
                properties.add(tp);
        }
        
        return toSortedList(properties);
    }

    private List<TableProperty> toSortedList(Set<TableProperty> props)
    {
        List<TableProperty> list = new ArrayList<TableProperty>(props);
        Collections.sort(list, TableProperty::compareByName);
        return list;
    }
}
