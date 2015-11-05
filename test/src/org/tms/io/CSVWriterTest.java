package org.tms.io;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

import org.junit.Test;
import org.tms.BaseTest;
import org.tms.api.Access;
import org.tms.api.Column;
import org.tms.api.Row;
import org.tms.api.Table;
import org.tms.api.factories.TableFactory;
import org.tms.api.io.CSVOptions;

public class CSVWriterTest extends BaseTest
{
    private static final String SAMPLE1 = "sample1.csv";
    private static final String SAMPLE2 = "sample2.csv";
    private static final String SAMPLE3 = "sample3.csv";    

    @Test
    public final void testExportSample1() throws IOException
    {
        Table t = TableFactory.importCSV(qualifiedFileName(SAMPLE1, "csv"), true, true);
        assertNotNull(t);
        
        File tmpFile = File.createTempFile("testExport", ".csv");
        
        // export the file
        t.export(tmpFile.getPath());
        
        // now reimport it
        Table t2 = TableFactory.importCSV(tmpFile.getPath(), true, true);
        tmpFile.delete();
        
        assertNotNull(t2);        
        assertThat(t2.isValid(), is(true));
        assertThat(t2.getNumRows(), is(t.getNumRows()));
        assertThat(t2.getNumColumns(), is(t.getNumColumns()));
        assertThat(t2.getNumCells(), is(t.getNumCells()));
        
        for (Row r2 : t2.getRows()) {
            assertNotNull(r2);
            Row r1 = t.getRow(Access.ByIndex, r2.getIndex());
            assertNotNull(r1);
            assertThat(r2.getLabel(), is(r1.getLabel()));
            
            for (Column c2 : t2.getColumns()) {
                assertNotNull(c2);
                Column c1 = t.getColumn(Access.ByIndex, c2.getIndex());
                assertNotNull(c1);
                assertThat(c2.getLabel(), is(c1.getLabel()));
                
                assertThat(t2.getCellValue(r2, c2), is(t.getCellValue(r1, c1)));
            }
        }
        
        t = TableFactory.importCSV(qualifiedFileName(SAMPLE2, "csv"), true, true);
        assertNotNull(t);
        
        tmpFile = File.createTempFile("testExport2", ".csv");
        
        // export the file
        t.export(tmpFile.getPath());
        
        // now reimport it
        t2 = TableFactory.importCSV(tmpFile.getPath(), true, true);
        tmpFile.delete();
        
        assertNotNull(t2);        
        assertThat(t2.isValid(), is(true));
        assertThat(t2.getNumRows(), is(t.getNumRows()));
        assertThat(t2.getNumColumns(), is(t.getNumColumns()));
        assertThat(t2.getNumCells(), is(t.getNumCells()));
        
        for (Row r2 : t2.getRows()) {
            assertNotNull(r2);
            Row r1 = t.getRow(Access.ByIndex, r2.getIndex());
            assertNotNull(r1);
            assertThat(r2.getLabel(), is(r1.getLabel()));
            
            for (Column c2 : t2.getColumns()) {
                assertNotNull(c2);
                Column c1 = t.getColumn(Access.ByIndex, c2.getIndex());
                assertNotNull(c1);
                assertThat(c2.getLabel(), is(c1.getLabel()));
                
                assertThat(t2.getCellValue(r2, c2), is(t.getCellValue(r1, c1)));
            }
        }
    }
    
    @Test
    public final void testExportSample2() throws IOException
    {
        Table t = TableFactory.importCSV(qualifiedFileName(SAMPLE2, "csv"), true, true);
        assertNotNull(t);
        
        File tmpFile = File.createTempFile("testExport2", ".csv");
        
        // export the file
        t.export(tmpFile.getPath());
        
        // now reimport it
        Table t2 = TableFactory.importCSV(tmpFile.getPath(), true, true);
        tmpFile.delete();
        
        assertNotNull(t2);        
        assertThat(t2.isValid(), is(true));
        assertThat(t2.getNumRows(), is(t.getNumRows()));
        assertThat(t2.getNumColumns(), is(t.getNumColumns()));
        assertThat(t2.getNumCells(), is(t.getNumCells()));
        
        for (Row r2 : t2.getRows()) {
            assertNotNull(r2);
            Row r1 = t.getRow(Access.ByIndex, r2.getIndex());
            assertNotNull(r1);
            assertThat(r2.getLabel(), is(r1.getLabel()));
            
            for (Column c2 : t2.getColumns()) {
                assertNotNull(c2);
                Column c1 = t.getColumn(Access.ByIndex, c2.getIndex());
                assertNotNull(c1);
                assertThat(c2.getLabel(), is(c1.getLabel()));
                
                assertThat(t2.getCellValue(r2, c2), is(t.getCellValue(r1, c1)));
            }
        }
    }
    
