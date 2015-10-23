package org.tms.api.utils;

public interface Taggable
{
    /*
     * Tagging
     */
    public boolean tag(String... tags);

    public boolean untag(String... tags);

    public void replaceTags(String... tags);

    public String[] getTags();

}