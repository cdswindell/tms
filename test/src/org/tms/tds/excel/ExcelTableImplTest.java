package org.tms.tds.excel;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.io.IOException;

import org.junit.Test;
import org.tms.BaseTest;
import org.tms.api.Cell;
import org.tms.api.Column;
import org.tms.api.Row;
import org.tms.api.io.XLSOptions;

public class ExcelTableImplTest extends BaseTest
{
    private static final String SAMPLE1 = "sample1.xlsx";

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
}
