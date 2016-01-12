package org.tms.io;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

import org.tms.api.Access;
import org.tms.api.Cell;
import org.tms.api.Column;
import org.tms.api.Row;
import org.tms.api.Subset;
import org.tms.api.Table;
import org.tms.api.TableContext;
import org.tms.api.factories.TableContextFactory;
import org.tms.api.factories.TableFactory;
import org.tms.tds.TableImpl;
import org.tms.util.Point;

abstract public class BaseArchivalTest extends BaseIOTest
{
    protected static final String SAMPLE1 = "sample1.csv";
    
    protected Table getBasicTable()
    {
        Table gst = importCVSFile(qualifiedFileName(SAMPLE1, "csv"), true, true);
        assertNotNull(gst);
        gst.setLabel("Test XML & Export Table");
        gst.tag("red", "green");
        
        Column gsc3 = gst.getColumn(3);
        Cell r1c3 = gst.getCell(gst.getRow(1), gsc3);
        r1c3.clear();
        r1c3.setLabel("foo");
        r1c3.setUnits("mph");
        r1c3.setDescription("Cell Description");
        
        Subset s1 = gst.addSubset(Access.ByLabel, "Excluded Cols");
        Subset s2 = gst.addSubset(Access.ByLabel, "Some Cols");
        s2.tag("red", "subset");
        
        Column gsc = gst.addColumn();
        s1.add(gsc);
        s2.add(gsc, gsc3);
        
        gsc = gst.addColumn();
        s2.add(gsc);

        gsc.setDerivation("col 1 * col 1");
        gsc.tag("derived", "calculated", "no-user-data");
        
        return gst;
    }
    
    protected Table getOneCellTable()
    {
        return getOneCellTable(TableContextFactory.fetchDefaultTableContext());
    }
    
    protected Table getOneCellTable(TableContext tc)
    {
        Table t = TableFactory.createTable(1024,  1024, tc); 
        t.setLabel("Test XML & Export One Cell Table");
        Row r = t.addRow(Access.ByIndex, 1024);
        Column c = t.addColumn(Access.ByIndex, 1024);
        
        t.setCellValue(r,c,"abc"); 

        return t;
    }
   
    protected Table getPointsTable()
    {
    	return getPointsTable(TableContextFactory.fetchDefaultTableContext());
    }
    
    protected Table getPointsTable(TableContext tc)
    {
        Table t = TableFactory.createTable(16,  16, tc); 
        t.setLabel("Points Table");
        
        Column c1 = t.addColumn();
        c1.setLabel("Point 1");
        c1.setDataType(Point.class);
        
        Column c2 = t.addColumn();
        c2.setLabel("Point 2");
        c2.setDataType(Point.class);
        
        for (int i = 1; i <= 16; i++) {
        	Row r = t.addRow();
        	
        	Point p = new Point(getRandomCoord(0, 128), getRandomCoord(0, 128));
            t.setCellValue(r, c1, p); 
            
        	p = new Point(getRandomCoord(0, 128), getRandomCoord(0, 128));
            t.setCellValue(r, c2, p); 
        }
        
        // add a few more cols
        t.addColumn(3);
        
        return t;
    }
    
    private int getRandomCoord(int init, int max) 
    {
		return init + (int)(Math.random() * max) + 1;
	}

	protected TableContext getPopulatedTableContext()
    {
        TableContext tc = TableContextFactory.createTableContext();
        
        Table t = getOneCellTable(tc); 
        
        assertThat(t.getNumRows(), is(1024));
        assertThat(t.getNumColumns(), is(1024));
        assertThat(t.getNumCells(), is(1));
        
        assertThat(1024, is(((TableImpl)t).getRowsCapacity()));
        assertThat(1024, is(((TableImpl)t).getColumnsCapacity()));
        
        Table t2 = TableFactory.createTable(32, 32, tc); 
        t2.setLabel("Empty Table");
        
        Table t3 = TableFactory.createTable(32, 32, tc); 
        t3.setLabel("Almost Empty Table");
        t3.addRow(10);
        Column c1 = t3.addColumn(1);
        c1.setDerivation("rIdx");
        Column c2 = t3.addColumn(2);
        c2.setDerivation("col 1 * col 1");
        
        return tc;
    }
}
