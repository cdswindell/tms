package org.tms.io.options;

import org.tms.api.io.ArchivalIOOption;
import org.tms.api.io.IOFileFormat;
import org.tms.api.io.IOOption;

/**
 * The base class that {@link IOOption} that support display styling extend.
 * Style elements
 * that are supported include:
 */
/**
 * The base class that {@link IOOption} where the output can persist TMS-specific functionality extend. 
 * Elements that are supported include:
 * <ul>
 * <li>derivations,</li> 
 * <li>validators,</li>
 * <li>descriptions,</li>
 * <li>display formats, and</li>
 * <li>units.</li>
 * </ul>
  * <b>Note</b>: {@code ArchivalIOOption} methods affect both import and export operations.
 * <p>
 * @param <T> the type of {@link IOOption} in this {@code ArchivalIOOption}
 * @since {@value org.tms.api.utils.ApiVersion#IO_ENHANCEMENTS_STR}
 * @version {@value org.tms.api.utils.ApiVersion#CURRENT_VERSION_STR}
 */
public abstract class ArchivalIOOptions<T extends ArchivalIOOptions<T>> 
    extends BaseIOOptions<T> 
    implements ArchivalIOOption<T>
{    
    private enum Options implements OptionEnum 
    {
        Derivations,
        Validators,
        Descriptions, 
        DisplayFormats,
        Units,
        Tags,
    }

    protected abstract T clone(final ArchivalIOOptions<T> model);
    
    protected ArchivalIOOptions(final IOFileFormat format,
                                final boolean rowNames, 
                                final boolean colNames, 
                                final boolean ignoreEmptyRows, 
                                final boolean ignoreEmptyCols,
                                final boolean withDerivations,
                                final boolean withValidators)
    {
        super(format, rowNames, colNames, ignoreEmptyRows, ignoreEmptyCols);

        set(Options.Derivations, withDerivations);
        set(Options.Validators, withValidators);
        
        set(Options.Descriptions, true);
        set(Options.DisplayFormats, true);
        set(Options.Units, true);
        set(Options.Tags, true);
    }

    protected ArchivalIOOptions(final ArchivalIOOptions<T> format)
    {
        super(format);
    }

    @Override
    protected T clone(final BaseIOOptions<T> model)
    {
        return clone((ArchivalIOOptions<T>) model);
    }

    /**
     * {@inheritDoc}
     */
    public boolean isTags()
    {
        return (Boolean)get(Options.Tags);
    }
    
    /**
     * {@inheritDoc}
     */
    public T withTags()
    {
        return withTags(true);
    }
    
    /**
     * {@inheritDoc}
     */
    public T withTags(boolean enabled)
    {
        final T newOptions = clone(this);
        newOptions.set(Options.Tags, enabled);
        return newOptions;
    }
    
    /**
     * {@inheritDoc}
     */
    public boolean isDerivations()
    {
        return (Boolean)get(Options.Derivations);
    }
    
    /**
     * {@inheritDoc}
     */
    public T withDerivations()
    {
        return withDescriptions(true);
    }
    
    /**
     * {@inheritDoc}
     */
    public T withDerivations(boolean enabled)
    {
        final T newOptions = clone(this);
        newOptions.set(Options.Derivations, enabled);
        return newOptions;
    }
    
    /**
     * {@inheritDoc}
     */
    public boolean isValidators()
    {
        return (Boolean)get(Options.Validators);
    }
    
    /**
     * {@inheritDoc}
     */
    public T withValidators()
    {
        return withValidators(true);
    }
    
    /**
     * {@inheritDoc}
     */
    public T withValidators(boolean enabled)
    {
        final T newOptions = clone(this);
        newOptions.set(Options.Validators, enabled);
        return newOptions;
    }
    
    /**
     * {@inheritDoc}
     */
    public boolean isDescriptions()
    {
        return (Boolean)get(Options.Descriptions);
    }
    
    /**
     * {@inheritDoc}
     */
    public T withDescriptions()
    {
        return withDescriptions(true);
    }
    
    /**
     * {@inheritDoc}
     */
    public T withDescriptions(boolean enabled)
    {
        final T newOptions = clone(this);
        newOptions.set(Options.Descriptions, enabled);
        return newOptions;
    } 
    
    /**
     * {@inheritDoc}
     */
    public boolean isUnits()
    {
        return (Boolean)get(Options.Units);
    }
    
    /**
     * {@inheritDoc}
     */
    public T withUnits()
    {
        return withDescriptions(true);
    }
    
    /**
     * {@inheritDoc}
     */
    public T withUnits(boolean enabled)
    {
        final T newOptions = clone(this);
        newOptions.set(Options.Units, enabled);
        return newOptions;
    } 
    
    /**
     * {@inheritDoc}
     */
    public boolean isDisplayFormats()
    {
        return (Boolean)get(Options.DisplayFormats);
    }
    
    /**
     * {@inheritDoc}
     */
    public T withDisplayFormats()
    {
        return withDisplayFormats(true);
    }
    
    /**
     * {@inheritDoc}
     */
    public T withDisplayFormats(boolean enabled)
    {
        final T newOptions = clone(this);
        newOptions.set(Options.DisplayFormats, enabled);
        return newOptions;
    } 
}