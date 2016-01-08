package org.tms.io;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.io.IOException;

import org.junit.AfterClass;
import org.junit.Test;
import org.tms.api.Access;
import org.tms.api.Cell;
import org.tms.api.Column;
import org.tms.api.Row;
import org.tms.api.Table;
import org.tms.api.TableContext;
import org.tms.api.factories.TableContextFactory;
import org.tms.api.io.XMLOptions;
import org.tms.api.utils.TableCellValidator;
import org.tms.tds.TableImpl;
import org.tms.tds.TdsUtils;

public class XMLReaderTest extends BaseArchivalTest
{
    @AfterClass
    static public void cleanup()
    {
        TableContext tc = TableContextFactory.fetchDefaultTableContext();
        TdsUtils.clearGlobalTagCache(tc);
    }
    
    private static final String ExportTableGold = "testExportTable.xml";
    private static final String ExportTableGold3 = "testExportValidator.xml";
    private static final String ExportTableGold4 = "testExportRow.xml";
    private static final String ExportTableGold5 = "testExportCol.xml";
    private static final String ExportTableGold6 = "testExportOneCellTable.xml";
    private static final String ExportTableGoldTC = "testExportTableContext.xml";
    
    @Test
    public final void testCSVReaderConstructor()
    {
        XMLReader r = new XMLReader(qualifiedFileName(ExportTableGold, "xml"), XMLOptions.Default); 
        assertNotNull(r);
        assertThat(r.getFileName(), is(ExportTableGold));
        assertThat(r.isRowNames(), is(true));
        assertThat(r.isColumnNames(), is(true));
    }

    @Test
    public final void testImportTableContext() 
    {
        TableContext tc = TableContextFactory.createTableContext();
        assertNotNull(tc);
        assertThat(0, is(tc.getNumTables()));
        
        try
        {
            tc.importTables(qualifiedFileName(ExportTableGoldTC, "xml"));
            assertThat(3, is(tc.getNumTables()));
            
            Table t3 = tc.getTable(Access.ByLabel, "Almost Empty Table");
            assertNotNull(t3);
            assertThat(10, is(t3.getNumRows()));
            assertThat(2, is(t3.getNumColumns()));
        }
        catch (Exception e)
        {
            fail(e.getMessage());
        }
    }
    
    @Test
    public final void testParseOneCellTable() 
    {
        // now read the xml
        XMLReader r = new XMLReader(qualifiedFileName(ExportTableGold6, "xml"), XMLOptions.Default); 
        assertNotNull(r);
        
        try
        {
            Table t = r.parse();
            assertNotNull(t);
            
            assertThat(t.getNumRows(), is(1024));
            assertThat(t.getNumColumns(), is(1024));
            assertThat(t.getNumCells(), is(1));
            
            assertThat(1024, is(((TableImpl)t).getRowsCapacity()));
            assertThat(1024, is(((TableImpl)t).getColumnsCapacity()));
        }
        catch (IOException e)
        {
            fail(e.getMessage());
        }
    }
    
