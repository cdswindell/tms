package org.tms.api.utils.es;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Map;

import org.apache.http.HttpHost;
import org.elasticsearch.action.admin.indices.create.CreateIndexRequest;
import org.elasticsearch.action.admin.indices.create.CreateIndexResponse;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexResponse;
import org.elasticsearch.action.admin.indices.get.GetIndexRequest;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.common.xcontent.XContentType;
import org.tms.api.Table;

public class ElasticSearchClient 
{
	public static String whitespace_chars =  ""       /* dummy empty string for homogeneity */
            + "\\u0009" // CHARACTER TABULATION
            + "\\u000A" // LINE FEED (LF)
            + "\\u000B" // LINE TABULATION
            + "\\u000C" // FORM FEED (FF)
            + "\\u000D" // CARRIAGE RETURN (CR)
            + "\\u0020" // SPACE
            + "\\u0085" // NEXT LINE (NEL) 
            + "\\u00A0" // NO-BREAK SPACE
            + "\\u1680" // OGHAM SPACE MARK
            + "\\u180E" // MONGOLIAN VOWEL SEPARATOR
            + "\\u2000" // EN QUAD 
            + "\\u2001" // EM QUAD 
            + "\\u2002" // EN SPACE
            + "\\u2003" // EM SPACE
            + "\\u2004" // THREE-PER-EM SPACE
            + "\\u2005" // FOUR-PER-EM SPACE
            + "\\u2006" // SIX-PER-EM SPACE
            + "\\u2007" // FIGURE SPACE
            + "\\u2008" // PUNCTUATION SPACE
            + "\\u2009" // THIN SPACE
            + "\\u200A" // HAIR SPACE
            + "\\u2028" // LINE SEPARATOR
            + "\\u2029" // PARAGRAPH SEPARATOR
            + "\\u202F" // NARROW NO-BREAK SPACE
            + "\\u205F" // MEDIUM MATHEMATICAL SPACE
            + "\\u3000" // IDEOGRAPHIC SPACE
            ;        
	/* A \s that actually works for Java’s native character set: Unicode */
	protected static String whitespace_charclass = "["  + whitespace_chars + "]";    
	
	/* A \S that actually works for  Java’s native character set: Unicode */
	protected static String not_whitespace_charclass = "[^" + whitespace_chars + "]";

	protected static RestHighLevelClient build(ESCOptions opts) 
	{
		RestHighLevelClient client = new RestHighLevelClient(
				RestClient.builder(
		                new HttpHost(opts.getServer(), opts.getPort(), "http"),
		                new HttpHost(opts.getServer(), opts.getPort()+1, "http")));
		
		return client;
	}
	
	public static void delete(String index, String type, String id, ESCOptions opts) throws IOException
	{
		if (opts == null)
			opts = ESCOptions.Default;
		
		RestHighLevelClient client = build(opts);		
		try {
			DeleteRequest dReq = new DeleteRequest(index, type, id);
			
			DeleteResponse dResp = client.delete(dReq, RequestOptions.DEFAULT);
			if (dResp != null) {
				
			}
		}
		finally {
			if (client != null)
				client.close();
		}
	}
	
	public static int bulkLoad(Table table, String index, ESCOptions opts) 
	throws IOException
	{
		if (opts == null)
			opts = ESCOptions.Default;
		
		if (index == null) {
			index = table.getLabel();
			if (index != null) {
				index = index.trim().toLowerCase();
				index = index.replaceAll(whitespace_charclass, "_");
			}
		}
		
		// recreate index, if requested
		if (opts.isRecreateIndex() && existsIndex(index, opts)) {
			deleteIndex(index, opts);
			createIndex(index, opts);
		}
		
		ByteArrayOutputStream baos = null;
		RestHighLevelClient client = build(opts);		
		try {			
	    	baos = new ByteArrayOutputStream();
			table.export(baos, opts.asESOptions().withType(null).withIndex(null)); // clear out index & type as we give it on load command
			
			BulkRequest req = new BulkRequest();
			
			req.add(baos.toByteArray(), 0, baos.size(), index, opts.getWorkingType(), XContentType.JSON);
			BulkResponse resp = client.bulk(req, RequestOptions.DEFAULT);
			int numLoaded = resp.getItems().length;
			return numLoaded;
		}
		finally {
			if (baos != null)
				baos.close();
			
			if (client != null)
				client.close();
		}
	}
	
