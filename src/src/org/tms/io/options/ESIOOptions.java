package org.tms.io.options;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.tms.api.Column;
import org.tms.api.io.IOFileFormat;
import org.tms.api.io.IOOption;
import org.tms.api.io.TitleableIOOption;

/**
 * The base class that {@link IOOption} that support titles extend.
 * <p>
 * <b>Note</b>: {@code TitledPageIOOptions} methods only affect export operations.
 * <p>
 * @param <T> the type of {@link IOOption} in this {@code TitledPageIOOptions}
 * @since {@value org.tms.api.utils.ApiVersion#IO_ENHANCEMENTS_STR}
 * @version {@value org.tms.api.utils.ApiVersion#CURRENT_VERSION_STR}
 * @see TitleableIOOption
 */
public abstract class ESIOOptions<T extends ESIOOptions<T>> 
    extends BaseIOOptions<T>
    implements IOOption<T>
{
	protected static final String DEFAULT_COMPLETION_FIELD = "suggest";
	
	protected enum Options implements OptionEnum 
    {
    	Index,
        Type,
        DefaultType,
        isDefaultType,
        IdColumn,
        IdUuid,
        IdOrdinal,
    	LowerCaseFieldNames,
    	IgnoreEmptyCells,
    	ExceptionOnEmptyIds,
    	OmitRecordsWithEmptyIds,
    	ExceptionOnDuplicatdeIds,
    	OmitRecordsWithDuplicateIds,
    	CompletionField,
    	Completions,
    }
    
    protected abstract T clone(final ESIOOptions<T> model);
    
    protected ESIOOptions()
    {
        super(IOFileFormat.ES, true, true);
    	set(Options.LowerCaseFieldNames, true);
    	set(Options.IgnoreEmptyCells, true);
    	
    	set(Options.ExceptionOnEmptyIds, true);
    	set(Options.OmitRecordsWithEmptyIds, false);
    	
    	set(Options.ExceptionOnDuplicatdeIds, true);
    	set(Options.OmitRecordsWithDuplicateIds, false);
    	
    	set(Options.IdOrdinal, false);
    	set(Options.IdUuid, false);
    	
    	set(Options.isDefaultType, true);
    	set(Options.DefaultType, "_doc");
    }

    protected ESIOOptions(final ESIOOptions<T> format)
    {
        super(format);
    }

    @Override
    protected T clone(BaseIOOptions<T> model)
    {
        return clone((ESIOOptions<T>) model);
    }
    
    public String getIndex() 
    {
        return (String)get(Options.Index);
    }

    public T withIndex(final String index) 
    {
    	final T newOptions = clone(this);
        newOptions.set(Options.Index, index);
        return newOptions;
    }
    
    public Column getIdColumn() 
    {
        return (Column)get(Options.IdColumn);
    }

    public boolean isIdColumn() 
    {
        return get(Options.IdColumn) != null;
    }

    public T withIdColumn(final Column column) 
    {
    	final T newOptions = clone(this);
        newOptions.set(Options.IdColumn, column);
        newOptions.set(Options.IdUuid, false);
        newOptions.set(Options.IdOrdinal, false);
        return newOptions;
    }
    
    public boolean isIdOrdinal() 
    {
        return (boolean)get(Options.IdOrdinal);
    }

    public T withIdOrdinal()
    {
    	return withIdOrdinal(true);
    }
    
    public T withIdOrdinal(final boolean opt) 
    {
    	final T newOptions = clone(this);
        if (opt) {
        	newOptions.set(Options.IdColumn, null);
            newOptions.set(Options.IdUuid, false);
        }
        
        newOptions.set(Options.IdOrdinal, opt);
        return newOptions;
    }
    
    public boolean isIdUuid() 
    {
        return (boolean)get(Options.IdUuid);
    }

    public T withIdUuid()
    {
    	return withIdUuid(true);
    }
    
    public T withIdUuid(final boolean opt) 
    {
    	final T newOptions = clone(this);
        if (opt) {
        	newOptions.set(Options.IdColumn, null);
            newOptions.set(Options.IdOrdinal, false);
        }
        
        newOptions.set(Options.IdUuid, opt);
        return newOptions;
    }
    
    public boolean isDefaultType()
    {
        return (boolean)get(Options.isDefaultType);
    }
    
    public String getDefaultType()
    {
        return (String)get(Options.DefaultType);
    }
    
    public String getType() 
    {
        return (String)get(Options.Type);
    }

    public T withType(final String dType) 
    {
    	final T newOptions = clone(this);
    	
    	if (dType == null || getDefaultType().equals(dType)) {
            newOptions.set(Options.Type, null);
            newOptions.set(Options.isDefaultType, true);
    	}
    	else {
    		newOptions.set(Options.Type, dType);
            newOptions.set(Options.isDefaultType, false);
    	}
    	
        return newOptions;
    }
    
    public String getWorkingType()
    {
        return isDefaultType() ? getDefaultType() : getType();
    }
           
    public boolean isLowerCaseFieldNames() 
    {
        return (boolean)get(Options.LowerCaseFieldNames);
    }

    public T withLowerCaseFieldNames() 
    {
        return withLowerCaseFieldNames(true);
    }

    public T withLowerCaseFieldNames(final boolean opt) 
    {
    	final T newOptions = clone(this);
        newOptions.set(Options.LowerCaseFieldNames, opt);
        return newOptions;
    }
    
    public boolean isIgnoreEmptyCells() 
    {
        return (boolean)get(Options.IgnoreEmptyCells);
    }

    public T withIgnoreEmptyCells() 
    {
        return withIgnoreEmptyCells(true);
    }

    public T withIgnoreEmptyCells(final boolean opt) 
    {
    	final T newOptions = clone(this);
        newOptions.set(Options.IgnoreEmptyCells, opt);
        return newOptions;
    }
    
    public boolean isExceptionOnEmptyIds() 
    {
        return (boolean)get(Options.ExceptionOnEmptyIds);
    }

    public T withExceptionOnEmptyIds() 
    {
        return withExceptionOnEmptyIds(true);
    }

    public T withExceptionOnEmptyIds(final boolean opt) 
    {
    	final T newOptions = clone(this);
        newOptions.set(Options.ExceptionOnEmptyIds, opt);
        if (opt) newOptions.set(Options.OmitRecordsWithEmptyIds, !opt);
        return newOptions;
    }
    
    public boolean isOmitRecordsWithEmptyIds() 
    {
        return (boolean)get(Options.OmitRecordsWithEmptyIds);
    }

    public T withOmitRecordsWithEmptyIds() 
    {
        return withOmitRecordsWithEmptyIds(true);
    }

    public T withOmitRecordsWithEmptyIds(final boolean opt) 
    {
    	final T newOptions = clone(this);
        newOptions.set(Options.OmitRecordsWithEmptyIds, opt);
        if (opt) newOptions.set(Options.ExceptionOnEmptyIds, !opt);
        return newOptions;
    }
    
    public boolean isExceptionOnDuplicatdeIds() 
    {
        return (boolean)get(Options.ExceptionOnDuplicatdeIds);
    }

    public T withExceptionOnDuplicatdeIds() 
    {
        return withExceptionOnDuplicatdeIds(true);
    }

    public T withExceptionOnDuplicatdeIds(final boolean opt) 
    {
    	final T newOptions = clone(this);
        newOptions.set(Options.ExceptionOnDuplicatdeIds, opt);
        if (opt) newOptions.set(Options.ExceptionOnDuplicatdeIds, !opt);
        return newOptions;
    }
    
    public boolean isOmitRecordsWithDuplicateIds() 
    {
        return (boolean)get(Options.OmitRecordsWithDuplicateIds);
    }

    public T withOmitRecordsWithDuplicateIds() 
    {
        return withOmitRecordsWithDuplicateIds(true);
    }

    public T withOmitRecordsWithDuplicateIds(final boolean opt) 
    {
    	final T newOptions = clone(this);
        newOptions.set(Options.OmitRecordsWithDuplicateIds, opt);
        if (opt) newOptions.set(Options.ExceptionOnDuplicatdeIds, !opt);
        return newOptions;
    }
    
    public boolean isCompletions() 
    {
        return get(Options.Completions) != null;
    }

    @SuppressWarnings("unchecked")
	public List<Column> getCompletions() 
    {
        Set<Column> cols = (Set<Column>)get(Options.Completions);
        if (cols != null)
        	return cols.stream().collect(Collectors.toList());
        else
        	return Collections.emptyList();
    }

    @SuppressWarnings("unchecked")
	public T addCompletion(final Column column) 
    {
    	if (column == null)
    		return (T)this;
    	
    	final T newOptions = clone(this);
    	
    	Set<Column> cols = (Set<Column>)newOptions.get(Options.Completions);
    	if (cols == null) {
    		cols = new LinkedHashSet<Column>();
    		newOptions.set(Options.Completions, cols);
    	}
    	
    	cols.add(column);
		if (!isCompletionField()) newOptions.set(Options.CompletionField, DEFAULT_COMPLETION_FIELD);

        return newOptions;
    }

	public T withCompletions(final Collection<Column> cols) 
    {
    	final T newOptions = clone(this);
    	
    	if (cols == null)
    		this.unset(Options.Completions);
    	else {
    		newOptions.set(Options.Completions, new LinkedHashSet<Column>(cols));
    		if (!isCompletionField()) newOptions.set(Options.CompletionField, DEFAULT_COMPLETION_FIELD);
    	}

		return newOptions;
    }
	
    public String getCompletionField() 
    {
    	return (String)get(Options.CompletionField);
    }

    public boolean isCompletionField() 
    {
    	return null != getCompletionField();
    }

    public T withCompletionField(final String val) 
    {
    	final T newOptions = clone(this);
    	newOptions.set(Options.CompletionField, val);
    	return newOptions;
    }
}
