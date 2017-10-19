package scout.g4;

import scout.sim.*;
import java.util.*;


//Read scout.sim.Player for more information!
public class PathGenerator {
    Point outpost;
    int n;
    int m;
    
    public PathGenerator(Point outpost, int n, int m) {
        this.outpost = outpost;
        this.n = n;
        this.m = m;
    }
    
    public ArrayList<Point> genPath() {
        int x = 1;
        int y = 1;
        ArrayList<Point> path = new ArrayList<Point>();
        path.add(new Point(0, 0));
        path.add(new Point(1, 1));
        Point dir1 = new Point(-1, 1);
        Point dir2 = new Point(1, -1);
        Point dir = dir1;
        boolean movingDiagonal = false;
        int horizontalMoves = 4;
        while (!(x == m - 1 && y == m - 1)) {
            if (movingDiagonal) {
                if (x + dir.x > m - 1 || x + dir.x < 1 || y + dir.y > m - 1 || y + dir.y < 1) {
                    movingDiagonal = false;
                    continue;
                } else {
                    x += dir.x;
                    y += dir.y;
                    path.add(new Point(x, y));
                }
            } else {
                if (horizontalMoves == 0) {
                    horizontalMoves = 4;
                    movingDiagonal = true;
                    dir = (dir == dir1) ? dir2 : dir1;
                } else {
                    if (x == 1) {
                        if (y < m - 1) {
                            y = y + 1;
                            path.add(new Point(x, y));
                            horizontalMoves--;
                        } else {
                            x = x + 1;
                            path.add(new Point(x, y));
                            horizontalMoves--;
                        }
                    } else if (y == 1) {
                        if (x < m - 1) {
                            x = x + 1;
                            path.add(new Point(x, y));
                            horizontalMoves--;
                        } else {
                            y = y + 1;
                            path.add(new Point(x, y));
                            horizontalMoves--;
                        }
                    } else if (x == m - 1) {
                        y = y + 1;
                        path.add(new Point(x, y));
                        horizontalMoves--;
                    } else if (y == m - 1) {
                        x = x + 1;
                        path.add(new Point(x, y));
                        horizontalMoves--;
                    }
                }
            }
        }
        if (outpost.x == n + 1) {
            for (Point p : path) {
                p.x = n + 1 - p.x;
            }
        }
        if (outpost.y == n + 1) {
            for (Point p : path) {
                p.y = n + 1 - p.y;
            }
        }
        return path;
    }
}
