package scout.g4;

import scout.sim.*;
import java.util.*;

// Read scout.sim.Player for more information!
public class Player extends scout.sim.Player {

    // variables
    Random gen;
    int seed;
    HashSet<Point> enemyLocations, safeLocations, unexploredLocations;
    int knownLocations[][], turnsSinceSync[], lastVisited[][];
    Point pos, endDir, lastMove, outpost;
    int t, n, turnsPassed;
    State state;
    boolean localized, localizedX, localizedY, endGame;
    String traveler;
    int xOffset;
    int yOffset;
    Point toExplore;
    List<Point> landmarks;

    public enum State {
        LOCALIZING, EXPLORING, MEETING, RETURNING
    }

    /**
     * Time since seeing another player after which their info should be
     * considered out of date (so we should try to communicate again
     */
    public final int STALE_THRESHOLD = 100;

    // better to use init instead of constructor, don't modify ID or simulator will throw error
    public Player(int id) {
        super(id);
        seed=id;
    }

    /**
     * Called at the start
     * @param id Is the letter 'S' concatenated with an index. Index is the same int as in the constructor.
     * @param s The number of scouts
     * @param n The size of the enemy space, board size is therefore (n+2)x(n+2)
     * @param t The number of turns for the game
     * @param landmarkLocations The list of (x,y) landmarks. Enemies and landmarks can coincide.
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
        this.lastVisited = new int[n + 2][n + 2];
        this.pos = new Point(0, 0);
        this.localized = false;
        this.turnsSinceSync = new int[s];
        this.landmarks = landmarkLocations;
        this.traveler = "S1";
        if (id.equals(traveler)){
            this.endDir = astar(id); // returns an outpost Point(x, y)
        } else if (this.seed % 4 == 0) {
            this.endDir = new Point(1, 1);
            this.outpost = new Point(n + 1, n + 1);
        } else if (this.seed % 4 == 1) {
            this.endDir = new Point(1, -1);
            this.outpost = new Point(n + 1, 0);
        } else if (this.seed % 4 == 2) {
            this.endDir = new Point(-1, 1);
            this.outpost = new Point(0, n + 1);
        } else if (this.seed % 4 == 3) {
            this.endDir = new Point(-1, -1);
            this.outpost = new Point(0, 0);
        }
        this.endGame = false;
        this.state = State.LOCALIZING;
        this.unexploredLocations = new HashSet<>();
        for (int i = 0; i < n + 2; i++) {
            for (int j = 0; j < n + 2; j++) {
                Point p = new Point(i, j);
                if (inQuadrant(p)) {
                    unexploredLocations.add(p);
                }
            }
        }

    }

    /**
     * Called when the actual move finishes, update your player's x and y here!
     */

    @Override
    public void moveFinished() {
        pos.x += this.lastMove.x;
        pos.y += this.lastMove.y;
        if (localized) {
            this.lastVisited[pos.x][pos.y]++;
        }
        if (toExplore != null && toExplore.x == pos.x && toExplore.y == pos.y) {
            toExplore = null;
        }
    }


    /**
     * @param nearbyIds 3 x 3 grid of list of neighbouring IDs
     *  nearbyIds.get(1).get(1) is the center, which is the current location of the player
     *  Scout IDs look like "S123" where 123 is some unique numbering of scouts
     *  Enemy IDs look like "E123" where 123 is some unique random number
     *  Landmark IDs look like "L123" where 123 is some unique numbering of landmarks
     *  Outpost IDs look like "O123" where 123 is some unique numbering of outposts
     *  Outposts can only be at (0,0) or (0,n+1) or (n+1,0) or (n+1,n+1) on the board
     *  If any position of the 3x3 grid is null, it is off the board, i.e. you are on the border
     *
     * @param concurrentObjects has the CellObjects on the (1,1) position of nearbyIds,
     *  i.e. the objects on the same cell
     *  The (x,y) coordinate of Landmarks can be retrieved using ((Landmark)object).getLocation();
     *  Information can be stored in or retrieved from Outposts using the public methods of scout.sim.Outpost
     *  Communication between players takes place using custom methods, since you have access to the player object
     *
     * @return (x,y) direction, x in {-1,0,1} y in {-1,0,1}
     */
    @Override
    public Point move(ArrayList<ArrayList<ArrayList<String>>> nearbyIds, List<CellObject> concurrentObjects) {
        logNeighbors(nearbyIds);
        Point move = getMove(nearbyIds, concurrentObjects);
        // check for out of bounds
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
                // check if an enemy is there
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
        if (state == State.LOCALIZING || state == State.RETURNING) {
            //move to our outpost
            return endDir;
        } else if (state == State.MEETING) {
            return moveTowards(new Point(n/2, n/2));
        } else if (state == State.EXPLORING) {
            if (!inQuadrant(this.pos)) {
                return moveTowards(this.outpost);
            }
            if (toExplore != null) {
                return moveTowards(toExplore);
            }
            Point meeting = meetingPoint(nearbyIds);
            if (meeting != null) {
                return meeting;
            }
            return bestPoint();
        } else {
            return new Point(0, 0);
        }
    }

