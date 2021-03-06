package org.tms.io;

import java.io.OutputStream;

import org.tms.io.options.ArchivalIOOptions;
import org.tms.io.xml.CellConverter;
import org.tms.io.xml.ColumnConverter;
import org.tms.io.xml.PendingStateConverter;
import org.tms.io.xml.RowConverter;
import org.tms.io.xml.SubsetConverter;
import org.tms.io.xml.TableContextConverter;
import org.tms.io.xml.TableConverter;
import org.tms.io.xml.dbms.DbmsCellConverter;
import org.tms.io.xml.dbms.DbmsColumnConverter;
import org.tms.io.xml.dbms.DbmsRowConverter;
import org.tms.io.xml.dbms.DbmsTableConverter;
import org.tms.io.xml.excel.ExcelCellConverter;
import org.tms.io.xml.excel.ExcelColumnConverter;
import org.tms.io.xml.excel.ExcelRowConverter;
import org.tms.io.xml.excel.ExcelTableConverter;
import org.tms.io.xml.logs.LogsCellConverter;
import org.tms.io.xml.logs.LogsColumnConverter;
import org.tms.io.xml.logs.LogsRowConverter;
import org.tms.io.xml.logs.LogsTableConverter;
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
import org.tms.tds.excel.ExcelCellImpl;
import org.tms.tds.excel.ExcelColumnImpl;
import org.tms.tds.excel.ExcelRowImpl;
import org.tms.tds.excel.ExcelTableImpl;
import org.tms.tds.logs.LogsCellImpl;
import org.tms.tds.logs.LogsColumnImpl;
import org.tms.tds.logs.LogsRowImpl;
import org.tms.tds.logs.LogsTableImpl;

import com.thoughtworks.xstream.XStream;

abstract class XStreamWriter<T extends ArchivalIOOptions<T>> extends ArchivalWriter<T>
{  
    abstract public XStreamWriter<?> createDelegate(TableExportAdapter tea);
    
    public XStreamWriter(TableExportAdapter tea, OutputStream out, T options)
    {
        super(tea, out, options);       
    }
    
    protected XStream getXStream(XStreamWriter<T> writer)
    {
        XStream xmlStreamer = new XStream();
                    
        xmlStreamer.alias(TableContextConverter.ELEMENT_TAG, ContextImpl.class);
        
        xmlStreamer.alias(DbmsTableConverter.ELEMENT_TAG, DbmsTableImpl.class);
        xmlStreamer.alias(DbmsRowConverter.ELEMENT_TAG, DbmsRowImpl.class);
        xmlStreamer.alias(DbmsColumnConverter.ELEMENT_TAG, DbmsColumnImpl.class);
        xmlStreamer.alias(DbmsCellConverter.ELEMENT_TAG, DbmsCellImpl.class);

        xmlStreamer.alias(ExcelTableConverter.ELEMENT_TAG, ExcelTableImpl.class);
        xmlStreamer.alias(ExcelRowConverter.ELEMENT_TAG, ExcelRowImpl.class);
        xmlStreamer.alias(ExcelColumnConverter.ELEMENT_TAG, ExcelColumnImpl.class);
        xmlStreamer.alias(ExcelCellConverter.ELEMENT_TAG, ExcelCellImpl.class);

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
        
        xmlStreamer.registerConverter(new ExcelTableConverter(writer));
        xmlStreamer.registerConverter(new ExcelRowConverter(writer));
        xmlStreamer.registerConverter(new ExcelColumnConverter(writer));
        xmlStreamer.registerConverter(new ExcelCellConverter(writer));
        
        xmlStreamer.registerConverter(new LogsTableConverter(writer));
        xmlStreamer.registerConverter(new LogsRowConverter(writer));
        xmlStreamer.registerConverter(new LogsColumnConverter(writer));
        xmlStreamer.registerConverter(new LogsCellConverter(writer));
        
        xmlStreamer.registerConverter(new TableConverter(writer));
        xmlStreamer.registerConverter(new RowConverter(writer));
        xmlStreamer.registerConverter(new ColumnConverter(writer));
        xmlStreamer.registerConverter(new SubsetConverter(writer));
        xmlStreamer.registerConverter(new CellConverter(writer));
        
		xmlStreamer.registerConverter(new PendingStateConverter(writer));
		
		XStream.setupDefaultSecurity(xmlStreamer);
		xmlStreamer.allowTypesByWildcard(new String[] {
			    "org.tms.tds.**"
		});
		
        return xmlStreamer;
    }
}
