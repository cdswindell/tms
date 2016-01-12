import org.tms.util.Point;

class pointFuncts {
    double distance(Point x, Point y) {
       def dx = x.getX() - y.getX();
       def dy = x.getY() - y.getY();
       
       return Math.sqrt(dx*dx + dy*dy);
    }
    
    int getX(Point x) {
       x.getX();
    }
    
    int getY(Point x) {
       x.getY();
    }
    
    Point makePoint(double x, double y) {
      return new Point((int)x, (int)y);
    }
}
