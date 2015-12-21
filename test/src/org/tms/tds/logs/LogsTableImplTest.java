package org.tms.tds.logs;

import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.io.IOException;

import org.junit.Test;
import org.tms.BaseTest;
import org.tms.api.Column;
import org.tms.api.Row;
import org.tms.api.utils.OSXSystemLogReader;

public class LogsTableImplTest extends BaseTest
{

	@Test
	public void testCreateLogFileTable() throws IOException 
	{
		File logFile = new File(qualifiedFileName("system.log"));
		
		LogsTableImpl lft = LogsTableImpl.createTable(logFile, new OSXSystemLogReader());
		assertNotNull(lft);
		
		Column dateCol = lft.getColumn(1);
		assertNotNull(dateCol);
		
		Row r1 = lft.getRow(1);
		assertNotNull(r1);
		
		Row r3 = lft.getRow(3);
		assertNotNull(r1);
		
		Row r5 = lft.getRow(5);
		assertNotNull(r5);
		
		Object dt = lft.getCellValue(r1,  dateCol);
		assertNotNull(dt);
		
		dt = lft.getCellValue(r5,  dateCol);
		assertNotNull(dt);
		
		dt = lft.getCellValue(r3,  dateCol);
		assertNotNull(dt);
	}
}
