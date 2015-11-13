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
        Recalculate,
        Validators,
        Descriptions, 
        DisplayFormats,
        Units,
        Tags,
        VerboseState,
        UUIDs
    }

    protected abstract T clone(final ArchivalIOOptions<T> model);
    
    protected ArchivalIOOptions(final IOFileFormat format,
                                final boolean rowNames, 
                                final boolean colNames, 
                                final boolean ignoreEmptyRows, 
                                final boolean ignoreEmptyCols,
                                final boolean withDerivations,
                                final boolean withValidators,
                                final boolean withVerboseState)
    {
        super(format, rowNames, colNames, ignoreEmptyRows, ignoreEmptyCols);

        set(Options.Derivations, withDerivations);
        set(Options.Validators, withValidators);
        set(Options.VerboseState, withVerboseState);
        
        set(Options.Descriptions, true);
        set(Options.DisplayFormats, true);
        set(Options.Units, true);
        set(Options.UUIDs, true);
        set(Options.Tags, true);
        
        set(Options.Recalculate, false);
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

    public boolean isRecalculate()
    {
        return (Boolean)get(Options.Recalculate);
    }
    
    /**
     * {@inheritDoc}
     */
    public T withRecalculate()
    {
        return withRecalculate(true);
    }
    
    /**
     * {@inheritDoc}
     */
    public T withRecalculate(boolean enabled)
    {
        final T newOptions = clone(this);
        newOptions.set(Options.Recalculate, enabled);
        return newOptions;
    }
    
    public boolean isVerboseState()
    {
        return (Boolean)get(Options.VerboseState);
    }
    
    /**
     * {@inheritDoc}
     */
    public T withVerboseState()
    {
        return withVerboseState(true);
    }
    
    /**
     * {@inheritDoc}
     */
    public T withVerboseState(boolean enabled)
    {
        final T newOptions = clone(this);
        newOptions.set(Options.VerboseState, enabled);
        return newOptions;
    }
    
    public boolean isUUIDs()
    {
        return (Boolean)get(Options.UUIDs);
    }
    
    /**
     * {@inheritDoc}
     */
    public T withUUIDs()
    {
        return withUUIDs(true);
    }
    
    /**
     * {@inheritDoc}
     */
    public T withUUIDs(boolean enabled)
    {
        final T newOptions = clone(this);
        newOptions.set(Options.UUIDs, enabled);
        return newOptions;
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
