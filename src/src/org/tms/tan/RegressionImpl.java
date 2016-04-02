package org.tms.tan;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

import org.tms.api.Access;
import org.tms.api.TableRowColumnElement;

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
		if (m_regression == null)
			performRegression();
		
		return m_regression.asEquation(refType);
	}

	private void performRegression() 
	{
		if (m_regression == null)
			m_regression = AbstractRegression.buildRegressionModel(m_y, new ArrayList<TableRowColumnElement>(m_x));
	}

	public double calculateRSquared()
	{
		if (m_regression == null)
			m_regression = AbstractRegression.buildRegressionModel(m_y, new ArrayList<TableRowColumnElement>(m_x));
		
		return m_regression.getR2();
	}
}
