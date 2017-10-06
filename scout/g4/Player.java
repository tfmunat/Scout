package scout.g7;

import scout.sim.*;

import java.util.*;


//Read scout.sim.Player for more information!
public class Player extends scout.sim.Player {
    HashSet<Point> enemyLocations;
    HashSet<Point> safeLocations;
    CellObject map[][];
    Point pos;
    Point endDir;
    Random gen;
    int t,n;
    int seed;
    boolean localized;

    /**
    * better to use init instead of constructor, don't modify ID or simulator will error
    */
    public Player(int id) {
        super(id);
        seed=id;
    }

    /**
    *   Called at the start
    */
    @Override
    public void init(String id, int s, int n, int t, List<Point> landmarkLocations) {
        enemyLocations = new HashSet<>();
        safeLocations = new HashSet<>();
        gen = new Random(seed);
        this.t = t;
        this.n = n;
        int size = 2 * this.n + 3;
        this.map = new CellObject[size][];
        this.pos = new Point(0, 0);
        this.localized = false;
        if (this.seed % 4 == 0) {
            this.endDir = new Point(1, 1);
        } else if (this.seed % 4 == 1) {
            this.endDir = new Point(1, -1);
        } else if (this.seed % 4 == 2) {
            this.endDir = new Point(-1, 1);
        } else if (this.seed % 4 == 3) {
            this.endDir = new Point(-1, -1);
        }
    }

    /**
     * nearby IDs is a 3 x 3 grid of nearby IDs with you in the center (1,1) position. A position is null if it is off the board.
     * Enemy IDs start with 'E', Player start with 'P', Outpost with 'O' and landmark with 'L'.
     *
     */
    @Override
    public Point move(ArrayList<ArrayList<ArrayList<String>>> nearbyIds, List<CellObject> concurrentObjects) {
        logNeighbors(nearbyIds);
        Point move = getMove(nearbyIds, concurrentObjects);
        if (nearbyIds.get(1 + move.x).get(1) == null) {
            move.x = 0;
        }
        if (nearbyIds.get(1).get(1 + move.y) == null) {
            move.y = 0;
        }
        pos.x += move.x;
        pos.y += move.y;
        System.out.println("Pos: (" + pos.x + "," + pos.y + ")");
        return move;
    }
    
    private void logNeighbors(ArrayList<ArrayList<ArrayList<String>>> nearbyIds) {
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                ArrayList<String> ids = nearbyIds.get(i).get(j);
                if (ids == null) {
                    continue;
                }
                boolean safe = true;
                for (String id : ids) {
                    if (id.charAt(0) == 'E') {
                        safe = false;
                    }
                    Point consideredLocation = new Point(pos.x + i - 1, pos.y + j - 1);
                    if(safe) {
                        if(!safeLocations.contains(consideredLocation)) {
                            safeLocations.add(consideredLocation);
                        }
                    } else {
                        if(!enemyLocations.contains(consideredLocation)) {
                            enemyLocations.add(consideredLocation);
                        }
                    }
                }
            }
        }
    }
    
    private Point getMove(ArrayList<ArrayList<ArrayList<String>>> nearbyIds, List<CellObject> concurrentObjects) {
        /* If there's fewer than the number of turns required to get to any
         * corner, start moving in the end direction.
         */
        if (this.t < n * 3) {
            //move to outpost with least x and y coordinate
            if (nearbyIds.get(1 + endDir.x).get(1 + endDir.y) != null) {
                //move x and y
                return endDir;
            }
            if (nearbyIds.get(1 + endDir.x).get(1) != null) {
                //move only x
                return new Point(endDir.x, 0);
            }
            if (nearbyIds.get(1).get(1 + endDir.y) != null) {
                //move only y
                return new Point(0, endDir.y);
            }
            return new Point(0, 0);
        }
        
        //return x \in {-1,1}, y \in {-1,1}
        return new Point((gen.nextInt(2) * 2) - 1, (gen.nextInt(2) * 2) - 1);
    }
    
    @Override
    public void communicate(ArrayList<ArrayList<ArrayList<String>>> nearbyIds, List<CellObject> concurrentObjects) {
        --t;
        for (CellObject obj : concurrentObjects) {
            if (obj instanceof Player) {
                if (obj == this) {
                    continue;
                }
                Player other = (Player) obj;
                for (Point p : other.safeLocations) {
                    this.safeLocations.add(translatePoint(p, other));
                }
                for (Point p : other.enemyLocations) {
                    this.enemyLocations.add(translatePoint(p, other));
                }
            } else if (obj instanceof Landmark && !localized) {
                localize(((Landmark) obj).getLocation());
            } else if (obj instanceof Outpost) {
                if (!localized) {
                    localize(((Outpost) obj).getLocation());
                }
                Outpost post = (Outpost) obj;
                for(Point safe : safeLocations) {
                    post.addSafeLocation(safe);
                }
                for(Point unsafe : enemyLocations) {
                    post.addEnemyLocation(unsafe);
                }
            }
        }
    }
    
    public Point translatePoint(Point pt, Player other) {
        return new Point(this.pos.x + pt.x - other.pos.x, this.pos.y + pt.y - other.pos.y);
    }
    
    public void localize(Point p) {
        int x = p.x;
        int y = p.y;
        int dx = x - pos.x;
        int dy = y - pos.y;
        this.pos = new Point(x, y);
        HashSet<Point> localizedSafe = new HashSet<>();
        for (Point safe : this.safeLocations) {
            localizedSafe.add(new Point(safe.x + dx, safe.y + dy));
        }
        this.safeLocations = localizedSafe;
        HashSet<Point> localizedEnemies = new HashSet<>();
        for (Point enemy : this.enemyLocations) {
            localizedEnemies.add(new Point(enemy.x + dx, enemy.y + dy));
        }
        this.enemyLocations = localizedEnemies;
        System.out.println("Player " + this.seed + "(" + pos.x + "," + pos.y + ") localized!");
        for (Point enemy : this.enemyLocations) {
            System.out.println("\t(" + enemy.x + "," + enemy.y + ")");
        }
        this.localized = true;
    }
}
