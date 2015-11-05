package org.tms.io.xml;

import org.tms.api.Access;
import org.tms.api.Column;
import org.tms.api.Row;
import org.tms.api.Subset;
import org.tms.api.Table;
import org.tms.io.BaseReader;
import org.tms.io.BaseWriter;
import org.tms.tds.SubsetImpl;

import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

public class SubsetConverter extends BaseConverter
{
    static final public String SUBSET_TAG = "subset";
    static final public String SUBSET_ROWS_TAG = "sRows";
    static final public String SUBSET_COLS_TAG = "sCols";
    
    public SubsetConverter(BaseWriter<?> writer)
    {
        super(writer);
    }

    public SubsetConverter(BaseReader<?> reader)
    {
        super(reader);
    }

    @Override
    public boolean canConvert(@SuppressWarnings("rawtypes") Class arg)
    {
        return SubsetImpl.class.isAssignableFrom(arg);
    }

    @Override
    public void marshal(Object arg, HierarchicalStreamWriter writer, MarshallingContext context)
    {
        SubsetImpl s = (SubsetImpl)arg;
        
        String rowIdxs = null;
        if (s.getNumRows() > 0) {
            StringBuffer sb = new StringBuffer();
            boolean needsComma = false;
            for (Row r : s.getRows()) {
                int idx = r.getIndex();
                if (!isIgnoreRow(idx)) {
                    if (needsComma) 
                        sb.append(",");
                    else
                        needsComma = true; 
                    
                    sb.append(getRemappedRowIndex(idx));
                }
            }
            
            if (sb.length() > 0)
                rowIdxs = sb.toString();
        }
        
        String colIdxs = null;
        if (s.getNumColumns() > 0) {
            StringBuffer sb = new StringBuffer();
            boolean needsComma = false;
            for (Column c : s.getColumns()) {
                if (!isIgnoreColumn(c)) {
                    if (needsComma) 
                        sb.append(",");
                    else
                        needsComma = true; 
                    
                    sb.append(getRemappedColumnIndex(c));
                }
            }
            
            
            if (sb.length() > 0)
                colIdxs = sb.toString();
        }
        
        // if all of the rows/columns are excluded, omit
        if (rowIdxs == null && colIdxs == null)
            return;
        
        // write the subset node
        writer.startNode(SUBSET_TAG);                   
        marshalTableElement(s, writer, context, true);
        
        // write rows, if any
        if (rowIdxs != null) {
            writer.startNode(SUBSET_ROWS_TAG);
            writer.setValue(rowIdxs);
            writer.endNode();
        }
        
        if (colIdxs != null) {
            writer.startNode(SUBSET_COLS_TAG);
            writer.setValue(colIdxs);
            writer.endNode();
        }
        
        writer.endNode();
    }

    @Override
    public Subset unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context)
    {
        Table t = (Table)context.get(TMS_TABLE_KEY);
        
        Subset s = t.addSubset(Access.Next);
        
        // upon return, we are left in the subset tag
        unmarshalTableElement(s, reader, context);
        
        String nodeName = reader.getNodeName();
        if (SUBSET_ROWS_TAG.equals(nodeName)) {
            String idxs = reader.getValue();
            addSubsetRows(s, idxs, context);
            reader.moveUp();
            
            // check for "sCols" tag (subset columns)
            if (reader.hasMoreChildren()) 
                reader.moveDown();
        }
        
        nodeName = reader.getNodeName();
        if (SUBSET_COLS_TAG.equals(nodeName)) {
            String idxs = reader.getValue();
            addSubsetCols(s, idxs, context);
            reader.moveUp();
        }
        
        // We're all set
        nodeName = reader.getNodeName();
        return s;
    }

    private void addSubsetCols(Subset s, String idxs, UnmarshallingContext context)
    {
        Table t = getTable(context);
        if (idxs != null && (idxs = idxs.trim()).length() > 0) {
            for (String sIdx : idxs.split(",")) {
                try {
                    int idx = Integer.parseInt(sIdx.trim());
                    Column c = t.getColumn(idx);
                    if (c != null)
                        s.add(c);
                }
                catch (Exception e) {}
            }
        }        
    }

    private void addSubsetRows(Subset s, String idxs, UnmarshallingContext context)
    {
        Table t = getTable(context);
        if (idxs != null && (idxs = idxs.trim()).length() > 0) {
            for (String sIdx : idxs.split(",")) {
                try {
                    int idx = Integer.parseInt(sIdx.trim());
                    Row r = t.getRow(idx);
                    if (r != null)
                        s.add(r);
                }
                catch (Exception e) {}
            }
        }        
    }        
}
