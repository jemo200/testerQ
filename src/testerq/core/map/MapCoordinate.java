/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package testerq.core.map;

/**
 *
 * @author emoj
 */
public class MapCoordinate {
    
    public MapCoordinate(CoordinateType type, int x, int y) {
        this.type = type;
        this.x = x;
        this.y = y;
    }
    public CoordinateType type;
    public int x;
    public int y;
}
