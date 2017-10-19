/* n = 100
 * s = 15
 * e = 1000
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
        int count2 = 1;
        int x = 0;
        int y = (n/2);
        while(locations.size() < num) {
            count1++;
            count2++;
            if((count1 >= (n/2) - 1) || (count2 >= (n - 1))){
                locations.add(new Point((gen.nextInt(n)) + 1, (gen.nextInt(n)) + 1));
            } else {
                locations.add(new Point(x + count1, y + count2));
                locations.add(new Point(x + count1, y - count2));
            }
        }
        return locations;
    }
}
