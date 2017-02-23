package org.tms.teq;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.io.IOException;
import java.util.LinkedHashMap;

import org.json.simple.JSONObject;
import org.junit.Test;
import org.tms.BaseTest;
import org.tms.api.Cell;
import org.tms.api.Column;
import org.tms.api.Row;
import org.tms.api.Table;
import org.tms.api.factories.TableContextFactory;
import org.tms.api.factories.TableFactory;
import org.tms.api.io.XMLOptions;
import org.tms.api.utils.RestConsumerOp;
import org.tms.api.utils.StockTickerOp;

public class WebServiceTest extends BaseTest
{
    @Test
    public void testWebServiceCall() throws InterruptedException, IOException 
    {       
        StockTickerOp stocks = new StockTickerOp();
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
        
        // export the file, we will read it later
        File tmpFile = File.createTempFile("ErrorCell", ".xml");
        tmpFile.deleteOnExit();
        t.export(tmpFile.getAbsolutePath());
        
        // check values
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
        
        // read export file and make sure the error cell is still in error
        Table t2 = TableFactory.importFile(tmpFile.getAbsolutePath(), XMLOptions.Default.withDerivations(false));
        assertNotNull(t2);
        
        cell = t2.getCell(t2.getRow(1), t2.getColumn(2));
        assertNotNull(cell);  
        assertThat(true, is(cell.isNumericValue()));
        
        cell = t2.getCell(t2.getRow(2), t2.getColumn(2));
        assertNotNull(cell);  
        assertThat(true, is(cell.isErrorValue()));
        
        tmpFile.delete();
    }
    
    @Test
    public void testWebServiceCall2() throws InterruptedException 
    {       
        StockTickerOp stocks = new StockTickerOp();
        TableContextFactory.fetchDefaultTableContext().registerOperator(stocks);
        
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
        
        t.setCellValue(r1,  c1, "JCOM");
        t.setCellValue(r2,  c1, "EIGI");
        
        Column c2 = t.addColumn();
        assertNotNull(c2);
        c2.setDerivation("ticker(col 1)");
        
        Column c3 = t.addColumn();
        assertNotNull(c3);
        c3.setDerivation("curConvert('USD', 'EUR', col 2)");
       
        Column c4 = t.addColumn();
        assertNotNull(c4);
        c4.setDerivation("curConvert('EUR', 'USD', col 3)");       
        
        while(c4.isPendings()) {
            assertNotNull(t);
            Thread.sleep(500);
        }
        
        Cell cell = t.getCell(r1,  c2);
        assertNotNull(cell);  
        assertThat(true, is(cell.isNumericValue()));
        assertThat(closeTo(t.getCellValue(r1, c4), (double)cell.getCellValue(), 0.01), is(true));
        
        cell = t.getCell(r2,  c2);
        assertNotNull(cell);  
        assertThat(true, is(cell.isNumericValue()));
        assertThat(closeTo(t.getCellValue(r2, c4), (double)cell.getCellValue(), 0.01), is(true));
    }
    
    @Test
    public void testWeatherWebService() throws InterruptedException 
    {       
        WeatherOp curTemp = new WeatherOp("curTemp", "main/temp", double.class);
        TableContextFactory.fetchDefaultTableContext().registerOperator(curTemp);
        
        WeatherOp weather = new WeatherOp("curWeather", "main", JSONObject.class);
        TableContextFactory.fetchDefaultTableContext().registerOperator(weather);
        
        Table t = TableFactory.createTable();
        assertNotNull(t);
        
        Row r1 = t.addRow();
        assertNotNull(r1);
        
        Row r2 = t.addRow();
        assertNotNull(r2);
        
        Column c1 = t.addColumn();
        assertNotNull(c1);
        
        t.setCellValue(r1,  c1, "01720");
        t.setCellValue(r2,  c1, "03254");
        
        Column c2 = t.addColumn();
        assertNotNull(c2);
        c2.setDerivation("curTemp(col 1)");
        
        Column c3 = t.addColumn();
        assertNotNull(c3);
        c3.setDerivation("curWeather(col 1)");
        
        Column c4 = t.addColumn();
        assertNotNull(c4);
        c4.setDerivation("fromJSON(col 3, 'temp')");
        
        while(t.isPendings()) {
            assertNotNull(t);
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
        private static final String BASE_URL = "https://apilayer.net/api/";
        private static final String ENDPOINT = "convert";
        private static final String API_TOKEN = "79cb2c564f02a96c481b49d971b14000";
        
        public CurrencyConverterOp(String label, String resultKey)
        {
            super(label, resultKey, double.class, BASE_URL + ENDPOINT);
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
    
    public static class WeatherOp extends RestConsumerOp
    {
        private static final String BASE_URL = "http://api.openweathermap.org/data/2.5/";
        private static final String ENDPOINT = "weather";
        private static final String API_TOKEN = "69d4f6e3828f05e2e310bb14ff2d1c4e";
        
        public WeatherOp(String label, String resultKey, Class<?> resultType)
        {
            super(label, resultKey, resultType, BASE_URL + ENDPOINT);
        }

        @Override
        public LinkedHashMap<String, Object> getUrlParamsMap()
        {
            LinkedHashMap<String, Object> urlParams = new LinkedHashMap<String, Object>(4);
            
            urlParams.put("units", "imperial");
            urlParams.put("appid", API_TOKEN);
            urlParams.put("zip", String.class);
            
            return urlParams;            
        }
        
        @Override
        protected Object postProcessParamValue(String paramName, Object paramValue)
        {
            if ("zip".equals(paramName)) {
                return paramValue != null ? paramValue.toString() + ",us" : paramValue;
            }
            else
                return paramValue;
        }
    }
}
