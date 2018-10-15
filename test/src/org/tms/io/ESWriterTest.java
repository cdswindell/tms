package org.tms.io;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Paths;

import org.junit.Test;
import org.tms.api.Access;
import org.tms.api.Column;
import org.tms.api.Row;
import org.tms.api.Table;
import org.tms.api.exceptions.IllegalTableStateException;
import org.tms.api.factories.TableFactory;
import org.tms.api.io.ESOptions;

public class ESWriterTest extends BaseIOTest 
{
    private static final String SAMPLE1 = "testESExport1.json";
    private static final String SAMPLE2 = "testESExport2.json";
    private static final String SAMPLE3 = "testESExport3.json";
    private static final String SAMPLE5 = "testESExport5.json";
    
    @Test
    public final void testElasticSearchExport1() throws IOException
    {        
    	Table t = TableFactory.createTable();
    	
    	t.setLabel("ESExport1");
    	t.setDescription("Elastic Search Export Test 1");
    	
    	// add some rows
    	Row r10 = t.addRow(10);
    	r10.setLabel("Row 10");
    	
    	// and columns
    	Column c1 = t.addColumn(Access.ByLabel, "Col 1");
    	c1.setLabel("A");
    	
    	Column c2 = t.addColumn();
    	c2.setLabel("B");
    	c2.setDerivation("col 1 * col 1");
    	
    	// set some data
    	for (int i = 1; i <= 10; i++) {
    		t.setCellValue(t.getRow(i),  c1, i);
    	}
    	
    	ByteArrayOutputStream baos = new ByteArrayOutputStream();
    	t.export(baos, ESOptions.Default);
       	
        // test byte streams are the same
    	String output = baos.toString();
        assertNotNull(output);

    	String gold = readFileAsString(Paths.get(qualifiedFileName(SAMPLE1, "misc")));
        assertThat(gold.length(), is(output.length())); 
        assertThat(gold, is(output));  
        
        // Test 2: Ordinal IDs
    	baos = new ByteArrayOutputStream();
    	t.export(baos, ESOptions.Default.withIdOrdinal().withIndex("myIndex").withType("_doc"));
       	
        // test byte streams are the same
    	output = baos.toString();
        assertNotNull(output);

    	gold = readFileAsString(Paths.get(qualifiedFileName(SAMPLE2, "misc")));
        assertThat(gold.length(), is(output.length())); 
        assertThat(gold, is(output));        
        
        // Test 3: ID Column
    	baos = new ByteArrayOutputStream();
    	t.export(baos, ESOptions.Default.withIdColumn(c1));
       	
        // test byte streams are the same
    	output = baos.toString();
        assertNotNull(output);

    	gold = readFileAsString(Paths.get(qualifiedFileName(SAMPLE3, "misc")));
        assertThat(gold.length(), is(output.length())); 
        assertThat(gold, is(output));        
        
        // Test 4: Null ID
		t.setCellValue(t.getRow(3),  c1, null);
		
    	baos = new ByteArrayOutputStream();
    	try {
    		t.export(baos, ESOptions.Default.withIdColumn(c1).withIgnoreEmptyCells(false));
    		fail("Table with null IDs processed");
    	}
    	catch (IllegalTableStateException e) {}
    	
        // Test 5: Null ID, skip record
    	baos = new ByteArrayOutputStream();
    	t.export(baos, ESOptions.Default.withIdColumn(c1).withIgnoreEmptyCells(false).withOmitRecordsWithEmptyIds());
       	
        // test byte streams are the same
    	output = baos.toString();
        assertNotNull(output);

    	gold = readFileAsString(Paths.get(qualifiedFileName(SAMPLE5, "misc")));
        assertThat(gold.length(), is(output.length())); 
        assertThat(gold, is(output));        
        
        // Test 6: Null ID, skip record, Duplicate ID Exception
		t.setCellValue(t.getRow(7),  c1, 2);
		
    	baos = new ByteArrayOutputStream();
    	try {
    		t.export(baos, ESOptions.Default.withIdColumn(c1).withIgnoreEmptyCells(false).withOmitRecordsWithEmptyIds());
    		fail("Table with duplicate IDs processed");
    	}
    	catch (IllegalTableStateException e) {}
    }
}
