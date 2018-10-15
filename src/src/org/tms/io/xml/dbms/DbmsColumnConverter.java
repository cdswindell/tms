package org.tms.io.xml.dbms;

import org.tms.api.Column;
import org.tms.io.LabeledReader;
import org.tms.io.LabeledWriter;
import org.tms.io.xml.ColumnConverter;
import org.tms.tds.dbms.DbmsColumnImpl;

public class DbmsColumnConverter extends ColumnConverter
{
    static final public String ELEMENT_TAG = "dbmsCol";
    
    public DbmsColumnConverter(LabeledReader<?> reader)
    {
        super(reader);
    }

    public DbmsColumnConverter(LabeledWriter<?> writer)
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
