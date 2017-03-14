package org.tms.io.xml.dbms;

import org.tms.api.Row;
import org.tms.io.BaseReader;
import org.tms.io.BaseWriter;
import org.tms.io.xml.RowConverter;
import org.tms.tds.dbms.DbmsRowImpl;

public class DbmsRowConverter extends RowConverter
{
    static final public String ELEMENT_TAG = "dbmsRow";
    
    public DbmsRowConverter(BaseReader<?> reader)
    {
        super(reader);
    }

    public DbmsRowConverter(BaseWriter<?> writer)
    {
        super(writer);
    }

    @Override
    public boolean canConvert(@SuppressWarnings("rawtypes") Class arg)
    {
        return DbmsRowImpl.class == arg;
    }

    @Override
    protected boolean isRelevant(Row c)
    {
    	return false;
    }
}
