package org.tms.tan;

import java.util.List;

import org.apache.commons.math3.stat.regression.OLSMultipleLinearRegression;
import org.tms.api.TableRowColumnElement;
import org.tms.util.Tuple;

class MultipleLinearRegression extends AbstractRegression 
{
	private OLSMultipleLinearRegression m_mr;
	private double [] m_regressionParams;
	
	public MultipleLinearRegression(TableRowColumnElement y, List<TableRowColumnElement> x) 
	{
		super(y, x);
		
        m_mr = new OLSMultipleLinearRegression();
	}

	@Override
	void loadData() 
	{
		double [] yData = new double [getNumObservations()];
		double [][] xData = new double [getNumObservations()][getNumDependent()];
		int idx = 0;
		for (Tuple<Double> t : this) {
			int jdx = 0;
			for (Double d : t) {
				if (jdx == 0)
					yData[idx] = d;
				else
					xData[idx][jdx - 1] = d;
				jdx++;
			}
			
			idx++;
		}	
		
		m_mr.newSampleData(yData, xData);
	}

	@Override
	double getRegressionParameter(int termIdx) 
	{
		if (m_regressionParams == null)
			m_regressionParams = m_mr.estimateRegressionParameters();
		
		return m_regressionParams[termIdx];
	}

	@Override
	double getR2() 
	{
		return  m_mr.calculateRSquared();
	}
}
