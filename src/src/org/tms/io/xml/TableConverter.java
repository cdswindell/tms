package org.tms.io.xml;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Set;

import org.tms.api.Column;
import org.tms.api.ElementType;
import org.tms.api.Row;
import org.tms.api.Subset;
import org.tms.api.Table;
import org.tms.api.TableProperty;
import org.tms.api.derivables.Derivable;
import org.tms.api.derivables.Precisionable;
import org.tms.api.derivables.TimeSeriesable;
import org.tms.api.exceptions.TableIOException;
import org.tms.api.factories.TableFactory;
import org.tms.io.LabeledReader;
import org.tms.io.LabeledWriter;
import org.tms.tds.CellImpl;
import org.tms.tds.ColumnImpl;
import org.tms.tds.ContextImpl;
import org.tms.tds.ExternalDependenceTableElement;
import org.tms.tds.RowImpl;
import org.tms.tds.SubsetImpl;
import org.tms.tds.TableImpl;
import org.tms.tds.TableSliceElementImpl;
import org.tms.teq.DerivationImpl;

import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

public class TableConverter extends BaseConverter
{
    static final public String ELEMENT_TAG = "table";
    
    static final protected String NROWS_ATTR = "nRows";
    static final protected String NCOLS_ATTR = "cCols";
    static final protected String ROWSCAP_ATTR = "rCap";
    static final protected String COLSCAP_ATTR = "cCap";
    static final protected String ROWSINCR_ATTR = "rIncr";
    static final protected String COLSINCR_ATTR = "cIncr";
    static final protected String FREESPACE_ATTR = "fsTh";
    static final protected String AUTOCALC_ATTR = "autoCalc";
    static final protected String ROWIDX_ATTR = "rlIdx";
    static final protected String COLIDX_ATTR = "clIdx";
    static final protected String SUBIDX_ATTR = "slIdx";
    static final protected String CELLIDX_ATTR = "cllIdx";
    static final protected String PRECISION_ATTR = "precision";
    
    static final protected String TIME_SERIESED_ROWS_PERIOD_ATTR = "tsRowsPer";
    static final protected String TIME_SERIESED_ROWS_ACTIVE_ATTR = "tsRows";
    
    static final protected String TIME_SERIESED_COLS_PERIOD_ATTR = "tsColsPer";
    static final protected String TIME_SERIESED_COLS_ACTIVE_ATTR = "tsCols";
    
    static final protected String ROWS_TAG = "rows";
    static final protected String COLS_TAG = "columns";
    static final protected String SUBSETS_TAG = "subsets";
    static final protected String CELLS_TAG = "cells";
    
    /*
     * In order to rebuild a row/column/cell, we need access to some methods
     * that are explicitly not available outside of the CellImpl
     * class/package.
     * 
     * We will use Java Reflection to get the method calls we need
     * then mark them as accessible
     */
    static private Method setRowColDerivation = null;
    static {
        try
        {
            // setDerivation(String, boolean) is protected
            setRowColDerivation = TableSliceElementImpl.class.getDeclaredMethod("setDerivation", 
                                                                   new Class<?>[] {String.class, boolean.class});
            setRowColDerivation.setAccessible(true);
        }
        catch (NoSuchMethodException | SecurityException e)
        {
            throw new TableIOException(e);
        }
    }
    
    static private Method setCellDerivation = null;
    static {
        try
        {
            // setDerivation(String, boolean) is protected
            setCellDerivation = CellImpl.class.getDeclaredMethod("setDerivation", 
                                                                  new Class<?>[] {String.class, boolean.class});
            setCellDerivation.setAccessible(true);
        }
        catch (NoSuchMethodException | SecurityException e)
        {
            throw new TableIOException(e);
        }
    }
    
    public TableConverter(LabeledWriter<?> writer)
    {
        super(writer);
    }

    public TableConverter(LabeledReader<?> reader)
    {
        super(reader);
    }

    @Override
    public boolean canConvert(@SuppressWarnings("rawtypes") Class arg)
    {
        return TableImpl.class == arg;
    }

