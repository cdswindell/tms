package org.tms.teq;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

import java.io.IOException;

import org.json.simple.parser.ParseException;
import org.junit.Test;
import org.tms.api.Cell;
import org.tms.api.Column;
import org.tms.api.Row;
import org.tms.api.Table;
import org.tms.api.factories.TableContextFactory;
import org.tms.api.factories.TableFactory;
import org.tms.api.utils.StockTickerOp;


public class WebServiceTest
{
    @Test
    public void testWebServiceCall() throws IOException, ParseException, InterruptedException
    {
        
        StockTickerOp stocks = new StockTickerOp("l_cur");
        TableContextFactory.fetchDefaultTableContext().registerOperator(stocks);
        
        Table t = TableFactory.createTable();
        assertNotNull(t);
        
        Row r1 = t.addRow();
        assertNotNull(r1);
        
        Row r2 = t.addRow();
        assertNotNull(r2);
        
        Column c1 = t.addColumn();
        assertNotNull(c1);
        
        t.setCellValue(r1,  c1, "CTCT");
        t.setCellValue(r2,  c1, "XYXYXYXYXY");
        
        Column c2 = t.addColumn();
        assertNotNull(c2);
        
        c2.setDerivation("ticker(col 1)");
        
        while(c2.isPendings()) {
            assertNotNull(c2);
            Thread.sleep(500);
        }
        
        Cell cell = t.getCell(r1,  c2);
        assertNotNull(cell);  
        assertThat(true, is(cell.isNumericValue()));
        
        cell = t.getCell(r2,  c2);
        assertNotNull(cell);  
        assertThat(true, is(cell.isErrorValue()));
        
        // retry 
        t.setCellValue(r2,  c1, "EIGI");
        while(c2.isPendings()) {
            assertNotNull(c2);
            Thread.sleep(500);
        }

        cell = t.getCell(r2,  c2);
        assertNotNull(cell);  
        assertThat(true, is(cell.isNumericValue()));
    }
}
