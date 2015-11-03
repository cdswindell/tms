package org.tms.api.io;

import org.tms.io.options.OptionEnum;
import org.tms.io.options.StyledPageIOOptions;

/**
 * {@code XLSOptions} is a concrete class for representing configuration options to import and export 
 * {@link org.tms.api.Table Table}s from/to MS Excel in xlsx or xls format.
 * <p>
 * @since {@value org.tms.api.utils.ApiVersion#IO_ENHANCEMENTS_STR}
 * @version {@value org.tms.api.utils.ApiVersion#CURRENT_VERSION_STR}
 */
public class XLSOptions extends StyledPageIOOptions<XLSOptions> implements StyleableIOOption<XLSOptions>
{
    static final int DefaultColumnWidthPx = 65;
    static final int DefaultFontSizePx = 12;
    
    /**
     * Constant with the most common MS Excel import and export configuration options already set.
     * The values for the various configuration options are defined as follows:
     * <ul>
     * <li>Row Labels: <b>{@code true}</b></li>
     * <li>Column Labels: <b>{@code true}</b></li>
     * <li>Ignore Empty Rows: <b>{@code false}</b></li>
     * <li>Ignore Empty Columns: <b>{@code false}</b></li>
     * <li>Default Column Width: <b>{@code 65px}</b></li>
     * <li>Default Font Size: <b>{@code 12px}</b></li>
     * <li>File Format: <b>{@code xlsx}</b></li>
     * <li>Interpret Blank Cells as Nulls: <b>{@code true}</b></li>
     * <li>Import/Export Formulas/Derivations: <b>{@code true}</b></li>
     * <li>Import/Export Comments/Descriptions: <b>{@code true}</b></li>
     * </ul>
     * <p>
     * To include these default values when exporting to MS Excel, simply include {@code XLSOptions.Default}
     * in the import factory method or supporting {@link org.tms.api.TableElement TableElement} export method.
     * @see org.tms.api.Table#export(String, IOOption) Table#export(String, IOOption)
     * @see org.tms.api.Table#export(java.io.OutputStream, IOOption) Table#export(java.io.OutputStream, IOOption)
     * @see org.tms.api.Row#export(String, IOOption) Row#export(String, IOOption)
     * @see org.tms.api.Row#export(java.io.OutputStream, IOOption) Row#export(java.io.OutputStream, IOOption)
     * @see org.tms.api.Column#export(String, IOOption) Column#export(String, IOOption)
     * @see org.tms.api.Column#export(java.io.OutputStream, IOOption) Column#export(java.io.OutputStream, IOOption)
     * @see org.tms.api.Subset#export(String, IOOption) Subset#export(String, IOOption)
     * @see org.tms.api.Subset#export(java.io.OutputStream, IOOption) Subset#export(java.io.OutputStream, IOOption)
     */
    public static final XLSOptions Default = new XLSOptions(true, true, false, false, DefaultColumnWidthPx, DefaultFontSizePx);

    private enum Options implements OptionEnum 
    {
        FileFormat,
        BlanksAsNull,
        Derivations,
        Descriptions, 
        CommentAuthor
    }

    private enum ExcelFileFormat  
    {
        XLS,
        XLSX,
    }

    private XLSOptions(final boolean rowNames, 
                      final boolean colNames, 
                      final boolean ignoreEmptyRows, 
                      final boolean ignoreEmptyCols,
                      final int colWidthPx,
                      final int defaultFontSize)
    {
        super(IOFileFormat.EXCEL, rowNames, colNames, ignoreEmptyRows, ignoreEmptyCols,
                colWidthPx, defaultFontSize, null);
        
        set(Options.FileFormat, ExcelFileFormat.XLSX);
        set(Options.BlanksAsNull, true);
        set(Options.Derivations, true);
        set(Options.Descriptions, true);
    }
    
    private XLSOptions (final XLSOptions format)
    {
        super(format);
    }
    
    @Override
    protected XLSOptions clone(final StyledPageIOOptions<XLSOptions> model)
    {
        return new XLSOptions((XLSOptions)model);
    }
    