    @Override
    public void marshal(Object arg, HierarchicalStreamWriter writer, MarshallingContext context)
    {
        TableImpl t = (TableImpl)arg;
        
        // if the table is "special", save the converter class
        if (t instanceof ExternalDependenceTableElement)
        	context.put(TMS_TABLE_CONVERTER_KEY, this);
        
        int nRows = getNumConsumableRows();
        int nCols = getNumConsumableColumns();
        
        writer.addAttribute(NROWS_ATTR, String.valueOf(nRows));
        writer.addAttribute(NCOLS_ATTR, String.valueOf(nCols));
        
        // if persisting complete state, add attributes for more details
        if (options().isVerboseState()) {
            writer.addAttribute(ROWSCAP_ATTR, String.valueOf(t.getProperty(TableProperty.numRowsCapacity)));
            writer.addAttribute(COLSCAP_ATTR, String.valueOf(t.getProperty(TableProperty.numColumnsCapacity)));
            writer.addAttribute(AUTOCALC_ATTR, String.valueOf(t.getProperty(TableProperty.isAutoRecalculate)));
            writer.addAttribute(PRECISION_ATTR, String.valueOf(t.getPrecision()));
            writer.addAttribute(FREESPACE_ATTR, String.valueOf(t.getProperty(TableProperty.FreeSpaceThreshold)));
        }        
        
        // Time Series handling
        if (options().isVerboseState() || options().isTimeSeries()) {
        	long tsRowsPeriod = t.getTimeSeriesedRowsPeriodInMilliSeconds();
        	if (tsRowsPeriod > 0) {
                writer.addAttribute(TIME_SERIESED_ROWS_PERIOD_ATTR, String.valueOf(tsRowsPeriod));        		
                writer.addAttribute(TIME_SERIESED_ROWS_ACTIVE_ATTR, String.valueOf(t.getProperty(TableProperty.isTimeSeriesedRowsActive)));
            }
        	
        	Column tsCol = t.getTimeSeriesedRowsTimeStampColumn();
        	if (tsCol != null)
        		context.put(TMS_TS_ROWS_TS_COL_KEY, tsCol);
        	
        	long tsColsPeriod = t.getTimeSeriesedColumnsPeriodInMilliSeconds();
        	if (tsColsPeriod > 0) {
                writer.addAttribute(TIME_SERIESED_COLS_PERIOD_ATTR, String.valueOf(tsColsPeriod));        		
                writer.addAttribute(TIME_SERIESED_COLS_ACTIVE_ATTR, String.valueOf(t.getProperty(TableProperty.isTimeSeriesedColumnsActive)));
        	}
        	
        	Row tsRow = t.getTimeSeriesedColumnsTimeStampRow();
        	if (tsRow != null)
        		context.put(TMS_TS_COLS_TS_ROW_KEY, tsRow);
        }
        
        marshalTableElement(t, writer, context, true);
        
        // Rows
        if (nRows > 0) {
            writer.startNode(ROWS_TAG);
            if (options().isVerboseState()) {
                writer.addAttribute(ROWSINCR_ATTR, String.valueOf(t.getProperty(TableProperty.RowCapacityIncr)));
                if (t.isRowLabelsIndexed())
                	writer.addAttribute(ROWIDX_ATTR, "true");
            }
            
            for (int i = 1; i <= nRows; i++) { 
            	Row r = getRowByEffectiveIndex(i);
            	if (r != null)
            		context.convertAnother(r);
            }
            
            writer.endNode();
        }
        
        // Columns
        if (nCols > 0) {
            writer.startNode(COLS_TAG);
            if (options().isVerboseState()) {
                writer.addAttribute(COLSINCR_ATTR, String.valueOf(t.getProperty(TableProperty.ColumnCapacityIncr)));
                if (t.isColumnLabelsIndexed())
                	writer.addAttribute(COLIDX_ATTR, "true");
            }
            
            for (Column c : getActiveColumns()) {
                context.convertAnother(c);
            }
            
            writer.endNode();
        }
        
        // Subsets
        if (t.getNumSubsets() > 0 && getTableElementType() == ElementType.Table) {
            writer.startNode(SUBSETS_TAG);
            if (options().isVerboseState()) {
                if (t.isSubsetLabelsIndexed())
                	writer.addAttribute(SUBIDX_ATTR, "true");
            }
            
            for (Subset s : t.getSubsets()) {
                context.convertAnother(s);
            }
            
            writer.endNode();
        }        

        // Cells
        if (nRows > 0 && nCols > 0) {
            writer.startNode(CELLS_TAG);
            if (options().isVerboseState()) {
                if (t.isCellLabelsIndexed())
                	writer.addAttribute(CELLIDX_ATTR, "true");
            }
            
            for (Column c : getActiveColumns()) {
                for (Row r : getActiveRows()) {
                    if (t.isCellDefined(r, c)) {
                    	Object o = t.getCell(r,  c);
                        context.convertAnother(o);
                    }
                }
            }
            
            writer.endNode();
        }
    }

