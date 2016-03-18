package org.tms.tds;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Tag implements Serializable, Comparable<Tag>
{
    private static final long serialVersionUID = -8379517266723894527L;

    static String [] decodeTags(Set<Tag> tags)
    {
        List<String> strTags = new ArrayList<String>(tags != null ? tags.size() : 0);
        if (tags != null) {
            for (Tag t : tags) {
                strTags.add(t.getLabel());
            }
            
            Collections.sort(strTags);
        }
        
        return strTags == null || strTags.size() == 0 ? null : strTags.toArray(new String [] {});
    }  
    
    static Set<Tag> encodeTags(String [] tags, ContextImpl tc)
    {
        return encodeTags(tags, tc, true);
    }
    
    static Set<Tag> encodeTags(String [] tags, ContextImpl tc, boolean createIfMissing)
    {
        if (tags == null || tags.length == 0)
            return Collections.emptySet();
        
        Set<Tag> tagObjs = new HashSet<Tag>(tags != null ? tags.length : 0);
        for (String t : tags) {
            Tag tagObj = tc.fetchTag(t, createIfMissing);
            if (tagObj != null)
                tagObjs.add(tagObj);
        }
        
        return tagObjs;
    }  
    
    private String m_label;
    
    public Tag(String label)
    {
        m_label = label.trim();
    }
    
    public String getLabel()
    {
        return m_label;
    }

    @Override
    public String toString()
    {
        return "Tag [" + m_label + "]";
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((m_label == null) ? 0 : m_label.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj) 
            return true;
        
        if (obj == null) 
            return false;
        
        if (!(obj instanceof Tag)) 
            return false;
        
        Tag other = (Tag) obj;
        if (m_label == null) {
            if (other.m_label != null) 
                return false;
        }
        else if (!m_label.equals(other.m_label)) 
            return false;
        
        return true;
    }
    

	@Override
	public int compareTo(Tag o) 
	{
		if (o == null)
			return 1;
		
		return this.getLabel().compareTo(o.getLabel());
	}
}
