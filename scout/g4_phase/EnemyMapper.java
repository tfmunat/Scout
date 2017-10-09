package scout.g4_phase;

import scout.sim.Point;

import java.util.HashSet;
import java.util.Set;
import java.util.List;
import java.util.Random;

public class EnemyMapper extends scout.sim.EnemyMapper {
    @Override
    public Set<Point> getLocations(int n, int num, List<Point> landmarkLocation, Random gen) {
        Set<Point> locations = new HashSet<>();
        for (Point p : landmarkLocation) {
            if (p.x == 1 || p.x == n || p.y == 1 || p.y == n) {
                continue;
            }
            locations.add(new Point(p.x + 1, p.y + 1));
            if (locations.size() == num) {
                break;
            }
            locations.add(new Point(p.x , p.y + 1));
            if (locations.size() == num) {
                break;
            }
            locations.add(new Point(p.x - 1, p.y + 1));
            if (locations.size() == num) {
                break;
            }
            locations.add(new Point(p.x + 1, p.y));
            if (locations.size() == num) {
                break;
            }
            locations.add(new Point(p.x - 1, p.y));
            if (locations.size() == num) {
                break;
            }
            locations.add(new Point(p.x + 1, p.y - 1));
            if (locations.size() == num) {
                break;
            }
            locations.add(new Point(p.x, p.y - 1));
            if (locations.size() == num) {
                break;
            }
            locations.add(new Point(p.x - 1, p.y - 1));
        }
        return locations;
    }
}

