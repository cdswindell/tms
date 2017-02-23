package org.tms.io;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.Test;
import org.tms.api.Table;
import org.tms.api.io.DOCOptions;

public class DOCXWriterTest extends BaseIOTest
{
    private static final String SAMPLE1 = "sample1.csv";
    private static final String ExportTableGold = "testExportTable.docx";

    @Test
    public final void testExportTable() throws IOException
    {
        /*
         * Note: If you change this test, be sure to update
         * the gold standard file ExportTableGold
         */
        Path path = Paths.get(qualifiedFileName(ExportTableGold, "misc"));
        byte[] gold = Files.readAllBytes(path);  

        assertNotNull(gold);
        assertThat(gold.length > 0, is(true));

        Table t = importCVSFile(qualifiedFileName(SAMPLE1, "csv"), true, true);
        assertNotNull(t);
        
        // create output stream
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        t.export(bos, DOCOptions.Default
                .withPages(true)
                .withPageNumbers(true)
                .withDateTimeFormat(null)
                .withIgnoreEmptyColumns()
                .withStickyColumnLabels(false)
                .withDefaultColumnWidthInInches(2.8)
                .withTitle("This is a very long title This is a very long title This is a very long title This is a very long title"));
        bos.close();

        // test byte streams are the same
        byte [] output =  bos.toByteArray();
        assertNotNull(output);

        assertThat(gold.length, is(output.length));
    }
}
