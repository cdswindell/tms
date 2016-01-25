package org.tms.io;

import java.io.File;
import java.io.InputStream;

import org.tms.api.TableContext;
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

abstract class ArchivalReader<T extends ArchivalIOOptions<T>> extends BaseReader<T>
{
    public ArchivalReader(File file, TableContext context, T format)
    {
        super(file, context, format);       
    }
    
    public ArchivalReader(InputStream in, TableContext context, T format)
    {
        super(in, context, format);       
    }

    protected XStream getXStream(ArchivalReader<T> reader)
    {
        XStream xmlStreamer = new XStream();
        xmlStreamer = new XStream();
            
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
            
        xmlStreamer.registerConverter(new TableContextConverter(reader));
        xmlStreamer.registerConverter(new TableConverter(reader));
        xmlStreamer.registerConverter(new RowConverter(reader));
        xmlStreamer.registerConverter(new ColumnConverter(reader));
        xmlStreamer.registerConverter(new SubsetConverter(reader));
        xmlStreamer.registerConverter(new CellConverter(reader));
        
        xmlStreamer.registerConverter(new DbmsTableConverter(reader));
        xmlStreamer.registerConverter(new DbmsRowConverter(reader));
        xmlStreamer.registerConverter(new DbmsColumnConverter(reader));
        xmlStreamer.registerConverter(new DbmsCellConverter(reader));
        
        xmlStreamer.registerConverter(new LogsTableConverter(reader));
        xmlStreamer.registerConverter(new LogsRowConverter(reader));
        xmlStreamer.registerConverter(new LogsColumnConverter(reader));
        xmlStreamer.registerConverter(new LogsCellConverter(reader));
        
     return xmlStreamer;
    }
}