    @Test
    public final void testExportIgnoreEmptyCols() throws IOException
    {
        Table t = TableFactory.importCSV(qualifiedFileName(SAMPLE3, "csv"), true, true);
        assertNotNull(t);
        
        File tmpFile = File.createTempFile("testExport3", ".csv");
        
        // export the file, ignoring empty columns       
        t.export(tmpFile.getPath(), CSVOptions.Default.withIgnoreEmptyColumns());
        
        // now reimport it
        Table t2 = TableFactory.importCSV(tmpFile.getPath(), true, true);
        tmpFile.delete();
        
        assertNotNull(t2);        
        assertThat(t2.isValid(), is(true));
        assertThat(t2.getNumRows(), is(t.getNumRows()));
        assertThat(t2.getNumColumns(), is(t.getNumColumns() - 1));
        assertThat(t2.getNumCells(), is(t.getNumCells()));
        
        for (Row r2 : t2.getRows()) {
            assertNotNull(r2);
            Row r1 = t.getRow(Access.ByIndex, r2.getIndex());
            assertNotNull(r1);
            assertThat(r2.getLabel(), is(r1.getLabel()));
            
            Column c1 = t.getColumn(Access.First);
            assertNotNull(c1);
            for (Column c2 : t2.getColumns()) {
                assertNotNull(c2);
                
                while (c1.isNull())
                    c1 = t.getColumn(Access.Next);                
                assertNotNull(c1);
                assertThat(c2.getLabel(), is(c1.getLabel()));
                
                assertThat(t2.getCellValue(r2, c2), is(t.getCellValue(r1, c1)));
                
                c1 = t.getColumn(Access.Next); 
            }
        }
    }
    
    @Test
    public final void testExportIgnoreEmptyRows() throws IOException
    {
        Table t = TableFactory.importCSV(qualifiedFileName(SAMPLE3, "csv"), true, true);
        assertNotNull(t);
        
        File tmpFile = File.createTempFile("testExport4", ".csv");
        
        // export the file, ignoring empty rows        
        t.export(tmpFile.getPath(), CSVOptions.Default.withIgnoreEmptyRows());
        
        // now reimport it
        Table t2 = TableFactory.importCSV(tmpFile.getPath(), true, true);
        tmpFile.delete();
        
        assertNotNull(t2);        
        assertThat(t2.isValid(), is(true));
        assertThat(t2.getNumRows(), is(t.getNumRows() - 1));
        assertThat(t2.getNumColumns(), is(t.getNumColumns()));
        assertThat(t2.getNumCells(), is(t.getNumCells()));
        
        Row r1 = t.getRow(Access.First);
        for (Row r2 : t2.getRows()) {
            assertNotNull(r2);
            
            while (r1.isNull())
                r1 = t.getRow(Access.Next); 
            assertNotNull(r1);
            assertThat(r2.getLabel(), is(r1.getLabel()));
            
            Column c1 = t.getColumn(Access.First);
            assertNotNull(c1);
            for (Column c2 : t2.getColumns()) {
                assertNotNull(c2);                
                assertNotNull(c1);
                assertThat(c2.getLabel(), is(c1.getLabel()));                
                assertThat(t2.getCellValue(r2, c2), is(t.getCellValue(r1, c1)));
                
                c1 = t.getColumn(Access.Next); 
            }
            
            r1 = t.getRow(Access.Next);
        }
    }
    
