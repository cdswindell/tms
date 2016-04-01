package org.tms.tan;

import java.util.List;

import org.tms.api.Access;
import org.tms.api.TableRowColumnElement;
import org.tms.teq.BuiltinOperator;
import org.tms.teq.TwoVariableStatEngine;
import org.tms.util.Tuple;

class LinearRegression extends AbstractRegression 
{
	private TwoVariableStatEngine m_tvse;
	
	public LinearRegression(TableRowColumnElement y, List<TableRowColumnElement> x) 
	{
		super(y, x);
		
		m_tvse = new TwoVariableStatEngine();
	}

	@Override
	void loadData() 
	{
		for (Tuple<Double> t : this) {
			m_tvse.enter(t.getSecondElement(), t.getFirstElement());
		}		
	}

	@Override
	String asEquation(Access refType) 
	{
		double slope = (double) m_tvse.calcStatistic(BuiltinOperator.LinearSlopeOper);
		double intercept = (double) m_tvse.calcStatistic(BuiltinOperator.LinearInterceptOper);
		
		StringBuffer sb = new StringBuffer();
		
		sb.append(formatNumber(Math.abs(slope)));
		
		sb.append(" * ");
		
		sb.append(formatReference(getDependent(0), refType));
		
		if (intercept < 0.0)
			sb.append(" - ");
		else if (intercept > 0.0)
			sb.append(" + ");
		else
			return sb.toString(); // don't try to append intercept
		
		sb.append(formatNumber(Math.abs(intercept)));
		return sb.toString();
	}
}
