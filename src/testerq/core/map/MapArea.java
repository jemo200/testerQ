/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package testerq.core.map;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author emoj
 */
public class MapArea {
    public String name;
    
    public List<MapZone> zones = new ArrayList<MapZone>();
    
    public String[] zoneLabels;
    
    public void setupZones(String areaName) {
        for(String zoneLabel: zoneLabels) {
            zones.add(generateZone(zoneLabel));
        }
    }
    
    private MapZone generateZone(String label) {
        MapZone zone = new MapZone();
        zone.name = label;
        BufferedReader br = null;
        try {
            List<String[]> lines = new ArrayList<>();
            //br = new BufferedReader(new FileReader("map/" + label + ".txt"));
            br = new BufferedReader(new InputStreamReader(getClass().getResourceAsStream("/map/" + label + ".txt")));
            String line = null;

            while ((line = br.readLine()) != null) {
                String[] values = line.split(",");
                lines.add(values);
            }
            String[][] result = new String[lines.size()][];
            for(int i = 0; i<result.length; i++) {
                result[i] = lines.get(i);
            }
            br.close();
            zone.zone2DArray = result;

        } catch (FileNotFoundException ex) {
            System.out.println(ex);
        } catch (IOException ex) {
            System.out.println(ex);
        } finally {
            try {
                br.close();
            } catch (IOException ex) {
                System.out.println(ex);
            }
        }
        return zone;
    }
    
}
