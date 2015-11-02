package org.tms.api.io;



public class XMLOptions extends BaseIOOptions<XMLOptions>
{
    public static final XMLOptions Default = new XMLOptions(true, true, false, false);

    private XMLOptions(final boolean rowNames, 
                       final boolean colNames, 
                       final boolean ignoreEmptyRows, 
                       final boolean ignoreEmptyCols)
    {
        super(IOFileFormat.XML, rowNames, colNames, ignoreEmptyRows, ignoreEmptyCols);
    }
    
    private XMLOptions (final XMLOptions format)
    {
        super(format);
    }
    
    @Override
    protected XMLOptions clone(final BaseIOOptions<XMLOptions> model)
    {
        return new XMLOptions((XMLOptions)model);
    }
    
    @Override
    /**
     * {@inheritDoc} 
     */
    public XMLOptions withRowLabels()
    {
        return withRowLabels(true);
    }
    
    @Override
    /**
     * {@inheritDoc} 
     */
    public XMLOptions withRowLabels(final boolean b)
    {
        XMLOptions newOptions = new XMLOptions(this);
        newOptions.setRowLabels(b);
        return newOptions;
    }
    
    @Override
    /**
     * {@inheritDoc} 
     */
    public XMLOptions withColumnLabels()
    {
        return withColumnLabels(true);
    }
    
    @Override
    /**
     * {@inheritDoc} 
     */
    public XMLOptions withColumnLabels(final boolean b)
    {
        XMLOptions newOptions = new XMLOptions(this);
        newOptions.setColumnLabels(b);
        return newOptions;
    }

    @Override
    /**
     * {@inheritDoc} 
     */
    public XMLOptions withIgnoreEmptyRows()
    {
        return withIgnoreEmptyRows(true);
    }
    
    @Override
    /**
     * {@inheritDoc} 
     */
    public XMLOptions withIgnoreEmptyRows(final boolean b)
    {
        XMLOptions newOptions = new XMLOptions(this);
        newOptions.setIgnoreEmptyRows(b);
        return newOptions;
    } 

    @Override
    /**
     * {@inheritDoc} 
     */
    public XMLOptions withIgnoreEmptyColumns()
    {
        return withIgnoreEmptyColumns(true);
    }

    @Override
    /**
     * {@inheritDoc} 
     */
    public XMLOptions withIgnoreEmptyColumns(final boolean b)
    {
        XMLOptions newOptions = new XMLOptions(this);
        newOptions.setIgnoreEmptyColumns(b);
        return newOptions;
    } 
}
