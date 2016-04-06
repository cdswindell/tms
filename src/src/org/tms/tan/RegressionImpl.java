package org.tms.tan;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

import org.tms.api.Access;
import org.tms.api.TableRowColumnElement;
import org.tms.api.exceptions.NotFoundException;

public class RegressionImpl 
{
	private TableRowColumnElement m_y;
	private Set<TableRowColumnElement> m_x;
	
	private AbstractRegression m_regression;
	
	public RegressionImpl(TableRowColumnElement yData)
	{
		m_y = yData;	
		m_x = new LinkedHashSet<TableRowColumnElement>();
		m_regression = null;
	}
	
	public RegressionImpl(TableRowColumnElement yData, TableRowColumnElement... xData)
	{
		this(yData);
		
		if (xData != null) {
			for (TableRowColumnElement te : xData) {
				m_x.add(te);
			}
		}
	}
	
	public RegressionImpl(TableRowColumnElement yData, Collection<TableRowColumnElement> xData)
	{
		this(yData);
		
		m_x.addAll(xData);
	}
	
	public boolean addIndepdentVariable(TableRowColumnElement... xData)
	{
		boolean addedAny = false;
		
		if (xData != null) {
			for (TableRowColumnElement te : xData) {
				if (te != null) {
					if (m_x.add(te));
					addedAny = true;
				}
			}
		}
		
		// if we added any, we need to reset regression
		if (addedAny)
			m_regression = null;
		
		return addedAny;
	}
	
	public boolean isDependentVariable() 
	{
		return m_y != null;
	}
	
	public boolean isIndependentVariable() 
	{
		return m_x != null && !m_x.isEmpty();
	}
	
	public int getNumIndependentVariables() 
	{
		return m_x != null ? m_x.size() : 0;
	}
	
	public String generateEquation()
	{
		return generateEquation(Access.ByIndex);
	}

	public String generateEquation(Access refType)
	{
		performRegression();	
		
		return m_regression.asEquation(refType);
	}

	public double  calculateCorrelationCoefficient(TableRowColumnElement te)
	{
		performRegression();	
		
		if (te != null && m_x.contains(te)) {
			int idx = 0;
			
			for (TableRowColumnElement x : m_x) {
				if (x.equals(te))
					break;
				idx++;
			}
			
			double [] coefs = m_regression.getCorrelationCoefficients();
			if (idx < coefs.length) 
				return coefs[idx];
		}
		
		throw new NotFoundException(te.getElementType(), "Not a dependent variable");
	}
	
	public double [] calculateCorrelationCoefficients()
	{
		performRegression();	
		
		return m_regression.getCorrelationCoefficients();
	}
	
	public double calculateRSquared()
	{
		performRegression();	
		
		return m_regression.getR2();
	}
	
	private void performRegression() 
	{
		if (m_regression == null)
			m_regression = AbstractRegression.buildRegressionModel(m_y, new ArrayList<TableRowColumnElement>(m_x));
	}
}
