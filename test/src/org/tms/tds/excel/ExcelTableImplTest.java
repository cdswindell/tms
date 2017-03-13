package org.tms.tds.excel;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.io.IOException;

import org.junit.Test;
import org.tms.BaseTest;
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
		
	}
}
