package org.tms.io;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.io.IOException;

import org.junit.Test;
import org.tms.BaseTest;
import org.tms.api.Column;
import org.tms.api.Row;
import org.tms.api.Table;
import org.tms.api.exceptions.TableIOException;
import org.tms.api.factories.TableFactory;
import org.tms.api.io.CSVOptions;

public class CSVReaderTest extends BaseTest
{
    private static final String SAMPLE1 = "sample1.csv";
    private static final String SAMPLE2 = "sample2.csv";
    private static final String SAMPLE3 = "sample3.csv";
    
    @Test
    public final void testCSVReaderConstructor()
    {
        CSVReader r = new CSVReader(qualifiedFileName(SAMPLE1), CSVOptions.Default); 
        assertNotNull(r);
        assertThat(r.getFileName(), is(SAMPLE1));
        assertThat(r.isRowNames(), is(true));
        assertThat(r.isColumnNames(), is(true));
    }

    @Test
    public final void testParse() 
    {
        CSVReader r = new CSVReader(qualifiedFileName(SAMPLE1), CSVOptions.Default); 
        assertNotNull(r);
        
        try
        {
            Table t = r.parse();
            assertNotNull(t);
            
            assertThat(t.getNumRows(), is(4));
            assertThat(t.getNumColumns(), is(3));
            assertThat(t.getNumCells(), is(12));
            
            assertThat(t.getColumn(1).getLabel(), is("A"));
            assertThat(t.getColumn(2).getLabel(), is("B"));
            assertThat(t.getColumn(3).getLabel(), is("C"));
            
            Column c1 = t.getColumn(1);
            Column c2 = t.getColumn(2);
            Column c3 = t.getColumn(3);
            for (Row row : t.rows()) {
                assertThat(Number.class.isAssignableFrom(t.getCellValue(row, c1).getClass()), is(true));
                assertThat(row.getLabel(), is(t.getCellValue(row, c2) + " Row"));
                assertThat(Boolean.class.isAssignableFrom(t.getCellValue(row, c3).getClass()), is(true));
            }
        }
        catch (IOException e)
        {
            fail(e.getMessage());
        }
    }
    
    @Test
    public final void testParseComplex() 
    {
        CSVReader r = new CSVReader(qualifiedFileName(SAMPLE2), CSVOptions.Default); 
        assertNotNull(r);
        
        try
        {
            Table t = r.parse();
            assertNotNull(t);
            
            assertThat(t.getNumRows(), is(4));
            assertThat(t.getNumColumns(), is(3));
            assertThat(t.getNumCells(), is(4 * 2));
            
            assertThat(t.getColumn(1).getLabel(), is("Abc"));
            assertThat(t.getColumn(2).getLabel(), nullValue());
            assertThat(t.getColumn(3).getLabel(), is("Def, Ghi"));
            
            Column c1 = t.getColumn(1);
            Column c2 = t.getColumn(2);
            Column c3 = t.getColumn(3);
            for (Row row : t.rows()) {
                if (t.getCellValue(row, c1) != null)
                    assertThat(Number.class.isAssignableFrom(t.getCellValue(row, c1).getClass()), is(true));
                
                if (t.getCellValue(row, c2) != null)
                    assertThat(row.getLabel(), is(t.getCellValue(row, c2) + " Row"));
                
                if (t.getCellValue(row, c3) != null)
                    assertThat(Boolean.class.isAssignableFrom(t.getCellValue(row, c3).getClass()), is(true));
            }
        }
        catch (IOException e)
        {
            fail(e.getMessage());
        }
    }
    
    @Test
    public final void testParseIgnoreEmptyRows() 
    {
        CSVReader r = new CSVReader(qualifiedFileName(SAMPLE2), CSVOptions.Default.withIgnoreEmptyRows()); 
        assertNotNull(r);
        
        try
        {
            Table t = r.parse();
            assertNotNull(t);
            
            assertThat(t.getNumRows(), is(3));
            assertThat(t.getNumColumns(), is(3));
            assertThat(t.getNumCells(), is(4 * 2));
            
            assertThat(t.getColumn(1).getLabel(), is("Abc"));
            assertThat(t.getColumn(2).getLabel(), nullValue());
            assertThat(t.getColumn(3).getLabel(), is("Def, Ghi"));
            
            Column c1 = t.getColumn(1);
            Column c2 = t.getColumn(2);
            Column c3 = t.getColumn(3);
            for (Row row : t.rows()) {
                if (t.getCellValue(row, c1) != null)
                    assertThat(Number.class.isAssignableFrom(t.getCellValue(row, c1).getClass()), is(true));
                
                if (t.getCellValue(row, c2) != null)
                    assertThat(row.getLabel(), is(t.getCellValue(row, c2) + " Row"));
                
                if (t.getCellValue(row, c3) != null)
                    assertThat(Boolean.class.isAssignableFrom(t.getCellValue(row, c3).getClass()), is(true));
            }
        }
        catch (IOException e)
        {
            fail(e.getMessage());
        }
    }
    
    @Test
    public final void testParseIgnoreEmptyCols() 
    {
        CSVReader r = new CSVReader(qualifiedFileName(SAMPLE3), CSVOptions.Default.withIgnoreEmptyColumns()); 
        assertNotNull(r);
        
        try
        {
            Table t = r.parse();
            assertNotNull(t);
            
            assertThat(t.getNumRows(), is(4));
            assertThat(t.getNumColumns(), is(2));
            assertThat(t.getNumCells(), is(6));
            
            assertThat(t.getColumn(1).getLabel(), nullValue());
            assertThat(t.getColumn(2).getLabel(), is("Def, Ghi"));
            
            Column c2 = t.getColumn(1);
            Column c3 = t.getColumn(2);
            for (Row row : t.rows()) {
                if (t.getCellValue(row, c2) != null)
                    assertThat(row.getLabel(), is(t.getCellValue(row, c2) + " Row"));
                
                if (t.getCellValue(row, c3) != null)
                    assertThat(Boolean.class.isAssignableFrom(t.getCellValue(row, c3).getClass()), is(true));
            }
        }
        catch (IOException e)
        {
            fail(e.getMessage());
        }
    }
    
