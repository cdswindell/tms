package org.tms.io;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.tms.BaseTest;
import org.tms.api.Table;
import org.tms.api.factories.TableFactory;
import org.tms.api.io.CSVOptions;
import org.tms.tds.ContextImpl;

public class BaseIOTest extends BaseTest
{
    protected Table importCVSFile(String fileName, boolean hasRowNames, boolean hasColumnHeaders)
    {
        return TableFactory.importFile(fileName, ContextImpl.fetchDefaultContext(), 
                CSVOptions.Default.withRowLabels(hasRowNames).withColumnLabels(hasColumnHeaders));
    }
    
    protected  String readFileAsString(String fileName) 
    throws IOException 
    {
    	return readFileAsString(Paths.get(fileName));
	}
    
    protected String readFileAsString(Path path) 
    throws IOException 
    {
		String text = new String(Files.readAllBytes(path));
		return text;
	}
}
