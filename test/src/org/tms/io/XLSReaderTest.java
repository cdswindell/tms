package org.tms.io;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.io.IOException;

import org.junit.Test;
import org.tms.BaseTest;
import org.tms.api.Access;
import org.tms.api.Cell;
import org.tms.api.Column;
import org.tms.api.Row;
import org.tms.api.Subset;
import org.tms.api.Table;
import org.tms.api.io.options.XlsOptions;

public class XLSReaderTest extends BaseTest
{
    private static final String SAMPLE1 = "sample1.xlsx";
    private static final String SAMPLE2 = "sample2.xlsx";
    private static final String SAMPLE3 = "sample3.xlsx";
    
    @Test
    public final void testXlsReaderConstructor()
    {
        XlsReader r = new XlsReader(qualifiedFileName(SAMPLE1), XlsOptions.Default); 
        assertNotNull(r);
        assertThat(r.getFileName(), is(SAMPLE1));
        assertThat(r.isRowNames(), is(true));
        assertThat(r.isColumnNames(), is(true));
    }

    @Test
    public final void testImportSimpleSheet() 
    {
        XlsReader r = new XlsReader(qualifiedFileName(SAMPLE1), XlsOptions.Default); 
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
    public final void testImportSheetWithRangesNotesEquations() 
    {
        XlsReader r = new XlsReader(qualifiedFileName(SAMPLE2), XlsOptions.Default); 
        assertNotNull(r);
        
        try
        {
            Table t = r.parse();
            assertNotNull(t);
            
            assertThat(t.getNumRows(), is(4));
            assertThat(t.getNumColumns(), is(4));
            assertThat(t.getNumCells(), is(12));
            
            Column c1 = t.getColumn(1);
            assertNotNull(c1);
            assertThat(c1.getLabel(), is("Abc"));
            
            Column c2 = t.getColumn(2);
            assertNotNull(c2);
            assertThat(c2.getLabel(), is ("Col 2"));
            
            Column c3 = t.getColumn(3);
            assertNotNull(c3);
            assertThat(c3.getLabel(), is("Def Ghi"));
            assertThat(c3.getDescription(), is("This is the Def Ghi Column label"));
            
            Column c4 = t.getColumn(4);
            assertNotNull(c4);           
            assertThat(c4.getLabel(), is ("Col 4"));
            assertThat(c4.isDerived(), is(true));
            assertThat(c4.getDerivation().getAsEnteredExpression(), is("col 1 * 3.0"));
            
            Row r1 = t.getRow(1);
            assertNotNull(r1);
            assertThat(r1.getLabel(), is("Blue Row"));
            assertThat(r1.getDescription(), is("This is the row label for the blue row"));
            
            Row r2 = t.getRow(2);
            assertNotNull(r2);
            assertThat(r2.getLabel(), nullValue());
            
            Row r3 = t.getRow(3);
            assertNotNull(r3);
            assertThat(r3.getLabel(), is("Yellow Row"));
            
            Row r4 = t.getRow(4);
            assertNotNull(r4);
            assertThat(r4.getLabel(), is("Cyan Row"));    
            
            // check cell values
            vetCellValue(t, r1, c1, 12.0);
            vetCellValue(t, r1, c2, "Blue");
            vetCellValue(t, r1, c3, true);
            vetCellValue(t, r1, c4, 36.0);
            
            vetCellValue(t, r2, c1, null);
            vetCellValue(t, r2, c2, null);
            vetCellValue(t, r2, c3, null);
            vetCellValue(t, r2, c4, null);
            
            vetCellValue(t, r3, c1, null);
            vetCellValue(t, r3, c2, "Yellow");
            Cell cell = vetCellValue(t, r3, c3, true);
            assertThat(cell.getDescription(), is("This is the boolean column in the Yellow row"));
            vetCellValue(t, r3, c4, null);
            
            vetCellValue(t, r4, c1, 17.65);
            cell = vetCellValue(t, r4, c2, "Cyan");
            assertThat(cell.getLabel(), is("Cyan"));
            vetCellValue(t, r4, c3, false);
            vetCellValue(t, r4, c4, 52.95);
            
            // check subsets
            Subset s = t.getSubset(Access.ByLabel, "Booleans");
            assertNotNull(s);
            assertNotNull(s.getColumns());
            assertThat(s.getNumColumns(), is(1));
            assertThat(s.contains(c1), is(false));
            assertThat(s.contains(c2), is(false));
            assertThat(s.contains(c3), is(true));
            assertThat(s.contains(c4), is(false));
            assertThat(s.getNumRows(), is(0));
            assertThat(s.contains(r1), is(false));
            assertThat(s.contains(r2), is(false));
            assertThat(s.contains(r3), is(false));
            assertThat(s.contains(r4), is(false));

            s = t.getSubset(Access.ByLabel, "YellowRow");
            assertNotNull(s);
            assertThat(s.getNumColumns(), is(0));
            assertThat(s.contains(c1), is(false));
            assertThat(s.contains(c2), is(false));
            assertThat(s.contains(c3), is(false));
            assertThat(s.contains(c4), is(false));
            assertThat(s.getNumRows(), is(1));
            assertThat(s.contains(r1), is(false));
            assertThat(s.contains(r2), is(false));
            assertThat(s.contains(r3), is(true));
            assertThat(s.contains(r4), is(false));

            s = t.getSubset(Access.ByLabel, "Subset");
            assertNotNull(s);
            assertThat(s.getNumColumns(), is(3));
            assertThat(s.contains(c1), is(true));
            assertThat(s.contains(c2), is(true));
            assertThat(s.contains(c3), is(true));
            assertThat(s.contains(c4), is(false));
            assertThat(s.getNumRows(), is(3));
            assertThat(s.contains(r1), is(false));
            assertThat(s.contains(r2), is(true));
            assertThat(s.contains(r3), is(true));
            assertThat(s.contains(r4), is(true));
        }
        catch (IOException e)
        {
            fail(e.getMessage());
        }
    }
    
    @Test
    public final void testImportSheetComplexEquations() 
    {
        XlsReader r = new XlsReader(qualifiedFileName(SAMPLE3), XlsOptions.Default); 
        assertNotNull(r);
        
        try
        {
            Table t = r.parse();
            assertNotNull(t);
            
            // validate basic stat calculations
            vetCellValue(t, "Count", 6.0);
            vetCellValue(t, "Range", 21.0);
            vetCellValue(t, "Sum", 262.95);
            vetCellValue(t, "Mean", 43.825);
            vetCellValue(t, "Median", 43.5);
            vetCellValue(t, "Mode", 36.0);
            vetCellValue(t, "StDev", 9.776694227);
            vetCellValue(t, "Min", 33);
            vetCellValue(t, "Max", 54);
            vetCellValue(t, "Skew", -0.014171252);
            vetCellValue(t, "SumSq", 12001.7025);
            vetCellValue(t, "Kurtosis", -3.067931799);
            vetCellValue(t, "DevSq", 477.91875);
            vetCellValue(t, "FirstQ", 36.0);
            vetCellValue(t, "SecondQ", 43.5);
            //vetCellValue(t, "ThirdQ", 52.4625);
            vetCellValue(t, "ForthQ", 54);
        }
        catch (IOException e)
        {
            fail(e.getMessage());
        }
    }            
}
