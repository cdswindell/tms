package org.tms.tds.dbms;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.junit.Test;
import org.tms.api.Access;
import org.tms.api.TableContext;
import org.tms.api.factories.TableContextFactory;

public class SalesForceTest extends BaseDbmsTest 
{
	@Test
	public final void testCreateSalesForceOppTable() throws ClassNotFoundException, SQLException, IOException 
	{
	    TableContext tc = TableContextFactory.fetchDefaultTableContext();
	    assertThat(tc, notNullValue());
		
        tc.loadDatabaseDriver("com.reliersoft.sforce.jdbc.Driver");
        assertThat(tc.isDatabaseDriverLoaded("com.reliersoft.sforce.jdbc.Driver"), is(true));

	
        // count the number of expected rows and columns
        ResultSet rs = fetchResultSet("jdbc:sforce://login.salesforce.com:443?user=dave@soundadviceservices.com&password=voB-Wxb-4e2-MNM&token=lVuMLR32aYLrdfJ4t61TYi62x", 
                                      "select a.Name \"Account\", o.Name \"Opportunity\", o.amount, o.closeDate, o.type from opportunity o Inner Join Account a on o.AccountId = a.Id where o.isClosed = true");
        
        int numRows = (int)getDbmsRowCount(rs);
        int numCols = getDbmsColumnCount(rs);
        close(rs);
        
        DbmsTableImpl t = new DbmsTableImpl("jdbc:sforce://login.salesforce.com:443?user=dave@soundadviceservices.com&password=voB-Wxb-4e2-MNM&token=lVuMLR32aYLrdfJ4t61TYi62x", 
                				"select a.Name \"Account\", o.Name \"Opportunity\", o.amount, o.closeDate, o.type from opportunity o Inner Join Account a on o.AccountId = a.Id where o.isClosed = true");
		assertThat(t, notNullValue());
		assertThat(t.getNumRows(), is(numRows));
		assertThat(t.getNumColumns(), is(numCols));
	
        Object value = t.getCellValue(t.getRow(Access.ByIndex, 2), t.getColumn(Access.First));
        assertThat(value, notNullValue());
        assertThat(value, is("GenePoint"));
//        
//        t.setLabel("SalesForce Opportunities");
//        t.export("salesforce.tms");
//        
//        Table tsf = TableFactory.importFile("salesforce.tms");
//		assertThat(tsf, notNullValue());
	}  
}
