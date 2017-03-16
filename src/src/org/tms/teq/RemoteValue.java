package org.tms.teq;

import java.lang.ref.WeakReference;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.tms.api.Column;
import org.tms.api.Row;
import org.tms.api.derivables.Operator;
import org.tms.api.derivables.Token;

public class RemoteValue 
{
	static final private Map<RCKey, String> sf_KEY_TO_UUID = new ConcurrentHashMap<RCKey, String>(1024);	
	static final private Map<String, RCKey> sf_UUID_TO_KEY = new ConcurrentHashMap<String, RCKey>(1024);	
	static final private Map<String, Object> sf_UUID_TO_VALUE = new ConcurrentHashMap<String, Object>(1024);
	
	public static Token prepareHandler(DerivationImpl deriv, Operator oper, Row row, Column col) 
	{
		if (deriv == null || deriv.isBeingDestroyed())
			return Token.createNullToken();
		
		// calculate the key from the row/column value
		RCKey key = new RCKey(deriv, oper, row, col);
		
		// have we already registered this location?
		String uuid = sf_KEY_TO_UUID.get(key);
		
		// if null, register the new value
		if (uuid == null) {
			uuid = UUID.randomUUID().toString();
			System.out.println(uuid);
			sf_KEY_TO_UUID.put(key, uuid);
			sf_UUID_TO_KEY.put(uuid, key);
			
			// remember this uuid so we can clean up when derivation is deleted
			deriv.registerRemoteUUID(uuid);
		}
		
		// finally, return the current value
		Object value = sf_UUID_TO_VALUE.get(uuid);
		if (value == null)
			return Token.createNullToken();
		else
			return new Token(value);
	}
	
	public static void postRemoteValue(String uuid, Object value)
	{
		RCKey key = sf_UUID_TO_KEY.get(uuid);
		if (key != null && key.isValid()) {
			if (key.getOperator() == BuiltinOperator.RemoteNumericOper)
				value = Double.parseDouble((String)value);
			
			sf_UUID_TO_VALUE.put(uuid, value);
			
			Row row = key.getRow();
			Column col = key.getColumn();
			key.getDerivation().recalculateTargetCell(row, col);
		}
	}
	
	/**
	 * Called when a derivation is deleted
	 * @param uuids
	 */
	static void removeRemoteHandlers(String[] uuids) 
	{
		for (String uuid : uuids) {
			RCKey key = sf_UUID_TO_KEY.remove(uuid);
			if (key != null)
				sf_KEY_TO_UUID.remove(key);
			
			sf_UUID_TO_VALUE.remove(uuid);
		}		
	}
	
	/**
	 * Inner class to provide unique key
	 */
	static class RCKey
	{
		private WeakReference<Row> m_row;
		private WeakReference<Column> m_col;
		private DerivationImpl m_deriv;
		private Operator m_oper;
		
		RCKey(DerivationImpl d, Operator oper, Row r, Column c)
		{
			m_deriv = d;
			m_oper = oper;
			m_row = new WeakReference<Row>(r);
			m_col = new WeakReference<Column>(c);
		}
		
		Row getRow()
		{
			return m_row.get();
		}
		
		Column getColumn()
		{
			return m_col.get();
		}
		
		DerivationImpl getDerivation()
		{
			return m_deriv;
		}
		
		Operator getOperator()
		{
			return m_oper;
		}
		
		boolean isValid()
		{
			Row row = getRow();
			Column col = getColumn();
			
			return m_deriv != null && row != null && row.isValid() && col != null && col.isValid();
		}

		@Override
		public int hashCode() 
		{
			final int prime = 31;
			int result = 1;
			result = prime * result + ((m_deriv == null) ? 0 : m_deriv.hashCode());
			result = prime * result + ((m_oper == null) ? 0 : m_oper.hashCode());
			
			Row row = getRow();
			Column col = getColumn();
			result = prime * result + ((col == null) ? 0 : col.hashCode());
			result = prime * result + ((row == null) ? 0 : row.hashCode());
			
			return result;
		}

		@Override
		public boolean equals(Object obj) 
		{
			if (this == obj)
				return true;
			
			if (obj == null)
				return false;
			
			if (getClass() != obj.getClass())
				return false;
			
			RCKey other = (RCKey) obj;
			
			if (m_deriv == null) {
				if (other.m_deriv != null)
					return false;
			} 
			else if (!m_deriv.equals(other.m_deriv))
				return false;
			
			if (m_oper == null) {
				if (other.m_oper != null)
					return false;
			} 
			else if (!m_oper.equals(other.m_oper))
				return false;
			
			Row row = getRow();
			Column col = getColumn();
			
			Row oRow = other.getRow();
			Column oCol = other.getColumn();
			
			if (col == null) {
				if (oCol != null)
					return false;
			} 
			else if (!col.equals(oCol))
				return false;
			
			if (row == null) {
				if (oRow != null)
					return false;
			} 
			else if (!row.equals(oRow))
				return false;
			
			return true;
		}
	}
}
