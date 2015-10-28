package org.tms.api;

/**
 * Methods that allow {@link org.tms.api.TableElement TableElement}s to be selectively tagged. Tags are short strings that can 
 * be assigned and removed from {@code TableElement}s to classify, identify, and otherwise differentiate tables, rows, columns, subsets, 
 * and cells.
 * 
 * @since {@value org.tms.api.utils.ApiVersion#IO_ENHANCEMENTS_STR}
 * @version {@value org.tms.api.utils.ApiVersion#CURRENT_VERSION_STR}
 */
public interface Taggable
{
    /**
     * Tags this object with the specified tags.
     * @param tags String array of tags to apply to this {@code Taggable} element.
     * @return true if any supplied tags are new
     */
    public boolean tag(String... tags);
    
    /**
     * Removes the specified tags from this {@code Taggable} element.
     * @param tags the tag(s) to remove from this element
     * @return true if any of the supplied tags were present
     */
    public boolean untag(String... tags);
    
    /**
     * Returns {@code true} if this taggable table element is tagged with the specified tags. If 
     * {@code tags} is {@code null}, returns {@code true} if the element contains any tags.
     * @param tags the specified tags to check for, or null
     * @return true if the TableElement is tagged with the specified tags
     */
    public boolean isTagged(String... tags);
    
    /**
     * Replaces the tags on this {@code Taggable} element with those supplied. If {@code tags}
     * is {@code null}, all tags are removed.
     * @param tags the new set of tags to apply to this {@code Taggable} element
     */
    public void setTags(String... tags);
    
    /**
     * Returns the tags assigned to this {@code Taggable} element.
     * @return String array of tags that have been applied to this {@code Taggable} element
     */
    public String[] getTags();
}