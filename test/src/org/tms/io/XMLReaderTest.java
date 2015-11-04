package org.tms.io;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.io.IOException;

import org.junit.Test;
import org.tms.BaseTest;
import org.tms.api.Column;
import org.tms.api.Row;
import org.tms.api.Table;
import org.tms.api.io.XMLOptions;

public class XMLReaderTest extends BaseTest
{
    private static final String SAMPLE1 = "sample1.csv";
    private static final String SAMPLE2 = "sample2.csv";
    private static final String SAMPLE3 = "sample3.csv";
    
    @Test
    public final void testCSVReaderConstructor()
    {
        XMLReader r = new XMLReader(qualifiedFileName(SAMPLE1), XMLOptions.Default); 
        assertNotNull(r);
        assertThat(r.getFileName(), is(SAMPLE1));
        assertThat(r.isRowNames(), is(true));
        assertThat(r.isColumnNames(), is(true));
    }

    @Test
    public final void testParse() 
    {
        XMLReader r = new XMLReader("foo.xml", XMLOptions.Default); 
        assertNotNull(r);
        
        try
        {
            Table t = r.parse();
            assertNotNull(t);
            
            assertThat(t.getNumRows(), is(4));
            assertThat(t.getNumColumns(), is(3));
            assertThat(t.getNumCells(), is(12));
            
            assertThat(t.getColumn(1).getLabel(), is("A"));
            assertThat(t.getColumn(2).getLabel(), is("B"));
            assertThat(t.getColumn(3).getLabel(), is("C"));
            
            Column c1 = t.getColumn(1);
            Column c2 = t.getColumn(2);
            Column c3 = t.getColumn(3);
            for (Row row : t.rows()) {
                assertThat(Number.class.isAssignableFrom(t.getCellValue(row, c1).getClass()), is(true));
                assertThat(row.getLabel(), is(t.getCellValue(row, c2) + " Row"));
                assertThat(Boolean.class.isAssignableFrom(t.getCellValue(row, c3).getClass()), is(true));
            }
        }
        catch (IOException e)
        {
            fail(e.getMessage());
        }
    }
    
}
