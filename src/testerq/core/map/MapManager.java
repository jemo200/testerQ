/*
 * (for now) Ser will have default starting zone/area/coord -> future lookup
 * 
 *
 */
package testerq.core.map;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MapManager {
    
    public MapManager() {
        zones = new HashMap<String, MapZone>();
        MapArea area1 = new MapArea();
        area1.name = "area1";
        area1.zoneLabels = new String[]{"zone1", "zone2"};
        area1.setupZones(area1.name);
        MapArea area2 = new MapArea();
        area2.name = "area2";
        area2.zoneLabels = new String[]{"zone1", "zone2"};
        area2.setupZones(area2.name);
        for (MapZone zone: area1.zones) {
            zones.put(area1.name + zone.name, zone);
        }
        for (MapZone zone: area2.zones) {
            zones.put(area2.name + zone.name, zone);
        }
    }
    
    public static Map<String, MapZone> zones;

}
