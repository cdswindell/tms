package org.tms.tan;

import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import org.tms.api.Access;
import org.tms.api.Cell;
import org.tms.api.ElementType;
import org.tms.api.Table;
import org.tms.api.TableElement;
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
		else {
			model = new MultipleLinearRegression(y, x);
		}
		
		if (model != null)
			model.loadData();
		
		return model;
	}
	
	abstract void loadData();
	abstract double [] getRegressionParameters();
	abstract double[] getCorrelationCoefficients();
	abstract double getR2();

	private TableRowColumnElement m_y;
	private List<TableRowColumnElement> m_x;
	
	private int m_idx;
	private Table m_parentTable;
	private ElementType m_source;
	private int m_maxItems;
	private int m_numDependents;
	private int m_numObservations;
	private Double[] m_values;
	
	AbstractRegression(TableRowColumnElement y, List<TableRowColumnElement> x) 
	{
		m_y = y;
		m_x = x;
		
		m_idx = 1;
		m_numObservations = -1;
		m_numDependents = m_x.size();
		m_source = m_y.getElementType();
		m_parentTable = m_y.getTable();	
		
		if (m_source == ElementType.Row)
			m_maxItems = m_parentTable.getNumColumns();
		else if (m_source == ElementType.Column)
			m_maxItems = m_parentTable.getNumRows();
	}
	
	public int getNumDependent()
	{
		return m_numDependents;
	}

	public int getNumObservations()
	{
		if (m_numObservations < 0) {
			m_numObservations = 0;
			for (@SuppressWarnings("unused") Tuple<Double> t : this)
				m_numObservations++;
		}
		
		return m_numObservations;
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

	String asEquation(Access refType) 
	{
		StringBuffer sb = new StringBuffer();
		
		double [] rParams = getRegressionParameters();
		
		for (int i = 0; i < m_numDependents; i++) {
			double coef = rParams[i + 1];
			TableRowColumnElement te = m_x.get(i);
			
			if (i > 0) {
				sb.append(" ");
				
				if (coef > 0)
					sb.append("+ ");
				else if (coef < 0)
					sb.append("- ");
			}
			else if (coef < 0)
				sb.append("-");				
			
			sb.append(formatNumber(Math.abs(coef)));
			sb.append(" * ");
			sb.append(formatReference(te, refType));
		}		
		
		double intercept = rParams[0];
		if (intercept < 0.0)
			sb.append(" - ");
		else if (intercept > 0.0)
			sb.append(" + ");
		else
			return sb.toString(); // don't try to append intercept
		
		sb.append(formatNumber(Math.abs(intercept)));
		return sb.toString();
	}
	
	@Override
	public Iterator<Tuple<Double>> iterator() 
	{
		m_idx = 1;
		return this;
	}

	private boolean isValid(Cell cell, TableRowColumnElement te)
	{
		if (cell != null && !cell.isNull() && cell.isNumericValue()) {
			if (cell.isDerived()) {
				List<TableElement> affects = cell.getAffectedBy();
				if (affects != null && affects.contains(te))
					return false;					
			}
			
			return true;
		}
		
		return false;
	}
	
	@Override
	public boolean hasNext() 
	{
		// hasNext generates the next tuple's worth of data; this check
		// is a short circuit if we've already determined the next
		// tuple but haven't grabbed it yet
		if (m_values != null)
			return true;
		
		while (m_idx <= m_maxItems) {
			Cell yCell = m_y.getCell(Access.ByIndex, m_idx);
			if (isValid(yCell, m_y)) {
				m_values = new Double [m_numDependents + 1]; 
				m_values[0] = (Double)yCell.getCellValue();
				int idx = 1;
				
				boolean validTuple = true;
				for (TableRowColumnElement tx : m_x) {
					Cell xCell = tx.getCell(Access.ByIndex, m_idx);
					if (isValid(xCell, tx)) {
						m_values[idx++] = (Double)xCell.getCellValue();
						continue;
					}
					
					validTuple = false;
					m_values = null;
					break; // from for loop
				}
				
				if (validTuple)
					return true;
			}
			
			m_idx++;
		}
		
		m_values = null;
		return false;
	}


	@Override
	public Tuple<Double> next() 
	{
        if (!hasNext())
            throw new NoSuchElementException();
        
        Tuple<Double> values = new Tuple<Double>(m_values);
        m_values = null;
        m_idx++;
        
		return values;
	}
}
