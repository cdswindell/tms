package org.tms.io;

import static org.junit.Assert.assertNotNull;

import java.io.IOException;

import org.junit.AfterClass;
import org.junit.Test;
import org.tms.BaseTest;
import org.tms.api.Column;
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
    
    @Test
    public final void testExportXml() throws IOException
    {
        Table t = TableFactory.importCSV(qualifiedFileName(SAMPLE1), true, true);
        assertNotNull(t);
        t.setLabel("Test XML & Export Table");
        t.tag("red", "green");
        
        Column c = t.addColumn();
        c = t.addColumn();
        c.setDerivation("col 1 * col 1");
        c.tag("derived", "calculated", "no-user-data");
        t.export("foo.xml", XMLOptions.Default.withIgnoreEmptyColumns(true));
        
    }
}
