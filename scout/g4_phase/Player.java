package scout.g4_phase;

import scout.sim.*;

import java.util.*;


//Read scout.sim.Player for more information!
public class Player extends scout.sim.Player {
    HashSet<Point> enemyLocations;
    HashSet<Point> safeLocations;
    CellObject map[][];
    Point pos;
    Point endDir;
    Point meetUpMove; // Direction all players should move to meet up. So top left corner
    // spot would be a move of (1,1)
    Random gen;
    int t,n, tStart;
    int seed;
    boolean localized;
    int phaseNumber;
    
    // There will be 4 phases.
    //      phase 0: Exploration phase. 
    //          This is a shorter phase where players move around and attempt to orient 
    //          themsleves. The time will be dependent on the number of turns so all  
    //          players can use it as a reference point
    //      phase 1: Meetup phase.
    //          Triggered by the number of turns in the game all players will attempt to
    //          meet in the center of the map and exchange information. Here players will
    //          coordinate which outpost each one should go to, who should wait in the
    //          center to collect information and which parts of the map need exploring.
    //      phase 2: Continued exploration.
    //          Using the remainign time scouts will explore their assigned portions of
    //          the map. Players will check back in and exhcange information with the
    //          player left in the center (if there is one).
    //      phase 3: Return to outposts.
    //          Here scouts will take all information they have gathered to their 
    //          assigned outposts. This event will be triggered by the amount of time
    //          left in the game and the distance the player is from their outpost.
    //          So at different times each player will return to their outpost, but
    //          the hope is that by the end each player will be at their outpost before 
    //          the end of the game.          

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
        this.tStart = t;// This will hold on to the maximum t at the start of the game
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
        this.meetUpMove = new Point(-1,-1);// This will move all players to the top left corner (0,0)
        this.phaseNumber = 0;// initially set to explore phase
    }

    /**
     * nearby IDs is a 3 x 3 grid of nearby IDs with you in the center (1,1) position. A position is null if it is off the board.
     * Enemy IDs start with 'E', Player start with 'P', Outpost with 'O' and landmark with 'L'.
     *
     */
    @Override
    public Point move(ArrayList<ArrayList<ArrayList<String>>> nearbyIds, List<CellObject> concurrentObjects) {
        logNeighbors(nearbyIds);
        checkPhase();
        Point move = new Point(0,0);
        switch(this.phaseNumber) {
            case 0 :// phase 0
                // Statements
            	move = move_explore();
                break; // optional

            case 1 :// phase 1
              // Statements
            	move = move_meetup(nearbyIds, concurrentObjects);
                break; // optional

            case 2:// phase 2
            	move = move_explore();
                break;

            case 3://phase 3
            	move = move_outpost(nearbyIds, concurrentObjects);
                break;
        }





//        Point move = getMove(nearbyIds, concurrentObjects);
//        Point move = move_meetup(nearbyIds, concurrentObjects);
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



    private void checkPhase() {
    /* Set the phase for the scout depending on the turn number, their position, and the 
    *   current phase
    */
    	if ( this.t <= n*3*2 & this.t > n*3) {// meetup Conditions
    		System.out.println("Meet up phase");
    		this.phaseNumber = 1;
    	} else if (this.t <= n*3) { // final reporting phase
    		System.out.println("Final reporting phase");
    		this.phaseNumber = 3;
    	} else {
    		System.out.println("Initial Exploration phase.");
    		this.phaseNumber = 0;
    	}
//    	this.phaseNumber = 1;
    	return;
    }


    private Point move_explore() {
        // in general movements from this function will opt for exploring. It will also
        // take into account how long it's been since it checked in, and how much new 
        // information has been gathered. Later iterations can move towards unexplored
        // areas and make sure not to explore things that have already been seen
    	return new Point((gen.nextInt(2) * 2) - 1, (gen.nextInt(2) * 2) - 1);
    }
    private Point move_meetup(ArrayList<ArrayList<ArrayList<String>>> nearbyIds, List<CellObject> concurrentObjects) {
        // Moves generated by this function will be towards the goal of moving to the 
        // meet up point as quickly as possible.
    	
    	//move to outpost with least x and y coordinate
    	 if (nearbyIds.get(1 + meetUpMove.x).get(1 + meetUpMove.y) != null) {
             //move x and y
             return meetUpMove;
         }
         if (nearbyIds.get(1 + meetUpMove.x).get(1) != null) {
             //move only x
             return new Point(meetUpMove.x, 0);
         }
         if (nearbyIds.get(1).get(1 + meetUpMove.y) != null) {
             //move only y
             return new Point(0, meetUpMove.y);
         }
         return new Point(0, 0);
//        return new Point(0, 0);
    }
    
    
    
    
    
    private Point move_outpost(ArrayList<ArrayList<ArrayList<String>>> nearbyIds, List<CellObject> concurrentObjects) {
        // Moves the player towards the outpost as quickly as possible. Movement will take
        // into account the shortest timed path, to the outpost given known enemy positions.
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
//        return new Point(0, 0);
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

	@Override
	public void moveFinished() {
		// TODO Auto-generated method stub
		
	}
}
