package org.tms.api.utils.googleapis;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.junit.Test;
import org.tms.api.Access;
import org.tms.api.Cell;
import org.tms.api.Column;
import org.tms.api.Table;
import org.tms.api.factories.TableFactory;

public class GeocodingOpTest 
{
	private static String BOSTON_ADDR = "Calle la Pampilla Nº138 Mz I-3 Lt 20 Zona industrial de Ventanilla-Callao, Boston, MA 02116, United States";
	private static String ACTON_ADDR = "10 John Swift Rd, Acton, MA 01720, USA";
	private static String MOLTONBOROUGH_ADDR = "182 Castle Shore Rd, Moultonborough, NH 03254, USA";
	
	private static JSONObject BOSTON = null;	
	private static JSONObject ACTON = null;
	private static JSONObject MOULTONBOROUGH = null;
	static {
		try {
			BOSTON = (JSONObject)(new JSONParser()).parse("{\"lng\":-71.0765188,\"lat\":42.353068}");
			ACTON = (JSONObject)(new JSONParser()).parse("{\"lng\":-71.4418101,\"lat\":42.4836453}");
			MOULTONBOROUGH = (JSONObject)(new JSONParser()).parse("{\"lng\":-71.36693029999999,\"lat\":43.7198979}");
		} catch (ParseException e) 
		{
			fail(e.getMessage());
		}
	}
	
	@Test
	public void testToLatLongOp() throws InterruptedException 
	{	
    	Table t = TableFactory.createTable();
    	
    	t.setLabel("Geocoding Test");
    	
    	// register geo operation
        t.getTableContext().registerOperator(new ToLatLongOp());
    	
    	// and columns
    	Column c1 = t.addColumn(Access.ByLabel, "Col 1");
    	c1.setLabel("Zip Code");
    	
    	Column c2 = t.addColumn();
    	c2.setLabel("Lat/Long");
    	c2.setDerivation("toLatLong(col 1)");
    	
    	// add some data
    	t.setCellValue(t.addRow(1), c1, "02116");
    	t.setCellValue(t.addRow(2), c1, "01720");
    	t.setCellValue(t.addRow(3), c1, "03254");
    	
    	// wait for pendings
    	while (t.isPendings()) {
            Thread.sleep(250);
    	}
    	
    	// check results   	
    	assertThat(3, is(t.getNumRows()));
    	
    	Cell cell = t.getCell(t.getRow(1), c2);
    	assertNotNull(cell); 	
    	Object value = cell.getCellValue();
    	assertNotNull(value);
    	assertThat(true, is(value instanceof JSONObject));
    	assertThat(BOSTON, is(value));   	   	
    	
    	cell = t.getCell(t.getRow(2), c2);
    	assertNotNull(cell); 	
    	value = cell.getCellValue();
    	assertNotNull(value);
    	assertThat(true, is(value instanceof JSONObject));
    	assertThat(ACTON, is(value));   	   	
    	
    	cell = t.getCell(t.getRow(3), c2);
    	assertNotNull(cell); 	
    	value = cell.getCellValue();
    	assertNotNull(value);
    	assertThat(true, is(value instanceof JSONObject));
    	assertThat(MOULTONBOROUGH, is(value));   	   	
	}
	
	@Test
	public void testFromLatLongOp() throws InterruptedException 
	{	
    	Table t = TableFactory.createTable();
    	
    	t.setLabel("Geocoding Test");
    	
    	// register geo operation
        t.getTableContext().registerOperator(new FromLatLongOp());
    	
    	// and columns
    	Column c1 = t.addColumn(Access.ByLabel, "Col 1");
    	c1.setLabel("Lat");
    	
    	Column c2 = t.addColumn();
    	c2.setLabel("Long");
    	
    	Column c3 = t.addColumn();
    	c3.setLabel("Address");
    	c3.setDerivation("fromLatLong(col 1, col 2)");
    	
    	// add some data
    	t.setCellValue(t.addRow(1), c1, BOSTON.get("lat"));
    	t.setCellValue(t.getRow(1), c2, BOSTON.get("lng"));    
    	
    	t.setCellValue(t.addRow(2), c1, ACTON.get("lat"));
    	t.setCellValue(t.getRow(2), c2, ACTON.get("lng"));    
    	
    	t.setCellValue(t.addRow(3), c1, MOULTONBOROUGH.get("lat"));
    	t.setCellValue(t.getRow(3), c2, MOULTONBOROUGH.get("lng"));    
    	
    	// wait for pendings
    	while (t.isPendings()) {
            Thread.sleep(250);
    	}
    	
    	// check results   	
    	assertThat(3, is(t.getNumRows()));
    	
    	Cell cell = t.getCell(t.getRow(1), c3);
    	assertNotNull(cell); 	
    	Object value = cell.getCellValue();
    	assertNotNull(value);
    	assertThat(true, is(value instanceof String));
    	assertThat(BOSTON_ADDR, is(value));   	   	
    	
    	cell = t.getCell(t.getRow(2), c3);
    	assertNotNull(cell); 	
    	value = cell.getCellValue();
    	assertNotNull(value);
    	assertThat(true, is(value instanceof String));
    	assertThat(ACTON_ADDR, is(value));   	   	
    	
    	cell = t.getCell(t.getRow(3), c3);
    	assertNotNull(cell); 	
    	value = cell.getCellValue();
    	assertNotNull(value);
    	assertThat(true, is(value instanceof String));
    	assertThat(MOLTONBOROUGH_ADDR, is(value));   	   	
	}

}
