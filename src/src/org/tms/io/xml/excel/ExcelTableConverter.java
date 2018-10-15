package org.tms.io.xml.excel;

import java.io.File;
import java.io.IOException;

import org.tms.api.TableElement;
import org.tms.api.exceptions.TableIOException;
import org.tms.api.factories.TableFactory;
import org.tms.api.io.XLSOptions;
import org.tms.io.LabeledReader;
import org.tms.io.LabeledWriter;
import org.tms.io.xml.ExternalDependenceTableConverter;
import org.tms.tds.TableImpl;
import org.tms.tds.excel.ExcelTableImpl;

import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

public class ExcelTableConverter extends ExternalDependenceTableConverter
{
    static final public String ELEMENT_TAG = "exTable";
    
    public ExcelTableConverter(LabeledReader<?> reader)
    {
        super(reader);
    }

    public ExcelTableConverter(LabeledWriter<?> writer)
    {
        super(writer);
    }

    @Override
    public boolean canConvert(@SuppressWarnings("rawtypes") Class arg)
    {
        return ExcelTableImpl.class == arg;
    }

    @Override
    protected void marshalClassSpecificElements(TableElement te, HierarchicalStreamWriter writer, MarshallingContext context)
    {
        ExcelTableImpl exTe = (ExcelTableImpl)te;
        
        super.marshalClassSpecificElements(te, writer, context);
        
        writeNode(exTe.getExcelFile(), "exFile", writer, context);
        writeNode(exTe.getOptions(), "exOpts", writer, context);
        writeNode(exTe.getSheetName(), "exSheet", writer, context);
    }
    
    @Override
    protected TableImpl createTable(HierarchicalStreamReader reader, UnmarshallingContext context) 
    {
		File exFile = null;
		XLSOptions exOpts = null;
		String exSheet = null;
		int processingExcelTags = 3;
    	while (processingExcelTags > 0 && reader.hasMoreChildren()) {
    		reader.moveDown();
    		String nodeName = reader.getNodeName();
    		switch (nodeName) {
				case "exFile":
					exFile = (File)context.convertAnother(null, File.class);
					processingExcelTags--;
					break;
					
				case "exOpts":
					exOpts = (XLSOptions)context.convertAnother(null, XLSOptions.class);
					processingExcelTags--;
					break;
					
				case "exSheet":
					exSheet = (String)context.convertAnother(null, String.class);
					processingExcelTags--;
					break;
				
			    default:
			    	processingExcelTags = 0;
			    	break;
    		}

            reader.moveUp();
    	}
  
    	// we need a connection url and a query to proceed
		if (exFile == null)
			throw new TableIOException("Excel File Required");
		
    	// we need a connection url and a query to proceed
		if (exSheet == null)
			throw new TableIOException("Excel Sheet Name Required");
		
    	try {
			ExcelTableImpl t = (ExcelTableImpl) TableFactory.createExcelTable(exFile, exOpts, exSheet, getTableContext());
			cacheDimensions(t);
			return t;
		} 
    	catch (IOException e) {
			throw new TableIOException(e);
		}
    }
    
	protected void postProcessTable(TableImpl t) 
	{
		// process row and column labels
		((ExcelTableImpl)t).excelColumns().forEach(c -> c.getLabel());
		((ExcelTableImpl)t).excelRows().forEach(r -> r.getLabel());
	}


}
