package org.tms.tds;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.tms.BaseTest;
import org.tms.api.Table;
import org.tms.api.factories.TableFactory;

public class TagTest extends BaseTest
{
    @Test
    public final void testConstructor()
    {
        Tag t = new Tag("foo"); 
        assertNotNull(t);
        assertThat(t.getLabel(), is("foo"));
    }
    
    @Test
    public final void testTagUsage()
    {
        Table tbl = TableFactory.createTable();
        assertNotNull(tbl);
        
        tbl.tag("DeF", "aBc");
        assertNotNull(tbl.getTags());
        assertThat(tbl.getTags(), is(new String []{"abc", "def"}));
        
        assertThat(tbl.tag("def", "abc"), is(false));
        assertThat(tbl.tag("ghi"), is(true));
        assertThat(tbl.getTags(), is(new String []{"abc", "def", "ghi"}));
        
        assertThat(tbl.untag("abc"), is(true));
        assertThat(tbl.untag("abc"), is(false));
        assertThat(tbl.getTags(), is(new String []{"def", "ghi"}));
        
        tbl.replaceTags("tms");
        assertThat(tbl.getTags(), is(new String []{"tms"}));
    }

}
