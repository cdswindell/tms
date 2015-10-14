package org.tms.io.options;

public abstract class TitledPageOptions<E extends TitledPageOptions<E>> 
    extends StyledPageOptions<E>
    implements TitleableOption
{
    private enum Options implements OptionEnum {
        Title,
        TitleFontSize,
    }
    
    protected abstract E clone(TitledPageOptions<E> model);
    
    protected TitledPageOptions(final org.tms.io.options.IOOptions.FileFormat format,
            final boolean rowNames, 
            final boolean colNames, 
            final boolean ignoreEmptyRows, 
            final boolean ignoreEmptyCols,
            final int colWidthPx,
            final int defaultFontSize,
            final String defaultFontFamily)
    {
        super(format, rowNames, colNames, 
              ignoreEmptyRows, ignoreEmptyCols, colWidthPx, defaultFontSize, defaultFontFamily);
    }

    public TitledPageOptions(final TitledPageOptions<E> format)
    {
        super(format);
    }

    @Override
    protected E clone(StyledPageOptions<E> model)
    {
        return clone((TitledPageOptions<E>) model);
    }

    @Override
    public String getTitle()
    {
        return (String)get(Options.Title);
    }

    protected void setTitle(String t)
    {
        set(Options.Title, t);
    }

    @Override
    public boolean hasTitle()
    {
        String title = getTitle();
        return title != null && title.trim().length() > 0;
    }

    public E withTitle(String t)
    {
        E newOption = clone(this);
        newOption.set(Options.Title, t);
        return newOption;
    }
    
    @Override
    public int getTitleFontSize()
    {
        Object d = get(Options.TitleFontSize);
        return d != null ? (int)d : (int)(getDefaultFontSize() * 2);
    }

    protected void setTitleFontSize(int i)
    {
        if (i < 4)
            throw new IllegalArgumentException("Title font size must be at least 4px");

        set(Options.TitleFontSize, i);
    }
    
    public E withTitleFontSize(int f)
    {
        E newOptions = clone(this);
        newOptions.setTitleFontSize(f);
        return newOptions;
    }
}
