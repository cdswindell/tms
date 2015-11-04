package org.tms.io;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.io.IOException;

import org.junit.AfterClass;
import org.junit.Test;
import org.tms.BaseTest;
import org.tms.api.Access;
import org.tms.api.Cell;
import org.tms.api.Column;
import org.tms.api.Row;
import org.tms.api.Subset;
import org.tms.api.Table;
import org.tms.api.TableContext;
import org.tms.api.factories.TableContextFactory;
import org.tms.api.factories.TableFactory;
import org.tms.api.io.XMLOptions;
import org.tms.tds.TdsUtils;

public class XMLReaderTest extends BaseTest
{
    @AfterClass
    static public void cleanup()
    {
        TableContext tc = TableContextFactory.fetchDefaultTableContext();
        TdsUtils.clearGlobalTagCache(tc);
    }
    
    private static final String SAMPLE1 = "sample1.csv";
    private static final String ExportTableGold = "testExportTable.xml";
    
    @Test
    public final void testCSVReaderConstructor()
    {
        XMLReader r = new XMLReader(qualifiedFileName(ExportTableGold), XMLOptions.Default); 
        assertNotNull(r);
        assertThat(r.getFileName(), is(ExportTableGold));
        assertThat(r.isRowNames(), is(true));
        assertThat(r.isColumnNames(), is(true));
    }

    @Test
    public final void testParse() 
    {
        // create the reference table
        Table gst = TableFactory.importCSV(qualifiedFileName(SAMPLE1), true, true);
        assertNotNull(gst);
        gst.setLabel("Test XML & Export Table");
        gst.tag("red", "green");
        
        Column gsc3 = gst.getColumn(3);
        Cell r1c3 = gst.getCell(gst.getRow(1), gsc3);
        r1c3.clear();
        r1c3.setLabel("foo");
        r1c3.setUnits("mph");
        r1c3.setDescription("Cell Description");
        
        Subset s1 = gst.addSubset(Access.ByLabel, "Excluded Cols");
        Subset s2 = gst.addSubset(Access.ByLabel, "Some Cols");
        s2.tag("red", "subset");
        
        Column gsc = gst.addColumn();
        s1.add(gsc);
        s2.add(gsc, gsc3);
        
        gsc = gst.addColumn();
        s2.add(gsc);

        gsc.setDerivation("col 1 * col 1");
        gsc.tag("derived", "calculated", "no-user-data");
        
        // now read the xml
        XMLReader r = new XMLReader(qualifiedFileName(ExportTableGold), XMLOptions.Default); 
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
            }
        }
        catch (IOException e)
        {
            fail(e.getMessage());
        }
    }
    
}
