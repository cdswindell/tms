package org.tms.io;

import java.io.IOException;
import java.io.OutputStream;

import org.json.simple.JSONObject;
import org.tms.api.ElementType;
import org.tms.api.Table;
import org.tms.api.TableProperty;
import org.tms.api.io.JSONOptions;

public class JSONWriter extends BaseWriter<JSONOptions>
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
    	
    	// Table metadata
    	JSONObject tMd = new JSONObject();
    	
    	Table t = getTable();
    	
    	TableProperty[] tProps = (options().isVerboseState() ? TableProperty.values():
    		new TableProperty[] {TableProperty.Label, TableProperty.Description, TableProperty.isSupportsNull, TableProperty.isReadOnly});
    	
    	for (TableProperty tp : tProps) {
    		if (hasValue(t, tp) && !tp.isReadOnly())
				tMd.put(tp.toString(), getTable().getProperty(tp));
    	}
    	
    	if (!tMd.isEmpty())
    		tblJson.put("metadata", tMd);
    	
    	// create the top-level item
    	JSONObject root = new JSONObject();    	
    	root.put(ElementType.Table.toString(), tblJson);
    	
    	// serialize output
    	root.writeJSONString(getOutputWriter());  	
    }
}
