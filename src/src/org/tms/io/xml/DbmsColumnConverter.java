package org.tms.io.xml;

import org.tms.api.Column;
import org.tms.io.BaseReader;
import org.tms.io.BaseWriter;
import org.tms.tds.dbms.DbmsColumnImpl;

public class DbmsColumnConverter extends ColumnConverter
{
    static final public String ELEMENT_TAG = "dbmsCol";
    
    public DbmsColumnConverter(BaseReader<?> reader)
    {
        super(reader);
    }

    public DbmsColumnConverter(BaseWriter<?> writer)
    {
        super(writer);
    }

    @Override
    public boolean canConvert(@SuppressWarnings("rawtypes") Class arg)
    {
        return DbmsColumnImpl.class == arg;
    }

    @Override
    protected boolean isRelevant(Column c)
    {
    	return false;
    }
}
