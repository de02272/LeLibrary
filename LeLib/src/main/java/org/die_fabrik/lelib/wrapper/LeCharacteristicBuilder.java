package org.die_fabrik.lelib.wrapper;

import org.die_fabrik.lelib.LeUtil;
import org.die_fabrik.lelib.data.LeData;

import java.util.UUID;

public class LeCharacteristicBuilder {
    private boolean notification = false;
    private String name;
    private UUID uuid;
    private Class<? extends LeData> dataClass;
    private ELeCharacteristicAccess access;

    public LeCharacteristic create() {
        LeUtil.checkExistence(name);
        LeUtil.checkExistence(access);
        LeUtil.checkExistence(dataClass);
    
        return new LeCharacteristic(name, uuid, notification, dataClass, access);
    }
    
    public LeCharacteristicBuilder setAccess(ELeCharacteristicAccess access) {
        LeUtil.checkExistence(access);
        
        this.access = access;
        return this;
    }
    
    public LeCharacteristicBuilder setDataClass(Class<? extends LeData> dataClass) {
        LeUtil.checkExistence(dataClass);
        
        this.dataClass = dataClass;
        return this;
    }
    
    public LeCharacteristicBuilder setName(String name) {
        LeUtil.checkExistence(name);
        
        this.name = name;
        return this;
    }
    
    public LeCharacteristicBuilder setNotification(boolean notification) {
        this.notification = notification;
        return this;
    }
    
    public LeCharacteristicBuilder setUUID(UUID uuid) {
        this.uuid = uuid;
        return this;
    }
}