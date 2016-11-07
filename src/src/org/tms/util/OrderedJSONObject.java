package org.tms.util;

import java.io.IOException;
import java.io.Writer;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.json.simple.JSONObject;

@SuppressWarnings("unchecked")
public class OrderedJSONObject extends JSONObject 
{
	private static final long serialVersionUID = -5580133316979344018L;
	
	private Map<Object, Object> m_keyMap;

	public OrderedJSONObject()
	{
		super();
		m_keyMap = new LinkedHashMap<Object, Object>();
	}

	@Override
	public Object put(Object key, Object value) 
	{
		super.put(key, value);
		return m_keyMap.put(key, value);
	}

	@Override
	public void putAll(@SuppressWarnings("rawtypes") Map m) 
	{
		m.forEach((k, v) -> put(k, v));
	}

	@Override
	public Object remove(Object key) 
	{
		super.remove(key);
		return m_keyMap.remove(key);
	}

	@Override
	public boolean remove(Object key, Object value) 
	{
		super.remove(key,  value);
		return m_keyMap.remove(key,  value);
	}

	@Override
	public void clear() 
	{
		super.clear();
		m_keyMap.clear();
	}

	@SuppressWarnings("rawtypes")
	@Override
	public Set keySet() 
	{
		return m_keyMap.keySet();
	}

	@SuppressWarnings("rawtypes")
	@Override
	public Collection values() 
	{
		return m_keyMap.values();
	}

	@SuppressWarnings("rawtypes")
	@Override
	public Set entrySet() 
	{
		return m_keyMap.entrySet();
	}
	
	@Override
	public void writeJSONString(Writer out) throws IOException
	{
		writeJSONString(m_keyMap, out);
	}
	
	@Override
	public String toJSONString()
	{
		return toJSONString(m_keyMap);
	}
	
	@Override
	public String toString()
	{
		return toJSONString(m_keyMap);
	}
}
