package org.tms.io;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.tms.api.Cell;
import org.tms.api.Column;
import org.tms.api.ElementType;
import org.tms.api.Row;
import org.tms.api.Subset;
import org.tms.api.Table;
import org.tms.api.TableElement;
import org.tms.api.TableProperty;
import org.tms.api.derivables.Derivation;
import org.tms.api.io.JSONOptions;

import scala.actors.threadpool.Arrays;

public class JSONWriter extends ArchivalWriter<JSONOptions>
{
    public static void export(TableExportAdapter tea, OutputStream out, JSONOptions options) 
    throws IOException
    {
        JSONWriter writer = new JSONWriter(tea, out, options);
        writer.export(); 
        writer.flushOutput();
    }
    
    private JSONWriter(TableExportAdapter t, OutputStream out, JSONOptions options)
    {
        super(t, out, options);
    }

    @SuppressWarnings("unchecked")
	@Override
    protected void export() throws IOException
    {
    	JSONObject tblJson = new JSONObject();  	
    	
    	// process table metadata
    	exportTableMetadata(tblJson);
    	
    	// Rows
    	exportRows(tblJson);
    	
    	// Columns
    	exportColumns(tblJson);
    	
    	// Subsets
    	exportSubsets(tblJson);
    	
    	// Cells
    	exportCells(tblJson);
    	
    	// create the top-level item
    	JSONObject root = new JSONObject();    	
    	root.put(ElementType.Table.toString(), tblJson);
    	
    	// serialize output
    	root.writeJSONString(getOutputWriter());  	
    }

	@SuppressWarnings("unchecked")
	protected Object getProperty(TableElement te, TableProperty tp) 
	{
		Object val = super.getProperty(te, tp);
		if (val != null) {
			switch (tp) {			
				case Derivation:
					val = ((Derivation)val).getExpression();
					break;
					
				case DataType:
					val = ((Class<?>)val).getCanonicalName();
					break;
					
				case Tags:
					JSONArray ja = new JSONArray();
					ja.addAll(Arrays.asList((String [])val));
					val = ja;
					break;
				
				case Validator:
					val = null; // Validators aren't handled
					break;
				
				default:
					break;
				
			}
		}
		
		return val;
	}

	@SuppressWarnings("unchecked")
	protected void exportTableMetadata(JSONObject tblJson) 
	{
		if (!isTable())
			return;
		
    	Table t = getTable();    	
		List<TableProperty> tProps = getExportableProperties(ElementType.Table);
		
		// Table metadata
    	JSONObject tMd = new JSONObject();
    	
    	for (TableProperty tp : tProps) {
    		if (hasValue(t, tp))
				tMd.put(tp.getTag(), getProperty(t, tp));
    	}
    	
    	if (!tMd.isEmpty())
    		tblJson.put("metadata", tMd);
	}

	@SuppressWarnings("unchecked")
	protected void exportRows(JSONObject tblJson) 
	{
    	JSONArray tRowsList = new JSONArray();
    	
		List<TableProperty> rProps = getExportableProperties(ElementType.Row);
		
        int nRows = getNumConsumableRows();
        for (int i = 1; i <= nRows; i++) { 
        	Row r = getRowByEffectiveIndex(i);
        	if (r != null) {
        		JSONObject rJson = new JSONObject();
        		
            	// add metadata
            	for (TableProperty p : rProps) {
            		if (hasValue(r, p))
            			rJson.put(p.getTag(), getProperty(r, p));
            	}
            	
            	// if any data written, include row
            	if (!rJson.isEmpty()) {
            		rJson.put("idx", i);
            		tRowsList.add(rJson);
            	}
        	}        	
        }
    	
        // finally, include entire rows object
    	if (!tRowsList.isEmpty()) 
    		tblJson.put("rows", tRowsList);    	
	}

	@SuppressWarnings("unchecked")
	protected void exportColumns(JSONObject tblJson) 
	{
    	JSONArray tColsList = new JSONArray();
    	
		List<TableProperty> cProps = getExportableProperties(ElementType.Column);
        for (Column c : getActiveColumns()) { 
        	if (c != null) {
        		JSONObject cJson = new JSONObject();
        		
            	// add metadata
            	for (TableProperty p : cProps) {
            		if (hasValue(c, p))
            			cJson.put(p.getTag(), getProperty(c, p));
            	}
            	
            	// if any data written, include column
            	if (!cJson.isEmpty()) {
            		cJson.put("idx", getRemappedColumnIndex(c));
            		tColsList.add(cJson);
            	}
        	}        	
        }
    	
        // finally, include entire rows object
    	if (!tColsList.isEmpty()) 
    		tblJson.put("cols", tColsList);    	
	}
	
	@SuppressWarnings("unchecked")
	private void exportSubsets(JSONObject tblJson) 
	{
		if (!isTable())
			return;
		
    	Table t = getTable();    	
    	JSONArray tSetsList = new JSONArray();
		List<TableProperty> sProps = getExportableProperties(ElementType.Table);
		
		for (Subset s : t.getSubsets()) {
    		JSONObject sJson = new JSONObject();
    		
        	// add metadata
        	for (TableProperty p : sProps) {
        		if (hasValue(s, p))
        			sJson.put(p.getTag(), getProperty(s, p));
        	}
    		
        	// encode the rows
        	List<Integer> rows = new ArrayList<Integer>(this.getNumConsumableRows());
        	for (Row r : s.getRows()) {
        		if (this.isIgnore(r)) continue;
        		
        		rows.add(getRemappedRowIndex(r));
        	}
        	if (!rows.isEmpty())
        		sJson.put("rows", rows);
        	
        	// encode the columns
        	List<Integer> cols = new ArrayList<Integer>(this.getNumConsumableColumns());
        	for (Column c : s.getColumns()) {
        		if (this.isIgnore(c)) continue;
        		
        		cols.add(getRemappedColumnIndex(c));
        	}
        	
        	if (!cols.isEmpty())
        		sJson.put("cols", cols);
        	
        	// if any data written, include subset
        	if (!sJson.isEmpty()) {
        		tSetsList.add(sJson);
        	}
		}
		
        // finally, include entire rows object
    	if (!tSetsList.isEmpty()) 
    		tblJson.put("subsets", tSetsList);    			
	}

	@SuppressWarnings("unchecked")
	private void exportCells(JSONObject tblJson) 
	{
    	Table t = getTable();    	
    	JSONArray tCellsList = new JSONArray();
		List<TableProperty> cProps = getExportableProperties(ElementType.Cell);
    	
        for (Column c : getActiveColumns()) {
            for (Row r : getActiveRows()) {
            	
                if (t.isCellDefined(r, c)) {
                	
                	Cell cell = t.getCell(r,  c);
                	if (cell != null) {
                		JSONObject cJson = new JSONObject();
                		
                    	// add metadata
                    	for (TableProperty p : cProps) {
                    		if (hasValue(cell, p))
                    			cJson.put(p.getTag(), getProperty(cell, p));
                    	}
                    	
                		Object cv = cell.getCellValue();
                		if (cv != null)
                			cJson.put(TableProperty.CellValue.getTag(), cv);
                	
	                	if (!cJson.isEmpty()) {
	                		cJson.put("r", getRemappedRowIndex(r));
	                		cJson.put("c", getRemappedColumnIndex(c));
	                		tCellsList.add(cJson);
	                	}
                	}
                }
            }
        }

        // finally, include entire cells object
    	if (!tCellsList.isEmpty()) 
    		tblJson.put("cells", tCellsList);    			
	}
}
