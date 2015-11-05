package org.tms.io.xml;

import org.tms.api.TableElement;
import org.tms.io.BaseReader;
import org.tms.io.BaseWriter;
import org.tms.tds.dbms.DbmsTableImpl;

import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

public class DbmsTableConverter extends TableConverter
{
    static final public String ELEMENT_TAG = "dbmsTable";
    
    public DbmsTableConverter(BaseReader<?> reader)
    {
        super(reader);
    }

    public DbmsTableConverter(BaseWriter<?> writer)
    {
        super(writer);
    }

    @Override
    public boolean canConvert(@SuppressWarnings("rawtypes") Class arg)
    {
        return DbmsTableImpl.class == arg;
    }

    @Override
    protected void marshalClassSpecificElements(TableElement te, HierarchicalStreamWriter writer, MarshallingContext context)
    {
        DbmsTableImpl dbTe = (DbmsTableImpl)te;
        writeNode(dbTe.getDriverClassName(), "driver", writer, context);
        writeNode(dbTe.getConnectionUrl(), "connectionUrl", writer, context);
        writeNode(dbTe.getQuery(), "query", writer, context);
    }
}
