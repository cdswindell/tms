package org.tms.io;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

import org.json.simple.JSONObject;
import org.tms.api.Cell;
import org.tms.api.Column;
import org.tms.api.Row;
import org.tms.api.io.ESOptions;

public class ESWriter extends BaseWriter<ESOptions>
{
    public static void export(TableExportAdapter tea, OutputStream out, ESOptions options) 
    throws IOException
    {
        ESWriter writer = new ESWriter(tea, out, options);
        writer.export();        
    }
    
    /*
     * Constructors
     */
    private ESWriter(TableExportAdapter t, OutputStream out, ESOptions options)
    {
        super(t, out, options);
    }

    /*
     * Methods
     */
    @SuppressWarnings("unchecked")
	@Override
    protected void export() throws IOException
    {
    	BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(getOutputStream(), "utf-8"));
    	
    	String index = options().getIndex();
    	String docType = options().getType();
    	
    	Column idCol = options().getId();
		JSONObject idx, idxContent, data;
		Cell cell;
    	for (Row r: getActiveRows()) {
    		idx = new JSONObject();
    		idxContent = new JSONObject();
    		
    		if (idCol != null) {
    			cell = r.getCell(idCol);
    			if (cell != null) 
    				idxContent.put("_id", serializeCellValue(cell));
    		}
    		
    		if (index != null)
    			idxContent.put("_index", index);
    		
    		if (docType != null)
    			idxContent.put("_doc", docType);
    		
    		idx.put("index", idxContent);
    		idx.writeJSONString(bw);
    		bw.newLine();
    		
    		data = new JSONObject();
    		for (Column c: getActiveColumns()) {
    			if (c == idCol) continue;   			
    			data.put(serializeFieldName(c), serializeCellValue(r.getCell(c)));   			
    		}  	
    		
    		//write the data row and trailing newline
    		data.writeJSONString(bw);   		
    		bw.newLine();
    	}
    	
    	// close buffered writer
    	bw.close();
    }

	private Object serializeFieldName(Column c) 
	{
		String fName = c.getLabel();
		return fName;
	}

	private Object serializeCellValue(Cell cell) 
	{
		Object val = cell == null ? null : cell.getCellValue();
		
		if (val == null)
			return val;
		if (val instanceof String || val instanceof Number || val instanceof Boolean)
			return val;
		
		//TODO handle other classes
		return val.toString();
	}    
}
