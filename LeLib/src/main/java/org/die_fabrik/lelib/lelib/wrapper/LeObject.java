package org.die_fabrik.lelib.lelib.wrapper;

/**
 * Created by Michael on 03.01.2018.
 */

public abstract class LeObject {
    protected final String TAG = this.getClass().getSimpleName();
    private final String name;
    
    public LeObject(String name) {
        this.name = name;
    }
    
    public String getName() {
        return name;
    }
}
