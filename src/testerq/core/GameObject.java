package testerq.core;

import java.awt.Point;

public class GameObject {
    private int mNetworkId = 0;
    private NetworkIdentity netId;
    private Point position;
    private String worldZone;

    public String getWorldZone() {
        return worldZone;
    }

    public void setWorldZone(String worldZone) {
        this.worldZone = worldZone;
    }

    public String getSprite() {
        return sprite;
    }

    public void setSprite(String sprite) {
        this.sprite = sprite;
    }
    private String sprite;
    
    GameObject() {
        position = new Point();
    }

    public Point getPosition() {
        return position;
    }
    
    public int getPositionX() {
        return position.x;
    }
    
    public int getPositionY() {
        return position.y;
    }

    public void setPosition(Point position) {
        this.position = position;
    }
    
    public void setPositionX(int x) {
        this.position.x = x;
    }
    
    public void setPositionY(int y) {
        this.position.y = y;
    }
    
    public void setPosition(int x, int y) {
        this.position.x = x;
        this.position.y = y;
    }

    public NetworkIdentity getNetId() {
        return netId;
    }

    public void setNetId(NetworkIdentity netId) {
        this.netId = netId;
    }

    public int getNetworkId() {
        return mNetworkId;
    }

    public void setNetworkId(int inNetworkId) {
        this.mNetworkId = inNetworkId;
    }
    
    public void update() {
        
    }
}
