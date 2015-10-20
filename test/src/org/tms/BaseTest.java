package org.tms;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

import java.io.File;

import org.tms.api.Access;
import org.tms.api.Cell;
import org.tms.api.Column;
import org.tms.api.Row;
import org.tms.api.Table;


public class BaseTest
{
    public boolean closeTo(double x, double y, double withIn) 
    {
        return withIn > Math.abs(x - y);
    }

    public boolean closeTo(Object x, double y, double withIn) 
    {
        if (x == null)
            return false;
        
        return withIn > Math.abs((Double)x - y);
    }
    
    protected final String qualifiedFileName(String fn)
    {
        StringBuffer sb = new StringBuffer();
        sb.append(System.getProperty("user.dir"));
        sb.append(File.separator);
        sb.append("src");
        sb.append(File.separator);
        
        String packagePath = this.getClass().getPackage().getName();
        packagePath = packagePath.replaceAll("\\.", File.separator);
        
        sb.append(packagePath);
        sb.append(File.separator);
        sb.append(fn);
        
        return sb.toString();
    }

    protected Cell vetCellValue(Table t, Row r, Column c, Object cv)
    {
        Cell cell = t.getCell(r, c);
        assertNotNull(cell);
        
        if (cv == null)
            assertThat(cell.isNull(), is(true));
        else if (Number.class.isAssignableFrom(cv.getClass()))
            assertThat(closeTo(cell.getCellValue(), ((Number)cv).doubleValue(), 0.0000001), is(true));
        else
            assertThat(cell.getCellValue(), is(cv));
        
        return cell;
    }    

    protected Cell vetCellValue(Table t, String label, Object cv)
    {
        Cell cell = t.getCell(Access.ByLabel, label);
        assertNotNull("Cell \"" + label + "\" not found", cell);
        
        if (cv == null)
            assertThat("cell is not null", cell.isNull(), is(true));
        else if (Number.class.isAssignableFrom(cv.getClass()))
            assertThat(String.format("cell value (%f) not within tolerance of expected (%f)", (Double)cell.getCellValue(), ((Number)cv).doubleValue()),
                    closeTo(cell.getCellValue(), ((Number)cv).doubleValue(), 0.0000001), is(true));
        else
            assertThat(cell.getCellValue(), is(cv));
        
        return cell;
    }    
}
