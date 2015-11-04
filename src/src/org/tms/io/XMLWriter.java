package org.tms.io;

import java.io.IOException;
import java.io.OutputStream;
import java.util.HashSet;
import java.util.Set;

import org.tms.api.Access;
import org.tms.api.Column;
import org.tms.api.Row;
import org.tms.api.TableElement;
import org.tms.api.TableProperty;
import org.tms.api.derivables.Derivable;
import org.tms.api.derivables.Derivation;
import org.tms.api.io.XMLOptions;
import org.tms.tds.CellImpl;
import org.tms.tds.ColumnImpl;
import org.tms.tds.RowImpl;
import org.tms.tds.SubsetImpl;
import org.tms.tds.TableImpl;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

public class XMLWriter extends BaseWriter<XMLOptions>
{
    public static void export(TableExportAdapter tea, OutputStream out, XMLOptions options) 
    throws IOException
    {
        XMLWriter writer = new XMLWriter(tea, out, options);
        writer.export();        
    }
    
    private XMLWriter(TableExportAdapter t, OutputStream out, XMLOptions options)
    {
        super(t, out, options);
    }

    @Override
    protected void export() throws IOException
    {
        XStream xs = getXStream();
        xs.toXML(getTable(), this.getOutputStream());
    }
    
    private XStream getXStream()
    {
        XStream xmlStreamer = new XStream();
        xmlStreamer = new XStream();
            
        xmlStreamer.alias("table", TableImpl.class);
        xmlStreamer.alias("row", RowImpl.class);
        xmlStreamer.alias("column", ColumnImpl.class);
        xmlStreamer.alias("subset", SubsetImpl.class);
        xmlStreamer.alias("cell", CellImpl.class);
            
        xmlStreamer.registerConverter(new TableConverter());
        xmlStreamer.registerConverter(new RowConverter());
        xmlStreamer.registerConverter(new ColumnConverter());
        xmlStreamer.registerConverter(new CellConverter());
        
        return xmlStreamer;
    }
    
    abstract public class ConverteBase implements Converter
    {
        public void marshalTableElement(TableElement te, 
                                        HierarchicalStreamWriter writer, 
                                        MarshallingContext context, 
                                        boolean includeLabel)
        {
            if ((te == te.getTable() && te.isReadOnly()) || te.isReadOnly() != te.getTable().isReadOnly())
                writer.addAttribute("readOnly", "true");
                
            if ((te == te.getTable() && !te.isSupportsNull()) || te.isSupportsNull() != te.getTable().isSupportsNull())
                writer.addAttribute("allowsNulls", "true");            
            
            if (includeLabel && te.hasProperty(TableProperty.Label)) {
                writer.startNode("label");
                writer.setValue(te.getLabel());
                writer.endNode();
            }
            
            if (te.hasProperty(TableProperty.Description)) {
                writer.startNode("description");
                writer.setValue(te.getDescription());
                writer.endNode();
            }
            
            if (te.isTagged()) {
                writer.startNode("tags");
                context.convertAnother(te.getTags());
                writer.endNode();
            }
            
            if (te instanceof Derivable && ((Derivable)te).isDerived()) {
                Derivation d =  ((Derivable)te).getDerivation();
                writer.startNode("derivation");
                writer.setValue(d.getExpression());
                writer.endNode();
            }
        }        
    }

    public class TableConverter extends ConverteBase
    {
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
            int nCols = XMLWriter.this.getNumConsumableColumns();
            
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
            return null;
        }       
    }
    
    class RowConverter extends ConverteBase
    {
        @Override
        public boolean canConvert(@SuppressWarnings("rawtypes") Class arg)
        {
            return RowImpl.class.isAssignableFrom(arg);
        }

        @Override
        public void marshal(Object arg, HierarchicalStreamWriter writer, MarshallingContext context)
        {
            RowImpl r = (RowImpl)arg;
            
            writer.startNode("row");                
            writer.addAttribute("index", String.valueOf(r.getIndex()));
            
            marshalTableElement(r, writer, context, options().isRowLabels());
            
            writer.endNode();
        }

        @Override
        public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext writer)
        {
            return null;
        }        
    }
    
    class ColumnConverter extends ConverteBase
    {
        @Override
        public boolean canConvert(@SuppressWarnings("rawtypes") Class arg)
        {
            return ColumnImpl.class.isAssignableFrom(arg);
        }

        @Override
        public void marshal(Object arg, HierarchicalStreamWriter writer, MarshallingContext context)
        {
            ColumnImpl c = (ColumnImpl)arg;
            if (options().isIgnoreEmptyColumns() && c.isNull())
                return;
            
            writer.startNode("column");                
            writer.addAttribute("index", String.valueOf(c.getIndex()));
            
            marshalTableElement(c, writer, context, options().isColumnLabels());
            
            writer.endNode();
        }

        @Override
        public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext writer)
        {
            return null;
        }        
    }
    
    class CellConverter extends ConverteBase
    {
        @Override
        public boolean canConvert(@SuppressWarnings("rawtypes") Class arg)
        {
            return CellImpl.class.isAssignableFrom(arg);
        }

        @Override
        public void marshal(Object arg, HierarchicalStreamWriter writer, MarshallingContext context)
        {
            CellImpl c = (CellImpl)arg;
            if (c.isNull())
                return;
            
            writer.startNode("cell");                
            writer.addAttribute("rIdx", String.valueOf(c.getRow().getIndex()));
            writer.addAttribute("cIdx", String.valueOf(c.getColumn().getIndex()));
            
            marshalTableElement(c, writer, context, true);
            
            writer.startNode("value");                
            context.convertAnother(c.getCellValue());
            writer.endNode();
            
            writer.endNode();
        }

        @Override
        public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext writer)
        {
            return null;
        }        
    }
}
