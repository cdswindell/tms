package org.tms.io.xml.dbms;

import org.tms.api.Row;
import org.tms.io.LabeledReader;
import org.tms.io.LabeledWriter;
import org.tms.io.xml.RowConverter;
import org.tms.tds.dbms.DbmsRowImpl;

public class DbmsRowConverter extends RowConverter
{
    static final public String ELEMENT_TAG = "dbmsRow";
    
    public DbmsRowConverter(LabeledReader<?> reader)
    {
        super(reader);
    }

    public DbmsRowConverter(LabeledWriter<?> writer)
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
