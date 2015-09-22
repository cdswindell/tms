package org.tms.io;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.io.IOException;

import org.junit.Test;
import org.tms.BaseTest;
import org.tms.api.Access;
import org.tms.api.Column;
import org.tms.api.Row;
import org.tms.api.Table;
import org.tms.api.factories.TableFactory;

public class CSVWriterTest extends BaseTest
{
    private static final String SAMPLE1 = "sample1.csv";
    private static final String SAMPLE2 = "sample2.csv";
    

    @Test
    public final void testExport() throws IOException
    {
        Table t = TableFactory.importCSV(qualifiedFileName(SAMPLE1), true, true);
        assertNotNull(t);
        
        File tmpFile = File.createTempFile("testExport", ".csv");
        tmpFile.deleteOnExit();
        
        // export the file
        t.export(tmpFile.getPath());
        
        // now reimport it
        Table t2 = TableFactory.importCSV(tmpFile.getPath(), true, true);
        assertNotNull(t2);        
        assertThat(t2.isValid(), is(true));
        assertThat(t2.getNumRows(), is(t.getNumRows()));
        assertThat(t2.getNumColumns(), is(t.getNumColumns()));
        assertThat(t2.getNumCells(), is(t.getNumCells()));
        
        for (Row r2 : t2.getRows()) {
            assertNotNull(r2);
            Row r1 = t.getRow(Access.ByIndex, r2.getIndex());
            assertNotNull(r1);
            assertThat(r2.getLabel(), is(r1.getLabel()));
            
            for (Column c2 : t2.getColumns()) {
                assertNotNull(c2);
                Column c1 = t.getColumn(Access.ByIndex, c2.getIndex());
                assertNotNull(c1);
                assertThat(c2.getLabel(), is(c1.getLabel()));
                
                assertThat(t2.getCellValue(r2, c2), is(t.getCellValue(r1, c1)));
            }
        }
    }

}
