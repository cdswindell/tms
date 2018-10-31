package org.tms.api.utils.es;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.util.UUID;

import org.elasticsearch.ElasticsearchStatusException;
import org.elasticsearch.rest.RestStatus;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.junit.Ignore;
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
	
	@Ignore
	@SuppressWarnings("unchecked")
	@Test
	public void testBulkLoadProfiles() throws IOException, InterruptedException 
	{		
		Table p = TableFactory.importFile(qualifiedFileName("profiles.csv", "misc"),CSVOptions.Default.withColumnLabels().withRowLabels(false));
		p.getTableContext().registerOperator(new ToLatLongStrOp());
		Column id = p.getColumn("id");
		Column ll = p.addColumn(Access.ByLabel,"location");
		
		((TableImpl)p).setPendingThreadPoolEnabled(true);
		((TableImpl)p).setPendingMaximumPoolSize(4);
		ll.setDerivation("toLatLong(zip_code)");
		
		p.getColumn("lat").delete();
		p.getColumn("lng").delete();
		
		/*
		 * Define Settings
		 */		
		JSONObject shinglesFilter = new JSONObject();
		shinglesFilter.put("type", "shingle");
		shinglesFilter.put("min_shingle_size", 2);
		shinglesFilter.put("max_shingle_size", 2);
		shinglesFilter.put("output_unigrams", false);
		
		JSONObject synonymFilter = new JSONObject();
		synonymFilter.put("type", "synonym");
		synonymFilter.put("synonyms_path", "synonyms.txt");
		synonymFilter.put("lenient", true);
		
		JSONObject filter = new JSONObject();
		filter.put("shingle_filter", shinglesFilter);		
		filter.put("synonym_filter", synonymFilter);		
		
		JSONObject shinglesAnalyzer = new JSONObject();
		shinglesAnalyzer.put("type", "custom");
		shinglesAnalyzer.put("tokenizer", "standard");
		shinglesAnalyzer.put("stopwords", "_english_");
		JSONArray shingleFilters = new JSONArray();
		shingleFilters.add("standard");
		shingleFilters.add("lowercase");
		shingleFilters.add("stop");
		shingleFilters.add("porter_stem");
		shingleFilters.add("shingle_filter");
		shinglesAnalyzer.put("filter", shingleFilters);
		
		JSONObject foldingAnalyzer = new JSONObject();
		foldingAnalyzer.put("type", "custom");
		foldingAnalyzer.put("tokenizer", "standard");
		foldingAnalyzer.put("stopwords", "_english_");
		JSONArray foldingFilters = new JSONArray();
		foldingFilters.add("standard");
		foldingFilters.add("lowercase");
		foldingFilters.add("stop");
		foldingFilters.add("asciifolding");
		foldingFilters.add("synonym_filter");
		foldingFilters.add("porter_stem");
		foldingAnalyzer.put("filter", foldingFilters);
		
		JSONObject analyzer = new JSONObject();
		analyzer.put("shingle_analyzer", shinglesAnalyzer);
		analyzer.put("folding_stemming_analyzer", foldingAnalyzer);
		
		JSONObject settings = new JSONObject();
		settings.put("filter", filter);
		settings.put("analyzer", analyzer);
				
		/*
		 * Build options object
		 */
		ESCOptions esOpts = ESCOptions.Default.withIdColumn(id);
		esOpts = esOpts.addSetting("analysis", settings);
		esOpts = esOpts.addMapping("location", "geo_point");
		esOpts = esOpts.addMapping("user_id", "keyword");
		esOpts = esOpts.addMapping("all_text", 
								   "type","text", 
								   "term_vector=with_positions_offsets_payloads",
								   "store", true,
								   "fields", "{\"stemmed\":{\"type\":\"text\",\"analyzer\":\"folding_stemming_analyzer\"},\"shingled\":{\"type\":\"text\",\"analyzer\":\"shingle_analyzer\"}}");
		esOpts = esOpts.withCatchAllField("all_text");
		esOpts = esOpts.addCompletion(p.getColumn("Title"));
		esOpts = esOpts.addCompletion(p.getColumn("Categories"));
		esOpts = esOpts.addCompletion(p.getColumn("Description"));
		
		/*
		 * make sure all pendings are complete
		 */
		while (p.isPendings())
			Thread.sleep(250);
		
		p.getColumn("zip_code").delete();
		
		/*
		 * perform bulk load
		 */
		int numLoaded = ElasticSearchClient.bulkLoad(p, "profile", esOpts);
		assertThat(numLoaded, is(437));
	}
}
