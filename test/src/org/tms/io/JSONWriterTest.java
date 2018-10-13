package org.tms.io;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Paths;

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
    private static final String SAMPLE1 = "testJSONExport1.json";
    
    @Test
    public final void testJSONExport1() throws IOException
    {
    	String gold = readFileAsString(Paths.get(qualifiedFileName(SAMPLE1, "misc")));
        
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
    	c2.setUnits("foobars");
    	
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
    	       	
    	ByteArrayOutputStream baos = new ByteArrayOutputStream();
    	t.export(baos, JSONOptions.Default.withVerboseState());
       	
        // test byte streams are the same
    	String output = baos.toString();
        assertNotNull(output);

        assertThat(gold.length(), is(output.length())); 
        assertThat(gold, is(output));    	
    }
}
