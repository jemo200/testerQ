/*
 * (for now) Ser will have default starting zone/area/coord -> future lookup
 * 
 *
 */
package testerq.core.map;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import testerq.core.MapTransfer;

public class MapManager {
    
    public MapManager() {
        zones = new HashMap<String, MapZone>();
        transfers = new HashMap<String, MapTransfer>();
        MapArea area1 = new MapArea();
        area1.name = "area1";
        area1.zoneLabels = new String[]{"zone1", "zone2", "castle1"};
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
        
        MapTransfer trans1 = new MapTransfer();
        trans1.x = 9;
        trans1.y = 9;
        trans1.map = "area1castle1";
        transfers.put("1218", trans1);
        MapTransfer trans2 = new MapTransfer();
        trans2.x = 13;
        trans2.y = 18;
        trans2.map = "area1zone1";
        transfers.put("109", trans2);
    }
    
    public static Map<String, MapZone> zones;
    
    public static Map<String, MapTransfer> transfers;

}
