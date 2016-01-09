package org.tms.io.xml;

import java.sql.SQLException;

import org.tms.api.TableElement;
import org.tms.api.exceptions.TableIOException;
import org.tms.api.factories.TableFactory;
import org.tms.io.BaseReader;
import org.tms.io.BaseWriter;
import org.tms.tds.TableImpl;
import org.tms.tds.dbms.DbmsTableImpl;

import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

public class DbmsTableConverter extends TableConverter
{
    static final public String ELEMENT_TAG = "dbmsTable";
    
    public DbmsTableConverter(BaseReader<?> reader)
    {
        super(reader);
    }

    public DbmsTableConverter(BaseWriter<?> writer)
    {
        super(writer);
    }

    @Override
    public boolean canConvert(@SuppressWarnings("rawtypes") Class arg)
    {
        return DbmsTableImpl.class == arg;
    }

    @Override
    protected void marshalClassSpecificElements(TableElement te, HierarchicalStreamWriter writer, MarshallingContext context)
    {
        DbmsTableImpl dbTe = (DbmsTableImpl)te;
        writeNode(dbTe.getDriverClassName(), "driver", writer, context);
        writeNode(dbTe.getConnectionUrl(), "connectionUrl", writer, context);
        writeNode(dbTe.getQuery(), "query", writer, context);
    }
    
    @Override
    protected TableImpl createTable(HierarchicalStreamReader reader, UnmarshallingContext context) 
    {
		String driver = null;
		String connectionUrl = null;
		String query = null;
		boolean processingDBMSTags = true;
    	while (processingDBMSTags && reader.hasMoreChildren()) {
    		reader.moveDown();
    		String nodeName = reader.getNodeName();
    		switch (nodeName) {
				case "driver":
					driver = (String)context.convertAnother(null, String.class);
					break;
					
				case "connectionUrl":
					connectionUrl = (String)context.convertAnother(null, String.class);
					break;
					
				case "query":
					query = (String)context.convertAnother(null, String.class);
					break;
					
			    default:
			    	processingDBMSTags = false;
			    	break;
    		}

            reader.moveUp();
    	}
  
    	// we need a connection url and a query to proceed
		if (connectionUrl == null)
			throw new TableIOException("Connection URL Required");
		
		if (query == null)
			throw new TableIOException("Query Required");

    	try {
			DbmsTableImpl t = (DbmsTableImpl) TableFactory.createDbmsTable(connectionUrl, query, driver, getTableContext());
			return t;
		} 
    	catch (ClassNotFoundException | SQLException e) {
			throw new TableIOException(e);
		}
    }
}
