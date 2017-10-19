/* n = 100
 * s = 15
 * e = 700
 * t = 2000
 * fps = 500
 */


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
        int count1 = 1;
        int count2 = n/4;
        int count3 = n/8;
        int x = 0;
        int x2 = n;
        int y = (n/2);
        while(locations.size() < num) {
            count1++;
            count2++;
            count3++;
            if((count1 <= (n/2) - 1)){
                locations.add(new Point(x + count1, y + count1));
                locations.add(new Point(x + count1, y - count1));
                locations.add(new Point(x2 - count1, y + count1));
                locations.add(new Point(x2 - count1, y - count1));
            }
            if((count2 <= (n/2) - 1)) {
                locations.add(new Point(x + count2, y + count1));
                locations.add(new Point(x + count2, y - count1));
                locations.add(new Point(x2 - count2, y + count1));
                locations.add(new Point(x2 - count2, y - count1));
            }
            if((count3 <= (n/2) - 1)) {
                locations.add(new Point(x + count3, y + count1));
                locations.add(new Point(x + count3, y - count1));
                locations.add(new Point(x2 - count3, y + count1));
                locations.add(new Point(x2 - count3, y - count1));
            }
            // rest are scattered
            locations.add(new Point((gen.nextInt(n)) + 1, (gen.nextInt(n)) + 1));
        }
        return locations;
    }
}
