package org.tms.io;

import java.io.OutputStream;

import org.tms.io.options.ArchivalIOOptions;
import org.tms.io.xml.CellConverter;
import org.tms.io.xml.ColumnConverter;
import org.tms.io.xml.DbmsTableConverter;
import org.tms.io.xml.RowConverter;
import org.tms.io.xml.SubsetConverter;
import org.tms.io.xml.TableConverter;
import org.tms.tds.CellImpl;
import org.tms.tds.ColumnImpl;
import org.tms.tds.RowImpl;
import org.tms.tds.SubsetImpl;
import org.tms.tds.TableImpl;
import org.tms.tds.dbms.DbmsTableImpl;

import com.thoughtworks.xstream.XStream;

abstract public class ArchivalWriter<T extends ArchivalIOOptions<T>> extends BaseWriter<T>
{
    public ArchivalWriter(TableExportAdapter tea, OutputStream out, T options)
    {
        super(tea, out, options);       
    }
    
    protected XStream getXStream(ArchivalWriter<T> writer)
    {
        XStream xmlStreamer = new XStream();
        xmlStreamer = new XStream();
            
        xmlStreamer.alias(DbmsTableConverter.ELEMENT_TAG, DbmsTableImpl.class);
        xmlStreamer.alias(TableConverter.ELEMENT_TAG, TableImpl.class);
        xmlStreamer.alias(RowConverter.ELEMENT_TAG, RowImpl.class);
        xmlStreamer.alias(ColumnConverter.ELEMENT_TAG, ColumnImpl.class);
        xmlStreamer.alias(SubsetConverter.ELEMENT_TAG, SubsetImpl.class);
        xmlStreamer.alias(CellConverter.ELEMENT_TAG, CellImpl.class);
            
        xmlStreamer.registerConverter(new DbmsTableConverter(writer));
        xmlStreamer.registerConverter(new TableConverter(writer));
        xmlStreamer.registerConverter(new RowConverter(writer));
        xmlStreamer.registerConverter(new ColumnConverter(writer));
        xmlStreamer.registerConverter(new SubsetConverter(writer));
        xmlStreamer.registerConverter(new CellConverter(writer));
        
        return xmlStreamer;
    }
}