package org.tms.tds.logs;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.io.IOException;

import org.junit.Test;
import org.tms.BaseTest;
import org.tms.api.Access;
import org.tms.api.Cell;
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
		assertThat(343, is(lft.getNumRows()));
		
		Column dateCol = lft.getColumn(1);
		assertNotNull(dateCol);
		
		Column sevCol = lft.getColumn(5);
		assertNotNull(sevCol);
		
		Row r1 = lft.getRow(1);
		assertNotNull(r1);
		
		Row r3 = lft.getRow(3);
		assertNotNull(r1);
		
		Row r5 = lft.getRow(5);
		assertNotNull(r5);
		
		Row rLast = lft.getRow(Access.Last);
		assertNotNull(rLast);
		
		Object dt = lft.getCellValue(r1,  dateCol);
		assertNotNull(dt);
		
		dt = lft.getCellValue(r5,  dateCol);
		assertNotNull(dt);
		
		dt = lft.getCellValue(r3,  dateCol);
		assertNotNull(dt);		
		
		dt = lft.getCellValue(rLast,  dateCol);
		assertNotNull(dt);		
		
		// add some summary rows
		Row sr = lft.addRow(Access.Last);
		Cell warningsCell = lft.getCell(sr,  sevCol);
		assertNotNull(warningsCell);	
		warningsCell.setDerivation("numberOf(col 'Severity', 'Warning')");
		
		double nWarnings = (Double)warningsCell.getCellValue();
		assertThat(19.0, is(nWarnings));		
	}
}