    /**
     * When exporting, generates an MS Excel <b>xls</b> file, 
     * readable by older versions of MS Excel. When importing, accepts the older-style MS Excel <b>xls</b>
     * file format.
     * @return a new {@link XLSOptions} that is equal to this with the format set to {@code xls}.
     */
    public XLSOptions withXlsFormat()
    {
        final XLSOptions newOptions = clone(this);
        newOptions.set(Options.FileFormat, ExcelFileFormat.XLS);
        return newOptions;
    }
    
    /**
     * When exporting, generates an MS Excel <b>xlsx</b> file, 
     * readable by recent versions of MS Excel. When importing, accepts the present-style MS Excel <b>xlsx</b>
     * file format.
     * @return a new {@link XLSOptions} that is equal to this with the format set to {@code xlsx}.
     */
    public XLSOptions withXlsXFormat()
    {
        final XLSOptions newOptions = clone(this);
        newOptions.set(Options.FileFormat, ExcelFileFormat.XLSX);
        return newOptions;
    }
    
    /**
     * Returns {@code true} if this {@link XLSOptions} is configured to accept/generate MS Excel <b>xls</b> format files.
     * @return {@code true} if this {@code XLSOptions} is configured to accept/generate MS Excel <b>xls</b> format files
     */
    public boolean isXlsFormat()
    {
        return get(Options.FileFormat) == ExcelFileFormat.XLS;
    }
    
    /**
     * Returns {@code true} if this {@link XLSOptions} is configured to accept/generate MS Excel <b>xlsx</b> format files.
     * @return {@code true} if this {@code XLSOptions} is configured to accept/generate MS Excel <b>xlsx</b> format files
     */
    public boolean isXlsXFormat()
    {
        return get(Options.FileFormat) == ExcelFileFormat.XLSX;
    }
    
    /**
     * Returns {@code true} if Excel cells that contain only blank (white space) characters should be interpreted 
     * as {@code null} values in TMS {@link org.tms.api.Table Table} {@link org.tms.api.Cell Cell}s. Only affects import operations.
     * @return {@code true} if Excel cells containing only blank values should be treated as {@code null} {@code Cell} values in TMS
     */
    public boolean isBlanksAsNull()
    {
        return (Boolean)get(Options.BlanksAsNull);
    }
    
    /**
     * Enables the treatment of blank MS Excel cells as {@code null} values in TMS {@link org.tms.api.Table Table} 
     * {@link org.tms.api.Cell Cell}s. Only affects import operations.
     * @return a new {@link XLSOptions} that is equal to this with Blanks as Nulls set to  {@code true}.
     */
    public XLSOptions withBlanksAsNull()
    {
        return withBlanksAsNull(true);
    }
    
    /**
     * Enables or disables the treatment of blank MS Excel cells as {@code null} values in TMS {@link org.tms.api.Table Table} 
     * {@link org.tms.api.Cell Cell}s. Only affects import operations.
     * @param enabled {@code true} to import blank Excel cells as null-valued TMS {@code Cell}s, {@code false}
     * to import them as blank strings
     * @return a new {@link XLSOptions} that is equal to this with Blanks as Nulls enabled or disabled.
     */
    public XLSOptions withBlanksAsNull(boolean enabled)
    {
        final XLSOptions newOptions = clone(this);
        newOptions.set(Options.BlanksAsNull, enabled);
        return newOptions;
    }
    
    /**
     * Returns {@code true} if TMS {@link org.tms.api.derivables.Derivation Derivation}s are exported to Excel cell formulas, and if
     * Excel cell formulas are imported to TMS {@link org.tms.api.derivables.Derivation Derivation}s. 
     * @return {@code true} if TMS {@code Derivation}s are exported and Excel formulas are imported
     */
    public boolean isDerivations()
    {
        return (Boolean)get(Options.Descriptions);
    }
    
    /**
     * Enable the import of Excel cell formulas as TMS {@link org.tms.api.TableElement TableElement}
     * {@link org.tms.api.derivables.Derivation Derivation}s, and the export of {@link org.tms.api.derivables.Derivation Derivation}s
     * to Excel cell formulas.
     * @return a new {@link XLSOptions} that is equal to this with {@code Derivation}s import/export enabled.
     */
    public XLSOptions withDerivations()
    {
        return withDescriptions(true);
    }
    
