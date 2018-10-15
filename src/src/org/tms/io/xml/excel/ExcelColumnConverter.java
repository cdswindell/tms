package org.tms.io.xml.excel;

import org.tms.api.Column;
import org.tms.io.LabeledReader;
import org.tms.io.LabeledWriter;
import org.tms.io.xml.ColumnConverter;
import org.tms.tds.excel.ExcelColumnImpl;

public class ExcelColumnConverter extends ColumnConverter
{
    static final public String ELEMENT_TAG = "exCol";
    
    public ExcelColumnConverter(LabeledReader<?> reader)
    {
        super(reader);
    }

    public ExcelColumnConverter(LabeledWriter<?> writer)
    {
        super(writer);
    }

    @Override
    public boolean canConvert(@SuppressWarnings("rawtypes") Class arg)
    {
        return ExcelColumnImpl.class == arg;
    }

    @Override
    protected boolean isRelevant(Column c)
    {
    	return false;
    }
}
