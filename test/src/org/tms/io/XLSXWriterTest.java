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
import org.tms.api.Cell;
import org.tms.api.Column;
import org.tms.api.Row;
import org.tms.api.Table;
import org.tms.api.derivables.ErrorCode;
import org.tms.api.factories.TableFactory;
import org.tms.api.io.options.XlsOptions;

public class XLSXWriterTest extends BaseTest
{
    private static final String SAMPLE1 = "sample1.csv";
    private static final String ExportTableGold = "testExportTable.xlsx";

    @Test
    public final void testExportTable() throws IOException
    {
        /*
         * Note: If you change this test, be sure to update
         * the gold standard file ExportTableGold
         */
        Path path = Paths.get(ExportTableGold);
        byte[] gold = Files.readAllBytes(path);  

        assertNotNull(gold);
        //assertThat(gold.length > 0, is(true));

        Table t = TableFactory.importCSV(qualifiedFileName(SAMPLE1), true, true);
        assertNotNull(t);
        t.setLabel("Test Table");
        
        Column dCol = t.addColumn();
        dCol.setDerivation("col 1 * 3");
        
        Column eCol = t.addColumn();        
        Row r1 = t.getRow(1);
        Cell cell = t.getCell(r1,  eCol);
        cell.setDerivation("col 1 / 0");
        
        Row r2 = t.getRow(2);
        cell = t.getCell(r2,  eCol);
        cell.setCellValue(Double.NaN);
        
        Row r3 = t.getRow(3);
        cell = t.getCell(r3,  eCol);
        cell.setCellValue(Double.POSITIVE_INFINITY);
        
        Row r4 = t.getRow(4);
        cell = t.getCell(r4,  eCol);
        cell.setCellValue(ErrorCode.ReferenceRequired);
        
        Row r5 = t.addRow(5);
        r5.setDerivation("sum(colRef(cidx))");
        
        // create output stream
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        t.export(ExportTableGold, XlsOptions.Default
                .withColumnNames(true)
                .withColumnWidthInInches(1.5)
                .withRowNameColumnWidthInInches(1)
                );
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

        // there will be failures, as new documents have date/time stamped into them
        System.out.println("Export Table to DocX, Failures: " + failures);
        assertThat(failures, is(24));
    }
}
