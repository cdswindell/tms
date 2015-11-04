package org.tms.io.xml;

import java.util.HashSet;
import java.util.Set;

import org.tms.api.Access;
import org.tms.api.Column;
import org.tms.api.Row;
import org.tms.api.Table;
import org.tms.api.factories.TableFactory;
import org.tms.io.BaseReader;
import org.tms.io.BaseWriter;
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
            context.convertAnother(getActiveColumns().toArray(new Column[] {}));
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
    public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context)
    {
        int nRows = Integer.valueOf(reader.getAttribute("nRows"));
        int nCols = Integer.valueOf(reader.getAttribute("nCols"));
        
        Table t = TableFactory.createTable(nRows, nCols, getTableContext());
        
        
        
        return t;
    }       
}
