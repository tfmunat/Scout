package scout.g4;

public class TruePlayer {
    HashSet<Point> enemyLocations;
    HashSet<Point> safeLocations;
    boolean seenLocations[][];
    
    Point pos;
    
    int id;
    
    boolean localizedX;
    boolean localizedY;
    
    int t;
    int n;
    
    public TruePlayer(int n, int t, int id) {
        enemyLocations = new HashSet<>();
        safeLocations = new HashSet<>();
    }
    
    public void isLocalized() {
        return localizedX && localizedY;
    }
    
    public boolean hasSeen(Point p) {
        if (this.isLocalized()) {
            return seenLocations[p.x][p.y];
        } else {
            return (enemyLocations.contains(p) || safeLocations.contains(p));
        }
    }
}
