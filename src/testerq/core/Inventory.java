package testerq.core;

import java.io.Serializable;
import java.util.HashMap;

public class Inventory implements Serializable{
    public HashMap<String,Item> inventory = new HashMap<>();
    
}
