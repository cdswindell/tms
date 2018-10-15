package org.tms.api.io;

import org.tms.api.Column;
import org.tms.io.options.BaseIOOptions;
import org.tms.io.options.OptionEnum;

public class ESOptions extends BaseIOOptions<ESOptions> implements IOOption<ESOptions>
{
    public static final ESOptions Default = new ESOptions();

    private enum Options implements OptionEnum 
    {
    	Index,
        Type,
        IdColumn,
        IdUuid,
        IdOrdinal,
    	LowerCaseFieldNames,
    	IgnoreEmptyCells,
    	ExceptionOnEmptyIds,
    	OmitRecordsWithEmptyIds,
    	ExceptionOnDuplicatdeIds,
    	OmitRecordsWithDuplicateIds,
    }
    
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
    private ESOptions()
    {
        super(IOFileFormat.ES, true, true);
    	set(Options.LowerCaseFieldNames, true);
    	set(Options.IgnoreEmptyCells, true);
    	
    	set(Options.ExceptionOnEmptyIds, true);
    	set(Options.OmitRecordsWithEmptyIds, false);
    	
    	set(Options.ExceptionOnDuplicatdeIds, true);
    	set(Options.OmitRecordsWithDuplicateIds, false);
    }
    
    private ESOptions (final ESOptions format)
    {
        super(format);
    }
    
    @Override
    protected ESOptions clone(final BaseIOOptions<ESOptions> model)
    {
        return new ESOptions((ESOptions)model);
    }    
    
    public String getIndex() 
    {
        return (String)get(Options.Index);
    }

    public ESOptions withIndex(final String index) 
    {
    	ESOptions newOptions = new ESOptions(this);
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

    public ESOptions withIdColumn(final Column column) 
    {
    	ESOptions newOptions = new ESOptions(this);
        newOptions.set(Options.IdColumn, column);
        newOptions.set(Options.IdUuid, false);
        newOptions.set(Options.IdOrdinal, false);
        return newOptions;
    }
    
    public boolean isIdOrdinal() 
    {
        return (boolean)get(Options.IdOrdinal);
    }

    public ESOptions withIdOrdinal()
    {
    	return withIdOrdinal(true);
    }
    
    public ESOptions withIdOrdinal(final boolean opt) 
    {
    	ESOptions newOptions = new ESOptions(this);
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

    public ESOptions withIdUuid()
    {
    	return withIdUuid(true);
    }
    
    public ESOptions withIdUuid(final boolean opt) 
    {
    	ESOptions newOptions = new ESOptions(this);
        if (opt) {
        	newOptions.set(Options.IdColumn, null);
            newOptions.set(Options.IdOrdinal, false);
        }
        
        newOptions.set(Options.IdUuid, opt);
        return newOptions;
    }
    
    public String getType() 
    {
        return (String)get(Options.Type);
    }

    public ESOptions withType(final String dType) 
    {
    	ESOptions newOptions = new ESOptions(this);
        newOptions.set(Options.Type, dType);
        return newOptions;
    }
        
    public boolean isLowerCaseFieldNames() 
    {
        return (boolean)get(Options.LowerCaseFieldNames);
    }

    public ESOptions withLowerCaseFieldNames() 
    {
        return withLowerCaseFieldNames(true);
    }

    public ESOptions withLowerCaseFieldNames(final boolean opt) 
    {
    	ESOptions newOptions = new ESOptions(this);
        newOptions.set(Options.LowerCaseFieldNames, opt);
        return newOptions;
    }
    
    public boolean isIgnoreEmptyCells() 
    {
        return (boolean)get(Options.IgnoreEmptyCells);
    }

    public ESOptions withIgnoreEmptyCells() 
    {
        return withIgnoreEmptyCells(true);
    }

    public ESOptions withIgnoreEmptyCells(final boolean opt) 
    {
    	ESOptions newOptions = new ESOptions(this);
        newOptions.set(Options.IgnoreEmptyCells, opt);
        return newOptions;
    }
    
    public boolean isExceptionOnEmptyIs() 
    {
        return (boolean)get(Options.ExceptionOnEmptyIds);
    }

    public ESOptions withExceptionOnEmptyIds() 
    {
        return withExceptionOnEmptyIds(true);
    }

    public ESOptions withExceptionOnEmptyIds(final boolean opt) 
    {
    	ESOptions newOptions = new ESOptions(this);
        newOptions.set(Options.ExceptionOnEmptyIds, opt);
        if (opt) newOptions.set(Options.OmitRecordsWithEmptyIds, !opt);
        return newOptions;
    }
    
    public boolean isOmitRecordsWithEmptyIds() 
    {
        return (boolean)get(Options.OmitRecordsWithEmptyIds);
    }

    public ESOptions withOmitRecordsWithEmptyIds() 
    {
        return withOmitRecordsWithEmptyIds(true);
    }

    public ESOptions withOmitRecordsWithEmptyIds(final boolean opt) 
    {
    	ESOptions newOptions = new ESOptions(this);
        newOptions.set(Options.OmitRecordsWithEmptyIds, opt);
        if (opt) newOptions.set(Options.ExceptionOnEmptyIds, !opt);
        return newOptions;
    }
    
    public boolean isExceptionOnDuplicatdeIds() 
    {
        return (boolean)get(Options.ExceptionOnDuplicatdeIds);
    }

    public ESOptions withExceptionOnDuplicatdeIds() 
    {
        return withExceptionOnDuplicatdeIds(true);
    }

    public ESOptions withExceptionOnDuplicatdeIds(final boolean opt) 
    {
    	ESOptions newOptions = new ESOptions(this);
        newOptions.set(Options.ExceptionOnDuplicatdeIds, opt);
        if (opt) newOptions.set(Options.ExceptionOnDuplicatdeIds, !opt);
        return newOptions;
    }
    
    public boolean isOmitRecordsWithDuplicateIds() 
    {
        return (boolean)get(Options.OmitRecordsWithDuplicateIds);
    }

    public ESOptions withOmitRecordsWithDuplicateIds() 
    {
        return withOmitRecordsWithDuplicateIds(true);
    }

    public ESOptions withOmitRecordsWithDuplicateIds(final boolean opt) 
    {
    	ESOptions newOptions = new ESOptions(this);
        newOptions.set(Options.OmitRecordsWithDuplicateIds, opt);
        if (opt) newOptions.set(Options.ExceptionOnDuplicatdeIds, !opt);
        return newOptions;
    }
}
