package org.tms.io.xml.logs;

import org.tms.api.Column;
import org.tms.io.LabeledReader;
import org.tms.io.LabeledWriter;
import org.tms.io.xml.ColumnConverter;
import org.tms.tds.logs.LogsColumnImpl;

public class LogsColumnConverter extends ColumnConverter
{
    static final public String ELEMENT_TAG = "logCol";
    
    public LogsColumnConverter(LabeledReader<?> reader)
    {
        super(reader);
    }

    public LogsColumnConverter(LabeledWriter<?> writer)
    {
        super(writer);
    }

    @Override
    public boolean canConvert(@SuppressWarnings("rawtypes") Class arg)
    {
        return LogsColumnImpl.class == arg;
    }

    @Override
    protected boolean isRelevant(Column c)
    {
    	return false;
    }
}
