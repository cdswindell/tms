import org.tms.api.utils.RegisterOp

@RegisterOp
class G3DPoint {
	int x
	int y
	int z

	@RegisterOp
	G3DPoint(int pX, int pY, int pZ) 
	{
		this.x = pX
		this.y = pY
		this.z = pZ
	}

	public int getX()
	{
		x
	}

	public int getY()
	{
		y
	}

	public int getZ()
	{
	  z
	}

	@RegisterOp(token="distanceTo3D")
	public double distanceTo(G3DPoint other)
	{
		def dx = getX() - other.getX()
		def dy = getY() - other.getY()
		def dz = getZ() - other.getZ()
		
	    Math.sqrt(dx*dx + dy*dy + dz*dz)
	}
	
	def String toString()
	{
		return "(" + getX() + "," + getY() + "," + getZ() +")"
	}
}