    @Test
    public final void testParseIgnoreEmptyRowsAndCols() 
    {
        CSVReader r = new CSVReader(qualifiedFileName(SAMPLE3), CSVOptions.Default.withIgnoreEmptyRows().withIgnoreEmptyColumns()); 
        assertNotNull(r);
        
        try
        {
            Table t = r.parse();
            assertNotNull(t);
            
            assertThat(t.getNumRows(), is(3));
            assertThat(t.getNumColumns(), is(2));
            assertThat(t.getNumCells(), is(6));
            
            assertThat(t.getColumn(1).getLabel(), nullValue());
            assertThat(t.getColumn(2).getLabel(), is("Def, Ghi"));
            
            Column c2 = t.getColumn(1);
            Column c3 = t.getColumn(2);
            for (Row row : t.rows()) {
                if (t.getCellValue(row, c2) != null)
                    assertThat(row.getLabel(), is(t.getCellValue(row, c2) + " Row"));
                
                if (t.getCellValue(row, c3) != null)
                    assertThat(Boolean.class.isAssignableFrom(t.getCellValue(row, c3).getClass()), is(true));
            }
        }
        catch (IOException e)
        {
            fail(e.getMessage());
        }
    }
    
    @Test
    public final void testParseNoColumnNames() 
    {
        CSVReader r = new CSVReader(qualifiedFileName(SAMPLE1), CSVOptions.Default.withColumnLabels(false)); 
        assertNotNull(r);
        
        try
        {
            Table t = r.parse();
            assertNotNull(t);
            
            assertThat(t.getNumRows(), is(5));
            assertThat(t.getNumColumns(), is(3));
            assertThat(t.getNumCells(), is(15));
            
            assertThat(t.getColumn(1).getLabel(), nullValue());
            assertThat(t.getColumn(2).getLabel(), nullValue());
            assertThat(t.getColumn(3).getLabel(), nullValue());
            
            Column c1 = t.getColumn(1);
            Column c2 = t.getColumn(2);
            Column c3 = t.getColumn(3);
            for (Row row : t.rows()) {
                if (row.getIndex() == 1) {
                    assertThat(t.getCellValue(row, c1), is("A"));
                    assertThat(t.getCellValue(row, c2), is("B"));
                    assertThat(t.getCellValue(row, c3), is("C"));                    
                }
                else {
                    assertThat(Number.class.isAssignableFrom(t.getCellValue(row, c1).getClass()), is(true));
                    assertThat(row.getLabel(), is(t.getCellValue(row, c2) + " Row"));
                    assertThat(Boolean.class.isAssignableFrom(t.getCellValue(row, c3).getClass()), is(true));
                }
            }
        }
        catch (IOException e)
        {
            fail(e.getMessage());
        }
    }
    
    @Test
    public final void testParseNoRowNames() 
    {
        CSVReader r = new CSVReader(qualifiedFileName(SAMPLE1), CSVOptions.Default.withRowLabels(false)); 
        assertNotNull(r);
        
        try
        {
            Table t = r.parse();
            assertNotNull(t);
            
            assertThat(t.getNumRows(), is(4));
            assertThat(t.getNumColumns(), is(4));
            assertThat(t.getNumCells(), is(16));
            
            assertThat(t.getColumn(2).getLabel(), is("A"));
            assertThat(t.getColumn(3).getLabel(), is("B"));
            assertThat(t.getColumn(4).getLabel(), is("C"));
            
            Column labelCol = t.getColumn(1);
            Column numCol = t.getColumn(2);
            Column textCol = t.getColumn(3);
            Column boolCol = t.getColumn(4);
            for (Row row : t.rows()) {
                assertThat(row.getLabel(), nullValue());
                assertThat(Number.class.isAssignableFrom(t.getCellValue(row, numCol).getClass()), is(true));
                assertThat(t.getCellValue(row, labelCol), is(t.getCellValue(row, textCol) + " Row"));
                assertThat(Boolean.class.isAssignableFrom(t.getCellValue(row, boolCol).getClass()), is(true));
            }
        }
        catch (IOException e)
        {
            fail(e.getMessage());
        }
    }
    
    @Test
    public final void testImportCSV() 
    {
        try
        {
            Table t = TableFactory.importCSV(qualifiedFileName(SAMPLE1), true, true); 
            assertNotNull(t);
            
            assertThat(t.getNumRows(), is(4));
            assertThat(t.getNumColumns(), is(3));
            assertThat(t.getNumCells(), is(12));
            
            assertThat(t.getColumn(1).getLabel(), is("A"));
            assertThat(t.getColumn(2).getLabel(), is("B"));
            assertThat(t.getColumn(3).getLabel(), is("C"));
            
            Column c1 = t.getColumn(1);
            Column c2 = t.getColumn(2);
            Column c3 = t.getColumn(3);
            for (Row row : t.rows()) {
                assertThat(Number.class.isAssignableFrom(t.getCellValue(row, c1).getClass()), is(true));
                assertThat(row.getLabel(), is(t.getCellValue(row, c2) + " Row"));
                assertThat(Boolean.class.isAssignableFrom(t.getCellValue(row, c3).getClass()), is(true));
            }
        }
        catch (TableIOException e)
        {
            fail(e.getMessage());
        }
    }
}
