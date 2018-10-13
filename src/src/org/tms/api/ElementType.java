package org.tms.api;

import java.util.ArrayList;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * The core elements provided in the TMS system
 * 
 * @since {@value org.tms.api.utils.ApiVersion#INITIAL_VERSION_STR}
 * @version {@value org.tms.api.utils.ApiVersion#CURRENT_VERSION_STR}
 */
public enum ElementType
{
    /** A collection of {@link Table}s */
    TableContext,
    /** A data table, consisting of {@link Row}s, {@link Column}s, {@link Cell}s, and {@link Subset}s */
    Table,
    /** A {@link Table} row */
    Row,
    /** A {@link Table} column */
    Column,
    /** A {@link Table} cell */
    Cell,
    /** A {@link Table} subset; a range of rows, columns, cells, and other subsets */
    Subset,
    /** A formula used to calculate cell values*/
    Derivation;  
    
    /**
     * Returns a {@link List} of {@link TableProperty}s that relate to this {@code ElementType}.
     * @return a {@code List} of {@code TableProperty}s that relate to this {@code ElementType}
     */
    public List<TableProperty> getProperties()
    {
        Set<TableProperty> properties = new HashSet<TableProperty>();
        
        for (TableProperty tp : TableProperty.values()) {
            if (tp.isImplementedBy(this) )
                properties.add(tp);
        }
        
        return toSortedList(properties);
    }

    /**
     * Returns a {@link List} of the optional {@link TableProperty}s that relate to this {@code ElementType}.
     * Optional properties <em>may</em> but do not have to be implemented by 
     * implementations of this {@code ElementType}.
     * @return a {@code List} of the optional {@code TableProperty}s that relate to this {@code ElementType}
     */
    public List<TableProperty> getOptionalProperties()
    {
        Set<TableProperty> properties = new HashSet<TableProperty>();
        
        for (TableProperty tp : TableProperty.values()) {
            if (tp.isImplementedBy(this) && tp.isOptional())
                properties.add(tp);
        }
        
        return toSortedList(properties);
    }
    
    /**
     * Returns a {@link List} of the non-optional (required) {@link TableProperty}s that 
     * relate to this {@code ElementType}. Non-optional properties <em>must</em> be implemented
     * by implementations of this {@code ElementType}.
     * @return a {@code List} of the non-optional (required) {@code TableProperty}s that 
     * relate to this {@code ElementType}
     */
    public List<TableProperty> getNonOptionalProperties()
    {
        Set<TableProperty> properties = new HashSet<TableProperty>();
        
        for (TableProperty tp : TableProperty.values()) {
            if (tp.isImplementedBy(this) && !tp.isOptional())
                properties.add(tp);
        }
        
        return toSortedList(properties);
    }
    
    /**
     * Returns a {@link List} of the initializable (required) {@link TableProperty}s that 
     * relate to this {@code ElementType}. Initializable properties are have their values set
     * when the {@code ElementType} is created from a template or from the parent {@link TableContext}.
     * @return a {@code List} of the initializable {@code TableProperty}s that 
     * relate to this {@code ElementType}
     */
    public List<TableProperty> getInitializableProperties()
    {
        Set<TableProperty> properties = new HashSet<TableProperty>();
        
        for (TableProperty tp : TableProperty.values()) {
            if (tp.isImplementedBy(this) && tp.isInitializable())
                properties.add(tp);
        }
        
        return toSortedList(properties);
    }
    
    /**
     * Returns a {@link List} of the read-only {@link TableProperty}s that 
     * relate to this {@code ElementType}. Read-only properties may be queried but their values
     * cannot be changed.
     * @return a {@code List} of the read-only {@code TableProperty}s that 
     * relate to this {@code ElementType}
     */
    public List<TableProperty> getReadOnlyProperties()
    {
        Set<TableProperty> properties = new HashSet<TableProperty>();
        
        for (TableProperty tp : TableProperty.values()) {
            if (tp.isImplementedBy(this) && tp.isReadOnly())
                properties.add(tp);
        }
        
        return toSortedList(properties);
    }

    /**
     * Returns a {@link List} of the mutable (not read-only) {@link TableProperty}s that 
     * relate to this {@code ElementType}. Mutable properties may be queried and their values
     * changed.
     * @return a {@code List} of the mutable {@code TableProperty}s that 
     * relate to this {@code ElementType}
     */
    public List<TableProperty> getMutableProperties()
    {
        Set<TableProperty> properties = new HashSet<TableProperty>();       
        for (TableProperty tp : TableProperty.values()) {
            if (tp.isImplementedBy(this) && !tp.isReadOnly())
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

    /**
     * Returns a string for this {@code ElementType} that is appropriate to use to reference such elements in derivations.
     * @return a string for this {@code ElementType}
     */
	public String asReferenceLabel() 
	{
		switch (this) {
			case Column:
				return "Col";
		
			default:
				return this.name().toLowerCase();
		}
	}
}
