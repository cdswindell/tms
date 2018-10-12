package org.tms.io;

import java.io.File;
import java.io.IOException;

import org.junit.Test;
import org.tms.api.Access;
import org.tms.api.Column;
import org.tms.api.Row;
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
    	
    	// set data
    	t.setCellValue(r10,  c1, 25);
    	
        File tmpFile = File.createTempFile("testJSONExport1", ".json");
    	//t.export(tmpFile.getPath());
    	t.export("foo1.json", JSONOptions.Default.withVerboseState());
    	
    	tmpFile.deleteOnExit();
    }
}
