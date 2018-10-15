package org.tms.io.xml.dbms;

import org.tms.api.Cell;
import org.tms.io.LabeledReader;
import org.tms.io.LabeledWriter;
import org.tms.io.xml.CellConverter;
import org.tms.tds.dbms.DbmsCellImpl;

public class DbmsCellConverter extends CellConverter
{
    static final public String ELEMENT_TAG = "dbmsCell";
    
    public DbmsCellConverter(LabeledReader<?> reader)
    {
        super(reader);
    }

    public DbmsCellConverter(LabeledWriter<?> writer)
    {
        super(writer);
    }

    @Override
    public boolean canConvert(@SuppressWarnings("rawtypes") Class arg)
    {
        return DbmsCellImpl.class == arg;
    }

    @Override
    protected boolean isRelevant(Cell c)
    {
    	return false;
    }
}
