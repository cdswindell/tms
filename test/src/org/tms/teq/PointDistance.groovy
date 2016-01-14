import org.tms.util.Point;
import org.tms.api.utils.AsynchronousOp;

class PointDistance extends AsynchronousOp {
    PointDistance() {
		super("pointDist", [Point.class, Point.class], double.class);
	}
	
    Object performCalculation(Object [] args) {
       def x = (Point) args[0]
       def y = (Point) args[1]
       def dx = x.getX() - y.getX()
       def dy = x.getY() - y.getY()
       
       def sleepTime = ((int)(Math.random() * 30) + 1) * 500
       sleep(sleepTime)
       return Math.sqrt(dx*dx + dy*dy)
    }
 }
