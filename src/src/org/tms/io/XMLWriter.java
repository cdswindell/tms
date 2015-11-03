package org.tms.io;

import java.beans.XMLEncoder;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

import org.tms.api.Access;
import org.tms.api.Cell;
import org.tms.api.Column;
import org.tms.api.ElementType;
import org.tms.api.Row;
import org.tms.api.Subset;
import org.tms.api.Table;
import org.tms.api.TableContext;
import org.tms.api.TableElement;
import org.tms.api.TableProperty;
import org.tms.api.TableRowColumnElement;
import org.tms.api.derivables.Derivable;
import org.tms.api.events.TableElementEventType;
import org.tms.api.events.TableElementListener;
import org.tms.api.io.IOOption;
import org.tms.api.io.XMLOptions;

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
        XMLEncoder e = new XMLEncoder( new BufferedOutputStream(getOutputStream()));  
        XmlTableWrapper tw = new XmlTableWrapper(getTable(), options());
        e.writeObject(tw);
        e.close();      
    }
    
    static public class XmlTableWrapper implements Table
    {
        public String label;
        public String [] tags;
        
        private Table m_sourceTable;
        public XmlTableWrapper()
        {
            m_sourceTable = this;
        }
        
        public XmlTableWrapper(Table source, XMLOptions options)
        {
            m_sourceTable = source;
        }
        
        public String getLabel()
        {
            return m_sourceTable == this ? this.label : m_sourceTable.getLabel();
        }
        
        public void setLabel(String s)
        {
            this.label = s;
        }
        
        public String [] getTags()
        {
            return m_sourceTable == this ? tags : m_sourceTable.getTags();
        }
        
        public void setTags(String... ta)
        {
            tags = ta;
        }

        @Override
        public TableContext getTableContext()
        {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public Table getTable()
        {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public void delete()
        {
            // TODO Auto-generated method stub
            
        }

        @Override
        public boolean fill(Object value)
        {
            // TODO Auto-generated method stub
            return false;
        }

        @Override
        public boolean clear()
        {
            // TODO Auto-generated method stub
            return false;
        }

        @Override
        public int getNumCells()
        {
            // TODO Auto-generated method stub
            return 0;
        }

        @Override
        public Iterable<Cell> cells()
        {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public boolean isNull()
        {
            // TODO Auto-generated method stub
            return false;
        }

        @Override
        public boolean isInvalid()
        {
            // TODO Auto-generated method stub
            return false;
        }

        @Override
        public boolean isValid()
        {
            // TODO Auto-generated method stub
            return false;
        }

        @Override
        public boolean isPendings()
        {
            // TODO Auto-generated method stub
            return false;
        }

        @Override
        public boolean isLabelIndexed()
        {
            // TODO Auto-generated method stub
            return false;
        }

        @Override
        public String getDescription()
        {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public void setDescription(String description)
        {
            // TODO Auto-generated method stub
            
        }

        @Override
        public List<Subset> getSubsets()
        {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public Iterable<Subset> subsets()
        {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public int getNumSubsets()
        {
            // TODO Auto-generated method stub
            return 0;
        }

        @Override
        public List<Derivable> getAffects()
        {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public List<Derivable> getDerivedElements()
        {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public ElementType getElementType()
        {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public boolean isReadOnly()
        {
            // TODO Auto-generated method stub
            return false;
        }

        @Override
        public boolean isSupportsNull()
        {
            // TODO Auto-generated method stub
            return false;
        }

        @Override
        public boolean hasProperty(TableProperty key)
        {
            // TODO Auto-generated method stub
            return false;
        }

        @Override
        public boolean hasProperty(String key)
        {
            // TODO Auto-generated method stub
            return false;
        }

        @Override
        public Object getProperty(TableProperty key)
        {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public Object getProperty(String key)
        {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public Integer getPropertyInt(TableProperty key)
        {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public Long getPropertyLong(TableProperty key)
        {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public Double getPropertyDouble(TableProperty key)
        {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public String getPropertyString(TableProperty key)
        {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public Boolean getPropertyBoolean(TableProperty key)
        {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public boolean addListeners(TableElementEventType evT, TableElementListener... tel)
        {
            // TODO Auto-generated method stub
            return false;
        }

        @Override
        public boolean removeListeners(TableElementEventType evT, TableElementListener... tel)
        {
            // TODO Auto-generated method stub
            return false;
        }

        @Override
        public List<TableElementListener> getListeners(TableElementEventType... evTs)
        {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public List<TableElementListener> removeAllListeners(TableElementEventType... evTs)
        {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public boolean hasListeners(TableElementEventType... evTs)
        {
            // TODO Auto-generated method stub
            return false;
        }

        @Override
        public boolean tag(String... tags)
        {
            // TODO Auto-generated method stub
            return false;
        }

        @Override
        public boolean untag(String... tags)
        {
            // TODO Auto-generated method stub
            return false;
        }

        @Override
        public boolean isTagged(String... tags)
        {
            // TODO Auto-generated method stub
            return false;
        }

        @Override
        public boolean isAutoRecalculate()
        {
            // TODO Auto-generated method stub
            return false;
        }

        @Override
        public void setAutoRecalculate(boolean autoRecalculate)
        {
            // TODO Auto-generated method stub
            
        }

        @Override
        public int getRowCapacityIncr()
        {
            // TODO Auto-generated method stub
            return 0;
        }

        @Override
        public void setRowCapacityIncr(int increment)
        {
            // TODO Auto-generated method stub
            
        }

        @Override
        public int getColumnCapacityIncr()
        {
            // TODO Auto-generated method stub
            return 0;
        }

        @Override
        public void setColumnCapacityIncr(int increment)
        {
            // TODO Auto-generated method stub
            
        }

        @Override
        public boolean isRowLabelsIndexed()
        {
            // TODO Auto-generated method stub
            return false;
        }

        @Override
        public void setRowLabelsIndexed(boolean isIndexed)
        {
            // TODO Auto-generated method stub
            
        }

        @Override
        public boolean isColumnLabelsIndexed()
        {
            // TODO Auto-generated method stub
            return false;
        }

        @Override
        public void setColumnLabelsIndexed(boolean isIndexed)
        {
            // TODO Auto-generated method stub
            
        }

        @Override
        public boolean isCellLabelsIndexed()
        {
            // TODO Auto-generated method stub
            return false;
        }

        @Override
        public void setCellLabelsIndexed(boolean isIndexed)
        {
            // TODO Auto-generated method stub
            
        }

        @Override
        public boolean isSubsetLabelsIndexed()
        {
            // TODO Auto-generated method stub
            return false;
        }

        @Override
        public void setSubsetLabelsIndexed(boolean isIndexed)
        {
            // TODO Auto-generated method stub
            
        }

        @Override
        public Row addRow()
        {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public Row addRow(int idx)
        {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public Row addRow(Access mode, Object... mda)
        {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public Row getRow()
        {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public Row getRow(int idx)
        {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public Row getRow(String label)
        {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public Row getRow(Access mode, Object... mda)
        {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public List<Row> getRows()
        {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public Iterable<Row> rows()
        {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public int getNumRows()
        {
            // TODO Auto-generated method stub
            return 0;
        }

        @Override
        public Column addColumn()
        {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public Column addColumn(int idx)
        {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public Column addColumn(Access mode, Object... mda)
        {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public Column getColumn()
        {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public Column getColumn(int idx)
        {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public Column getColumn(String label)
        {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public Column getColumn(Access mode, Object... mda)
        {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public int getNumColumns()
        {
            // TODO Auto-generated method stub
            return 0;
        }

        @Override
        public List<Column> getColumns()
        {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public Iterable<Column> columns()
        {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public Subset addSubset(Access mode, Object... mda)
        {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public Subset getSubset(Access mode, Object... mda)
        {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public Cell getCell(Row row, Column col)
        {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public Cell getCell(Access mode, Object... mda)
        {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public Object getCellValue(Row row, Column col)
        {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public String getFormattedCellValue(Row row, Column col)
        {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public boolean setCellValue(Row row, Column col, Object newValue)
        {
            // TODO Auto-generated method stub
            return false;
        }

        @Override
        public void pushCurrent()
        {
            // TODO Auto-generated method stub
            
        }

        @Override
        public void popCurrent()
        {
            // TODO Auto-generated method stub
            
        }

        @Override
        public void sort(ElementType et, TableProperty tp, TableRowColumnElement... others)
        {
            // TODO Auto-generated method stub
            
        }

        @Override
        public void delete(TableElement... elements)
        {
            // TODO Auto-generated method stub
            
        }

        @Override
        public void recalculate()
        {
            // TODO Auto-generated method stub
            
        }

        @Override
        public void export(String fileName, IOOption<?> options) throws IOException
        {
            // TODO Auto-generated method stub
            
        }

        @Override
        public void export(OutputStream out, IOOption<?> options) throws IOException
        {
            // TODO Auto-generated method stub
            
        }

        @Override
        public boolean isCellDefined(Row row, Column col)
        {
            // TODO Auto-generated method stub
            return false;
        }

        @Override
        public boolean isPersistant()
        {
            // TODO Auto-generated method stub
            return false;
        }

        @Override
        public void setPersistant(boolean persistent)
        {
            // TODO Auto-generated method stub
            
        }
    }
}
