package org.tms.api.utils;

/**
 * Methods that allow {@link org.api.tms.TableElement TableElement}s to be selectively tagged. Tags are short strings that can 
 * be assigned and removed from {@code TableElement}s to classify, identify, and otherwise differentiate tables, rows, columns, subsets, 
 * and cells.
 * 
 * @since {@value org.tms.api.utils.ApiVersion#INITIAL_VERSION_STR}
 * @version {@value org.tms.api.utils.ApiVersion#CURRENT_VERSION_STR}
 */
public interface Taggable
{
    /**
     * 
     * @param tags String array of tags to apply to this TableELement
     * @return true if any supplied tags are new
     */
    public boolean tag(String... tags);
    
    /**
     * 
     * @param tags
     * @return
     */
    public boolean untag(String... tags);
    
    /**
     * 
     * @param tags
     * @return
     */
    public boolean isTagged(String... tags);
    
    /**
     * 
     * @param tags
     */
    public void setTags(String... tags);
    
    /**
     * 
     * @return
     */
    public String[] getTags();
}