package testerq.core;

import java.io.Serializable;

public class Task implements Serializable{
    public Task(String verify) {
        this.verify = verify;
    }
    public String verify;
}
