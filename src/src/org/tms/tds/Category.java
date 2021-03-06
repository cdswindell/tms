package org.tms.tds;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Category extends Tag
{
	private static final long serialVersionUID = -8230907623744620687L;

	static String [] decodeCategories(Set<Category> tags)
    {
        List<String> strTags = new ArrayList<String>(tags != null ? tags.size() : 0);
        if (tags != null) {
            for (Category t : tags) {
                strTags.add(t.getLabel());
            }
            
            Collections.sort(strTags);
        }
        
        return strTags == null || strTags.size() == 0 ? null : strTags.toArray(new String [] {});
    }  
    
    static Set<Category> encodeCategories(String [] tags, ContextImpl tc)
    {
        return encodeCategories(tags, tc, true);
    }
    
    static Set<Category> encodeCategories(String [] tags, ContextImpl tc, boolean createIfMissing)
    {
        if (tags == null || tags.length == 0)
            return Collections.emptySet();
        
        Set<Category> tagObjs = new HashSet<Category>(tags != null ? tags.length : 0);
        TokenMapper tm = TokenMapper.fetchTokenMapper(tc);
        for (String t : tags) {
        	Category tagObj = tm.fetchCategory(t, createIfMissing);
            if (tagObj != null)
                tagObjs.add(tagObj);
        }
        
        return tagObjs;
    }  
	public Category(String label) 
	{
		super(label);
	}

    @Override
    public String toString()
    {
        return "Category [" + this.getLabel() + "]";
    }
}
