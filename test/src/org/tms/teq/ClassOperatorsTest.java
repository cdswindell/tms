package org.tms.teq;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.tms.BaseTest;
import org.tms.api.Cell;
import org.tms.api.Column;
import org.tms.api.Table;
import org.tms.api.TableContext;
import org.tms.api.TableProperty;
import org.tms.api.derivables.Derivation;
import org.tms.api.derivables.TokenMapper;
import org.tms.api.factories.TableContextFactory;
import org.tms.api.factories.TableFactory;
import org.tms.api.utils.RegisterOp;

public class ClassOperatorsTest extends BaseTest 
{

    @Test
    public void testClassConstructors()
    {
        TableContext tc = TableContextFactory.createTableContext();
        
        Table t1 = TableFactory.createTable(10, 12, tc);        
        assertThat(t1, notNullValue());
        assertThat(t1.getPropertyInt(TableProperty.numCells), is (0));
        
        // get token map and register MyPoint class
        TokenMapper tm = tc.getTokenMapper();
        assertNotNull(tm);
        
        tm.registerOperators(MyPoint.class);
        
        // create some rows and columns to work with
        t1.addRow(10);
        
        // set derivation
        Column c1 = t1.addColumn(1);
        Derivation d1 = c1.setDerivation("makeOriginMyPoint()");
        assertNotNull(d1);
        
        // set derivation
        Column c2 = t1.addColumn(2);
        Derivation d2 = c2.setDerivation("makeMyPoint(rIdx, cIdx)");
        assertNotNull(d2);
        
        // test the world
        for (int i = 1; i <= t1.getNumRows(); i++) {
        	Cell cl1 = t1.getCell(t1.getRow(i), c1);
            assertNotNull(cl1);
            assertThat(cl1.isNull(), is(false));
            assertThat(cl1.getDataType() == MyPoint.class, is(true));
            
            MyPoint p1 = (MyPoint)cl1.getCellValue();
            assertNotNull(p1);
            assertThat(0, is(p1.getX()));
            assertThat(0, is(p1.getY()));
            
        	Cell cl2 = t1.getCell(t1.getRow(i), c2);
            assertNotNull(cl2);
            assertThat(cl2.isNull(), is(false));
            assertThat(cl2.getDataType() == MyPoint.class, is(true));
            
            MyPoint p2 = (MyPoint)cl2.getCellValue();
            assertNotNull(p2);
            assertThat(i, is(p2.getX()));
            assertThat(c2.getIndex(), is(p2.getY()));
        }
    }

    @Test
    public void testClassMethodsSync()
    {
        TableContext tc = TableContextFactory.createTableContext();
        
        Table t1 = TableFactory.createTable(10, 12, tc);        
        assertThat(t1, notNullValue());
        assertThat(t1.getPropertyInt(TableProperty.numCells), is (0));
        
        // get token map and register MyPoint class
        TokenMapper tm = tc.getTokenMapper();
        assertNotNull(tm);
        
        tm.registerOperators(MyPoint.class);
        
        // create some rows and columns to work with
        t1.addRow(10);
        
        // set derivation
        Column c1 = t1.addColumn(1);
        Derivation d1 = c1.setDerivation("makeMyPoint(rIdx, cIdx)");
        assertNotNull(d1);
        
        // set derivation
        Column c2 = t1.addColumn(2);
        Derivation d2 = c2.setDerivation("getX(col 1)");
        assertNotNull(d2);
        
        // test the world
        for (int i = 1; i <= t1.getNumRows(); i++) {
        	Cell cl1 = t1.getCell(t1.getRow(i), c1);
            assertNotNull(cl1);
            assertThat(cl1.isNull(), is(false));
            assertThat(cl1.getDataType() == MyPoint.class, is(true));
            
            MyPoint p1 = (MyPoint)cl1.getCellValue();
            assertNotNull(p1);
            assertThat(i, is(p1.getX()));
            assertThat(c1.getIndex(), is(p1.getY()));
            
        	Cell cl2 = t1.getCell(t1.getRow(i), c2);
            assertNotNull(cl2);
            assertThat(cl2.isNull(), is(false));
            assertThat(cl2.isNumericValue(), is(true));
            assertThat((double)i, is(cl2.getCellValue()));
        }
    }

    @RegisterOp
	public static class MyPoint
	{
    	private int m_x;
		private int m_y;
		
		@RegisterOp(token="makeOriginMyPoint")
		public MyPoint() 
		{
			this(0, 0);
		}

		@RegisterOp
		public MyPoint(int x, int y) 
		{
			m_x = x;
			m_y = y;
		}
		
		public int getX()
		{
			return m_x;
		}
		
		public int getY()
		{
			return m_y;
		}
		
		@RegisterOp(async=true)
		public double distance(MyPoint other)
		{
			int dx = other.getX() - getX();
			int dy = other.getY() - getY();
			return Math.sqrt(dx*dx + dy*dy);
		}
		
		public String toString()
		{
			return String.format("(%d, %d)", getX(), getY());
		}
	}
}
