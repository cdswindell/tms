package org.tms.io.xml.excel;

import org.tms.api.Cell;
import org.tms.io.LabeledReader;
import org.tms.io.LabeledWriter;
import org.tms.io.xml.CellConverter;
import org.tms.tds.excel.ExcelCellImpl;

public class ExcelCellConverter extends CellConverter
{
    static final public String ELEMENT_TAG = "exCell";
    
    public ExcelCellConverter(LabeledReader<?> reader)
    {
        super(reader);
    }

    public ExcelCellConverter(LabeledWriter<?> writer)
    {
        super(writer);
    }

    @Override
    public boolean canConvert(@SuppressWarnings("rawtypes") Class arg)
    {
        return ExcelCellImpl.class == arg;
    }

    @Override
    protected boolean isRelevant(Cell c)
    {
    	return false;
    }
}
