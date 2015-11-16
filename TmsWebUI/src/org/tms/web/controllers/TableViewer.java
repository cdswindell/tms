package org.tms.web.controllers;

import java.util.List;

import javax.annotation.PostConstruct;
import javax.faces.bean.ApplicationScoped;
import javax.faces.bean.ManagedBean;

import org.primefaces.push.EventBus;
import org.primefaces.push.EventBusFactory;
import org.tms.api.Column;
import org.tms.api.Row;
import org.tms.api.Table;
import org.tms.api.events.TableElementEvent;
import org.tms.api.events.TableElementEventType;
import org.tms.api.events.TableElementListener;
import org.tms.api.factories.TableFactory;
import org.tms.api.utils.StockTickerOp;

@ManagedBean(name = "tableView")
@ApplicationScoped

public class TableViewer implements TableElementListener
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
    		
    		m_table.getTableContext().registerOperator(new StockTickerOp());
    		
    		m_table.addListeners(TableElementEventType.OnNoPendings, this);
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

	@Override
	public void eventOccured(TableElementEvent e) 
	{
		try {
			Thread.sleep(250);
		} catch (InterruptedException e1) { }
		
		EventBusFactory eb = EventBusFactory.getDefault();
        EventBus eventBus = eb.eventBus();
        eventBus.publish("/tableUpdated", "pending cells updated");
	}
}
