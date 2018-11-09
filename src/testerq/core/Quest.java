package testerq.core;

import java.io.Serializable;
import java.util.ArrayList;

public class Quest implements Serializable{
    public boolean complete;
    public int currentTask;
    public ArrayList<Task> tasks;
    public ArrayList<Item> reward;
}
