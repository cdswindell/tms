import org.tms.api.utils.RegisterOp

@RegisterOp
class GPoint {
	int x
	int y

	@RegisterOp
	GPoint(int pX, int pY) 
	{
		this.x = pX
		this.y = pY
	}

	int getX()
	{
		return x
	}

	int getY()
	{
		return y
	}

	double distanceTo(GPoint other)
	{
		def dx = getX() - other.getX()
		def dy = getY() - other.getY()
		
	    return Math.sqrt(dx*dx + dy*dy)
	}
	
	String toString()
	{
		return "(" + getX() + "," + getY() + ")"
	}
}
