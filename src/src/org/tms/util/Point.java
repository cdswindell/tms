package org.tms.util;

import org.tms.api.utils.RegisterOp;

@RegisterOp
public class Point 
{
	private int m_x;
	private int m_y;
	
	@RegisterOp()
	public Point(int x, int y)
	{
		m_x = x;
		m_y = y;
	}
	
	@RegisterOp(token="copyPoint")
	public Point(Point op)
	{
		this(op != null ? op.getX() : 0, op != null ? op.getY() : 0);
	}
	
	public int getX()
	{
		return m_x;
	}
	
	public int getY()
	{
		return m_y;
	}
	
	public double distanceTo(Point other)
	{
		int dx = getX() - other.getX();
		int dy = getY() - other.getY();

		return Math.sqrt(dx*dx + dy*dy);
	}
	
	@RegisterOp(async=true)
	public double distanceToAsync(Point other) 
			throws InterruptedException
	{
		Thread.sleep(2500);
		return distanceTo(other);
	}
	
	public String toString()
	{
		return String.format("(%d,%d)", m_x, m_y);
	}
}
