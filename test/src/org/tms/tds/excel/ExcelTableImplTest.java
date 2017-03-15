package org.tms.tds.excel;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.zip.Adler32;

import org.junit.Test;
import org.tms.BaseTest;
import org.tms.api.Access;
import org.tms.api.Cell;
import org.tms.api.Column;
import org.tms.api.Row;
import org.tms.api.Table;
import org.tms.api.TableContext;
import org.tms.api.factories.TableContextFactory;
import org.tms.api.io.XLSOptions;
import org.tms.api.io.XMLOptions;

public class ExcelTableImplTest extends BaseTest
{
    private static final String SAMPLE1 = "sample1.xlsx";
    private static final String ExportSample1Gold = "sample1.xml";

	@Test
	public void testSimpleSheet() throws IOException 
	{
		File xlsFile = new File(qualifiedFileName(SAMPLE1, "xls"));
		
		ExcelTableImpl et = ExcelTableImpl.createTable(xlsFile, XLSOptions.Default);
		assertNotNull(et);		
		assertThat(et.getNumRows(), is(4));
		
		Row r1 = et.getRow(1);
		assertNotNull(r1);
		assertThat(r1.getLabel(), is("Blue Row"));
		
		Row r2 = et.getRow(2);
		assertNotNull(r2);
		assertThat(r2.getLabel(), is("Green Row"));
		
		Row r3 = et.getRow(3);
		assertNotNull(r3);
		assertThat(r3.getLabel(), is("Yellow Row"));
		
		Row r4 = et.getRow(4);
		assertNotNull(r4);
		assertThat(r4.getLabel(), is("Cyan Row"));
		
		Column c1 = et.getColumn(1);
		assertNotNull(c1);
		assertThat(c1.getLabel(), is("A"));
		
		Column c2 = et.getColumn(2);
		assertNotNull(c2);
		assertThat(c2.getLabel(), is("B"));
		
		Column c3 = et.getColumn(3);
		assertNotNull(c3);
		assertThat(c3.getLabel(), is("C"));
		
		Row nr = et.addRow();
		assertNotNull(nr);
		
		Cell cell = et.getCell(nr,  c1);
		assertNotNull(cell);
		cell.setDerivation("mean(col 1)");
		
		// recalculate table, it should succeed
		et.recalculate();
		
		Object cv = cell.getCellValue();
		assertNotNull(cv);
		assertThat(cv, is(3254.4125));
		
		// check for derivations
		cell = et.getCell(r1,  c3);
		assertNotNull(cell);
		assertThat(cell.isDerived(), is(true));
		assertThat(cell.getCellValue(), is(true));
		
		cell = et.getCell(r2,  c3);
		assertNotNull(cell);
		assertThat(cell.isDerived(), is(false));
		assertThat(cell.getCellValue(), is(false));
		
		cell = et.getCell(r3,  c3);
		assertNotNull(cell);
		assertThat(cell.isDerived(), is(true));	
		assertThat(cell.getCellValue(), is(false));
		
		// recalculate table, it should succeed
		et.recalculate();

		cell = et.getCell(r3,  c3);
		assertNotNull(cell);
		assertThat(cell.isDerived(), is(true));	
		assertThat(cell.getCellValue(), is(false));	
	}
	
	@Test
	public void testExcelExport() throws IOException 
	{
        Path path = Paths.get(qualifiedFileName(ExportSample1Gold, "xls"));
        byte[] gold = toLinuxByteArray(Files.readAllBytes(path)); 

        assertNotNull(gold);
        assertThat(gold.length > 0, is(true));
		File xlsFile = new File(qualifiedFileName(SAMPLE1, "xls"));
		
		ExcelTableImpl et = ExcelTableImpl.createTable(xlsFile, XLSOptions.Default);
		assertNotNull(et);		
		assertThat(et.getNumRows(), is(4));
		
		Row nr = et.addRow();
		assertNotNull(nr);
		
		Cell cell = et.getCell(nr,  et.getColumn(1));
		assertNotNull(cell);
		cell.setDerivation("mean(col 1)");
		
	    ByteArrayOutputStream bos = new ByteArrayOutputStream();
		et.export(bos, XMLOptions.Default);
		bos.close();
		
        // test byte streams are the same
        byte [] output = toLinuxByteArray(bos);
        assertNotNull(output);

        assertThat(gold.length, is(output.length));      
        
        Adler32 goldCRC = new Adler32();
        goldCRC.update(gold);
        long goldCRCVal = goldCRC.getValue();
        
        Adler32 testCRC = new Adler32();
        testCRC.update(output);
        long testCRCVal = testCRC.getValue();
        
        assertThat(testCRCVal, is(goldCRCVal));     
	}
	
	@Test
	public void testExcelImport() throws IOException 
	{
        TableContext tc = TableContextFactory.createTableContext();
        assertNotNull(tc);
        assertThat(0, is(tc.getNumTables()));
        
        try
        {
            tc.importTable(qualifiedFileName(ExportSample1Gold, "xls"));
            assertThat(1, is(tc.getNumTables()));
            
            Table t1 = tc.getTable(Access.ByLabel, "Sample 1");
            assertNotNull(t1);
            assertThat(5, is(t1.getNumRows()));
            assertThat(3, is(t1.getNumColumns()));
        }
        catch (Exception e)
        {
            fail(e.getMessage());
        }
	}
}
