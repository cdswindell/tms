package org.tms.web.rest;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class Pojo 
{
	private String m_value;
	private int m_count;
	
	public String getValue() 
	{
		return m_value;
	}
	
	public void setValue(String value) 
	{
		m_value = value;
	}
	
	public int getCount() 
	{
		return m_count;
	}
	
	public void setCount(int count) 
	{
		m_count = count;
	}
	
	@Override
	public String toString()
	{
		return String.format("[Value: %s, Count: %d]", getValue(), getCount());
	}
}
