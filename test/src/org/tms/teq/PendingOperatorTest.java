package org.tms.teq;

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

public class PendingOperatorTest extends BaseTest
{

    @Test
    public final void testPendingColumnOperator()
    {
        TableContext tc = TableContextFactory.createTableContext();
        Table t = TableFactory.createTable(tc);
        
        TokenMapper tm = tc.getTokenMapper();
        tm.registerOperator(new PendingOperator());
        
        t.addRow(Access.ByIndex, 100);
        
        Column c1 = (Column)t.addColumn().setDerivation("randInt(50)");
        Column c2 = (Column)t.addColumn().setDerivation("pending(col 1, 2500)");
        
        for (Row r : t.rows()) {
            double v1 = (double)t.getCellValue(r,  c1);
            
            Cell c = t.getCell(r, c2);
            assertThat(c, notNullValue());
        }
    }
    
    public class PendingOperator implements Operator, Runnable
    {
        private Token[] m_args;
        
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
            
            m_args[0].postResult(m_args[0].getValue());
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
            m_args = args;        
            return Token.createPendingToken(this);
        }       
    }
}
