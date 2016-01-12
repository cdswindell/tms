package org.tms.util;

public class Point 
{
	private int m_x;
	private int m_y;
	
	public Point(int x, int y)
	{
		m_x = x;
		m_y = y;
	}
	
	public int getX()
	{
		return m_x;
	}
	
	public int getY()
	{
		return m_y;
	}
	
	public String toString()
	{
		return String.format("(%d,%d)", m_x, m_y);
	}
}
