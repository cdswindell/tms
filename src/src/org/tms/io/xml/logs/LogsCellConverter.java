package org.tms.io.xml.logs;

import org.tms.api.Cell;
import org.tms.io.BaseReader;
import org.tms.io.BaseWriter;
import org.tms.io.xml.CellConverter;
import org.tms.tds.logs.LogsCellImpl;

public class LogsCellConverter extends CellConverter
{
    static final public String ELEMENT_TAG = "logCell";
    
    public LogsCellConverter(BaseReader<?> reader)
    {
        super(reader);
    }

    public LogsCellConverter(BaseWriter<?> writer)
    {
        super(writer);
    }

    @Override
    public boolean canConvert(@SuppressWarnings("rawtypes") Class arg)
    {
        return LogsCellImpl.class == arg;
    }

    @Override
    protected boolean isRelevant(Cell c)
    {
    	return false;
    }
}
