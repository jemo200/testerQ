package testerq.core;

import java.awt.Point;
import java.io.Serializable;
import java.util.HashMap;

public class MemberSave implements Serializable{
    public Point position;
    public String zone;
    public String avatar;
    public HashMap<String, Item> inventory;
}
