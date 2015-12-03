package org.tms.tds;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;

import org.junit.AfterClass;
import org.junit.Test;
import org.tms.BaseTest;
import org.tms.api.Access;
import org.tms.api.Column;
import org.tms.api.Table;
import org.tms.api.TableContext;
import org.tms.api.TableProperty;
import org.tms.api.factories.TableContextFactory;
import org.tms.api.factories.TableFactory;

public class TagTest extends BaseTest
{
    @AfterClass
    static public void cleanup()
    {
        TableContext tc = TableContextFactory.fetchDefaultTableContext();
        TdsUtils.clearGlobalTagCache(tc);
    }
    
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
        
        ContextImpl tc = (ContextImpl)tbl.getTableContext();
        assertNotNull(tc);
        
        tc.clearGlobalTagCache();
        assertThat(tc.getGlobalTagCache().size(), is(0));

        tbl.tag("DeF", "aBc");
        assertNotNull(tbl.getTags());
        assertThat(tbl.getTags(), is(new String []{"abc", "def"}));
        assertThat(tc.getGlobalTagCache().size(), is(2));
        
        assertThat(tbl.tag("def", "abc"), is(false));
        assertThat(tbl.tag("ghi"), is(true));
        assertThat(tbl.getTags(), is(new String []{"abc", "def", "ghi"}));
        assertThat(tc.getGlobalTagCache().size(), is(3));
        
        assertThat(tbl.untag("abc"), is(true));
        assertThat(tbl.untag("abc"), is(false));
        assertThat(tbl.untag("xxx"), is(false));
        assertThat(tbl.getTags(), is(new String []{"def", "ghi"}));
        assertThat(tc.getGlobalTagCache().size(), is(3));
       
        tbl.setTags("tms");
        assertThat(tbl.getTags(), is(new String []{"tms"}));
        assertThat(tbl.getProperty(TableProperty.Tags), is(new String []{"tms"}));
        assertThat(tbl.isTagged(), is (true));
        assertThat(tbl.isTagged("tms"), is (true));
        assertThat(tbl.isTagged("tms", "abc"), is (false));
        assertThat(tbl.isTagged("xxx", "abyyyyc"), is (false));
        assertThat(tc.getGlobalTagCache().size(), is(4));
        
        tbl.setTags();
        assertThat(tbl.isTagged(), is (false));
        assertThat(tbl.getTags(), nullValue());
        assertThat(tc.getGlobalTagCache().size(), is(4));
        tc.clearGlobalTagCache();
    }
    
    @Test
    public final void testTagAccess()
    {
        Table tbl = TableFactory.createTable();
        assertNotNull(tbl);
        
        Column c1 = tbl.addColumn();
        c1.tag("c1");
        
        Column c2 = tbl.addColumn();
        c2.tag("c1", "c2");
        
        Column c = tbl.getColumn(Access.ByTag, "c1");
        assertNotNull(c);
        assertThat(c, is(c1));
        
        c = tbl.getColumn(Access.ByTag, "c2");
        assertNotNull(c);
        assertThat(c, is(c2));
        
        c = tbl.getColumn(Access.ByTag, "c1", "c2");
        assertNotNull(c);
        assertThat(c, is(c2));
        
        c = tbl.getColumn(Access.ByTag, "c1", "c2", "c3");
        assertNull(c);
    }
}
