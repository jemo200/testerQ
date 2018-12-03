package testerq.core;

import java.io.Serializable;
import java.util.HashMap;

public class MemberStats implements Serializable {
    public HashMap<String, Skill> stats = new HashMap<>();
}
