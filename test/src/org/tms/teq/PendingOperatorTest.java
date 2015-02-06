package org.tms.teq;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.tms.BaseTest;
import org.tms.api.Access;
import org.tms.api.Cell;
import org.tms.api.Column;
import org.tms.api.Operator;
import org.tms.api.Row;
import org.tms.api.Table;
import org.tms.api.TableContext;
import org.tms.api.factories.TableContextFactory;
import org.tms.api.factories.TableFactory;
import org.tms.tds.TableImpl;

public class PendingOperatorTest extends BaseTest
{

    @Test
    public final void testPendingColumnOperator() throws InterruptedException
    {
        TableContext tc = TableContextFactory.createTableContext();
        Table t = TableFactory.createTable(tc);
        
        TokenMapper tm = tc.getTokenMapper();
        tm.registerOperator(new PendingOperator());
        
        t.addRow(Access.ByIndex, 2500);
        
        Column c1 = (Column)t.addColumn().setDerivation("randInt(50)");
        Column c2 = (Column)t.addColumn().setDerivation("7 * pending(5, 50) + pending(col 1, 50)/2");
        //Column c3 = (Column)t.addColumn().setDerivation("col 2 / 2");
        
        assertThat(((TableImpl)t).isPendings(), is(true));
        
        while (((TableImpl)t).isPendings()) {
            Thread.sleep(1000);
        }
        
        assertThat(((TableImpl)t).isPendings(), is(false));
        
        for (Row r : t.rows()) {
            double v1 = (double)t.getCellValue(r,  c1);
            
            Cell c = t.getCell(r, c2);
            assertThat(c, notNullValue());
            assertThat(c.getCellValue(), is(7.0*5.0*2.0 + v1*2.0/2.0));
            
            //c = t.getCell(r, c3);
            //assertThat(c, notNullValue());
            //assertThat(c.getCellValue(), is(v1));
        }
        
        assertThat(((TableImpl)t).isPendings(), is(false));
        
        t.recalculate();
        assertThat(((TableImpl)t).isPendings(), is(true));
        
        while (((TableImpl)t).isPendings()) {
            Thread.sleep(1000);
        }
        
        assertThat(((TableImpl)t).isPendings(), is(false));
    }
    
    public class PendingOperator implements Operator, Runnable
    {
        private Token[] m_args;
        
        public PendingOperator(Token[] args)
        {
            m_args = args;
        }

        public PendingOperator()
        {
            m_args = null;
        }

        @Override
        public void run()
        {
            int sleepTime = m_args[1].getNumericValue().intValue();
            try
            {
                Thread.sleep(sleepTime);
            }
            catch (InterruptedException e)
            {
                // noop;
            }
            
            m_args[0].postResult(2.0 * (Double)m_args[0].getValue());
        }

        @Override
        public String getLabel()
        {
            return "pending";
        }

        @Override
        public TokenType getTokenType()
        {
            return TokenType.BinaryFunc;
        }

        @Override
        public Class<?>[] getArgTypes()
        {
             return new Class<?>[] {double.class, double.class};
        }

        @Override
        public Token evaluate(Token... args)
        {   
            PendingOperator po = new PendingOperator(args);
            return Token.createPendingToken(po);
        }       
    }
}
