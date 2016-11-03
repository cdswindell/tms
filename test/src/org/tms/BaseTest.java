package org.tms;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

import java.io.ByteArrayOutputStream;
import java.io.File;

import org.json.simple.JSONObject;
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
        return qualifiedFileName(fn, null);
    }
    
    protected final String qualifiedFileName(String fn, String subDir)
    {
        StringBuffer sb = new StringBuffer();
        sb.append(System.getProperty("user.dir"));
        sb.append(File.separator);
        sb.append("src");
        sb.append(File.separator);
        
        String packagePath = this.getClass().getPackage().getName();
        int perIdx = -1;
        while ((perIdx = packagePath.indexOf('.')) > -1)
        	packagePath = packagePath.substring(0, perIdx) + File.separator + packagePath.substring(perIdx + 1);
        
        sb.append(packagePath);
        sb.append(File.separator);
        
        if (subDir != null) 
            sb.append(subDir.trim()).append(File.separator);
        
        sb.append(fn);
        
        return sb.toString();
    }

    protected Cell vetCellValue(Table t, int rIdx, int cIdx, Object cv)
    {
        return vetCellValue(t, t.getRow(rIdx), t.getColumn(cIdx), cv);
    }
    
    protected Cell vetCellValue(Table t, Row r, Column c, Object cv)
    {
        Cell cell = t.getCell(r, c);
        assertNotNull(cell);
        
        if (cv == null)
            assertThat(cell.isNull(), is(true));
        else if (Number.class.isAssignableFrom(cv.getClass())) 
            assertThat(String.format("cell value (%f) not within tolerance of expected (%f)", (Double)cell.getCellValue(), ((Number)cv).doubleValue()),
                    closeTo(cell.getCellValue(), ((Number)cv).doubleValue(), 0.0000001), is(true));
        else
            assertThat(cell.getCellValue(), is(cv));
        
        return cell;
    }    

    protected Cell vetCellValue(Table t, String label, Object cv)
    {
        return vetCellValue(t, label, cv, null);
    }
    
    protected Cell vetCellValue(Table t, String label, Object cv, String deriv)
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
        
        if (deriv != null) {
            assertThat(cell.isDerived(), is(true));
            assertThat(String.format("Expected: %s; observed: %s", deriv, cell.getDerivation().getExpression()),
                    cell.getDerivation().getExpression(), is(deriv));
        }
        return cell;
    }    
    
    protected byte[] toLinuxByteArray(ByteArrayOutputStream bos) 
    {
    	String lineSep = System.lineSeparator();
		if ("\r\n".equals(lineSep)) {
			String winStr = bos.toString();
			int idx = winStr.indexOf(lineSep);
			while(idx > -1) {
				winStr = winStr.substring(0, idx) + "\n" + winStr.substring(idx + 2);
				idx = winStr.indexOf(lineSep);
			}
			
			return winStr.getBytes();
		}
		else
			return bos.toByteArray();
	}
    
    protected byte[] toLinuxByteArray(byte[] bytes) 
    {
    	String lineSep = System.lineSeparator();
		if ("\r\n".equals(lineSep)) {
			String winStr = new String(bytes);
			int idx = winStr.indexOf(lineSep);
			while(idx > -1) {
				winStr = winStr.substring(0, idx) + "\n" + winStr.substring(idx + 2);
				idx = winStr.indexOf(lineSep);
			}
			
			return winStr.getBytes();
		}
		else
			return bytes;
	}
    
	@SuppressWarnings("unchecked")
	protected JSONObject buildSimpleJSONObject()
	{
		JSONObject json = new JSONObject();
        json.put("Balance", 1000.21);
        json.put("Num", 100);
        json.put("Nick Name", null);
        json.put("Name", "Sam Sneed");
        json.put("1", JSONObject.escape("Tricky Text: \\, /, \r, \t"));
        json.put("VIP", true);
        
        return json;
	}	
}
