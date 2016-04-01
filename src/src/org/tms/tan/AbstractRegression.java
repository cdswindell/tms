package org.tms.tan;

import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import org.tms.api.Access;
import org.tms.api.Cell;
import org.tms.api.ElementType;
import org.tms.api.Table;
import org.tms.api.TableRowColumnElement;
import org.tms.util.Tuple;

abstract class AbstractRegression implements Iterator<Tuple<Double>>, Iterable<Tuple<Double>>
{

	static AbstractRegression buildRegressionModel(TableRowColumnElement y, List<TableRowColumnElement> x) 
	{
		int numIndependents = x.size();
		
		AbstractRegression model = null;
		if (numIndependents == 1) {
			model = new LinearRegression(y, x);
		}
		
		if (model != null)
			model.loadData();
		
		return model;
	}
	
	abstract String asEquation(Access refType);
	abstract void loadData();

	private TableRowColumnElement m_y;
	private List<TableRowColumnElement> m_x;
	
	private int m_idx;
	private Table m_parentTable;
	private ElementType m_source;
	private int m_maxItems;
	private int m_nItems;
	private Double[] m_values;
	
	AbstractRegression(TableRowColumnElement y, List<TableRowColumnElement> x) 
	{
		m_y = y;
		m_x = x;
		
		m_idx = 1;
		m_nItems = 1 + m_x.size();
		m_source = m_y.getElementType();
		m_parentTable = m_y.getTable();	
		
		if (m_source == ElementType.Row)
			m_maxItems = m_parentTable.getNumColumns();
		else if (m_source == ElementType.Column)
			m_maxItems = m_parentTable.getNumRows();
	}
	

	protected String formatNumber(double val) 
	{
		return String.valueOf(val);
	}

	protected String formatReference(TableRowColumnElement te, Access mode) 
	{
		String prefix = m_source.asReferenceLabel() + " ";
		switch (mode)
		{
			case ByUUID:
				return prefix + quote(te.getUuid());
			
			case ByLabel:
				if (te.isLabeled())
					return prefix + quote(te.getLabel());
				
			// Note: we want to fall thru if there is no label
				
			case ByIndex:
			default:
				return prefix + te.getIndex();
		}
	}


	protected TableRowColumnElement getDependent(int idx) 
	{
		return m_x.get(idx);
	}
	
	private String quote(String val) 
	{
		return "\"" + val + "\"";
	}

	@Override
	public Iterator<Tuple<Double>> iterator() 
	{
		return this;
	}

	@Override
	public boolean hasNext() 
	{
		while (m_idx <= m_maxItems) {
			Cell yCell = m_y.getCell(Access.ByIndex, m_idx);
			if (yCell != null && !yCell.isNull() && yCell.isNumericValue()) {
				m_values = new Double [m_nItems]; 
				m_values[0] = (Double)yCell.getCellValue();
				int idx = 1;
				
				boolean validTuple = true;
				for (TableRowColumnElement tx : m_x) {
					Cell xCell = tx.getCell(Access.ByIndex, m_idx);
					if (xCell != null && !xCell.isNull() && xCell.isNumericValue() ) {
						m_values[idx++] = (Double)xCell.getCellValue();
						continue;
					}
					
					validTuple = false;
					break; // from for loop
				}
				
				if (validTuple)
					return true;
			}
			
			m_idx++;
		}
		
		return false;
	}


	@Override
	public Tuple<Double> next() 
	{
        if (!hasNext())
            throw new NoSuchElementException();
        
        Tuple<Double> values = new Tuple<Double>(m_values);
        m_values = null;
        
		return values;
	}
}