	public static boolean deleteIndex(String index) throws IOException
	{
		return deleteIndex(index, ESCOptions.Default);
	}
	
	public static boolean deleteIndex(String index, ESCOptions opts) throws IOException
	{
		if (opts == null)
			opts = ESCOptions.Default;
		
		RestHighLevelClient client = build(opts);		
		try {
			DeleteIndexRequest req = new DeleteIndexRequest(index);
			
			DeleteIndexResponse resp = client.indices().delete(req, RequestOptions.DEFAULT);
			
			return resp != null && resp.isAcknowledged();
		}
		finally {
			if (client != null)
				client.close();
		}
	}
	
	public static boolean existsIndex(String index) throws IOException
	{
		return existsIndex(index, ESCOptions.Default);
	}
	
	public static boolean existsIndex(String index, ESCOptions opts) throws IOException
	{
		if (opts == null)
			opts = ESCOptions.Default;
		
		RestHighLevelClient client = build(opts);		
		try {
			GetIndexRequest gReq = new GetIndexRequest();
			gReq.indices(index);
			
			boolean exists = client.indices().exists(gReq, RequestOptions.DEFAULT);
			
			return exists;
		}
		finally {
			if (client != null)
				client.close();
		}
	}
	
	public static boolean createIndex(String index) throws IOException
	{
		return createIndex(index, null, ESCOptions.Default);
	}
	
	public static boolean createIndex(String index, ESCOptions opts) throws IOException
	{
		return createIndex(index, opts.getMappings(), opts);
	}
	
	public static boolean createIndex(String index, Map<String, String> mapping, ESCOptions opts) throws IOException
	{
		if (opts == null)
			opts = ESCOptions.Default;
		
		RestHighLevelClient client = build(opts);		
		try {
			CreateIndexRequest req = new CreateIndexRequest(index); 
			req.settings(Settings.builder() 
				    .put("index.number_of_shards", (int)opts.getShards())
				    );
			
			// build mappings, if they exist
			if (opts.isMappings() || opts.isCatchAllField())
				req.mapping(opts.getWorkingType(), getMappingsAsBuilder(opts));
			
			CreateIndexResponse resp = client.indices().create(req, RequestOptions.DEFAULT);
			
			return resp.isAcknowledged();
		}
		finally {
			if (client != null)
				client.close();
		}
	}

	private static XContentBuilder getMappingsAsBuilder(ESCOptions opts) 
	throws IOException 
	{
		XContentBuilder builder = XContentFactory.jsonBuilder();
	    builder.startObject();
	    {
		    builder.startObject(opts.getWorkingType());
		    {
		    	if (opts.isCatchAllField()) {
			        builder.startArray("dynamic_templates");
			        builder.startObject();
			        {
				        builder.startObject("strings");
				        {
		        			builder.field("match_mapping_type", "string");
					        builder.startObject("mapping");
					        {
			        			builder.field("type", "text");
			        			builder.field("copy_to", opts.getCatchAllField());
					        }
					        builder.endObject();				        	
				        }
				        builder.endObject();
			        }		        
			        builder.endObject();
			        builder.endArray();
		    	}
		    	
		    	if (opts.isMappings() || opts.isCatchAllField()) {
			        builder.startObject("properties");
			        {
			        	for (Map.Entry<String, String> t : opts.getMappings().entrySet()) {
			        		builder.startObject(t.getKey());
			        		{
			        			builder.field("type", t.getValue());
			        		}
					        builder.endObject();
			        	}
			        	
			        	if (opts.isCatchAllField()) {
			        		builder.startObject(opts.getCatchAllField());
		        			builder.field("type", "text");
		        			builder.field("term_vector", "with_positions_offsets_payloads");			        		
		        			builder.field("store", "true");
			        		builder.endObject();
			        	}
			        }
			        builder.endObject();
		    	}
		    }
		    builder.endObject();
	    }
	    builder.endObject();

		return builder;
	}
}
