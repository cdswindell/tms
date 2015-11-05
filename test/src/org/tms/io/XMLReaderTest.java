package org.tms.io;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.io.IOException;

import org.junit.AfterClass;
import org.junit.Test;
import org.tms.api.Column;
import org.tms.api.Row;
import org.tms.api.Table;
import org.tms.api.TableContext;
import org.tms.api.factories.TableContextFactory;
import org.tms.api.io.XMLOptions;
import org.tms.tds.TdsUtils;

public class XMLReaderTest extends XMLTest
{
    @AfterClass
    static public void cleanup()
    {
        TableContext tc = TableContextFactory.fetchDefaultTableContext();
        TdsUtils.clearGlobalTagCache(tc);
    }
    
    private static final String ExportTableGold = "testExportTable.xml";
    
    @Test
    public final void testCSVReaderConstructor()
    {
        XMLReader r = new XMLReader(qualifiedFileName(ExportTableGold, "xml"), XMLOptions.Default); 
        assertNotNull(r);
        assertThat(r.getFileName(), is(ExportTableGold));
        assertThat(r.isRowNames(), is(true));
        assertThat(r.isColumnNames(), is(true));
    }

    @Test
    public final void testParse() 
    {
        // create the reference table
        Table gst = getBasicTable();
        
        // now read the xml
        XMLReader r = new XMLReader(qualifiedFileName(ExportTableGold, "xml"), XMLOptions.Default); 
        assertNotNull(r);
        
        try
        {
            Table t = r.parse();
            assertNotNull(t);
            
            assertThat(t.getNumRows(), is(gst.getNumRows()));
            assertThat(t.getNumColumns(), is(gst.getNumColumns()));
            assertThat(t.getNumCells(), is(gst.getNumCells()));
            
            assertThat(t.getColumn(1).getLabel(), is("A"));
            assertThat(t.getColumn(2).getLabel(), is("B"));
            assertThat(t.getColumn(3).getLabel(), is("C"));
            
            Column c1 = t.getColumn(1);
            Column c2 = t.getColumn(2);
            Column c3 = t.getColumn(3);
            for (Row row : t.rows()) {
                assertThat(Number.class.isAssignableFrom(t.getCellValue(row, c1).getClass()), is(true));
                assertThat(row.getLabel(), is(t.getCellValue(row, c2) + " Row"));
                
                if (row.getIndex() != 1)
                    assertThat(Boolean.class.isAssignableFrom(t.getCellValue(row, c3).getClass()), is(true));
                else {
                    assertThat(t.getCell(row,  c3).getLabel(), is("foo"));
                    assertThat(t.getCell(row,  c3).getUnits(), is("mph"));
                    assertThat(t.getCell(row,  c3).getDescription(), is("Cell Description"));
                }                    
            }
        }
        catch (IOException e)
        {
            fail(e.getMessage());
        }
    }    
}
