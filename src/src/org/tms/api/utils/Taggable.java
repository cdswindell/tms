package org.tms.api.utils;

public interface Taggable
{
    /*
     * Tagging
     */
    public boolean tag(String... tags);
    public boolean untag(String... tags);
    public boolean isTagged(String... tags);
    public void setTags(String... tags);
    public String[] getTags();

}