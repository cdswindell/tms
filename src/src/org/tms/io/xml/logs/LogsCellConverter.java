package org.tms.io.xml.logs;

import org.tms.api.Cell;
import org.tms.io.LabeledReader;
import org.tms.io.LabeledWriter;
import org.tms.io.xml.CellConverter;
import org.tms.tds.logs.LogsCellImpl;

public class LogsCellConverter extends CellConverter
{
    static final public String ELEMENT_TAG = "logCell";
    
    public LogsCellConverter(LabeledReader<?> reader)
    {
        super(reader);
    }

    public LogsCellConverter(LabeledWriter<?> writer)
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
