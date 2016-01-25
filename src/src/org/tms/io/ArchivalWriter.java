package org.tms.io;

import java.io.OutputStream;

import org.tms.api.io.TMSOptions;
import org.tms.api.io.XMLOptions;
import org.tms.io.options.ArchivalIOOptions;
import org.tms.io.xml.CellConverter;
import org.tms.io.xml.ColumnConverter;
import org.tms.io.xml.DbmsCellConverter;
import org.tms.io.xml.DbmsColumnConverter;
import org.tms.io.xml.DbmsRowConverter;
import org.tms.io.xml.DbmsTableConverter;
import org.tms.io.xml.LogsCellConverter;
import org.tms.io.xml.LogsColumnConverter;
import org.tms.io.xml.LogsRowConverter;
import org.tms.io.xml.LogsTableConverter;
import org.tms.io.xml.RowConverter;
import org.tms.io.xml.SubsetConverter;
import org.tms.io.xml.TableContextConverter;
import org.tms.io.xml.TableConverter;
import org.tms.tds.CellImpl;
import org.tms.tds.ColumnImpl;
import org.tms.tds.ContextImpl;
import org.tms.tds.RowImpl;
import org.tms.tds.SubsetImpl;
import org.tms.tds.TableImpl;
import org.tms.tds.dbms.DbmsCellImpl;
import org.tms.tds.dbms.DbmsColumnImpl;
import org.tms.tds.dbms.DbmsRowImpl;
import org.tms.tds.dbms.DbmsTableImpl;
import org.tms.tds.logs.LogsCellImpl;
import org.tms.tds.logs.LogsColumnImpl;
import org.tms.tds.logs.LogsRowImpl;
import org.tms.tds.logs.LogsTableImpl;

import com.thoughtworks.xstream.XStream;

abstract class ArchivalWriter<T extends ArchivalIOOptions<T>> extends BaseWriter<T>
{
    public ArchivalWriter(TableExportAdapter tea, OutputStream out, T options)
    {
        super(tea, out, options);       
    }
    
    protected XStream getXStream(ArchivalWriter<T> writer)
    {
        XStream xmlStreamer = new XStream();
            
        xmlStreamer.alias(TableContextConverter.ELEMENT_TAG, ContextImpl.class);
        xmlStreamer.alias(DbmsTableConverter.ELEMENT_TAG, DbmsTableImpl.class);
        xmlStreamer.alias(DbmsRowConverter.ELEMENT_TAG, DbmsRowImpl.class);
        xmlStreamer.alias(DbmsColumnConverter.ELEMENT_TAG, DbmsColumnImpl.class);
        xmlStreamer.alias(DbmsCellConverter.ELEMENT_TAG, DbmsCellImpl.class);

        xmlStreamer.alias(LogsTableConverter.ELEMENT_TAG, LogsTableImpl.class);
        xmlStreamer.alias(LogsRowConverter.ELEMENT_TAG, LogsRowImpl.class);
        xmlStreamer.alias(LogsColumnConverter.ELEMENT_TAG, LogsColumnImpl.class);
        xmlStreamer.alias(LogsCellConverter.ELEMENT_TAG, LogsCellImpl.class);

        xmlStreamer.alias(TableConverter.ELEMENT_TAG, TableImpl.class);
        xmlStreamer.alias(RowConverter.ELEMENT_TAG, RowImpl.class);
        xmlStreamer.alias(ColumnConverter.ELEMENT_TAG, ColumnImpl.class);
        xmlStreamer.alias(SubsetConverter.ELEMENT_TAG, SubsetImpl.class);
        xmlStreamer.alias(CellConverter.ELEMENT_TAG, CellImpl.class);
            
        xmlStreamer.registerConverter(new TableContextConverter(writer));
        
        xmlStreamer.registerConverter(new DbmsTableConverter(writer));
        xmlStreamer.registerConverter(new DbmsRowConverter(writer));
        xmlStreamer.registerConverter(new DbmsColumnConverter(writer));
        xmlStreamer.registerConverter(new DbmsCellConverter(writer));
        
        xmlStreamer.registerConverter(new LogsTableConverter(writer));
        xmlStreamer.registerConverter(new LogsRowConverter(writer));
        xmlStreamer.registerConverter(new LogsColumnConverter(writer));
        xmlStreamer.registerConverter(new LogsCellConverter(writer));
        
        xmlStreamer.registerConverter(new TableConverter(writer));
        xmlStreamer.registerConverter(new RowConverter(writer));
        xmlStreamer.registerConverter(new ColumnConverter(writer));
        xmlStreamer.registerConverter(new SubsetConverter(writer));
        xmlStreamer.registerConverter(new CellConverter(writer));
        
        return xmlStreamer;
    }
    
    @Override
    public ArchivalWriter<?> createDelegate(TableExportAdapter tea)
    {
        ArchivalWriter<?> writer = null;
        switch (options().getFileFormat()) {
            case TMS:                
                writer = new TMSWriter(tea, (TMSOptions)options());
                break;
                
            case XML:                
                writer = new XMLWriter(tea, (XMLOptions)options());
                break;
            
            default:
                ;
        }
        
        this.reset(tea);
        return writer;
    }
}
