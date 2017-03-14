package org.tms.io.xml.excel;

import org.tms.api.Row;
import org.tms.io.BaseReader;
import org.tms.io.BaseWriter;
import org.tms.io.xml.RowConverter;
import org.tms.tds.excel.ExcelRowImpl;

public class ExcelRowConverter extends RowConverter
{
    static final public String ELEMENT_TAG = "exRow";
    
    public ExcelRowConverter(BaseReader<?> reader)
    {
        super(reader);
    }

    public ExcelRowConverter(BaseWriter<?> writer)
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
