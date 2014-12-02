package org.tms.tds;

import static org.junit.Assert.*;

import java.util.Set;

import org.junit.Test;
import org.tms.api.TableProperty;

public class RowTest
{

    @Test
    public void getPropertiesTest()
    {
        Table t = new Table(12, 10);        
        assert (t != null);
        
        Row r = new Row(t);

        Set<TableProperty> props = r.getProperties();
        for (TableProperty p : props) {
            
            Object value = t.getProperty(p);
            if (!p.isOptional() && value == null)
                fail("Parameter not retrieved: " + p);
        }
    }
}