    @Test
    public final void testParseRow() 
    {
        // create the reference table
        Table gst = getBasicTable();
        
        // now read the xml
        XMLReader r = new XMLReader(qualifiedFileName(ExportTableGold4, "xml"), XMLOptions.Default); 
        assertNotNull(r);
        
        try
        {
            Table t = r.parse();
            assertNotNull(t);
            
            assertThat(t.getNumRows(), is(1));
            assertThat(t.getNumColumns(), is(gst.getNumColumns()));
            assertThat(t.getNumCells(), is(4));
            
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
    public final void testParseCol() 
    {
        // now read the xml
        XMLReader r = new XMLReader(qualifiedFileName(ExportTableGold5, "xml"), XMLOptions.Default); 
        assertNotNull(r);
        
        try
        {
            Table t = r.parse();
            assertNotNull(t);
            
            assertThat(t.getNumRows(), is(4));
            assertThat(t.getNumColumns(), is(1));
            assertThat(t.getNumCells(), is(4));
        }
        catch (IOException e)
        {
            fail(e.getMessage());
        }
    }
    
    @Test
    public final void testParse() 
    {
        // create the reference table
        Table gst = getBasicTable();
        
        // now read the xml
        XMLReader r = new XMLReader(qualifiedFileName(ExportTableGold, "xml"), 
        		XMLOptions.Default
        			.withDescriptions()
        			.withUnits()); 
        assertNotNull(r);
        
        try
        {
            Table t = r.parse();
            assertNotNull(t);
            
            assertThat(t.getNumRows(), is(gst.getNumRows()));
            assertThat(t.getNumColumns(), is(gst.getNumColumns()));
            assertThat(t.getNumCells(), is(gst.getNumCells()));
            
            assertThat(t.getColumn(1).getLabel(), is("A"));
            assertThat(t.getColumn(2).getLabel(), is("B"));
            assertThat(t.getColumn(3).getLabel(), is("C"));
            
            Column c1 = t.getColumn(1);
            Column c2 = t.getColumn(2);
            Column c3 = t.getColumn(3);
            for (Row row : t.rows()) {
                assertThat(Number.class.isAssignableFrom(t.getCellValue(row, c1).getClass()), is(true));
                assertThat(row.getLabel(), is(t.getCellValue(row, c2) + " Row"));
                
                if (row.getIndex() != 1)
                    assertThat(Boolean.class.isAssignableFrom(t.getCellValue(row, c3).getClass()), is(true));
                else {
                    assertThat(t.getCell(row,  c3).getLabel(), is("foo"));
                    assertThat(t.getCell(row,  c3).getUnits(), is("mph"));
                    assertThat(t.getCell(row,  c3).getDescription(), is("Cell Description"));
                }                    
            }
        }
        catch (IOException e)
        {
            fail(e.getMessage());
        }
    }    
    
    @Test
    public final void testParseWithValidate() 
    {
        // create the reference table
        Table gst = getBasicTable();
        assertNotNull(gst);
        
        // now read the xml
        XMLReader r = new XMLReader(qualifiedFileName(ExportTableGold3, "xml"), XMLOptions.Default.withValidators()); 
        assertNotNull(r);
        
        try
        {
            Table t = r.parse();
            assertNotNull(t);
            
            Column c1 = t.getColumn(1);
            assertNotNull(c1);
            
            TableCellValidator v = c1.getValidator();
            assertNotNull(v);
            
            Row r1 = t.getRow(1);
            Cell cell = t.getCell(r1,  c1);
            
            // the following should fail
            try {
                cell.setCellValue("abcd");
                fail("Cell set");
            }
            catch (Exception e) {
                // noop
            }
            
            // clear validator
            Column c5 = t.getColumn(5);
            assertNotNull(c5);
            assertThat(true, is(c5.isDerived()));
            c5.clearDerivation();
            
            // clear the validator, we should now be able to set the cell to a string
            c1.setValidator(null);
            cell.setCellValue("abcd");
            
            // try the other validators/transformers
            cell = t.getCell(r1,  t.getColumn(2));
            assertNotNull(cell);
            
            cell.setCellValue(10);
            assertThat(20.0, is(cell.getCellValue()));
            
            // this should succeed
            cell = t.getCell(r1,  t.getColumn(4));
            assertNotNull(cell);
            
            assertThat(true, is(cell.setCellValue(35)));
            
            // the following should fail
            try {
                cell.setCellValue(10);
                fail("Cell set");
            }
            catch (Exception e) {
                // noop
            }
            
            // the following should fail
            try {
                cell.setCellValue("abc");
                fail("Cell set");
            }
            catch (Exception e) {
                // noop
            }
        }
        catch (IOException e)
        {
            fail(e.getMessage());
        }
    }
}
