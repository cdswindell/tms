import org.tms.api.utils.RegisterOp
import G3DPoint

@RegisterOp
class GSphere extends G3DPoint
{
	int x
	int y
	int z
	int radius

	@RegisterOp
	GSphere(int pX, int pY, int pZ, int pR) 
	{
		super(pX, pY, pZ)
		radius = pR
	}

	int getRadius()
	{
		return radius
	}
	
	String toString()
	{
		return "(" + getX() + "," + getY() + "," + getZ() +" r:" + getRadius() + ")"
	}
}
