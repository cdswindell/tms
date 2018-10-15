package org.tms.io;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.json.simple.JSONObject;
import org.tms.api.Column;
import org.tms.api.Row;
import org.tms.api.Table;
import org.tms.api.exceptions.IllegalTableStateException;
import org.tms.api.io.ESOptions;

public class ESWriter extends BaseWriter<ESOptions>
{
    public static void export(TableExportAdapter tea, OutputStream out, ESOptions options) 
    throws IOException
    {
        ESWriter writer = new ESWriter(tea, out, options);
        writer.export();        
    }
    
    private Map<Column, String> m_fieldNameMap = new HashMap<Column, String>();
    
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
    	
    	try {   	
	    	Table t = getTable();
	    	
	    	Column idCol = options().getIdColumn();
			JSONObject data;
			Object cellValue;
			int ordinalId = 0;
	    	for (Row r: getActiveRows()) {  		
	    		data = new JSONObject();
	    		int fieldNo = 0;
	    		
	    		for (Column c: getActiveColumns()) {
	    			if (c == idCol) continue;  
	    			
	    			fieldNo++;
	    			
	    			if (t.isCellDefined(r, c)) {
		    			cellValue = (r.getCell(c)).getCellValue();
		    			if (cellValue != null || !options().isIgnoreEmptyCells())
		    				data.put(serializeFieldName(c, fieldNo), serializelValue(cellValue)); 
	    			}
	    		}  	
	    		
	    		//write the data row and trailing newline  		
	    		if (!data.isEmpty()) {
	    			try {
		        		writeIndex(r, idCol, ++ordinalId).writeJSONString(bw);
		    			bw.newLine();
	    			}
	    			catch (HaltOnNullIdException e) {
	    				if (options().isExceptionOnEmptyIs()) 
	    					throw new IllegalTableStateException(e.getMessage());	    				
	    			}
	    			catch (SkipNullIdException s) {
	    				continue;
	    			}
	       		
	        		data.writeJSONString(bw);   		
	        		bw.newLine();
	    		}
	    	}
    	}
    	finally {
    		// close buffered writer
    		bw.close();
    	}
    }

	@SuppressWarnings("unchecked")
	protected JSONObject writeIndex(Row r, Column idCol, int ordinal)
	throws IOException 
	{
		Object cellValue;
		JSONObject idx = new JSONObject();
		JSONObject idxContent = new JSONObject();
		
    	Table t = getTable();
    	String index = options().getIndex();
    	String docType = options().getType();
		
    	if (options().isIdOrdinal()) 
			idxContent.put("_id", ordinal);
    	else if (options().isIdUuid()) 
			idxContent.put("_id", UUID.randomUUID().toString());
    	else if (idCol != null) {
			if (t.isCellDefined(r, idCol)) {	
				cellValue = (r.getCell(idCol)).getCellValue();
				
				if (cellValue == null) {
					if (options().isOmitRecordsWithEmptyIds())
						throw new SkipNullIdException(r, idCol);
					else if (options().isExceptionOnEmptyIs())
						throw new HaltOnNullIdException(r, idCol);
				}
				else {
					;
				
					idxContent.put("_id", serializelValue(cellValue));
				}
			}
		}
		
		if (index != null)
			idxContent.put("_index", index);
		
		if (docType != null)
			idxContent.put("_doc", docType);
		
		idx.put("index", idxContent);
		
		return idx;
	}

	private Object serializeFieldName(Column c, int fieldNo) 
	{
		String fName = m_fieldNameMap.get(c);
		if (fName == null) {
			fName = c.getLabel();
			if (fName == null || (fName = fName.trim()).length() == 0) 
				fName = String.format("field_%d", fieldNo); 
			else {
				if (options().isLowerCaseFieldNames())
					fName = fName.toLowerCase();
				
				// translate whitespace to underscores
				fName.replaceAll("\\s", "_");
			}
			
			m_fieldNameMap.put(c,  fName);
		}			
		
		return fName;
	}

	private Object serializelValue(Object val) 
	{
		if (val == null)
			return val;
		if (val instanceof String || val instanceof Number || val instanceof Boolean)
			return val;
		
		//TODO handle other classes
		return val.toString();
	}    
	
	/*
	 * Inner Classes
	 */
	class HaltOnNullIdException extends IllegalTableStateException
	{
		private static final long serialVersionUID = 7798845629886149052L;

		public HaltOnNullIdException(Row r, Column c) 
		{
			super(String.format("Row %d Column %d: Null ID value not allowed here", r.getIndex(), c.getIndex()));
		}
	}
	
	class SkipNullIdException extends IllegalTableStateException
	{
		private static final long serialVersionUID = 7798845629886149052L;

		public SkipNullIdException(Row r, Column c) 
		{
			super(String.format("Row %d Column %d: Omiting record with null ID value", r.getIndex(), c.getIndex()));
		}
	}
}
