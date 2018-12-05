package testerq.core;

import java.io.Serializable;

public class Skill implements Serializable {
    int skillExperience;
    
    public Skill() {
        this.skillExperience = 0;
    }
    
    public int getLevel() {
        if (this.skillExperience < 10) {
            return 1;
        } else if (this.skillExperience >= 10 && this.skillExperience < 20) {
            return 2;
        } else if (this.skillExperience >= 20 && this.skillExperience < 30) {
            return 3;
        } else if (this.skillExperience >= 30 && this.skillExperience < 40) {
            return 4;
        } else if (this.skillExperience > 40) {
            return 5;
        }
        return 0;
    }
    
    public void addExp(int exp) {
        this.skillExperience += exp;
    }
    
    public int getExp() {
        return this.skillExperience;
    }
    
}
