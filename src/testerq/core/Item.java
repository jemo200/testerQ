package testerq.core;

import java.io.Serializable;

public class Item implements Serializable{
    public String name;
    public int itemId;
    public int quantity;
    
    public Item(String name, int itemId, int quantity) {
        this.name = name;
        this.itemId = itemId;
        this.quantity = quantity;
    }
    
}
