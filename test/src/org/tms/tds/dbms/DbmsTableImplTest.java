package org.tms.tds.dbms;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertThat;

import java.sql.SQLException;

import org.junit.Test;
import org.tms.api.Access;
import org.tms.api.Column;
import org.tms.api.Row;
import org.tms.api.TableContext;
import org.tms.api.factories.TableContextFactory;

public class DbmsTableImplTest
{

    @Test
    public final void testCreateDBMSTable() throws ClassNotFoundException, SQLException
    {
        TableContext tc = TableContextFactory.fetchDefaultTableContext();
        assertThat(tc, notNullValue());
        
        tc.loadDatabaseDriver("com.mysql.jdbc.Driver");
        assertThat(tc.isDatabaseDriverLoaded("com.mysql.jdbc.Driver"), is(true));
        
        // create basic table using mysql "cds" database
        DbmsTableImpl t = new DbmsTableImpl("jdbc:mysql://localhost/cds?user=davids&password=mysql", 
                                            "select * from emp order by ename");
        assertThat(t, notNullValue());
        
        Object value = t.getCellValue(t.getRow(Access.ByIndex, 2), t.getColumn(Access.First));
        assertThat(value, notNullValue());
        
        Column c = t.addColumn();
        c.setDerivation("col 1 * col 1");
        
        for (int i = 1; i < t.getNumRows(); i++) {
            Row r = t.getRow(Access.ByIndex, i);
            Object dbValue = t.getCellValue(r, t.getColumn(Access.First));
            Object derivedValue = t.getCellValue(r, c);
            
            assertThat(dbValue instanceof Number, is(true));
            assertThat(derivedValue instanceof Number, is(true));
            
            assertThat((double)derivedValue, is((double)dbValue * (double)dbValue));            
        }
    }

}
