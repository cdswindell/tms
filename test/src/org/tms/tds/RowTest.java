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
            System.out.println("Row property: " + p);
            Object value = t.getProperty(p);
            if (!p.isOptional() && value == null)
                fail("Parameter not retrieved: " + p);
        }
    }
    
    @Test
    public void getBooleanPropertiesTest()
    {
        Table t = new Table(12, 10);        
        assert (t != null);
        
        Row r = new Row(t);

        Set<TableProperty> props = r.getProperties();
        for (TableProperty p : props) { 
            if (!p.isBooleanValue()) continue;
            
            // will fail if property getter not implemented
            System.out.println("Row boolean property: " + p);
            r.getPropertyBoolean(p);
        }
    }

    @Test
    public void getIntPropertiesTest()
    {
        Table t = new Table(12, 10);        
        assert (t != null);
        
        Row r = new Row(t);

        Set<TableProperty> props = r.getProperties();
        for (TableProperty p : props) { 
            if (!p.isIntValue()) continue;
            
            // will fail if property getter not implemented
            System.out.println("Row int property: " + p);
            r.getPropertyInt(p);
        }
    }

}
