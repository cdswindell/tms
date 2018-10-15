package org.tms.io;

import java.io.IOException;

import org.junit.Test;
import org.tms.api.Access;
import org.tms.api.Column;
import org.tms.api.Row;
import org.tms.api.Table;
import org.tms.api.factories.TableFactory;
import org.tms.api.io.ESOptions;

public class ESWriterTest extends BaseIOTest 
{
    private static final String SAMPLE1 = "testJSONExport1.json";
    
    @Test
    public final void testElasticSearchExport1() throws IOException
    {
    	//String gold = readFileAsString(Paths.get(qualifiedFileName(SAMPLE1, "misc")));
        
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
    	
    	t.setCellValue(t.getRow(4), c1, null);
    	    	       	
    	//ByteArrayOutputStream baos = new ByteArrayOutputStream();
    	t.export("es.json", ESOptions.Default.withIdColumn(c1).withIgnoreEmptyCells(false).withExceptionOnEmptyIds(false));
       	
        // test byte streams are the same
    	//String output = baos.toString();
        //assertNotNull(output);

        //assertThat(gold.length(), is(output.length())); 
        //assertThat(gold, is(output));    	
    }
}
