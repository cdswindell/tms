package org.tms.api.io;

import org.tms.io.options.BaseIOOptions;

/**
 * An {@link BaseIOOptions} where the output can be paged.
 * Paging elements
 * that are supported include:
 * <ul>
 * <li>page width,</li> 
 * <li>page height,</li>
 * <li>page numbers,</li>
 * <li>paging,</li>
 * <li>sticky row labels,</li>
 * <li>sticky columns labels, and</li>
 * </ul>
 * <p>
 * <b>Note</b>: {@code PageableIOOption} methods only affect export operations.
 * <p>
 * @since {@value org.tms.api.utils.ApiVersion#IO_ENHANCEMENTS_STR}
 * @version {@value org.tms.api.utils.ApiVersion#CURRENT_VERSION_STR}
 */
public interface PageableIOOption<T extends PageableIOOption<T>>
{
    /**
     * Returns {@code true} if output is to be divided into discrete pages,
     * optionally with page numbers, headers, and footers. Returns {@code false} if output paging has not been enabled.
     * @return {@code true} if output paging is enabled
     */
    public boolean isPaged();
    
    /**
     * Enable paged output in exports. {@link org.tms.api.Table Table} data is output in discrete pages,
     * optionally, with column headings repeated on the top of each page, and page numbers and the date-time
     * that the export was performed output on the bottom of each page.
     * @return a new {@link T} with output paging enabled
     * @see DateTimeFormatIOOption#withDateTimeFormat withDateTimeFormat
     * @see PageableIOOption#withPageNumbers withPageNumbers
     * @see PageableIOOption#withStickyColumnLabels withStickyColumnLabels
     */
    public T withPages();
    
    /**
     * Enable or disable paged output in exports. When {@code true},
     * {@link org.tms.api.Table Table} data is output in discrete pages,
     * optionally, with column headings repeated on the top of each page, and page numbers and the date-time
     * that the export was performed output on the bottom of each page.
     * When {@code false}, the output is not paged.
     * @param enabled {@code true} to enable paging, {@code false} to disable paging
     * @return a new {@link T} with output paging enabled or disabled
     * @see DateTimeFormatIOOption#withDateTimeFormat withDateTimeFormat
     * @see PageableIOOption#withPageNumbers withPageNumbers
     * @see PageableIOOption#withStickyColumnLabels withStickyColumnLabels
     */
    public T withPages(boolean enabled);
    
    /**
     * Returns {@code true} if paging is enabled and page numbers are to be printed on each page.
     * @return {@code true} if page numbers are to be printed
     */
    public boolean isPageNumbers();
    
    /**
     * Enable the generation of page numbers in the page footer in the export output.
     * @return a new {@link T} with page numbers enabled
     */
    public T withPageNumbers();
    
    /**
     * Enable or disable page numbers in the footer section of generated export output.
     * @param enabled {@code true} to enable page numbers, {@code false} to disable
     * @return a new {@link T} with page numbers enabled or disabled
     */
    public T withPageNumbers(boolean enabled);
    
    /**
     * Returns the page width, in inches, of the output page.
     * @return the page width, in inches, of the output page
     */
    public int getPageWidth();
    
    /**
     * Set the page width, in inches, in the generated export output.
     * @param width the new page width of the export output, in inches
     * @return a new {@link T} with the specified page width, in inches
     */
    public T withPageWidthInInches(double width);
    
    /**
     * Set the page width, in pixels, in the generated export output.
     * @param width the new page width of the export output, in pixels
     * @return a new {@link T} with the specified page width, in pixels
     */
    public T withPageWidthInPx(int width);
    
    /**
     * Returns the page height, in inches, of the output page.
     * @return the page height, in inches, of the output page
     */
    public int getPageHeight();
    
    /**
     * Set the page height, in inches, in the generated export output.
     * @param height the new page height of the export output, in inches
     * @return a new {@link T} with the specified page height, in inches
     */
    public T withPageHeightInInches(double height);
    
    /**
     * Set the page height, in pixels, in the generated export output.
     * @param height the new page height of the export output, in pixels
     * @return a new {@link T} with the specified page height, in pixels
     */
    public T withPageHeightInPx(int height);
    
    /**
     * Returns {@code true} if {@link org.tms.api.Row Row} labels are to be repeated on subsequent pages when
     * {@link org.tms.api.Column Column} output spans a single page.
     * @return {@code true} if row labels are to be repeated on subsequent pages
     */
    public boolean isStickyRowLabels();
    
    /**
     * Enables sticky {@link org.tms.api.Row Row} labels. This causes {@link org.tms.api.Row Row} labels 
     * to reprint on subsequent export output pages
     * when column data spans more than one page.
     * @return a new {@link T} with sticky row labels enabled
     */
    public T withStickyRowLabels();
    
    /**
     * Enables or disables sticky {@link org.tms.api.Row Row} labels. When enabled,
     * {@link org.tms.api.Row Row} labels are reprinted on subsequent export output pages
     * when column data spans more than one page.
     * @param sticky {@code true} to enable sticky row labels, {@code false} to disable
     * @return a new {@link T} with sticky row labels enabled or disabled
     */
    public T withStickyRowLabels(boolean sticky);
    
    /**
     * Returns {@code true} if {@link org.tms.api.Column Column} labels are to be repeated at the top
     * of every export page.
     * @return {@code true} if column labels are to be reprinted at the top of every export page
     */
    public boolean isStickyColumnLabels();
    
    /**
     * Enables sticky {@link org.tms.api.Column Column} labels. This causes {@link org.tms.api.Column Column} labels 
     * are reprinted at the top of subsequent export output pages
     * when there are more rows in the exported {@link org.tms.api.Table Table} than can fit on a single page.
     * @return a new {@link T} with sticky column labels enabled
     */
    public T withStickyColumnLabels();
    
    /**
     * Enables or disables sticky {@link org.tms.api.Column Column} labels. When enabled,
     * {@link org.tms.api.Column Column} labels are reprinted at the top of subsequent export output pages
     * when there are more rows in the exported {@link org.tms.api.Table Table} than can fit on a single page.
     * @param sticky {@code true} to enable sticky column labels, {@code false} to disable
     * @return a new {@link T} with sticky column labels enabled or disabled
     */
    public T withStickyColumnLabels(boolean sticky);
}
