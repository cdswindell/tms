package org.tms.io;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.tms.api.Column;
import org.tms.api.ElementType;
import org.tms.api.Row;
import org.tms.api.Subset;
import org.tms.api.Table;
import org.tms.api.TableProperty;
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
    	Table t = getTable();    	
    	JSONObject tblJson = new JSONObject();  	
    	
    	// process table metadata
    	exportTableMetadata(t, tblJson);
    	
    	// Rows
    	exportRows(t, tblJson);
    	
    	// Columns
    	exportColumns(t, tblJson);
    	
    	// Subsets
    	exportSubsets(t, tblJson);
    	
    	// Cells
    	exportCells(t, tblJson);
    	
    	// create the top-level item
    	JSONObject root = new JSONObject();    	
    	root.put(ElementType.Table.toString(), tblJson);
    	
    	// serialize output
    	root.writeJSONString(getOutputWriter());  	
    }

	@SuppressWarnings("unchecked")
	protected void exportTableMetadata(Table t, JSONObject tblJson) 
	{
		List<TableProperty> tProps = (options().isVerboseState() ? ElementType.Table.getMutableProperties():
    		Arrays.asList(new TableProperty[] {TableProperty.Label, TableProperty.Description}) );
    	
    	// Table metadata
    	JSONObject tMd = new JSONObject();
    	
    	for (TableProperty tp : tProps) {
    		if (hasValue(t, tp))
				tMd.put(tp.toString(), getTable().getProperty(tp));
    	}
    	
    	if (!tMd.isEmpty())
    		tblJson.put("metadata", tMd);
	}
	
	@SuppressWarnings("unchecked")
	protected void exportRows(Table t, JSONObject tblJson) 
	{
    	JSONArray tRowsList = new JSONArray();
    	
		List<TableProperty> rProps = (options().isVerboseState() ? ElementType.Row.getMutableProperties():
    		Arrays.asList(new TableProperty[] {TableProperty.Label}) );
		
		// don't export row labels, if not requested
		if (!options().isRowLabels())
			rProps.remove(TableProperty.Label);
		
		// handle derivations separately
		rProps.remove(TableProperty.Derivation);
		
        int nRows = getNumConsumableRows();
        for (int i = 1; i <= nRows; i++) { 
        	Row r = getRowByEffectiveIndex(i);
        	if (r != null) {
        		JSONObject rJson = new JSONObject();
        		
            	// add metadata
            	for (TableProperty p : rProps) {
            		if (hasValue(r, p))
            			rJson.put(p.toString(), r.getProperty(p));
            	}
            	
            	// add derivations, if requested
            	if (options().isDerivations() && r.isDerived()) 
            		rJson.put("fx", r.getDerivation().getExpression());
            	
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
	protected void exportColumns(Table t, JSONObject tblJson) 
	{
    	JSONArray tColsList = new JSONArray();
    	
		List<TableProperty> rProps = (options().isVerboseState() ? ElementType.Column.getMutableProperties():
    		Arrays.asList(new TableProperty[] {TableProperty.Label}) );
		
		// don't export column labels, if not requested
		if (!options().isColumnLabels())
			rProps.remove(TableProperty.Label);
		
		// handle derivations separately
		rProps.remove(TableProperty.Derivation);
			
        for (Column c : getActiveColumns()) { 
        	if (c != null) {
        		JSONObject cJson = new JSONObject();
        		
            	// add metadata
            	for (TableProperty p : rProps) {
            		if (hasValue(c, p))
            			cJson.put(p.toString(), c.getProperty(p));
            	}
            	
            	// add derivations, if requested
            	if (options().isDerivations() && c.isDerived()) 
            		cJson.put("fx", c.getDerivation().getExpression());
            	
            	// if any data written, include column
            	if (!cJson.isEmpty()) {
            		cJson.put("idx", getRemappedColumnIndex(c));
            		tColsList.add(cJson);
            	}
        	}        	
        }
    	
        // finally, include entire rows object
    	if (!tColsList.isEmpty()) 
    		tblJson.put("columns", tColsList);    	
	}
	
	@SuppressWarnings("unchecked")
	private void exportSubsets(Table t, JSONObject tblJson) 
	{
		if (!isTable())
			return;
		
    	JSONArray tSetsList = new JSONArray();
    	
		List<TableProperty> sProps = (options().isVerboseState() ? ElementType.Subset.getMutableProperties():
    		Arrays.asList(new TableProperty[] {TableProperty.Label}) );
		
		for (Subset s : t.getSubsets()) {
    		JSONObject sJson = new JSONObject();
    		
        	// add metadata
        	for (TableProperty p : sProps) {
        		if (hasValue(s, p))
        			sJson.put(p.toString(), s.getProperty(p));
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
	private void exportCells(Table t, JSONObject tblJson) 
	{
    	JSONArray tCellsList = new JSONArray();
    	
        for (Column c : getActiveColumns()) {
            for (Row r : getActiveRows()) {
                if (t.isCellDefined(r, c)) {
                	Object o = t.getCell(r,  c);
                }
            }
        }

        // finally, include entire cells object
    	if (!tCellsList.isEmpty()) 
    		tblJson.put("cells", tCellsList);    			
	}
}
