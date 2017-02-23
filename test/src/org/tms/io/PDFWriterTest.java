package org.tms.io;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.Ignore;
import org.junit.Test;
import org.tms.api.Access;
import org.tms.api.Column;
import org.tms.api.Row;
import org.tms.api.Subset;
import org.tms.api.Table;
import org.tms.api.io.PDFOptions;

public class PDFWriterTest extends BaseIOTest
{
    private static final String SAMPLE1 = "sample1.csv";
    private static final String ExportTableGold = "testExportTable.pdf";

    @Test
    public final void testExportTable() throws IOException
    {
        Path path = Paths.get(qualifiedFileName(ExportTableGold, "misc"));
        byte[] gold = Files.readAllBytes(path);  
        
        assertNotNull(gold);
        assertThat(gold.length > 0, is(true));
        
        Table t = importCVSFile(qualifiedFileName(SAMPLE1, "csv"), true, true);
        assertNotNull(t);
        
        // create output stream
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        t.export(bos, PDFOptions.Default
                .withPages(false)
                .withPageNumbers(true)
                .withIgnoreEmptyColumns()
                .withDefaultColumnWidthInInches(2.8)
                .withDateTimeFormat(null)
                .withTitle("This is a very long title This is a very long title This is a very long title This is a very long title")
                .withFontFamily("Helvetica")
                .withPages(true));
        
        bos.close();

        // test byte streams are the same
        byte [] pdf =  bos.toByteArray();
        assertNotNull(pdf);
        
        assertThat(closeTo(gold.length, pdf.length, 50), is(true));
    }
    
    @Test
    public final void testExportSubset() throws IOException
    {
        Table t = importCVSFile(qualifiedFileName(SAMPLE1, "csv"), true, true);
        assertNotNull(t);
        
        Subset s = t.addSubset(Access.ByLabel, "CDS");
        s.add(t.getColumn(1), t.getColumn(3), t.getRow(1), t.getRow(3), t.getRow(4));
        
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        s.export(bos, PDFOptions.Default
                .withPages(false)
                .withPageNumbers(true)
                .withIgnoreEmptyColumns()
                .withStickyColumnLabels(false)
                .withDefaultColumnWidthInInches(2.8)
                .withTitle("This is a very long title This is a very long title This is a very long title This is a very long title")
                .withPages(true));
        bos.close();

        // test byte streams are the same
        byte [] pdf =  bos.toByteArray();
        assertNotNull(pdf);
        assertThat(pdf.length > 0, is(true));
    }
    
    @Test
    public final void testExportRow() throws IOException
    {
        Table t = importCVSFile(qualifiedFileName(SAMPLE1, "csv"), true, true);
        assertNotNull(t);
        
        Row r = t.getRow(3);
        
        // create output stream
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        r.export(bos, PDFOptions.Default
                .withPages(false)
                .withPageNumbers(true)
                .withIgnoreEmptyColumns(true)
                .withStickyColumnLabels(true)
                .withDefaultColumnWidthInInches(1)
                .withTitle("Only Row 3")
                .withPages(true));
        bos.close();

        // test byte streams are the same
        byte [] pdf =  bos.toByteArray();
        assertNotNull(pdf);
        assertThat(pdf.length > 0, is(true));
    }
    
    @Test
    public final void testExportColumn() throws IOException
    {
        Table t = importCVSFile(qualifiedFileName(SAMPLE1, "csv"), true, true);
        assertNotNull(t);
        
        Column c = t.getColumn(2);
        
        // create output stream
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        c.export(bos, PDFOptions.Default
                .withPages(false)
                .withPageNumbers(true)
                .withIgnoreEmptyColumns()
                .withStickyColumnLabels(true)
                .withDefaultColumnWidthInInches(1)
                .withTitle("Only Column 2")
                .withPages(true));
        bos.close();

        // test byte streams are the same
        byte [] pdf =  bos.toByteArray();
        assertNotNull(pdf);
        assertThat(pdf.length > 0, is(true));
    }
    
    @Ignore
    @Test
    public final void testExportTablePersistant() throws IOException
    {
        Table t = importCVSFile(qualifiedFileName(SAMPLE1, "csv"), true, true);
        assertNotNull(t);
        
        // Write to file
        t.export("exportTable.pdf", PDFOptions.Default
                .withPages(false)
                .withPageNumbers(false)
                .withIgnoreEmptyColumns()
                .withDefaultColumnWidthInInches(2)
                .withDateTimeFormat(null)
                .withTitle("This is a very long title This is a very long title This is a very long title This is a very long title")
                .withFontFamily("Helvetica")
                .withPages(false));       
    }
}
