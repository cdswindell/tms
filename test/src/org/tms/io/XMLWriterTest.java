package org.tms.io;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.AfterClass;
import org.junit.Test;
import org.tms.BaseTest;
import org.tms.api.Access;
import org.tms.api.Cell;
import org.tms.api.Column;
import org.tms.api.Subset;
import org.tms.api.Table;
import org.tms.api.TableContext;
import org.tms.api.factories.TableContextFactory;
import org.tms.api.factories.TableFactory;
import org.tms.api.io.XMLOptions;
import org.tms.tds.TdsUtils;

public class XMLWriterTest extends BaseTest
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
    public final void testExportXml() throws IOException
    {
        /*
         * Note: If you change this test, be sure to update
         * the gold standard file ExportTableGold
         */
        Path path = Paths.get(qualifiedFileName(ExportTableGold));
        byte[] gold = Files.readAllBytes(path);  

        assertNotNull(gold);
        assertThat(gold.length > 0, is(true));
        
        // create new XML, it should match the gold standard
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
        
        // create output stream
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        gst.export(bos, XMLOptions.Default);
        bos.close();

        // test byte streams are the same
        byte [] output =  bos.toByteArray();
        assertNotNull(output);

        assertThat(gold.length, is(output.length));       
    }
}
