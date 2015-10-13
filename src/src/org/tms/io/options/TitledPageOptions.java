package org.tms.io.options;

public abstract class TitledPageOptions<T extends TitledPageOptions<T>> 
    extends StyledPageOptions<T>
    implements TitleableOption
{
    private enum Options implements OptionEnum {
        Title,
        TitleFontSize,
    }
    
    protected abstract T clone(TitledPageOptions<T> model);
    
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

    public TitledPageOptions(final TitledPageOptions<T> format)
    {
        super(format);
    }

    @Override
    protected T clone(StyledPageOptions<T> model)
    {
        return clone((TitledPageOptions<T>) model);
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

    public T withTitle(String t)
    {
        T newOption = clone(this);
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
    
    public T withTitleFontSize(int f)
    {
        T newOptions = clone(this);
        newOptions.setTitleFontSize(f);
        return newOptions;
    }
}
