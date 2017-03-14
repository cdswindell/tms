package org.tms.io.xml.logs;

import org.tms.api.Row;
import org.tms.io.BaseReader;
import org.tms.io.BaseWriter;
import org.tms.io.xml.RowConverter;
import org.tms.tds.logs.LogsRowImpl;

public class LogsRowConverter extends RowConverter
{
    static final public String ELEMENT_TAG = "logRow";
    
    public LogsRowConverter(BaseReader<?> reader)
    {
        super(reader);
    }

    public LogsRowConverter(BaseWriter<?> writer)
    {
        super(writer);
    }

    @Override
    public boolean canConvert(@SuppressWarnings("rawtypes") Class arg)
    {
        return LogsRowImpl.class == arg;
    }

    @Override
    protected boolean isRelevant(Row c)
    {
    	return false;
    }
}
