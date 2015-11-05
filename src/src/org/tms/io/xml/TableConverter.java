package org.tms.io.xml;

import java.util.Map;

import org.tms.api.Column;
import org.tms.api.Row;
import org.tms.api.Subset;
import org.tms.api.Table;
import org.tms.api.derivables.Derivable;
import org.tms.api.derivables.Precisionable;
import org.tms.api.factories.TableFactory;
import org.tms.io.BaseReader;
import org.tms.io.BaseWriter;
import org.tms.tds.CellImpl;
import org.tms.tds.ColumnImpl;
import org.tms.tds.RowImpl;
import org.tms.tds.SubsetImpl;
import org.tms.tds.TableImpl;

import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

public class TableConverter extends BaseConverter
{
    static final protected String NROWS_ATTR = "nRows";
    static final protected String NCOLS_ATTR = "nCols";
    static final protected String PRECISION_ATTR = "precision";
    
    static final protected String ROWS_TAG = "rows";
    static final protected String COLS_TAG = "columns";
    
    public TableConverter(BaseWriter<?> writer)
    {
        super(writer);
    }

    public TableConverter(BaseReader<?> reader)
    {
        super(reader);
    }

    @Override
    public boolean canConvert(@SuppressWarnings("rawtypes") Class arg)
    {
        return TableImpl.class.isAssignableFrom(arg);
    }

    @Override
    public void marshal(Object arg, HierarchicalStreamWriter writer, MarshallingContext context)
    {
        TableImpl t = (TableImpl)arg;
        int nRows = t.getNumRows();
        int nCols = getNumConsumableColumns();
        
        writer.addAttribute(NROWS_ATTR, String.valueOf(nRows));
        writer.addAttribute(NCOLS_ATTR, String.valueOf(nCols));
        writer.addAttribute(PRECISION_ATTR, String.valueOf(t.getPrecision()));
        
        marshalTableElement(t, writer, context, true);
        
        // Rows
        if (nRows > 0) {
            writer.startNode(ROWS_TAG);
            for (int i = 1; i <= nRows; i++) { 
                if (!isIgnoreRow(i)) 
                    context.convertAnother(t.getRow(i));
            }
            
            writer.endNode();
        }
        
        // Columns
        if (nCols > 0) {
            writer.startNode(COLS_TAG);
            for (Column c : getActiveColumns()) {
                context.convertAnother(c);
            }
            
            writer.endNode();
        }
        
        // Subsets
        if (t.getNumSubsets() > 0) {
            writer.startNode("subsets");
            for (Subset s : t.getSubsets()) {
                context.convertAnother(s);
            }
            
            writer.endNode();
        }        

        // Cells
        if (nRows > 0 && nCols > 0) {
            writer.startNode("cells");
            for (Column c : getActiveColumns()) {
                for (int rIdx = 1; rIdx <= nRows; rIdx++) {
                    if (!isIgnoreRow(rIdx)) {
                        Row r = t.getRow(rIdx);
                        if (t.isCellDefined(r, c))
                            context.convertAnother(t.getCell(r,  c));
                    }
                }
            }
            
            writer.endNode();
        }
    }

    @Override
    public Table unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context)
    {
        int nRows = readAttributeInteger(NROWS_ATTR, reader);
        int nCols = readAttributeInteger(NCOLS_ATTR, reader);
        
        Table t = TableFactory.createTable(nRows, nCols, getTableContext());
        context.put(TMS_TABLE_KEY, t);
        
        if (t instanceof Precisionable) {
            Integer precision = readAttributeInteger(PRECISION_ATTR, reader);
            if (precision != null && precision > 0)
                ((Precisionable)t).setPrecision(precision);
        }
        
        // upon return, we are left in the Rows or Columns or Cells tag
        unmarshalTableElement(t, reader, context);
        
        // so where are we now?
        String nodeName = reader.getNodeName();
        
        // process rows
        if (ROWS_TAG.equals(nodeName)) 
            nodeName = processChildren(t,  RowImpl.class, reader, context);
        
        if (COLS_TAG.equals(nodeName)) 
            nodeName = processChildren(t,  ColumnImpl.class, reader, context);
        
        if ("subsets".equals(nodeName)) 
            nodeName = processChildren(t,  SubsetImpl.class, reader, context);
        
        if ("cells".equals(nodeName)) 
            nodeName = processChildren(t,  CellImpl.class, reader, context);
        
        // process derivations, if any
        for (Map.Entry<Derivable, String> e : getDerivationsMap(context).entrySet()) {
            e.getKey().setDerivation(e.getValue());
        }
        
        return t;
    }
    
    private String processChildren(Table t, Class<?> clazz, HierarchicalStreamReader reader, UnmarshallingContext context)
    {
        String nodeName = reader.getNodeName();
        while (reader.hasMoreChildren()) {
            reader.moveDown();
            context.convertAnother(t, clazz);  
            reader.moveUp();
        }
        
        // we're done with cols, so move out of the "columns" tag
        reader.moveUp();
        
        // set up to process remaining elements (subsets, cells)
        if (reader.hasMoreChildren()) {
            reader.moveDown();            
            nodeName = reader.getNodeName();
        }
        
        return nodeName;
    }
}
