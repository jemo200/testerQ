package testerq.core;

import java.awt.Point;
import java.util.HashMap;


public class Member extends GameObject{
    
    public String name;
    public Inventory inventory;
    public QuestLog questLog;
    public MemberStats stats;
    
    public Member(String name, int x, int y, String sprite) {
        super();
        this.name = name;
        super.setPosition(x, y);
        super.setSprite(sprite);
    }
    
    public Member(String name, int x, int y, String sprite, String worldZone) {
        super();
        this.name = name;
        super.setWorldZone(worldZone);
        super.setPosition(x, y);
        super.setSprite(sprite);
    }
    
    public Member(String name, Point position, String sprite) {
        super();
        this.name = name;
        super.setPosition(position);
        super.setSprite(sprite);
    }
    
    public Member(String name, Point position, String sprite, String worldZone) {
        super();
        this.name = name;
        super.setWorldZone(worldZone);
        super.setPosition(position);
        super.setSprite(sprite);
    }
    
}
