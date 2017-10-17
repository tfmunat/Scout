package scout.g4;

import scout.sim.Point;

import java.util.HashSet;
import java.util.ArrayList;
import java.util.Set;
import java.util.List;
import java.util.Random;

public class EnemyMapper extends scout.sim.EnemyMapper {
    @Override
    public Set<Point> getLocations(int n, int num, List<Point> landmarkLocation, Random gen) {
        Set<Point> locations = new HashSet<>();
//        ArrayList<Point> offsets = new ArrayList<Point>();
//        offsets.add(new Point(n/3 + 1, 1));
//        offsets.add(new Point(1, n/3 + 1));
//        offsets.add(new Point(n/3 + 1, n/3 + 1));
//        offsets.add(new Point(2*n/3 + 1, n/3 + 1));
//        offsets.add(new Point(n/3 + 1, 2*n/3 + 1));
         for (int i = 0; i < num; i++) {
//            Point offset = offsets.get(i % 5);
//            int subNum = (i % 5) < (num % 5) ? num / 5 + 1 : num / 5;
//            locations.add(placePoint(n / 3, subNum, i / 5, offset));
             locations.add(placePoint(n, num, i, new Point(0, 0)));
         }
         return locations;
        
    }
    
    public Point placePoint(int sideLength, int num, int i, Point offset) {
        int sqrt = (int)Math.ceil(Math.sqrt(num));
        int perRow = sqrt;
        int subOffset = sideLength / sqrt / 2 + 1;
        int x = i / sqrt;
        int y = i % sqrt;
        return new Point(offset.x + (x * sideLength / sqrt) + subOffset, offset.y + (y * sideLength / sqrt) + subOffset);
    }
}

