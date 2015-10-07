package org.tms.api.io.options;

import org.tms.io.options.IOOptions;

public class XMLOptions extends IOOptions
{

    public static final XMLOptions Default = new XMLOptions(true, true, false, false);

    private XMLOptions(final boolean rowNames, 
                       final boolean colNames, 
                       final boolean ignoreEmptyRows, 
                       final boolean ignoreEmptyCols)
    {
        super(org.tms.io.options.IOOptions.FileFormat.XML, rowNames, colNames, ignoreEmptyRows, ignoreEmptyCols);
    }
    
    private XMLOptions (final XMLOptions format)
    {
        super(format);
    }
    
    @Override
    public XMLOptions withRowNames()
    {
        return withRowNames(true);
    }
    
    @Override
    public XMLOptions withRowNames(final boolean b)
    {
        XMLOptions newOptions = new XMLOptions(this);
        newOptions.setRowNames(b);
        return newOptions;
    }
    
    @Override
    public XMLOptions withColumnNames()
    {
        return withColumnNames(true);
    }
    
    @Override
    public XMLOptions withColumnNames(final boolean b)
    {
        XMLOptions newOptions = new XMLOptions(this);
        newOptions.setColumnNames(b);
        return newOptions;
    }

    @Override
    public XMLOptions withIgnoreEmptyRows()
    {
        return withIgnoreEmptyRows(true);
    }
    
    @Override
    public XMLOptions withIgnoreEmptyRows(final boolean b)
    {
        XMLOptions newOptions = new XMLOptions(this);
        newOptions.setIgnoreEmptyRows(b);
        return newOptions;
    } 

    @Override
    public XMLOptions withIgnoreEmptyColumns()
    {
        return withIgnoreEmptyColumns(true);
    }

    @Override
    public XMLOptions withIgnoreEmptyColumns(final boolean b)
    {
        XMLOptions newOptions = new XMLOptions(this);
        newOptions.setIgnoreEmptyColumns(b);
        return newOptions;
    } 
}
