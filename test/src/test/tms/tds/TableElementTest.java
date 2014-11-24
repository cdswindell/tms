package test.tms.tds;

import static org.junit.Assert.*;
import static org.hamcrest.core.Is.is;

import org.junit.Test;
import org.tms.tds.Table;

public class TableElementTest
{

    @Test
    public void testTableElement()
    {
        Table t = new Table();
        
        assert (t != null);
        
        t.setLabel("abcdef");
        String l = t.getLabel();
        
        assertThat(l, is("abcdef"));
    }

}
