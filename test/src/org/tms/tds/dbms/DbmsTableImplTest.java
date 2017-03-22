package org.tms.tds.dbms;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.junit.Ignore;
import org.junit.Test;
import org.tms.api.Access;
import org.tms.api.Cell;
import org.tms.api.Column;
import org.tms.api.Row;
import org.tms.api.TableContext;
import org.tms.api.factories.TableContextFactory;
import org.tms.api.io.TMSOptions;

public class DbmsTableImplTest extends BaseDbmsTest
{
    private static final String ExportMusicTableGold = "music.tms";
    
    @Test
    public final void testCreateDBMSTable() throws ClassNotFoundException, SQLException
    {
        TableContext tc = TableContextFactory.fetchDefaultTableContext();
        assertThat(tc, notNullValue());
        
        tc.loadDatabaseDriver("com.mysql.jdbc.Driver");
        assertThat(tc.isDatabaseDriverLoaded("com.mysql.jdbc.Driver"), is(true));
        
        // count the number of expected rows and columns
        ResultSet rs = fetchResultSet("jdbc:mysql://localhost/cds?user=davids&password=mysql", 
                                      "select * from employee order by name");
        
        int numRows = (int)getDbmsRowCount(rs);
        int numCols = getDbmsColumnCount(rs);
        close(rs);
        
        // create basic table using mysql "cds" database
        DbmsTableImpl t = new DbmsTableImpl("jdbc:mysql://localhost/cds?user=davids&password=mysql", 
                                            "select * from employee order by name desc");
        assertThat(t, notNullValue());
        assertThat(t.getNumRows(), is(numRows));
        assertThat(t.getNumColumns(), is(numCols));
        
        Object value = t.getCellValue(t.getRow(Access.ByIndex, 2), t.getColumn(Access.First));
        assertThat(value, notNullValue());
        
        t.refresh();
        Column c = t.addColumn();
        c.setDerivation("col empno * col 'empno'");
        
        // test deleting a database row
        Row fr = t.getRow(Access.First);
        assertThat(fr, notNullValue());
        
        fr.delete();
        assertThat(t.getNumDbmsRows(), is(numRows - 1));
        assertThat(t.getNumRows(), is(numRows - 1));
        
        for (int i = 1; i < t.getNumRows(); i++) {
            Row r = t.getRow(Access.ByIndex, i);
            Object dbValue = t.getCellValue(r, t.getColumn(Access.First));
            Object derivedValue = t.getCellValue(r, c);
            
            assertThat(dbValue instanceof Number, is(true));
            assertThat(derivedValue instanceof Number, is(true));
            
            assertThat((double)derivedValue, is(1.0 * (int)dbValue * (int)dbValue));            
        }
        
        Row r = t.addRow();
        Cell cell = t.getCell(r, t.getColumn(Access.First));
        cell.setDerivation("mean(col 1)");
        assertThat(cell.isNumericValue(), is(true));
        
        cell = t.getCell(r, t.getColumn(Access.ByLabel, "boss"));
        cell.setDerivation("mean(col 'boss')");
        assertThat(cell.getCellValue(), is(7742.75));
        
        r.delete();
        r = t.addRow();
        assertThat(r, notNullValue());

        r.setDerivation("mean(colref('empno'))");
        r.setDerivation("mean(colref(1))");
    }
	
    @Ignore
    @Test
    public final void testMusicDBMSTable() throws ClassNotFoundException, SQLException, IOException
    {
        /*
         * Note: If you change this test, be sure to update
         * the gold standard file ExportTableGold
         */
        Path path = Paths.get(qualifiedFileName(ExportMusicTableGold, "tms"));
        byte[] gold = Files.readAllBytes(path);  

        assertNotNull(gold);
        assertThat(gold.length > 0, is(true));
        
        // construct table
        TableContext tc = TableContextFactory.fetchDefaultTableContext();
        assertThat(tc, notNullValue());
        
        tc.loadDatabaseDriver("com.mysql.jdbc.Driver");
        assertThat(tc.isDatabaseDriverLoaded("com.mysql.jdbc.Driver"), is(true));
        
        // count the number of expected rows and columns
        ResultSet rs = fetchResultSet("jdbc:mysql://localhost/music?user=davids&password=mysql", 
                                      "select * from metadata");
        
        int numRows = (int)getDbmsRowCount(rs);
        int numCols = getDbmsColumnCount(rs);
        close(rs);
        
        // create basic table using mysql "music" database
        DbmsTableImpl t = new DbmsTableImpl("jdbc:mysql://localhost/music?user=davids&password=mysql", 
        									"select * from metadata");
        assertThat(t, notNullValue());
        assertThat(t.getNumRows(), is(numRows));
        assertThat(t.getNumColumns(), is(numCols));
        
        Object value = t.getCellValue(t.getRow(Access.ByIndex, 2), t.getColumn(Access.First));
        assertThat(value, notNullValue());
        
        t.refresh();
        Column tempoC = t.getColumn(Access.ByLabel, "tempo");
        assertNotNull(tempoC);
        
        Column c = t.addColumn(tempoC.getIndex() + 1);
        c.setDerivation("col tempo * col 'tempo'");
        
        Column c2 = t.addColumn(Access.Last);
        c2.setDerivation("2 * col " + c.getIndex());
        
        Column fc = t.addColumn(Access.First);
        t.setCellValue(t.getRow(Access.ByIndex, 2), fc, 13.6);
        
        // test deleting a database row
        Row fr = t.getRow(Access.First);
        assertThat(fr, notNullValue());
        
        fr.delete();
        assertThat(t.getNumDbmsRows(), is(numRows - 1));
        assertThat(t.getNumRows(), is(numRows - 1));
        
        Column tempo =  t.getColumn(Access.ByLabel, "tempo");
        
        for (int i = 1; i < t.getNumRows(); i++) {
            Row r = t.getRow(Access.ByIndex, i);
            Object dbValue = t.getCellValue(r, tempo);
            Object derivedValue = t.getCellValue(r, c);
            
            assertThat(dbValue instanceof Number, is(true));
            assertThat(derivedValue instanceof Number, is(true));
            
            assertThat(closeTo(derivedValue, (double)dbValue * (double)dbValue, 0.001), is(true));            
        }
        
        // create output stream
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        t.export(bos, TMSOptions.Default);
        //t.export("music.xml", XMLOptions.Default.withVerboseState());
        bos.close();

        // test byte streams are the same
        byte [] output =  bos.toByteArray();
        assertNotNull(output);

        assertThat(String.format("Gold: %d, Observed: %d",  gold.length, output.length), closeTo(output.length, gold.length, 8), is(true));   
        
        Row r = t.addRow();
        Cell cell = t.getCell(r, tempo);
        cell.setDerivation("mean(col tempo)");
        assertThat(cell.isNumericValue(), is(true));
        
        r.delete();
        r = t.addRow();
        assertThat(r, notNullValue());
        
        t.delete();
        t = null;
    }
}
