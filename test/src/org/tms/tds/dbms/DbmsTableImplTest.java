package org.tms.tds.dbms;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertThat;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.junit.Test;
import org.tms.api.Access;
import org.tms.api.Cell;
import org.tms.api.Column;
import org.tms.api.Row;
import org.tms.api.TableContext;
import org.tms.api.factories.TableContextFactory;

public class DbmsTableImplTest extends BaseDbmsTest
{
    @Test
    public final void testCreateDBMSTable() throws ClassNotFoundException, SQLException
    {
        TableContext tc = TableContextFactory.fetchDefaultTableContext();
        assertThat(tc, notNullValue());
        
        tc.loadDatabaseDriver("com.mysql.jdbc.Driver");
        assertThat(tc.isDatabaseDriverLoaded("com.mysql.jdbc.Driver"), is(true));
        
        // count the number of expected rows
        ResultSet rs = fetchResultSet("jdbc:mysql://localhost/cds?user=davids&password=mysql", 
                                      "select * from emp order by ename");
        
        int numRows = getDbmsRowCount(rs);
        int numCols = getDbmsColumnCount(rs);
        close(rs);
        
        // create basic table using mysql "cds" database
        DbmsTableImpl t = new DbmsTableImpl("jdbc:mysql://localhost/cds?user=davids&password=mysql", 
                                            "select * from emp order by ename desc");
        assertThat(t, notNullValue());
        assertThat(t.getNumRows(), is(numRows));
        assertThat(t.getNumColumns(), is(numCols));
        
        Object value = t.getCellValue(t.getRow(Access.ByIndex, 2), t.getColumn(Access.First));
        assertThat(value, notNullValue());
        
        t.refresh();
        Column c = t.addColumn();
        c.setDerivation("col empno * col 'empno'");
        
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
        
        cell = t.getCell(r, t.getColumn(Access.ByLabel, "mgr"));
        cell.setDerivation("mean(col 'mgr')");
        assertThat(cell.getCellValue(), is(10.0));
    }
}
