package scout.g4;

import scout.sim.*;

import java.util.*;


//Read scout.sim.Player for more information!
public class Player extends scout.sim.Player {
    HashSet<Point> enemyLocations;
    HashSet<Point> safeLocations;
    int knownLocations[][];
    Point pos;
    Point endDir;
    Random gen;
    Point lastMove;
    int t,n, turnsPassed;
    int seed;
    boolean localized;
    boolean localizedX;
    boolean localizedY;
    int turnsSinceSync[];
    
    // Time since seeing another player after which their info should be
    // considered out of date (so we should try to communicate again)
    public final int STALE_THRESHOLD = 30;

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
        this.turnsPassed = 0;
        int size = 2 * this.n + 3;
        //this.map = new CellObject[size][];
        this.pos = new Point(0, 0);
        this.localized = false;
        this.turnsSinceSync = new int[s];
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
    
    @Override
    public void moveFinished() {
        pos.x += this.lastMove.x;
        pos.y += this.lastMove.y;
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
        this.lastMove = move;
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
                }
                Point consideredLocation = new Point(pos.x + i - 1, pos.y + j - 1);
                if(safe) {
                    safeLocations.add(consideredLocation);
                } else {
                    enemyLocations.add(consideredLocation);
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
        
        Point meeting = meetingPoint(nearbyIds);
        if (meeting != null) {
            return meeting;
        }
        return bestPoint();
    }
    
    @Override
    public void communicate(ArrayList<ArrayList<ArrayList<String>>> nearbyIds, List<CellObject> concurrentObjects) {
        if (!localizedY) {
            if (nearbyIds.get(1).get(0) == null) {
                localize(new Point(pos.x, 0));
                localized = false;
                localizedY = true;
            }
            if (nearbyIds.get(1).get(2) == null) {
                localize(new Point(pos.x, this.n + 1));
                localized = false;
                localizedY = true;
            }
        }
        if (!localizedX) {
            if (nearbyIds.get(0).get(1) == null) {
                localize(new Point(0, pos.y));
                localized = false;
                localizedX = true;
            }
            if (nearbyIds.get(2).get(1) == null) {
                localize(new Point(this.n + 1, pos.y));
                localized = false;
                localizedX = true;
            }
        }
        if (localizedX && localizedY) {
            localized = true;
        }
        for (CellObject obj : concurrentObjects) {
            if (obj instanceof Player) {
                Player other = (Player) obj;
                if (!localized && other.localized) {
                    localize(other.pos);
                }
                for (Point p : other.safeLocations) {
                    this.safeLocations.add(translatePoint(p, other));
                }
                for (Point p : other.enemyLocations) {
                    this.enemyLocations.add(translatePoint(p, other));
                }
                turnsSinceSync[other.seed] = 0;
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
        --t;
        turnsPassed++;
        for (int i = 0; i < turnsSinceSync.length; i++) {
            turnsSinceSync[i]++;
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
        //System.out.println("Player " + this.seed + " (" + pos.x + "," + pos.y + ") localized on turn " + turnsPassed + "!");
        this.localized = true;
    }
    
    // Parse the raw number from an id string
    public int idNum(String id) {
        String suffix = id.substring(1);
        return Integer.parseInt(suffix);
    }
    
    // Get a point to meet with an adjacent scout at, if necessary
    public Point meetingPoint(ArrayList<ArrayList<ArrayList<String>>> nearbyIds) {
        int maxId = -1;
        int scoutX = 0;
        int scoutY = 0;
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                if (i == 0 && j == 0) {
                    continue;
                }
                ArrayList<String> ids = nearbyIds.get(i).get(j);
                if (ids == null) {
                    continue;
                }
                for (String id : ids) {
                    if (id.charAt(0) == 'P') {
                        int consideredId = idNum(id);
                        if (turnsSinceSync[consideredId] > STALE_THRESHOLD && consideredId > maxId) {
                            //System.out.println(seed + ": Haven't seen scout " + consideredId + " in " + turnsSinceSync[consideredId] + " turns, trying to meet up");
                            maxId = consideredId;
                            scoutX = i - 1;
                            scoutY = j - 1;
                        }
                    }
                }
            }
        }
        if (maxId != -1) {
            if (scoutX == 1) {
                return new Point(1, 0);
            } else if (scoutY == 1) {
                return new Point(0, 1);
            } else if (scoutX == 0 && scoutY == 0) {
                return new Point(0, -1);
            } else {
                return new Point(0, 0);
            }
        } else {
            return null;
        }
    }
    
    public Point bestPoint() {
        int maxUnseen = -1;
        ArrayList<Point> maxPoints = new ArrayList<Point>();
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                if (i == 0 && j == 0) {
                    continue;
                }
                Point move = new Point(i - 1, j - 1);
                Point newPos = new Point(this.pos.x + move.x, this.pos.y + move.y);
                int unseen = unseenSquares(newPos);
                if (unseen > maxUnseen) {
                    maxUnseen = unseen;
                    maxPoints.clear();
                    maxPoints.add(move);
                } else if (unseen == maxUnseen) {
                    maxPoints.add(move);
                }
            }
        }
        int index = gen.nextInt(maxPoints.size());
        return maxPoints.get(index);
    }
    
    public int unseenSquares(Point surrounding) {
        int unseen = 0;
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                Point p = new Point(surrounding.x + i - 1, surrounding.y + j - 1);
                if ((!(localizedX || localized) || (p.x <= this.n && p.x >= 1)) && (!(localizedY || localized) || (p.y <= this.n && p.y >= 1))) {
                    if (!this.safeLocations.contains(p) && !this.enemyLocations.contains(p)) {
                        unseen++;
                    }
                }
            }
        }
        return unseen;
    }
}
