package org.tms.api.io;

import java.util.Collection;
import java.util.List;

import org.tms.api.Column;

public interface ESIOOption<T extends ESIOOption<T>> extends IOOption<T> 
{
    /**
     * Constant with the most common ElasticSearch export configuration options already set.
     * <p>
     * To include these default values when exporting to ElasticSearch, simply include {@code ESOptions.Default}
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
    public String getIndex(); 
    public T withIndex(final String index); 
    public Column getIdColumn() ;
    public boolean isIdColumn(); 
    public T withIdColumn(final Column column);    
    public boolean isIdOrdinal(); 
    public T withIdOrdinal();
    public T withIdOrdinal(final boolean opt); 
    public boolean isIdUuid(); 
    public T withIdUuid();
    public T withIdUuid(final boolean opt); 
    public boolean isDefaultType(); 
    public String getType(); 
    public String getWorkingType(); 
    public T withType(final String dType); 
    public boolean isLowerCaseFieldNames(); 
    public T withLowerCaseFieldNames();
    public T withLowerCaseFieldNames(final boolean opt); 
    public boolean isIgnoreEmptyCells(); 
    public T withIgnoreEmptyCells(); 
    public T withIgnoreEmptyCells(final boolean opt); 
    public boolean isExceptionOnEmptyIds(); 
    public T withExceptionOnEmptyIds(); 
    public T withExceptionOnEmptyIds(final boolean opt); 
    public boolean isOmitRecordsWithEmptyIds(); 
    public T withOmitRecordsWithEmptyIds(); 
    public T withOmitRecordsWithEmptyIds(final boolean opt); 
    public boolean isExceptionOnDuplicatdeIds(); 
    public T withExceptionOnDuplicatdeIds(); 
    public T withExceptionOnDuplicatdeIds(final boolean opt); 
    public boolean isOmitRecordsWithDuplicateIds(); 
    public T withOmitRecordsWithDuplicateIds(); 
    public T withOmitRecordsWithDuplicateIds(final boolean opt); 
    public boolean isCompletions();
	public List<Column> getCompletions();
	public T addCompletion(final Column column);
	public T withCompletions(final Collection<Column> cols); 


}
