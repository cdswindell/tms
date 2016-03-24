package org.tms.api.io;


/**
 * An {@link IOOption} where the output can persist TMS-specific functionality. 
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
public interface ArchivalIOOption<T extends ArchivalIOOption<T>> extends IOOption<T> 
{   
    /**
     * Returns {@code true} if TMS {@link org.tms.api.derivables.Derivation Derivation}s are imported or exported. 
     * @return {@code true} if TMS {@code Derivation}s are imported and exported
     */
    public boolean isTags();
    
    /**
     * Enable the import and export of TMS {@link org.tms.api.TableElement TableElement}
     * {@link org.tms.api.derivables.Derivation Derivation}s.
     * @return a new {@link T} that is equal to this with {@code Derivation}s import/export enabled.
     */
    public T withTags();
    
    /**
     * Enables or disables the import or export of TMS {@link org.tms.api.TableElement TableElement}
     * {@link org.tms.api.derivables.Derivation Derivation}s.
     * <p>
     * When disabled, only cell values are imported or exported.
     * @param enabled {@code true} to import/export {@code Derivation}s, {@code false} to omit
     * @return a new {@link T} that is equal to this with {@code Derivation}s import/export enabled or disabled.
     */
    public T withTags(boolean enabled);
    
    /**
     * Returns {@code true} if TMS {@link org.tms.api.derivables.Derivation Derivation}s are imported or exported. 
     * @return {@code true} if TMS {@code Derivation}s are imported and exported
     */
    public boolean isDerivations();
    
    /**
     * Enable the import and export of TMS {@link org.tms.api.TableElement TableElement}
     * {@link org.tms.api.derivables.Derivation Derivation}s.
     * @return a new {@link T} that is equal to this with {@code Derivation}s import/export enabled.
     */
    public T withDerivations();
    
    /**
     * Enables or disables the import or export of TMS {@link org.tms.api.TableElement TableElement}
     * {@link org.tms.api.derivables.Derivation Derivation}s.
     * <p>
     * When disabled, only cell values are imported or exported.
     * @param enabled {@code true} to import/export {@code Derivation}s, {@code false} to omit
     * @return a new {@link T} that is equal to this with {@code Derivation}s import/export enabled or disabled.
     */
    public T withDerivations(boolean enabled);
    
    /**
     * Returns {@code true} if TMS {@link org.tms.api.utils.Validatable Validatable}s are imported or exported. 
     * @return {@code true} if TMS {@code Validatable}s are imported and exported
     */
    public boolean isValidators();
    
    /**
     * Enable the import and export of TMS {@link org.tms.api.TableElement TableElement}
     * {@link org.tms.api.utils.Validatable Validatable}s.
     * @return a new {@link T} that is equal to this with {@code Validatable}s import/export enabled.
     */
    public T withValidators();
    
    /**
     * Enables or disables the import or export of TMS {@link org.tms.api.TableElement TableElement}
     * {@link org.tms.api.utils.Validatable Validatable}s.
     * <p>
     * When disabled, only cell values are imported or exported.
     * @param enabled {@code true} to import/export {@code Validatable}s, {@code false} to omit
     * @return a new {@link T} that is equal to this with {@code Validatable}s import/export enabled or disabled.
     */
    public T withValidators(boolean enabled);
    
    /**
     * Returns {@code true} to import/export TMS {@link org.tms.api.TableElement TableElement} Description fields.
     * @return {@code true} if TMS Description fields are imported/exported
     */
    public boolean isDescriptions();

    /**
     * Enables import/export of TMS {@link org.tms.api.TableElement TableElement} Description fields.
     * @return a new {@link T} that is equal to this with Description field import/export enabled
     */
    public T withDescriptions();

    /**
     * Enable or disable the import/export of TMS {@link org.tms.api.TableElement TableElement} Description fields.
     * @param enabled {@code true} to import/export Descriptions, {@code false} to ignore them
     * @return a new {@link T} that is equal to this with Description field import/export enabled or disabled
     */
    public T withDescriptions(boolean enabled);
   
    /**
     * Returns {@code true} to import/export TMS {@link org.tms.api.TableElement TableElement} Units fields.
     * @return {@code true} if TMS Units fields are imported/exported
     */
    public boolean isUnits();

    /**
     * Enables import/export of TMS {@link org.tms.api.TableElement TableElement} Units fields.
     * @return a new {@link T} that is equal to this with Units field import/export enabled
     */
    public T withUnits();

    /**
     * Enable or disable the import/export of TMS {@link org.tms.api.TableElement TableElement} Units fields.
     * @param enabled {@code true} to import/export the Units field, {@code false} to ignore it
     * @return a new {@link T} that is equal to this with Units field import/export enabled or disabled
     */
    public T withUnits(boolean enabled);
    
    /**
     * Returns {@code true} if TMS {@link org.tms.api.derivables.TimeSeries TimeSeries}s are imported or exported. 
     * @return {@code true} if TMS {@code TimeSeries}s are imported and exported
     */
    public boolean isTimeSeries();
    public T withTimeSeries();
    public T withTimeSeries(boolean enabled);
    
    public boolean isVerboseState();
    public T withVerboseState();
    public T withVerboseState(boolean enabled);
    
    public boolean isRecalculate();
    public T withRecalculate();
    public T withRecalculate(boolean enabled);
    
    public boolean isReenable();
    public T withReenable();
    public T withReenable(boolean enabled);
    
    public boolean isDisplayFormats();
    public T withDisplayFormats();
    public T withDisplayFormats(boolean enabled);
    
    public boolean isUUIDs();
    public T withUUIDs();
    public T withUUIDs(boolean enabled);
}