    /**
     * Enables or disables translation of MS Excel cell formulas to TMS {@link org.tms.api.TableElement TableElement}
     * {@link org.tms.api.derivables.Derivation Derivation}s on import, and {@link org.tms.api.derivables.Derivation Derivation}s
     * to Excel cell formulas on export. When enabled, TMS {@link org.tms.api.derivables.Derivation Derivation} expressions are translated to 
     * Excel format and, in the case of a derived {@link org.tms.api.Row Row} or {@link org.tms.api.Column Column}, set as the cell
     * formula for each {@link org.tms.api.Cell Cell} in the parent {@link org.tms.api.Row Row} or {@link org.tms.api.Column Column}. 
     * In the case of import, attempts are made to <i>roll up</i> repeated Excel cell formuli into a derived
     * {@link org.tms.api.Row Row} or {@link org.tms.api.Column Column}.
     * <p>
     * When disabled, only cell values are imported or exported.
     * @param enabled {@code true} to import/export {@code Derivation}s, {@code false} to omit
     * @return a new {@link XLSOptions} that is equal to this with {@code Derivation}s import/export enabled or disabled.
     */
    public XLSOptions withDerivations(boolean enabled)
    {
        final XLSOptions newOptions = clone(this);
        newOptions.set(Options.Derivations, enabled);
        return newOptions;
    }
    
    /**
     * Returns {@code true} to translate TMS {@link org.tms.api.TableElement TableElement} Description fields to Excel cell comments on export, 
     * and if Excel cell comments will populate the Description fields on import.
     * @return {@code true} if TMS Description fields are imported to/exported from Excel cell comments
     */
    public boolean isDescriptions()
    {
        return (Boolean)get(Options.Descriptions);
    }
    
    /**
     * Enables the translation of TMS {@link org.tms.api.TableElement TableElement} Description fields to Excel cell comments on export, 
     * and the translation of Excel cell comments to TMS Description fields on import.
     * @return a new {@link XLSOptions} that is equal to this with Derivation/cell comment support enabled
     */
    public XLSOptions withDescriptions()
    {
        return withDescriptions(true);
    }
    
    /**
     * Enable or disable the translation of TMS {@link org.tms.api.TableElement TableElement} Description fields to Excel cell comments on export, 
     * and the translation ofExcel cell comments to TMS Description fields on import.
     * @param enabled {@code true} to exchange Descriptions with Excel comments, {@code false} to ignore comments and descriptions
     * @return a new {@link XLSOptions} that is equal to this with Derivation/cell comment support enabled or disabled
     */
    public XLSOptions withDescriptions(boolean enabled)
    {
        final XLSOptions newOptions = clone(this);
        newOptions.set(Options.Descriptions, enabled);
        return newOptions;
    }
    
    /**
     * Returns {@code true} if a comment author has been defined. In Excel, the name of the logged in user is prepended to all cell comments.
     * In TMS, {@link XLSOptions#withCommentAuthor(String) withCommentAuthor(String)} is used to set the author name string that is prepended
     * to TMS {@link org.tms.api.TableElement TableElement} Description fields when they are exported to Excel cell comments.
     * @return {@code true} if a comment author has been defined
     * @see XLSOptions#withCommentAuthor(String) withCommentAuthor(String)
     * @see XLSOptions#withDescriptions() withDescriptions()
     * @see XLSOptions#withDescriptions(boolean) withDescriptions(boolean)
     */
    public boolean isCommentAuthor()
    {
        return get(Options.CommentAuthor) != null;
    }
    
    /**
     * Returns the defined Comment Author string prepended to {@link org.tms.api.TableElement TableElement} 
     * Description fields when they are exported to Excel cell comments, or {@code null} if none has been defined.
     * @return the defined Comment Author string
     */
    public String getCommentAuthor()
    {
        return (String)get(Options.CommentAuthor);
    }
    
    /**
     * Sets the Comment Author string to prepend to {@link org.tms.api.TableElement TableElement} 
     * Description fields when they are exported to Excel cell comments. 
     * Set to {@code null} to omit a Comment Author string when exporting descriptions as comments.
     * @param author the new Comment Author, or {@code null} to disable
     * @return a new {@link XLSOptions} that is equal to this with the specified Comment Author string
     */
    public XLSOptions withCommentAuthor(String author)
    {
        final XLSOptions newOptions = clone(this);
        newOptions.set(Options.CommentAuthor, author);
        return newOptions;
    }
}
