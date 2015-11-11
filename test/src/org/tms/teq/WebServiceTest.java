package org.tms.teq;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

import java.util.LinkedHashMap;

import org.junit.Test;
import org.tms.api.Cell;
import org.tms.api.Column;
import org.tms.api.Row;
import org.tms.api.Table;
import org.tms.api.derivables.RestConsumerOp;
import org.tms.api.factories.TableContextFactory;
import org.tms.api.factories.TableFactory;
import org.tms.api.utils.StockTickerOp;

public class WebServiceTest
{
    @Test
    public void testWebServiceCall() throws InterruptedException 
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
    
    @Test
    public void testWebServiceCall2() throws InterruptedException 
    {       
        CurrencyConverterOp curConvert = new CurrencyConverterOp("curConvert", "result");
        TableContextFactory.fetchDefaultTableContext().registerOperator(curConvert);
        
        Table t = TableFactory.createTable();
        assertNotNull(t);
        
        Row r1 = t.addRow();
        assertNotNull(r1);
        
        Row r2 = t.addRow();
        assertNotNull(r2);
        
        Column c1 = t.addColumn();
        assertNotNull(c1);
        
        t.setCellValue(r1,  c1, 10);
        t.setCellValue(r2,  c1, 32.56);
        
        Column c2 = t.addColumn();
        assertNotNull(c2);
        
        c2.setDerivation("curConvert(\"USD\", \"EUR\", col 1)");
        
        while(c2.isPendings()) {
            assertNotNull(c2);
            Thread.sleep(500);
        }
        
        Cell cell = t.getCell(r1,  c2);
        assertNotNull(cell);  
        assertThat(true, is(cell.isNumericValue()));
        
        cell = t.getCell(r2,  c2);
        assertNotNull(cell);  
        assertThat(true, is(cell.isNumericValue()));
    }
    
    public static class CurrencyConverterOp extends RestConsumerOp
    {
        private static final String API_TOKEN = "79cb2c564f02a96c481b49d971b14000";
        private static final String BASE_URL = "https://apilayer.net/api/";
        
        public CurrencyConverterOp(String label, String resultKey)
        {
            super(label, resultKey, double.class);
        }

        @Override
        public String getUrl()
        {
            return BASE_URL + "convert";
        }

        @Override
        public LinkedHashMap<String, Object> getUrlParamsMap()
        {
            LinkedHashMap<String, Object> urlParams = new LinkedHashMap<String, Object>(4);
            
            urlParams.put("access_key", API_TOKEN);
            urlParams.put("from", String.class);
            urlParams.put("to", String.class);
            urlParams.put("amount", Double.class);
            
            return urlParams;            
        }
    }
}
