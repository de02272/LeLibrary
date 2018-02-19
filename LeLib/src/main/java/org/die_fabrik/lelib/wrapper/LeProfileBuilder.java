package org.die_fabrik.lelib.wrapper;

import org.die_fabrik.lelib.LeUtil;

public class LeProfileBuilder {
    private String name;
    private LeService[] leServices;
    
    public LeProfile create() {
        LeUtil.checkExistence(leServices);
        LeUtil.checkExistence(name);
        
        return new LeProfile(name, leServices);
    }
    
    public LeProfileBuilder setLeServices(LeService... leServices) {
        LeUtil.checkExistence(leServices);
        
        this.leServices = leServices;
        return this;
    }
    
    public LeProfileBuilder setName(String name) {
        LeUtil.checkExistence(name);
        
        this.name = name;
        return this;
    }
}