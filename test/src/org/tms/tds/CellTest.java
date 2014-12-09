package org.tms.tds;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Test;
import org.tms.api.TableProperty;

public class CellTest
{

    @Test
    public void getPropertiesTest()
    {
        Cell c = new Cell(null, -1);

        List<TableProperty> props = c.getProperties();
        for (TableProperty p : props) {
            System.out.print("Cell property: " + p);
            Object value = c.getProperty(p);
            System.out.println(" = " + (value != null ? value.toString() : "<null>"));
        }
    }
    
    @Test
    public void getInitializablePropertiesTest()
    {
        Cell c = new Cell(null, -1);

        List<TableProperty> props = c.getInitializableProperties();
        for (TableProperty p : props) { 
            // will fail if property getter not implemented
            System.out.print("Cell initializable property: " + p);
            Object value = c.getProperty(p);
            System.out.println(" = " + (value != null ? value.toString() : "<null>")); 
        }
    }
    
    @Test
    public void getIntPropertiesTest()
    {
        Cell c = new Cell(null, -1);

        List<TableProperty> props = c.getProperties();
        for (TableProperty p : props) { 
        	if (!p.isIntValue()) continue;
            // will fail if property getter not implemented
            System.out.print("Cell int property: " + p);
            Object value = c.getProperty(p);
            System.out.println(" = " + (value != null ? value.toString() : "<null>")); 
        }
    }
    
    
    @Test
    public void getBooleanPropertiesTest()
    {
        Cell c = new Cell(null, -1);

        List<TableProperty> props = c.getProperties();
        for (TableProperty p : props) { 
        	if (!p.isBooleanValue()) continue;
            // will fail if property getter not implemented
            System.out.print("Cell boolean property: " + p);
            Object value = c.getProperty(p);
            System.out.println(" = " + (value != null ? value.toString() : "<null>")); 
        }
    }
    
}
