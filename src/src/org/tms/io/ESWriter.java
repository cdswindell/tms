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
		Map <Object, Row> rowIdMap = new HashMap<Object, Row>(getNumActiveRows());
    	
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
		        		writeIndex(r, idCol, ++ordinalId, rowIdMap).writeJSONString(bw);
		    			bw.newLine();
	    			}
	    			catch (HaltOnInvalidIdException e) {
	    				throw new IllegalTableStateException(e.getMessage());	    				
	    			}
	    			catch (SkipInvalidIdException s) {
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
    		
    		rowIdMap.clear();
    		rowIdMap = null;
    	}
    }

	@SuppressWarnings("unchecked")
	protected JSONObject writeIndex(Row r, Column idCol, int ordinal, Map<Object, Row> rowIdMap)
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
						throw new SkipInvalidIdException("Row %d Column %d: Omiting record with null ID value", r, idCol);
					else if (options().isExceptionOnEmptyIds())
						throw new HaltOnInvalidIdException("Row %d Column %d: Null ID value not allowed here", r, idCol);
				}
				else {
					Object id = serializelValue(cellValue);
					if (options().isExceptionOnDuplicatdeIds() || options().isOmitRecordsWithDuplicateIds()) {
						if (null != rowIdMap.put(id, r)) {
							if (options().isOmitRecordsWithDuplicateIds())
								throw new SkipInvalidIdException("Row %d Column %d: Omiting record with duplicate ID value", r, idCol);
							else if (options().isExceptionOnDuplicatdeIds())
								throw new HaltOnInvalidIdException("Row %d Column %d: Duplicte ID value not allowed here", r, idCol);
						}
					}
				
					idxContent.put("_id", id);
				}
			}
		}
		
		if (index != null)
			idxContent.put("_index", index);
		
		if (docType != null)
			idxContent.put("_type", docType);
		
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
	class HaltOnInvalidIdException extends IllegalTableStateException
	{
		private static final long serialVersionUID = -4858243560467314573L;

		public HaltOnInvalidIdException(String msg, Row r, Column c) 
		{
			super(String.format(msg , r.getIndex(), c.getIndex()));
		}
	}
	
	class SkipInvalidIdException extends IllegalTableStateException
	{
		private static final long serialVersionUID = -9193966239451018026L;

		public SkipInvalidIdException(String msg, Row r, Column c) 
		{
			super(String.format(msg, r.getIndex(), c.getIndex()));
		}
	}
}
