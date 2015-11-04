package org.tms.io.xml;

import java.util.HashSet;
import java.util.Set;

import org.tms.api.Access;
import org.tms.api.Column;
import org.tms.api.Row;
import org.tms.api.Table;
import org.tms.api.derivables.Precisionable;
import org.tms.api.factories.TableFactory;
import org.tms.io.BaseReader;
import org.tms.io.BaseWriter;
import org.tms.tds.CellImpl;
import org.tms.tds.ColumnImpl;
import org.tms.tds.RowImpl;
import org.tms.tds.TableImpl;

import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

public class TableConverter extends ConverterBase
{
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
        
        writer.addAttribute("nRows", String.valueOf(nRows));
        writer.addAttribute("nCols", String.valueOf(nCols));
        writer.addAttribute("precision", String.valueOf(t.getPrecision()));
        
        marshalTableElement(t, writer, context, true);
        
        // Rows
        Set<Integer> activeRows = new HashSet<Integer>(nRows);
        if (nRows > 0) {
            writer.startNode("rows");
            for (int i = 1; i <= nRows; i++) { 
                if (t.isRowDefined(Access.ByIndex, i)) {
                    Row r = t.getRow(i);
                    if (!options().isIgnoreEmptyRows() || !r.isNull()) {
                        context.convertAnother(t.getRow(i));
                        activeRows.add(i);
                    }
                }
            }
            
            writer.endNode();
        }
        
        // Columns
        if (nCols > 0) {
            writer.startNode("columns");
            for (Column c : getActiveColumns()) {
                context.convertAnother(c);
            }
            
            writer.endNode();
        }
        
        // Cells
        if (nRows > 0 && nCols > 0) {
            writer.startNode("cells");
            for (Column c : getActiveColumns()) {
                for (int rIdx = 1; rIdx <= nRows; rIdx++) {
                    if (activeRows.contains(rIdx)) {
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
        int nRows = Integer.valueOf(reader.getAttribute("nRows"));
        int nCols = Integer.valueOf(reader.getAttribute("nCols"));
        
        Table t = TableFactory.createTable(nRows, nCols, getTableContext());
        context.put(TMS_TABLE_KEY, t);
        
        if (t instanceof Precisionable) {
            int precision = Integer.valueOf(reader.getAttribute("precision"));
            if (precision > 0)
                ((Precisionable)t).setPrecision(precision);
        }
        
        // upon return, we are left in the Rows or Columns or Cells tag
        unmarshalTableElement(t, reader, context);
        
        String nodeName = reader.getNodeName();
        
        // process rows
        if ("rows".equals(nodeName)) {
            while (reader.hasMoreChildren()) {
                reader.moveDown();
                context.convertAnother(t, RowImpl.class);  
                reader.moveUp();
            }
            
            // we're done with rows, so move out of the "rows" tag
            reader.moveUp();
            
            // set up to process remaining elements (columns, subsets, cells)
            if (reader.hasMoreChildren()) {
                reader.moveDown();            
                nodeName = reader.getNodeName();
            }
        }
        
        if ("columns".equals(nodeName)) {
            while (reader.hasMoreChildren()) {
                reader.moveDown();
                context.convertAnother(t, ColumnImpl.class);  
                reader.moveUp();
            }
            
            // we're done with cols, so move out of the "columns" tag
            reader.moveUp();
            
            // set up to process remaining elements (subsets, cells)
            if (reader.hasMoreChildren()) {
                reader.moveDown();            
                nodeName = reader.getNodeName();
            }
        }
        
        // TODO: process subsets, if any exist
        
        if ("cells".equals(nodeName)) {
            while (reader.hasMoreChildren()) {
                reader.moveDown();
                context.convertAnother(t, CellImpl.class);  
                reader.moveUp();
            }
            
            // we're done with cells, so move out of the "columns" tag
            reader.moveUp();
        }
        
        return t;
    }
}
