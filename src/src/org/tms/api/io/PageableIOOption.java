package org.tms.api.io;

/**
 * An {@link BaseIOOptions} where the output can be paged.
 * <p>
 * <b>Note</b>: {@code PageableIOOption} methods only affect export operations.
 * <p>
 * @since {@value org.tms.api.utils.ApiVersion#IO_ENHANCEMENTS_STR}
 * @version {@value org.tms.api.utils.ApiVersion#CURRENT_VERSION_STR}
 * @see FormattedPageIOOptions
 */
public interface PageableIOOption
{
    /**
     * Returns {@code true} if output is to be divided into discrete pages,
     * optionally with page numbers, headers, and footers. Returns {@code false} if output paging has not been enabled.
     * @return {@code true} if output paging is enabled
     */
    public boolean isPaged();
    
    /**
     * Returns {@code true} if paging is enabled and page numbers are to be printed on each page.
     * @return {@code true} if page numbers are to be printed
     */
    public boolean isPageNumbers();
    
    /**
     * Returns the page width, in inches, of the output page.
     * @return the page width, in inches, of the output page
     */
    public int getPageWidth();
    
    /**
     * Returns the page height, in inches, of the output page.
     * @return the page height, in inches, of the output page
     */
    public int getPageHeight();
    
    /**
     * Returns {@code true} if {@link org.tms.api.Row Row} labels are to be repeated on subsequent pages when
     * {@link org.tms.api.Column Column} output spans a single page.
     * @return {@code true} if row labels are to be repeated on subsequent pages
     */
    public boolean isStickyRowLabels();
    
    /**
     * Returns {@code true} if {@link org.tms.api.Column Column} labels are to be repeated at the top
     * of every export page.
     * @return {@code true} if column labels are to be reprinted at the top of every export page
     */
    public boolean isStickyColumnLabels();
}
