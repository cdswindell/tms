package org.tms.api;

import java.util.HashSet;
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
    
    
    public Set<TableProperty> getProperties()
    {
        Set<TableProperty> properties = new HashSet<TableProperty>();
        
        for (TableProperty tp : TableProperty.values()) {
            if (tp.isImplementedBy(this) )
                properties.add(tp);
        }
        
        return properties;
    }

    public Set<TableProperty> getOptionalProperties()
    {
        Set<TableProperty> properties = new HashSet<TableProperty>();
        
        for (TableProperty tp : TableProperty.values()) {
            if (tp.isImplementedBy(this) && tp.isOptional())
                properties.add(tp);
        }
        
        return properties;
    }
    
    public Set<TableProperty> getNonOptionalProperties()
    {
        Set<TableProperty> properties = new HashSet<TableProperty>();
        
        for (TableProperty tp : TableProperty.values()) {
            if (tp.isImplementedBy(this) && !tp.isOptional())
                properties.add(tp);
        }
        
        return properties;
    }
    
    public Set<TableProperty> getInitializableProperties()
    {
        Set<TableProperty> initializableProperties = new HashSet<TableProperty>();
        
        for (TableProperty tp : TableProperty.values()) {
            if (tp.isImplementedBy(this) && tp.isInitializable())
                initializableProperties.add(tp);
        }
        
        return initializableProperties;
    }
    
    public Set<TableProperty> getReadOnlyProperties()
    {
        Set<TableProperty> properties = new HashSet<TableProperty>();
        
        for (TableProperty tp : TableProperty.values()) {
            if (tp.isImplementedBy(this) && tp.isReadOnly())
                properties.add(tp);
        }
        
        return properties;
    }
}
