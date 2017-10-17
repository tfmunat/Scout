package scout.g4;

import scout.sim.*;

public class PathNode {
    Point p;
    int cost;
    PathNode from;
    
    public PathNode(Point p, int cost, PathNode from) {
        this.p = p;
        this.cost = cost;
        this.from = from;
    }
}
