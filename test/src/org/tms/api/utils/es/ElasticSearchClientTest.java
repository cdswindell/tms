package org.tms.api.utils.es;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.util.UUID;

import org.elasticsearch.ElasticsearchStatusException;
import org.elasticsearch.rest.RestStatus;
import org.junit.Test;
import org.tms.BaseTest;
import org.tms.api.Access;
import org.tms.api.Column;
import org.tms.api.Table;
import org.tms.api.factories.TableFactory;
import org.tms.api.io.CSVOptions;
import org.tms.api.utils.googleapis.ToLatLongStrOp;
import org.tms.tds.TableImpl;

public class ElasticSearchClientTest extends BaseTest 
{
	@Test
	public void testExistsIndex1() 
	{
		try {
			boolean exists = ElasticSearchClient.existsIndex(UUID.randomUUID().toString(), ESCOptions.Default);
			assertThat(exists, is(false));
		} 
		catch (IOException e) {
			fail(e.getMessage());
		}
	}

	@Test
	public void testCreateIndex1() 
	{
		try {			
			String index = UUID.randomUUID().toString();
			assertNotNull(index);
			
			// index shouldn't exist
			boolean exists = ElasticSearchClient.existsIndex(index);
			assertThat(exists, is(false));			
			
			// define some mappings
			ESCOptions opts = ESCOptions.Default;
			opts = opts.addMapping("location", "geo_point");
			opts = opts.addMapping("zip_code", "text");
			
			// create index
			boolean ack = ElasticSearchClient.createIndex(index, opts);
			assertThat(ack, is(true));
			
			// index should exist
			exists = ElasticSearchClient.existsIndex(index, opts);
			assertThat(exists, is(true));
			
			// shouldn't be able to create it again
			try {
				ack = ElasticSearchClient.createIndex(index, opts);
				fail("index created");
			}
			catch (ElasticsearchStatusException e) {
				assertNotNull(e);
				assertThat(e.status(), is(RestStatus.BAD_REQUEST));
				assertNotNull(e.getIndex());
				assertThat(index, is(e.getIndex().getName()));
			}	
			
			// delete created index
			ack = ElasticSearchClient.deleteIndex(index, opts);
			assertThat(ack, is(true));
			
			// finally, make sure index doesn't exist anymore
			exists = ElasticSearchClient.existsIndex(index, opts);
			assertThat(exists, is(false));
		} 
		catch (IOException e) {
			fail(e.getMessage());
		}
	}
	
	@Test
	public void testBulkLoad() 
	{
		try {			
			String index = UUID.randomUUID().toString();
			assertNotNull(index);
			
			// create default indices
			ESCOptions opts = ESCOptions.Default.withType("myDoc");
			
			// create table
			Table t = TableFactory.createTable();
			
			Column c1 = t.addColumn(Access.ByLabel, "c1");
			Column c2 = t.addColumn(Access.ByLabel, "c2");
			c2.setDerivation("c1 * c1");
			
			int maxRows = 10;
			for (int i = 1; i <= maxRows; i++) {
				t.setCellValue(t.addRow(i), c1, i);
			}
			assertThat(t.getNumRows(), is(maxRows));
			
			// bulk load table
			int numItems = ElasticSearchClient.bulkLoad(t, index, opts);
			assertThat(numItems, is(maxRows));
			
			// check that index exists
			boolean exists = ElasticSearchClient.existsIndex(index);
			assertThat(exists, is(true));	
			
			// delete created index
			boolean ack = ElasticSearchClient.deleteIndex(index, opts);
			assertThat(ack, is(true));
			
			// finally, make sure index doesn't exist anymore
			exists = ElasticSearchClient.existsIndex(index, opts);
			assertThat(exists, is(false));
		} 
		catch (IOException e) {
			fail(e.getMessage());
		}
	}
	
	@Test
	public void testBulkLoadProfiles() throws IOException 
	{		
		Table p = TableFactory.importFile("profiles.csv",CSVOptions.Default.withColumnLabels().withRowLabels(false));
		p.getTableContext().registerOperator(new ToLatLongStrOp());
		Column id = p.getColumn("id");
		Column ll = p.addColumn(Access.ByLabel,"location");
		
		((TableImpl)p).setPendingThreadPoolEnabled(true);
		((TableImpl)p).setPendingMaximumPoolSize(2);
		ll.setDerivation("toLatLong(zip_code)");
		
		p.getColumn("lat").delete();
		p.getColumn("lng").delete();
		
		int numLoaded = ElasticSearchClient.bulkLoad(p, "profile",ESCOptions.Default.withIdColumn(id).withCatchAllField("all_text").addMapping("location", "geo_point"));
		assertThat(numLoaded > 1, is(true));
	}

}
