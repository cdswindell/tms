package org.tms.io.xml.logs;

import org.tms.api.Row;
import org.tms.io.LabeledReader;
import org.tms.io.LabeledWriter;
import org.tms.io.xml.RowConverter;
import org.tms.tds.logs.LogsRowImpl;

public class LogsRowConverter extends RowConverter
{
    static final public String ELEMENT_TAG = "logRow";
    
    public LogsRowConverter(LabeledReader<?> reader)
    {
        super(reader);
    }

    public LogsRowConverter(LabeledWriter<?> writer)
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
