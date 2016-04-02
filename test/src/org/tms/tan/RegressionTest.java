package org.tms.tan;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

import java.io.IOException;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.math3.stat.regression.OLSMultipleLinearRegression;
import org.junit.Test;
import org.tms.BaseTest;
import org.tms.api.Access;
import org.tms.api.Column;
import org.tms.api.Table;
import org.tms.api.io.XLSOptions;
import org.tms.io.XlsReader;

public class RegressionTest extends BaseTest 
{

	@Test
	public void testMultipleLinearRegression() throws IOException 
	{
        XlsReader r = new XlsReader(qualifiedFileName("IQData.xlsx"), XLSOptions.Default.withRowLabels(false)); 
        assertNotNull(r);
        
        Table t = r.parseActiveSheet();       
        assertNotNull(t);
        
        // calculate regression using base Apache class
        OLSMultipleLinearRegression mr = new OLSMultipleLinearRegression();
        assertNotNull(mr);
        
        Column yCol = t.getColumn(1);
        double[] y = ArrayUtils.toPrimitive(yCol.toArray(new Double[0]));
        
        Column x1Col = t.getColumn(2);
        double[] x1 = ArrayUtils.toPrimitive(x1Col.toArray(new Double[0]));
        
        Column x2Col = t.getColumn(3);
        double[] x2 = ArrayUtils.toPrimitive(x2Col.toArray(new Double[0]));
        
        Column x3Col = t.getColumn(4);
        double[] x3 = ArrayUtils.toPrimitive(x3Col.toArray(new Double[0]));
        
        double [][] x = new double [38][3];
        for (int i = 0; i < 38; i++) {
        	x[i][0] = x1[i];
        	x[i][1] = x2[i];
        	x[i][2] = x3[i];
        }
       
        mr.newSampleData(y,  x);
        
        // generate using TMS class
        RegressionImpl ri = new RegressionImpl(yCol, x1Col, x2Col, x3Col);
        assertNotNull(ri);
        
        assertThat(mr.calculateRSquared(), is(ri.calculateRSquared()));
        
        String eq = ri.generateEquation();
        assertNotNull(eq);
        
        eq = ri.generateEquation(Access.ByLabel);
        assertNotNull(eq);
	}
}
