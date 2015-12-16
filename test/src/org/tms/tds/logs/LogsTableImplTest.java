package org.tms.tds.logs;

import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.io.IOException;

import org.junit.Test;
import org.tms.BaseTest;
import org.tms.api.io.LogFileFormat;

public class LogsTableImplTest extends BaseTest
{

	@Test
	public void testCreateLogFileTable() throws IOException 
	{
		File logFile = new File("/private/var/log/system.log");
		
		LogsTableImpl lft = LogsTableImpl.createTable(logFile, new TestLogFileFormat());
		assertNotNull(lft);
	}

	public class TestLogFileFormat implements LogFileFormat
	{
		@Override
		public int getNumFields() 
		{
			return 0;
		}		
	}
}
