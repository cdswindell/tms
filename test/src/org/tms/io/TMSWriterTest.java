package org.tms.io;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.AfterClass;
import org.junit.Test;
import org.tms.api.Column;
import org.tms.api.Table;
import org.tms.api.TableContext;
import org.tms.api.exceptions.ConstraintViolationException;
import org.tms.api.factories.TableContextFactory;
import org.tms.api.io.TMSOptions;
import org.tms.api.utils.NumericRange;
import org.tms.tds.TdsUtils;

public class TMSWriterTest extends BaseArchivalTest
{
    @AfterClass
    static public void cleanup()
    {
        TableContext tc = TableContextFactory.fetchDefaultTableContext();
        TdsUtils.clearGlobalTagCache(tc);
    }
    
    private static final String ExportTableGold = "simpleTable.tms";
    private static final String ExportTableGoldVal = "tableWithValidators.tms";
    private static final String ExportTableContext = "TMSTableContext.tms";
    
    @Test
    public final void testExportTableContext() throws IOException
    {
        /*
         * Note: If you change this test, be sure to update
         * the gold standard file ExportTableGold
         */
        Path path = Paths.get(qualifiedFileName(ExportTableContext, "tms"));
        byte[] gold = Files.readAllBytes(path);  

        assertNotNull(gold);
        assertThat(gold.length > 0, is(true));
        
        // create new XML, it should match the gold standard
        TableContext tc = this.getPopulatedTableContext();
        
        // create output stream
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        tc.export(bos, TMSOptions.Default);
        bos.close();

        // test byte streams are the same
        byte [] output =  bos.toByteArray();
        assertNotNull(output);

        assertThat(String.format("Gold: %d, Observed: %d",  gold.length, output.length), closeTo(output.length, gold.length, 16), is(true));   
        
        TableContext tc2 = TableContextFactory.importTables(qualifiedFileName(ExportTableContext, "tms"));
        assertNotNull(tc2);
        
        assertThat(tc.getNumTables(), is(tc2.getNumTables()));       
    }
    
    @Test
    public final void testExportBasicTable() throws IOException
    {
        /*
         * Note: If you change this test, be sure to update
         * the gold standard file ExportTableGold
         */
        Path path = Paths.get(qualifiedFileName(ExportTableGold, "tms"));
        byte[] gold = Files.readAllBytes(path);  

        assertNotNull(gold);
        assertThat(gold.length > 0, is(true));
        
        // create new XML, it should match the gold standard
        Table gst = getBasicTable();
        
        // create output stream
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        gst.export(bos, TMSOptions.Default);
        bos.close();

        // test byte streams are the same
        byte [] output =  bos.toByteArray();
        assertNotNull(output);

        assertThat(String.format("Gold: %d, Observed: %d",  gold.length, output.length), closeTo(output.length, gold.length, 16), is(true));       
    }
    
    @Test
    public final void testExportValidatorXml() throws IOException
    {
        /*
         * Note: If you change this test, be sure to update
         * the gold standard file ExportTableGold
         */
        Path path = Paths.get(qualifiedFileName(ExportTableGoldVal, "tms"));
        byte[] gold = Files.readAllBytes(path);  

        assertNotNull(gold);
        assertThat(gold.length > 0, is(true));
        
        // create new XML, it should match the gold standard
        Table gst = getBasicTable();
        Column c1 = gst.getColumn(1);

        c1.setValidator(v -> { if (v == null || !(v instanceof Number)) throw new ConstraintViolationException("Number Required"); });
        
        Column c2 = gst.getColumn(2);
        c2.setTransformer(v -> { if (v != null && v instanceof Number) return ((Number)v).doubleValue() * 2; return v;});
        
        Column c4 = gst.getColumn(4);
        c4.setValidator(new NumericRange(30, 40));
        
        // create output stream
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        gst.export(bos, TMSOptions.Default);
        bos.close();

        // test byte streams are the same
        byte [] output =  bos.toByteArray();
        assertNotNull(output);

        assertThat(String.format("Gold: %d, Observed: %d",  gold.length, output.length), closeTo(output.length, gold.length, 16), is(true));       
    }
}
