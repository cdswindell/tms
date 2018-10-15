package org.tms.io.xml.excel;

import org.tms.api.Row;
import org.tms.io.LabeledReader;
import org.tms.io.LabeledWriter;
import org.tms.io.xml.RowConverter;
import org.tms.tds.excel.ExcelRowImpl;

public class ExcelRowConverter extends RowConverter
{
    static final public String ELEMENT_TAG = "exRow";
    
    public ExcelRowConverter(LabeledReader<?> reader)
    {
        super(reader);
    }

    public ExcelRowConverter(LabeledWriter<?> writer)
    {
        super(writer);
    }

    @Override
    public boolean canConvert(@SuppressWarnings("rawtypes") Class arg)
    {
        return ExcelRowImpl.class == arg;
    }

    @Override
    protected boolean isRelevant(Row c)
    {
    	return false;
    }
}