	@Override
    public Table unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context)
    {
		// cache the attributes, we can't "go back" once we've read nodes
		cacheAllAttributes(reader);
		
		// create the table
        TableImpl t = createTable(reader, context);
        
        // get the total number of rows/columns
        Integer nRows = readAttributeInteger(NROWS_ATTR, reader);
        Integer nCols = readAttributeInteger(NCOLS_ATTR, reader);
        
        // if full state was persisted, process more attributes
        if (options().isVerboseState()) {
            Double dVal = readAttributeDouble(FREESPACE_ATTR, reader);
            if (dVal != null) t.setFreeSpaceThreshold(dVal);
            
            Boolean bVal = readAttributeBoolean(AUTOCALC_ATTR, reader);
            if (bVal != null) t.setAutoRecalculate(bVal);
            
            if (t instanceof Precisionable) {
                Integer precision = readAttributeInteger(PRECISION_ATTR, reader);
                if (precision != null && precision > 0)
                    ((Precisionable)t).setPrecision(precision);
            }
        }
              
        // Handle time series
        Long tsRowsPeriod = null;
        Boolean tsRowsActive = null;
        Long tsColsPeriod = null;
        Boolean tsColsActive = null;
        if (options().isVerboseState() || options().isTimeSeries()) {
        	tsRowsPeriod = readAttributeLong(TIME_SERIESED_ROWS_PERIOD_ATTR, reader);
        	tsRowsActive = readAttributeBoolean(TIME_SERIESED_ROWS_ACTIVE_ATTR, reader);
        	
        	tsColsPeriod = readAttributeLong(TIME_SERIESED_COLS_PERIOD_ATTR, reader);
        	tsColsActive = readAttributeBoolean(TIME_SERIESED_COLS_ACTIVE_ATTR, reader);
        }
        
        // upon return, we are left in the Rows or Columns or Cells tag
        unmarshalTableElement(t, true, reader, context);
        
        // save the table for others to access
        context.put(TMS_TABLE_KEY, t);
        
        // if the table is "special", save the converter class
        if (t instanceof ExternalDependenceTableElement)
        	context.put(TMS_TABLE_CONVERTER_KEY, this);
        
        // so where are we now?
        String nodeName = reader.getNodeName();
        
        // process rows
        if (ROWS_TAG.equals(nodeName)) {
        	if (options().isVerboseState()) {
	            Integer iVal = readAttributeInteger(ROWSINCR_ATTR, reader);
	            if (iVal != null) t.setRowCapacityIncr(iVal);
	            
	            Boolean bVal = readAttributeBoolean(ROWIDX_ATTR, reader);
	            if (bVal != null) t.setRowLabelsIndexed(bVal);
        	}
            
            nodeName = processChildren(t,  RowImpl.class, reader, context);
        }
        
        if (COLS_TAG.equals(nodeName)) {
        	if (options().isVerboseState()) {
	            Integer iVal = readAttributeInteger(COLSINCR_ATTR, reader);
	            if (iVal != null) t.setColumnCapacityIncr(iVal);            
	            
	            Boolean bVal = readAttributeBoolean(COLIDX_ATTR, reader);
	            if (bVal != null) t.setColumnLabelsIndexed(bVal);
        	}
            
            nodeName = processChildren(t,  ColumnImpl.class, reader, context);
        }
       
        if (SUBSETS_TAG.equals(nodeName)) {
        	if (options().isVerboseState()) {
                Boolean bVal = readAttributeBoolean(SUBIDX_ATTR, reader);
                if (bVal != null) t.setSubsetLabelsIndexed(bVal);
        	}
        	
            nodeName = processChildren(t,  SubsetImpl.class, reader, context);
        }
        
        if (CELLS_TAG.equals(nodeName)) {
        	if (options().isVerboseState()) {
        		Boolean bVal = readAttributeBoolean(CELLIDX_ATTR, reader);
                if (bVal != null) t.setCellLabelsIndexed(bVal);
        	}
        	
            nodeName = processChildren(t,  CellImpl.class, reader, context);           
        }

        // verify the table has the correct number of rows & cols
        if (extendTableRows() && nRows != null && nRows > t.getNumRows())
        	t.addRow(nRows);
        
        if (extendTableColumns() && nCols != null && nCols > t.getNumColumns())
        	t.addColumn(nCols);
        
        // postprocess; this is normally a noop
        postProcessTable(t);
        
        // register annotated dataTypes, if any
		@SuppressWarnings("unchecked")
		Set<Class<?>> toRegisterDataTypes = (Set<Class<?>>)context.get(TMS_TO_REGISTER_CACHE_KEY);
		if (toRegisterDataTypes != null) {
			ContextImpl tc = t.getTableContext();
			if (tc != null) {
				for (Class<?> clazz : toRegisterDataTypes) {
					tc.registerOperators(clazz);
				}
			}
		}

        // process derivations, if any
        if (options().isDerivations()) {
            for (Map.Entry<Derivable, CachedDerivation> e : getDerivationsMap(context).entrySet()) {
                Derivable d = e.getKey();
                CachedDerivation eq = e.getValue();
                
                restoreDerivation(d, eq);
            }
            
            if (options().isRecalculate())
                t.recalculate();
        }
               
        // process time series, if any; do this only we'e extended the table, as it
        // can instantly start appending rows/columns
        if (options().isTimeSeries()) {
        	// reassign the time series formulas to their rows and columns
            for (Map.Entry<TimeSeriesable, CachedDerivation> e : getTimeSeriesMap(context).entrySet()) {
            	TimeSeriesable d = e.getKey();
                CachedDerivation eq = e.getValue();
                
        		d.setTimeSeries(eq.getDerivation());
            }
            
            // enable time series, if we're so told
            if (t.isTimeSeriesedRows() && tsRowsPeriod != null && tsRowsPeriod > 0) {
            	Column tsCol = (Column)context.get(TMS_TS_ROWS_TS_COL_KEY);
            	if (tsRowsActive != null && tsRowsActive.booleanValue() && options().isReenable())
            		t.enableTimeSeriesedRows(tsCol, tsRowsPeriod);
            	else {          	
	            	// TODO: handle restoration of period and time stamp column, even
	            	// if we're not reenabling time seriesed rows
            	}
            }
            
            // Columns Time Series
            if (t.isTimeSeriesedColumns() && tsColsPeriod != null && tsColsPeriod > 0) {
            	Row tsRow = (Row)context.get(TMS_TS_COLS_TS_ROW_KEY);
            	if (tsColsActive != null && tsColsActive.booleanValue() && options().isReenable())
            		t.enableTimeSeriesedColumns(tsRow, tsColsPeriod);
            	else {
	            	// TODO: handle restoration of period and time stamp column, even
	            	// if we're not reenabling time seriesed columns
            	}
            }
        }
               
        return t;
    }
    
	/**
	 * Override as required
	 * @param t Table
	 */
	protected void postProcessTable(TableImpl t) 
	{
		// noop
	}

	protected boolean extendTableRows()
	{
		return true;
	}
	
	protected boolean extendTableColumns()
	{
		return true;
	}
	
	protected TableImpl createTable(HierarchicalStreamReader reader, UnmarshallingContext context) 
	{
        Integer rCap = readAttributeInteger(ROWSCAP_ATTR, reader);
        if (rCap == null)
        	rCap = readAttributeInteger(NROWS_ATTR, reader);
        if (rCap == null)
        	rCap = getTableContext().getPropertyInt(TableProperty.numRowsCapacity);
        
        Integer cCap = readAttributeInteger(COLSCAP_ATTR, reader);
        if (cCap == null)
        	cCap = readAttributeInteger(NCOLS_ATTR, reader);
        if (cCap == null)
        	cCap = getTableContext().getPropertyInt(TableProperty.numColumnsCapacity);
        
		return (TableImpl)TableFactory.createTable(rCap, cCap, getTableContext());
	}
	
	private void restoreDerivation(Derivable d, CachedDerivation eq)
	{
	    try
	    {
	        if (d instanceof TableSliceElementImpl) 
	            setRowColDerivation.invoke(d, eq.getDerivation(), false);
	        else if (d instanceof CellImpl)
                setCellDerivation.invoke(d, eq.getDerivation(), false);
	        
	        if (eq.getPeriod() > 0) {
	        	DerivationImpl dv = (DerivationImpl)d.getDerivation();
	        	if (dv != null)
	        		dv.recalculateEvery(eq.getPeriod());
	        }
	    }
	    catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e)
	    {
	        throw new TableIOException(e);
	    }
	}

    private String processChildren(Table t, Class<?> clazz, HierarchicalStreamReader reader, UnmarshallingContext context)
    {
        String nodeName = reader.getNodeName();
        while (reader.hasMoreChildren()) {
            reader.moveDown();
            context.convertAnother(t, clazz);  
            reader.moveUp();
        }
        
        // we're done with cols, so move out of the "columns" tag
        reader.moveUp();
        
        // set up to process remaining elements (subsets, cells)
        if (reader.hasMoreChildren()) {
            reader.moveDown();            
            nodeName = reader.getNodeName();
        }
        
        return nodeName;
    }
}
