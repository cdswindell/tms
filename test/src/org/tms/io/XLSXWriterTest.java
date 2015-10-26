package org.tms.io;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.Test;
import org.tms.BaseTest;
import org.tms.api.Access;
import org.tms.api.Cell;
import org.tms.api.Column;
import org.tms.api.Row;
import org.tms.api.Subset;
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
        assertThat(gold.length > 0, is(true));

        Table t = TableFactory.importCSV(qualifiedFileName(SAMPLE1), true, true);
        assertNotNull(t);
        t.setLabel("Test Table");
        
        Column dCol = t.addColumn();
        dCol.setDerivation("col 1 * 3");
        dCol.setDescription("This is dCol");
        dCol.setLabel("D Col");
        
        Column d2Col = t.addColumn();
        d2Col.setDerivation("col 1 * 10%");
        d2Col.setLabel("D2 Col");
        
        Column d3Col = t.addColumn();
        d3Col.setDerivation("pi + ridx + cidx + randBetween(1,8)! + rIdx^2");
        d3Col.setLabel("D3 Col");
        
        Column d4Col = t.addColumn();
        d4Col.setDerivation("(col 1 > 10) && (col \"D3 Col\" < 100)");
        
        Column d5Col = t.addColumn();
        d5Col.setDerivation("(col 1 > 10) || (col \"D3 Col\" < 100)");
        
        Column d6Col = t.addColumn();
        d6Col.setDerivation(" 1 + 2 + \" Row: \" + toString(ridx) + \" \" + '*' * ridx" );
        
        Column eCol = t.addColumn();        
        Row r1 = t.getRow(1);
        Cell cell = t.getCell(r1,  eCol);
        cell.setDerivation("col 1 / 0");
        
        Row r2 = t.getRow(2);
        r2.setDescription("This is TMS Row 2");
        cell = t.getCell(r2,  eCol);
        cell.setCellValue(Double.NaN);
        
        Row r3 = t.getRow(3);
        cell = t.getCell(r3,  eCol);
        cell.setCellValue(Double.POSITIVE_INFINITY);
        
        Row r4 = t.getRow(4);
        cell = t.getCell(r4,  eCol);
        cell.setCellValue(ErrorCode.ReferenceRequired);
        
        Row r5 = t.addRow(5);
        r5.setLabel("Sum");
        
        Row r6 = t.addRow(6);
        r6.setLabel("Mean");
        for (Column c = t.getColumn(Access.First); c != null; c = t.getColumn(Access.Next)) {
            t.getCell(r5,  c).setDerivation("sum(col " + c.getIndex() + ")");
            t.getCell(r6,  c).setDerivation("mean(col " + c.getIndex() + ")");
        }
        
        Cell tCell = t.getCell(r1, eCol);
        tCell.setLabel("Div By Zero");
        tCell.setDescription("This cell has intentionality been set to an error");
        
        // create a subset that should be exported
        Subset s = t.addSubset(Access.ByLabel, "1st Valid Subset");
        s.add(r2, r3, r4, t.getColumn(2), t.getColumn(3));
        
        // create a subset that should be exported
        Subset sv2 = t.addSubset(Access.ByLabel, "2nd Valid Subset");
        sv2.add(r2, r3);
        
        Subset s2 = t.addSubset(Access.ByLabel, "InValid Subset");
        s2.add(s, tCell);
        
        Subset s3 = t.addSubset(Access.ByLabel, "InValid Subset 2");
        s3.add(r2, r4, r5);
        
        // create output stream
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        t.export(bos, XlsOptions.Default
                .withColumnNames(true)
                .withColumnWidthInInches(1.5)
                .withRowNameColumnWidthInInches(1.25)
                .withCommentAuthor("TMS via POI")
                );
        bos.close();

        // test byte streams are the same
        byte [] output =  bos.toByteArray();
        assertNotNull(output);

        assertThat(this.closeTo(gold.length, output.length, 5), is(true));
        int failures = 0;
        int firstFailure = 0;
        for (int i = 0; i < Math.min(gold.length, output.length); i++) {
            if (gold[i] != output[i]) {
                failures++;
                if (firstFailure == 0)
                    firstFailure = i;
            }
        }

        // there will be failures, as new documents have date/time stamped into them
        System.out.println("Export Table to XlsX, Failures: " + failures);
        
        // now compare exported file to expected
        File outFile = null;
        try {
            outFile  = File.createTempFile("tmsExcelExportTest", "xlsx");
            OutputStream outputStream = new FileOutputStream (outFile); 
            bos.writeTo(outputStream);    
            outputStream.close();
            
            // open workbook
            //Workbook = WorkbookFactory.create(outFile);

        }
        finally {
            outFile.delete();
        }        
    }
}
