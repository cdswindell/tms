package org.tms.io;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.validator.routines.EmailValidator;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.tms.api.Cell;
import org.tms.api.Column;
import org.tms.api.Row;
import org.tms.api.Table;
import org.tms.api.exceptions.IllegalTableStateException;
import org.tms.api.io.ESOptions;

public class ESWriter extends BaseWriter<ESOptions>
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
	protected static String whitespace_delim_charclass = "["  + whitespace_chars + "\\p{Punct}" + "]";    
	
	/* A \S that actually works for  Java’s native character set: Unicode */
	protected static String not_whitespace_charclass = "[^" + whitespace_chars + "]";

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
	    	
	    	Cell cell;
	    	Column idCol = options().getIdColumn();
			JSONObject data;
			int ordinalId = 0;
			
			// make access to completions columns more efficient
			Set<Column> compCols = options().isCompletions() ? new LinkedHashSet<Column>(options().getCompletions()) : null;
			Set<String> completions = new LinkedHashSet<String>();
			
	    	for (Row r: getActiveRows()) {  	
	    		data = new JSONObject();
	    		int fieldNo = 0;
	    		completions.clear();
	    		
	    		for (Column c: getActiveColumns()) {
	    			if (c == idCol) continue;  
	    			
	    			fieldNo++;
	    			
	    			if (t.isCellDefined(r, c)) {
	    				cell = r.getCell(c);
	    				
	    				if (cell.isErrorValue()) continue; // skip error cells
	    				if (cell.isNull() && options().isIgnoreEmptyCells()) continue; // skip null/empty cells, if so instructed
	    				
	    				// write cell value to record
	    				data.put(serializeFieldName(c, fieldNo), serializeCellValue(cell)); 
	    				
	    				// if completion column, process data
	    				if (compCols != null && !cell.isNull() && compCols.contains(c))
	    					cacheCompletions(cell, completions);	    					
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
	       		
	    			if (compCols != null && !completions.isEmpty())
	    				data.put(options().getCompletionField(), serializeCompletions(completions));
	    			
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
	private JSONObject serializeCompletions(Set<String> completions) 
	{
		JSONObject input = new JSONObject();
		
		// convert completions to JSONArray
		JSONArray inputs = new JSONArray();
		inputs.addAll(completions);
		
		input.put("input", inputs);
		
		return input;		
	}

	private void cacheCompletions(Cell cell, Set<String> completions) 
	{
		// get cell value as String
		String scv = cell.getCellValue().toString();
		
		String [] tokens = scv.split(whitespace_charclass);
		for (String s : tokens) {
			if (s != null && (s=s.trim().toLowerCase()).length() > 0) {
				if (EmailValidator.getInstance().isValid(s))
					completions.add(s);
				else {
					String tks[] = s.split("\\p{Punct}");
					for (String s1 : tks) {
						if (s1 != null && (s1=s1.trim()).length() > 0)
							completions.add(s1);
					}						
				}
			}
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
				fName = fName.replaceAll(whitespace_charclass, "_");
			}
			
			m_fieldNameMap.put(c,  fName);
		}			
		
		return fName;
	}

	private Object serializeCellValue(Cell cell) 
	{
		return serializelValue(cell.getCellValue());
	}
	
	private Object serializelValue(Object val) 
	{
		if (val == null)
			return val;
		if (val instanceof Number || val instanceof Boolean || val instanceof JSONObject)
			return val;
		
		//TODO handle other classes
		String sVal = val.toString();
		sVal = sVal.replaceAll(whitespace_charclass, " ");
		return sVal;
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
