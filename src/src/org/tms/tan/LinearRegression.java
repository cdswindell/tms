package org.tms.tan;

import java.util.List;

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
	double getRegressionParameter(int termIdx) 
	{
		switch (termIdx) {
			case 0:
				return  (double) m_tvse.calcStatistic(BuiltinOperator.LinearInterceptOper);
				
			case 1:
				return (double) m_tvse.calcStatistic(BuiltinOperator.LinearSlopeOper);
				
			default:
				throw new IllegalArgumentException();
		}
	}

	double getR() 
	{
		return (double)m_tvse.calcStatistic(BuiltinOperator.LinearROper);
	}

	@Override
	double getR2() 
	{
		return (double)m_tvse.calcStatistic(BuiltinOperator.LinearR2Oper);
	}
}