    @Test
    public final void testExportIgnoreEmptyRowsAndCols() throws IOException
    {
        Table t = TableFactory.importCSV(qualifiedFileName(SAMPLE3, "csv"), true, true);
        assertNotNull(t);
        
        File tmpFile = File.createTempFile("testExport5", ".csv");
        
        // export the file, ignoring empty rows and columns       
        t.export(tmpFile.getPath(), CSVOptions.Default.withIgnoreEmptyRows().withIgnoreEmptyColumns());
        
        // now reimport it
        Table t2 = TableFactory.importCSV(tmpFile.getPath(), true, true);
        tmpFile.delete();
        
        assertNotNull(t2);        
        assertThat(t2.isValid(), is(true));
        assertThat(t2.getNumRows(), is(t.getNumRows() - 1));
        assertThat(t2.getNumColumns(), is(t.getNumColumns() - 1));
        assertThat(t2.getNumCells(), is(t.getNumCells()));
        
        Row r1 = t.getRow(Access.First);
        for (Row r2 : t2.getRows()) {
            assertNotNull(r2);
            
            while (r1.isNull())
                r1 = t.getRow(Access.Next); 
            assertNotNull(r1);
            assertThat(r2.getLabel(), is(r1.getLabel()));
            
            Column c1 = t.getColumn(Access.First);
            assertNotNull(c1);
            for (Column c2 : t2.getColumns()) {
                assertNotNull(c2);                
                while (c1.isNull())
                    c1 = t.getColumn(Access.Next);                
                assertNotNull(c1);
                assertThat(c2.getLabel(), is(c1.getLabel()));                
                assertThat(t2.getCellValue(r2, c2), is(t.getCellValue(r1, c1)));
                
                c1 = t.getColumn(Access.Next); 
            }
            
            r1 = t.getRow(Access.Next);
        }
    }
    
    @Test
    public final void testExportToString() throws IOException
    {
        String expected = "\"\",Abc,,\"Def, Ghi\"\r\n" +
                          "Blue Row,,Blue,true\r\n" + 
                          "\"\",,,\r\n" +
                          "Yellow Row,,Yellow,true\r\n" +
                          "Cyan Row,,Cyan,false\r\n";
        
        Table t = TableFactory.importCSV(qualifiedFileName(SAMPLE3, "csv"), true, true);
        assertNotNull(t);
        
        // export the file, ignoring empty rows and columns    
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        t.export(bos, CSVOptions.Default);
        
        byte [] csv = bos.toByteArray();
        assertNotNull(csv);
        assertThat(csv.length > 0, is(true));
        
        String csvStr= new String(csv);
        assertThat(csvStr, is(expected));
    }   
    
    @Test
    public final void testExportToStringNoColNames() throws IOException
    {
        String expected = "Blue Row,,Blue,true\r\n" + 
                          "\"\",,,\r\n" +
                          "Yellow Row,,Yellow,true\r\n" +
                          "Cyan Row,,Cyan,false\r\n";
        
        Table t = TableFactory.importCSV(qualifiedFileName(SAMPLE3, "csv"), true, true);
        assertNotNull(t);
        
        // export the file, ignoring empty rows and columns    
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        t.export(bos, CSVOptions.Default.withColumnLabels(false));
        
        byte [] csv = bos.toByteArray();
        assertNotNull(csv);
        assertThat(csv.length > 0, is(true));
        
        String csvStr= new String(csv);
        assertThat(csvStr, is(expected));
    }   
    
    @Test
    public final void testExportToStringNoRowNames() throws IOException
    {
        String expected = "Abc,,\"Def, Ghi\"\r\n" +
                "\"\",Blue,true\r\n" + 
                "\"\",,\r\n" +
                "\"\",Yellow,true\r\n" +
                "\"\",Cyan,false\r\n";

        Table t = TableFactory.importCSV(qualifiedFileName(SAMPLE3, "csv"), true, true);
        assertNotNull(t);
        
        // export the file, ignoring empty rows and columns    
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        t.export(bos, CSVOptions.Default.withRowLabels(false));
        
        byte [] csv = bos.toByteArray();
        assertNotNull(csv);
        assertThat(csv.length > 0, is(true));
        
        String csvStr= new String(csv);
        assertThat(csvStr, is(expected));
    }   
}
