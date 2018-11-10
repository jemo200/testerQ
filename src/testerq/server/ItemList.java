package testerq.server;

import java.util.HashMap;
import testerq.core.Item;

public class ItemList {
    public HashMap<String, Item> items = new HashMap<>();
    
    public ItemList() {
        this.items.put("treelogs", new Item("treelogs", 00001, 1));
        this.items.put("pickaxe", new Item("pickaxe", 00002, 1));
        this.items.put("stone", new Item("stone", 00003, 1));
        this.items.put("coins", new Item("coins", 00004, 1));
    }
}
