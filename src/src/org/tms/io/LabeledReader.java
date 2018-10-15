package org.tms.io;

import java.io.File;
import java.io.InputStream;

import org.tms.api.TableContext;
import org.tms.api.io.LabeledIOOption;

abstract public class LabeledReader<E extends LabeledIOOption<?>> extends BaseReader<E>
{    
    LabeledReader(File inputFile, TableContext context, E options) 
    {
    	super(inputFile, context, options);
    }
    
    public LabeledReader(InputStream in, TableContext context, E options)
    {
        super(in, context, options);
    }
    /**
     * Return {@code true} if the Default file contains row names.
     * @return true if the Default file contains row names
     */
    public boolean isRowNames()
    {
        return options().isRowLabels();
    }
    
    /**
     * Return {@code true} if the Default file contains column names.
     * @return true if the Default file contains column names
     */
    public boolean isColumnNames()
    {
        return options().isColumnLabels();
    }   
}
