package org.tms.api;

import java.util.List;

import org.tms.api.derivables.Derivable;
import org.tms.api.events.Listenable;

/**
 * Methods common to all {@link TableElement}s, including {@link Table}s, {@link Row}s, {@link Column}s, 
 * {@link Subset}s, and {@link Cell}s. 
 * <p>
 * @since {@value org.tms.api.utils.ApiVersion#INITIAL_VERSION_STR}
 * @version {@value org.tms.api.utils.ApiVersion#CURRENT_VERSION_STR}
 */
public interface TableElement extends BaseElement, Listenable, Taggable
{
    /**
     * Returns the parent {@link TableContext}.
     * @return the parent TableContext
     */
    public TableContext getTableContext();

    /**
     * Returns the parent {@link Table}.
     * @return the parent Table
     */
    public Table getTable();
    
    /**
     * Delete this {@link TableElement} and remove it from the parent {@link Table} or {@link TableContext}.
     */
    public void delete();
    
    /**
     * Set all {@link Cell}s in this {@link TableElement} to the specified value. If the {@link TableElement} contains any
     * {@link org.tms.api.derivables.Derivation Derivation}s, they are removed.
     * @param value the value
     * @return {@code true} if any {@link Cell} value(s) in this {@link TableElement} were modified as the result of this call
     */
    public boolean fill(Object value);
    
    /**
     * Set all {@link Cell}s in this {@link TableElement} to {@code null}. If the {@link TableElement} contains any
     * {@link org.tms.api.derivables.Derivation Derivation}s, they are removed.
     * @return {@code true} if any {@link Cell} value(s) in this {@link TableElement} were modified as the result of this call
     */
    public boolean clear();
    
    /**
     * Returns the number of {@link Cell}s in this {@link TableElement}.
     * @return the number of {@code Cell}s in this {@code TableElement}
     */
    public int getNumCells();
    
    /**
     * Returns a {@link Iterable Iterable&lt;Cell&gt;} that iterates over all of the 
     * {@link Cell}s in this {@link TableElement}.
     * @return an {@code Iterable<Cell>}
     */
    public Iterable<Cell> cells();
    
    /**
     * Returns {@code true} if all of the {@link Cell}s in this {@link TableElement} are {@code null}.
     * @return {@code true} if all of the {@code Cell}s in this {@code TableElement} are {@code null}
     */
    public boolean isNull();
    
    /**
     * Returns {@code true} if this {@link TableElement} is invalid. {@link TableElement}s become invalid when
     * they are deleted.
     * @return {@code true} if this {@code TableElement} is invalid
     * @see TableElement#isValid()
     */
    public boolean isInvalid();
    
    /**
     * Returns {@code true} if this {@link TableElement} is valid. {@link TableElement}s become invalid when
     * they are deleted.
     * @return {@code true} if this {@code TableElement} is valid
     * @see TableElement#isInvalid()
     */
    public boolean isValid();
   
    /**
     * Returns {@code true} if any {@link Cell}s in this {@link TableElement} have values that are dependent
     * on the completion of {@link org.tms.api.derivables.Derivation Derivation}s within this {@link TableElement}.
     * @return {@code true} if any {@code Cell} values are in the process of being calculated
     */
    public boolean isPendings();
    
    /**
     * Return the label string assigned to this {@link TableElement}.
     * @return the label string assigned to this {@code TableElement}
     */
    public String getLabel();
    
    /**
     * Set the label string to associate with this {@link TableElement}. Set the label string to 
     * {@code null} to remove the existing label, if one exists. Label strings can be used in
     * {@link org.tms.api.derivables.Derivation Derivation} expressions to refer to this or other
     * {@link TableElement}s. For example, if a {@link Column}'s label is set to the string "Units", 
     * and that column contains numeric values, then the following {@link org.tms.api.derivables.Derivation Derivation} 
     * expression is valid:
     * <blockquote><pre>
     *    mean(col "Units") * 10.50
     * </pre></blockquote>  
     * This expression could be used to set a {@link Cell} value to the average cost of inventory per site.
     * @param label the new label string
     */
    public void setLabel(String label);
    
    /**
     * Returns {@code true} if this {@link TableElement}s labels are indexed. When indexed, label strings must
     * be unique within the set of items indexed. For example, if {@link Row} labels in a {@link Table} are indexed, then
     * the {@link Row} labels in that table must all be different.
     * @return Returns {@code true} if this {@code TableElement}s labels are indexed
     */
    public boolean isLabelIndexed();
    
    public String getUuid();
    
    /**
     * Return the description string assigned to this {@link TableElement}.
     * @return the description string assigned to this {@code TableElement}
     */
    public String getDescription();
    
    /**
     * Set the description string of this {@link TableElement} to the specified value.
     * @param description the new description string
     */
    public void setDescription(String description);
    
    /**
     * Returns an {@link java.util.Collections#unmodifiableList Collections.unmodifiableList&lt;Subset&gt;} of {@link Subset}s of this {@link Table}. 
     * @return an {@code unmodifiableList} of the table subsets
     */
    public List<Subset> getSubsets();
    
    /**
     * Returns an {@link Iterable} to an {@link java.util.Collections#unmodifiableList Collections.unmodifiableList&lt;Subset&gt;} 
     * of {@link Subset}s of this {@link Table}. 
     * @return an {@link Iterable} to an {@code unmodifiableList} of the table subsets
     */
    public Iterable<Subset> subsets();
       
    /** 
     * Return the number of {@link Subset}s that this element is contained within.
     * @return the number of Subsets that this element is contained within
     */
    public int getNumSubsets();
    
    /**
     * Returns a {@link List} of the {@code Derivable} elements
     * that this {@code TableElement} impacts when modified
     * @return a List of the Derivable elements that this element affects
     */
    public List<Derivable> getAffects();
    
    /**
     * Returns an {@link java.util.Collections#unmodifiableList Collections.unmodifiableList&lt;Derivation&gt;} 
     * of the {@link org.tms.api.derivables.Derivation Derivation}s associated with this {@code TableElement}
     * @return an {@code unmodifiableList} of the {@code Derivation}s associated with this {@code TableElement}.
     */
    public List<Derivable> getDerivedElements();
}
