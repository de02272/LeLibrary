package org.die_fabrik.lelib.lelib.wrapper;

import org.die_fabrik.lelib.LeUtil;
import org.die_fabrik.lelib.wrapper.LeProfile;
import org.die_fabrik.lelib.wrapper.LeService;

public class LeProfileBuilder {
    private String name;
    private LeService[] leServices;
    
    public LeProfile create() {
        LeUtil.checkExistence(leServices);
        LeUtil.checkExistence(name);
        
        return new LeProfile(name, leServices);
    }
    
    public org.die_fabrik.lelib.wrapper.LeProfileBuilder setLeServices(LeService... leServices) {
        LeUtil.checkExistence(leServices);
        
        this.leServices = leServices;
        return this;
    }
    
    public org.die_fabrik.lelib.wrapper.LeProfileBuilder setName(String name) {
        LeUtil.checkExistence(name);
        
        this.name = name;
        return this;
    }
}