package org.tms.io;

import static org.junit.Assert.assertNotNull;

import java.io.IOException;

import org.junit.Test;
import org.tms.BaseTest;
import org.tms.api.Table;
import org.tms.api.factories.TableFactory;
import org.tms.io.options.PDFOptions;

public class PDFWriterTest extends BaseTest
{
    private static final String SAMPLE1 = "sample1.csv";

    @Test
    public final void testExport() throws IOException
    {
        Table t = TableFactory.importCSV(qualifiedFileName(SAMPLE1), true, true);
        assertNotNull(t);
        
        t.export("a.pdf", PDFOptions.Default
                .withPages(false)
                .withColumnWidthInInches(.45)
                .withTitle("This is a very long title This is a very long title This is a very long title This is a very long title"));
    }
}
