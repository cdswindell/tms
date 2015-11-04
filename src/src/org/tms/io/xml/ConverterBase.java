package org.tms.io.xml;

import java.util.List;

import org.tms.api.Column;
import org.tms.api.TableContext;
import org.tms.api.TableElement;
import org.tms.api.TableProperty;
import org.tms.api.derivables.Derivable;
import org.tms.api.derivables.Derivation;
import org.tms.api.io.IOOption;
import org.tms.io.BaseReader;
import org.tms.io.BaseWriter;

import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

abstract public class ConverterBase implements Converter
{
    private BaseWriter<?> m_writer;
    private BaseReader<?> m_reader;
    private IOOption<?> m_options;
    
    public ConverterBase(BaseWriter<?> writer)
    {
        m_writer = writer;
        m_options = writer.options();
    }

    public ConverterBase(BaseReader<?> reader)
    {
        m_reader = reader;
        m_options = reader.options();
    }

    protected int getNumConsumableColumns()
    {
        return m_writer.getNumConsumableColumns();
    }

    protected List<Column> getActiveColumns()
    {
        return m_writer.getActiveColumns();
    }
    
    protected IOOption<?> options()
    {
        return m_options;
    }

    protected TableContext getTableContext()
    {
        return m_reader.getTableContext();
    }
    
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
