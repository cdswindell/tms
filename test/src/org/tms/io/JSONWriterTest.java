package org.tms.io;

import java.io.File;
import java.io.IOException;

import org.junit.Test;
import org.tms.api.Access;
import org.tms.api.Cell;
import org.tms.api.Column;
import org.tms.api.Row;
import org.tms.api.Subset;
import org.tms.api.Table;
import org.tms.api.factories.TableFactory;
import org.tms.api.io.JSONOptions;

public class JSONWriterTest extends BaseIOTest 
{
    @Test
    public final void testJSONExport1() throws IOException
    {
    	Table t = TableFactory.createTable();
    	
    	t.setLabel("JSONExport1");
    	t.setDescription("JSON Export Test 1");
    	
    	// add some rows
    	Row r10 = t.addRow(10);
    	r10.setLabel("Row 10");
    	
    	// and columns
    	Column c1 = t.addColumn(Access.ByLabel, "Col 1");
    	Column c2 = t.addColumn();
    	c2.setDerivation("col 1 * col 1");
    	
    	// set some data
    	for (int i = 5; i <= 10; i++) {
    		t.setCellValue(t.getRow(i),  c1, i);
    	}
    	
    	// cell derivation
    	Cell cell = t.getCell(t.getRow(3), c1);
    	cell.setDerivation("pi");
    	cell.setReadOnly(true);
    	
    	// and create a subset
    	Subset s = t.addSubset();
    	s.add(c2);
    	s.tag("subset", "column");
    	
        File tmpFile = File.createTempFile("testJSONExport1", ".json");
    	//t.export(tmpFile.getPath());
    	t.export("foo1.json", JSONOptions.Default.withVerboseState());
    	
    	tmpFile.deleteOnExit();
    }
}
