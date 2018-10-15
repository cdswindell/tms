package org.tms.io.xml;

import java.util.HashMap;
import java.util.Map;

import org.tms.api.Column;
import org.tms.api.Row;
import org.tms.api.Table;
import org.tms.api.TableElement;
import org.tms.io.LabeledReader;
import org.tms.io.LabeledWriter;
import org.tms.tds.ExternalDependenceTableElement;

import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

public abstract class ExternalDependenceTableConverter extends TableConverter
{
	private int m_lastRowIdx;
	private int m_lastColIdx;
	
	private int m_lastExtRowIdx;
	private int m_lastExtColIdx;
	
	private int m_curExtRowIdx;
	private int m_curExtColIdx;
	
	private Table m_table;
	
	private Map<Integer, Integer> m_rowIdxMap;
	private Map<Integer, Integer> m_colIdxMap;
	
    public ExternalDependenceTableConverter(LabeledReader<?> reader)
    {
        super(reader);
        m_lastExtRowIdx = m_lastExtColIdx = m_curExtRowIdx = m_curExtColIdx = -1;
    }

    public ExternalDependenceTableConverter(LabeledWriter<?> writer)
    {
        super(writer);
        m_lastExtRowIdx = m_lastExtColIdx = m_curExtRowIdx = m_curExtColIdx = -1;
    }
    
    @Override
    protected void marshalClassSpecificElements(TableElement te, HierarchicalStreamWriter writer, MarshallingContext context)
    {
    	int lastExtRowIdx = findLastExternalRowIdx(te);
    	writer.addAttribute("lerIdx", String.valueOf(lastExtRowIdx));
    	
    	int lastExtColIdx = findLastExternalColIdx(te);
    	writer.addAttribute("lecIdx", String.valueOf(lastExtColIdx));
    }

	private int findLastExternalRowIdx(TableElement te) 
	{
		Table t = (Table)te;
    	t.pushCurrent();
    	int lastExtRowIdx = 0;
    	
    	try {
    		for (int i = t.getNumRows(); i > 0; i--) {
    			Row r = t.getRow(i);
    			if (r instanceof ExternalDependenceTableElement) {
    				lastExtRowIdx = i;
    				break;
    			}
    		}
    	}
    	finally {
    		t.popCurrent();
    	}
    	
    	return lastExtRowIdx;
	}
    
	private int findLastExternalColIdx(TableElement te) 
	{
		Table t = (Table)te;
    	t.pushCurrent();
    	int lastExtColIdx = 0;
    	
    	try {
    		for (int i = t.getNumColumns(); i > 0; i--) {
    			Column c = t.getColumn(i);
    			if (c instanceof ExternalDependenceTableElement) {
    				lastExtColIdx = i;
    				break;
    			}
    		}
    	}
    	finally {
    		t.popCurrent();
    	}
    	
    	return lastExtColIdx;
	}
    
	@Override
	protected void unmarshalClassSpecificElements(TableElement t, HierarchicalStreamReader reader, UnmarshallingContext context) 
	{
        Integer iVal = readAttributeInteger("lerIdx", reader);
        m_lastExtRowIdx = iVal != null ? iVal : -1;

        iVal = readAttributeInteger("lecIdx", reader);
        m_lastExtColIdx = iVal != null ? iVal : -1;
	}

	@Override
	protected boolean extendTableRows()
	{
		return false;
	}
	
    @Override
	protected boolean extendTableColumns()
	{
		return false;
	}
    
    protected int getLastRowIdx()
    {
    	return m_lastRowIdx;
    }
       
    protected int getLastColIdx()
    {
    	return m_lastColIdx;
    }    
    
    protected void cacheDimensions(Table t)
    {
    	m_lastRowIdx = t.getNumRows();
    	m_lastColIdx = t.getNumColumns();
    	
    	if (m_table == null)
    		m_table = t;
    }

	protected void postConstructAction(Table t, Object te) 
	{
		if (te instanceof Column) {
			Column col = (Column)te;
			if (col.getIndex() <= m_lastColIdx)
				cacheDimensions(t);
		}		
		else if (te instanceof Row) {
			Row row = (Row)te;
			if (row.getIndex() <= m_lastRowIdx)
				cacheDimensions(t);
		}
	}
	
    protected int getRemappedRowIdx(int rIdx)
    {
    	if (m_rowIdxMap != null && m_rowIdxMap.containsKey(rIdx))
    		return m_rowIdxMap.get(rIdx); // if a map exists and this idx is in it, use mapped value
    	else {
			if (m_rowIdxMap == null)
				m_rowIdxMap = new HashMap<Integer, Integer>();
			
    		if (rIdx <= m_lastRowIdx) {
    			int nrIdx = rIdx;
    			
    			// check if table has expanded and this element needs to be relocated
    			if (m_lastExtRowIdx > -1 && rIdx > m_lastExtRowIdx) {
    				if (m_curExtRowIdx == -1)
    					m_curExtRowIdx = findLastExternalRowIdx(m_table);
    				if (m_curExtRowIdx > 0 && m_curExtRowIdx > m_lastExtRowIdx) {
    					nrIdx += m_curExtRowIdx - m_lastExtRowIdx;  
    					if (nrIdx > m_lastRowIdx)
    						m_lastRowIdx = nrIdx;
    				}
    			}
    			
    			m_rowIdxMap.put(rIdx, nrIdx);    			
    			return rIdx; // else, if idx is <= current last, use the idx
    		}
    		else {
	    		// otherwise, check if requested idx indicates a gap, and if so, compress
	        	m_lastRowIdx++;
    			m_rowIdxMap.put(rIdx, m_lastRowIdx);    			
	    		
	    		return m_lastRowIdx;
	    	}
    	}
    }
    
    protected int getRemappedColIdx(int cIdx)
    {
    	if (m_colIdxMap != null && m_colIdxMap.containsKey(cIdx))
    		return m_colIdxMap.get(cIdx); // if a map exists and this idx is in it, use mapped value
    	else {
			if (m_colIdxMap == null)
				m_colIdxMap = new HashMap<Integer, Integer>();
			
    		if (cIdx <= m_lastColIdx) {
    			int ncIdx = cIdx;
    			
    			// check if table has expanded and this element needs to be relocated
    			if (m_lastExtColIdx > -1 && cIdx > m_lastExtColIdx) {
    				if (m_curExtColIdx == -1)
    					m_curExtColIdx = findLastExternalColIdx(m_table);
    				if (m_curExtColIdx > 0 && m_curExtColIdx > m_lastExtColIdx) {
    					ncIdx += m_curExtColIdx - m_lastExtColIdx;  
    					if (ncIdx > m_lastColIdx)
    						m_lastColIdx = ncIdx;
    				}
    			}
    			
    			// otherwise, add element in place
    			m_colIdxMap.put(cIdx, ncIdx);
    			return ncIdx; // else, if idx is <= current last, use the idx
    		}
	    	else {
	    		// otherwise, check if requested idx indicates a gap, and if so, compress
	    		m_lastColIdx++;
	    		m_colIdxMap.put(cIdx, m_lastColIdx);
	    		
	    		return m_lastColIdx;
	    	}
    	}
    }
}
