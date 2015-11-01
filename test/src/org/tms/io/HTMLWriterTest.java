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
import org.tms.BaseTest;
import org.tms.api.Access;
import org.tms.api.Column;
import org.tms.api.Row;
import org.tms.api.Subset;
import org.tms.api.Table;
import org.tms.api.factories.TableFactory;
import org.tms.api.io.options.HTMLOptions;

public class HTMLWriterTest extends BaseTest
{
    private static final String SAMPLE1 = "sample1.csv";
    private static final String ExportTableGold = "testExportTable.html";
    private static final String ExportSubsetGold = "testExportSubset.html";
    private static final String ExportRowGold = "testExportRow.html";
    private static final String ExportColumnGold = "testExportColumn.html";

    @Test
    public final void testExportTable() throws IOException
    {
        /*
         * Note: If you change this test, be sure to update
         * the gold standard file ExportTableGold
         */
        Path path = Paths.get(qualifiedFileName(ExportTableGold));
        byte[] gold = Files.readAllBytes(path);  

        assertNotNull(gold);
        assertThat(gold.length > 0, is(true));

        Table t = TableFactory.importCSV(qualifiedFileName(SAMPLE1), true, true);
        assertNotNull(t);

        // create output stream
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        t.export(bos, HTMLOptions.Default
                .withIgnoreEmptyColumns()
                .withDefaultColumnWidthInInches(5)
                .withTitle("This is a very long title This is a very long title This is a very long title This is a very long title"));
        bos.close();

        // test byte streams are the same
        byte [] output =  bos.toByteArray();
        assertNotNull(output);

        assertThat(gold.length, is(output.length));
        int failures = 0;
        int firstFailure = 0;
        for (int i = 0; i < gold.length; i++) {
            if (gold[i] != output[i]) {
                failures++;
                if (firstFailure == 0)
                    firstFailure = i;
            }
        }

        // there should be no failures
        System.out.println("Export Table to HTML, Failures: " + failures);
        assertThat(failures, is(0));
    }

    @Test
    public final void testExportSubset() throws IOException
    {
        Path path = Paths.get(qualifiedFileName(ExportSubsetGold));
        byte[] gold = Files.readAllBytes(path);  

        assertNotNull(gold);
        assertThat(gold.length > 0, is(true));

        Table t = TableFactory.importCSV(qualifiedFileName(SAMPLE1), true, true);
        assertNotNull(t);

        Subset s = t.addSubset(Access.ByLabel, "CDS");
        s.add(t.getColumn(1), t.getColumn(3), t.getRow(1), t.getRow(3), t.getRow(4));

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        s.export(bos, HTMLOptions.Default
                .withIgnoreEmptyColumns()
                .withColumnNames(false)
                .withRowLabelColumnWidthInInches(1.1)
                .withDefaultColumnWidthInInches(2)
                .withTitle("This is a very long title This is a very long title This is a very long title This is a very long title"));
        bos.close();

        // test byte streams are the same
        byte [] pdf =  bos.toByteArray();
        assertNotNull(pdf);
        assertThat(pdf.length > 0, is(true));
    }

    @Test
    public final void testExportRow() throws IOException
    {
        Path path = Paths.get(qualifiedFileName(ExportRowGold));
        byte[] gold = Files.readAllBytes(path);  

        assertNotNull(gold);
        assertThat(gold.length > 0, is(true));

        Table t = TableFactory.importCSV(qualifiedFileName(SAMPLE1), true, true);
        assertNotNull(t);

        Row r = t.getRow(2);

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        r.export(bos, HTMLOptions.Default
                .withColumnNames(false)
                .withRowLabels(false)
                .withDefaultColumnWidthInInches(1)
                .withFontFamily("Courier")
                .withTitle("Row 2"));
        bos.close();

        // test byte streams are the same
        byte [] pdf =  bos.toByteArray();
        assertNotNull(pdf);
        assertThat(pdf.length > 0, is(true));
    }

    @Test
    public final void testExportColumn() throws IOException
    {
        Path path = Paths.get(qualifiedFileName(ExportColumnGold));
        byte[] gold = Files.readAllBytes(path);  

        assertNotNull(gold);
        assertThat(gold.length > 0, is(true));

        Table t = TableFactory.importCSV(qualifiedFileName(SAMPLE1), true, true);
        assertNotNull(t);

        Column c = t.getColumn(1);

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        c.export(bos, HTMLOptions.Default
                .withFontFamily("Comic Sans MS")
                .withRowLabelColumnWidthInInches(1.25)
                .withTitle("Column 1"));
        bos.close();

        // test byte streams are the same
        byte [] pdf =  bos.toByteArray();
        assertNotNull(pdf);
        assertThat(pdf.length > 0, is(true));
    }
}
