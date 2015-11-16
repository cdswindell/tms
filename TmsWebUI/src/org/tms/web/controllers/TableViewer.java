package org.tms.web.controllers;

import java.util.List;

import javax.annotation.PostConstruct;
import javax.faces.bean.ApplicationScoped;
import javax.faces.bean.ManagedBean;

import org.tms.api.Column;
import org.tms.api.Row;
import org.tms.api.Table;
import org.tms.api.factories.TableFactory;
import org.tms.api.utils.StockTickerOp;

@ManagedBean(name = "tableView")
@ApplicationScoped

public class TableViewer 
{
	private Table m_table;
	
    @PostConstruct
    public void init()     
    {
    	if (m_table == null) {
    		m_table = TableFactory.createTable();
    		m_table.setLabel("TMS Table Demonstration");
    		
    		m_table.addRow(10);
    		m_table.addColumn(3);
    		
    		m_table.getTableContext().registerOperator(new StockTickerOp("l_cur"));
    	}
    }
	
    public Table getTable()
    {
    	return m_table;
    }
	
    public String getTitle()
    {
    	return m_table.getLabel();
    }
    
    public List<Row> getRows()
    {
    	return m_table.getRows();
    }
    
    public List<Column> getColumns()
    {
    	return m_table.getColumns();
    }

	public int getNumRows() 
	{
		return m_table.getNumRows();
	}

	public int getNumColumns() 
	{
		return m_table.getNumColumns();
	}
}