    /* inner class for traveler */
    public class Astar {
        int time;
        Astar prev;
        Point end, p;


        public PathNode(Point ending, Pointp, int time, Astar prev) {
            this.ending = ending;
            this.p = p;
            this.time = time;
            this.prev = prev;
        }
    }


    /**
     * Called every turn as opposed to every 2/3/6/9 turns.
     * @param nearbyIds 3 x 3 grid of list of neighbouring IDs. 
     *  Scout IDs look like "S123" where 123 is some unique numbering of scouts
     *  Enemy IDs look like "E123" where 123 is some unique random number
     *  Landmark IDs look like "L123" where 123 is some unique numbering of landmarks
     *  Outpost IDs look like "O123" where 123 is some unique numbering of outposts
     *  Outposts can only be at (0,0) or (0,n+1) or (n+1,0) or (n+1,n+1). 
     */
    @Override
    public void communicate(ArrayList<ArrayList<ArrayList<String>>> nearbyIds, List<CellObject> concurrentObjects) {
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
        if (this.state == State.EXPLORING && t <= travelTime(pos, new Point(n / 2, n / 2)) + travelTime(new Point(n / 2, n / 2), outpost)) {
            endGame = true;
            this.state = State.MEETING;
        }
        if (this.state == State.MEETING && t <= travelTime(pos, outpost)) {
            this.state = State.RETURNING;
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
            Point localSafe = new Point(safe.x + dx, safe.y + dy);
            localizedSafe.add(localSafe);
            unexploredLocations.remove(localSafe);
        }
        this.safeLocations = localizedSafe;
        HashSet<Point> localizedEnemies = new HashSet<>();
        for (Point enemy : this.enemyLocations) {
            Point localEnemy = new Point(enemy.x + dx, enemy.y + dy);
            localizedEnemies.add(localEnemy);
            unexploredLocations.remove(localEnemy);
        }
        this.enemyLocations = localizedEnemies;
        //System.out.println("Player " + this.seed + " (" + pos.x + "," + pos.y + ") localized on turn " + turnsPassed + "!");
        this.localized = true;
        if (this.state == State.LOCALIZING) {
            this.state = State.EXPLORING;
        }
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
                        if (seed % 4 == consideredId % 4 && turnsSinceSync[consideredId] > STALE_THRESHOLD && consideredId > maxId) {
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
                if (!inQuadrant(newPos)) {
                    continue;
                }
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
        if (maxUnseen > 0 || !localized) {
            int index = gen.nextInt(maxPoints.size());
            return maxPoints.get(index);
        } else {
            Point p = null;
            for (Point unexplored : unexploredLocations) {
                p = unexplored;
                break;
            }
            if (p == null) {
                return moveTowards(new Point(n/2, n/2));
            } else {
                unexploredLocations.remove(p);
                toExplore = p;
                return moveTowards(p);
            }
        }
    }

    public boolean inQuadrant(Point p) {
        if (this.seed % 4 == 0) {
            return (p.x >= (int) Math.ceil(n/2.0) && p.x <= n + 1 && p.y >= (int) Math.ceil(n/2.0) && p.y <= n + 1);
        } else if (this.seed % 4 == 1) {
            return (p.x >= (int) Math.ceil(n/2.0) && p.x <= n + 1 && p.y >= 0 && p.y <= (int) Math.ceil(n/2.0));
        } else if (this.seed % 4 == 2) {
            return (p.x >= 0 && p.x <= (int) Math.ceil(n/2.0) && p.y >= (int) Math.ceil(n/2.0) && p.y <= n + 1);
        } else {
            return (p.x >= 0 && p.x <= (int) Math.ceil(n/2.0) && p.y >= 0 && p.y <= (int) Math.ceil(n/2.0));
        }
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

    public int moveCost(Point move) {
        if (move.x == 0 && move.y == 0) {
            return 1;
        }
        int scale = 1;
        Point to = new Point(pos.x + move.x, pos.y + move.y);
        if (enemyLocations.contains(to) || enemyLocations.contains(pos)) {
            scale = 3;
        }
        int cost = (int) Math.abs(move.x) + Math.abs(move.y) + 1;
        return cost * scale;
    }

    public Point moveTowards(Point p) {
        return new Point(Integer.signum(p.x - pos.x), Integer.signum(p.y - pos.y));
    }

    public ArrayList<Point> getPath(Point to) {
        ArrayList<Point> path = new ArrayList<Point>();
        path.add(this.pos);
        return path;
    }

    public int travelTime(Point from, Point to) {
        return (int) 6 * Math.max(Math.abs(from.x - to.x), Math.abs(from.y - to.y));
    }

    public void addSafeLocation(Point p) {
        if (unexploredLocations != null) {
            unexploredLocations.remove(p);
        }
        safeLocations.add(p);
    }

    public void addEnemyLocation(Point p) {
        if (unexploredLocations != null) {
            unexploredLocations.remove(p);
        }
        enemyLocations.add(p);
    }
}
