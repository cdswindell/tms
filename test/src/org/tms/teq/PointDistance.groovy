import org.tms.util.Point;
import org.tms.api.utils.SynchronousOp;

class PointDistance extends SynchronousOp {
    PointDistance() {
		super("pointDist", [Point.class, Point.class], double.class);
	}
	
    Object performCalculation(Object [] args) {
       def x = (Point) args[0];
       def y = (Point) args[1];
       def dx = x.getX() - y.getX();
       def dy = x.getY() - y.getY();
       
       return Math.sqrt(dx*dx + dy*dy);
    }
 }
