package org.tms.io;

import java.io.File;
import java.io.InputStream;

import org.tms.api.TableContext;
import org.tms.io.options.ArchivalIOOptions;
import org.tms.io.xml.CellConverter;
import org.tms.io.xml.ColumnConverter;
import org.tms.io.xml.DbmsTableConverter;
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
import org.tms.tds.dbms.DbmsTableImpl;

import com.thoughtworks.xstream.XStream;

abstract public class ArchivalReader<T extends ArchivalIOOptions<T>> extends BaseReader<T>
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
        xmlStreamer.alias(TableConverter.ELEMENT_TAG, TableImpl.class);
        xmlStreamer.alias(RowConverter.ELEMENT_TAG, RowImpl.class);
        xmlStreamer.alias(ColumnConverter.ELEMENT_TAG, ColumnImpl.class);
        xmlStreamer.alias(SubsetConverter.ELEMENT_TAG, SubsetImpl.class);
        xmlStreamer.alias(CellConverter.ELEMENT_TAG, CellImpl.class);
            
        xmlStreamer.registerConverter(new TableContextConverter(reader));
        xmlStreamer.registerConverter(new DbmsTableConverter(reader));
        xmlStreamer.registerConverter(new TableConverter(reader));
        xmlStreamer.registerConverter(new RowConverter(reader));
        xmlStreamer.registerConverter(new ColumnConverter(reader));
        xmlStreamer.registerConverter(new SubsetConverter(reader));
        xmlStreamer.registerConverter(new CellConverter(reader));
        
        return xmlStreamer;
    }
}
