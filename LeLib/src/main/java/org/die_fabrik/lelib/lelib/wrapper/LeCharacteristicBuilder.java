package org.die_fabrik.lelib.lelib.wrapper;

import org.die_fabrik.lelib.LeUtil;
import org.die_fabrik.lelib.data.LeData;
import org.die_fabrik.lelib.wrapper.ELeCharacteristicAccess;
import org.die_fabrik.lelib.wrapper.ELeNotification;

import java.util.UUID;

public class LeCharacteristicBuilder {
    private ELeNotification notification = ELeNotification.NONE;
    private String name;
    private UUID uuid;
    private Class<? extends LeData> dataClass;
    private org.die_fabrik.lelib.wrapper.ELeCharacteristicAccess access = org.die_fabrik.lelib.wrapper.ELeCharacteristicAccess.READ;
    
    public LeCharacteristic create() {
        LeUtil.checkExistence(name);
        LeUtil.checkExistence(access);
        LeUtil.checkExistence(dataClass);
        LeUtil.checkExistence(notification);
        
        return new LeCharacteristic(name, uuid, notification, dataClass, access);
    }
    
    public org.die_fabrik.lelib.wrapper.LeCharacteristicBuilder setAccess(ELeCharacteristicAccess access) {
        LeUtil.checkExistence(access);
        
        this.access = access;
        return this;
    }
    
    public org.die_fabrik.lelib.wrapper.LeCharacteristicBuilder setDataClass(Class<? extends LeData> dataClass) {
        LeUtil.checkExistence(dataClass);
        
        this.dataClass = dataClass;
        return this;
    }
    
    public org.die_fabrik.lelib.wrapper.LeCharacteristicBuilder setName(String name) {
        LeUtil.checkExistence(name);
        
        this.name = name;
        return this;
    }
    
    public org.die_fabrik.lelib.wrapper.LeCharacteristicBuilder setNotification(ELeNotification notification) {
        LeUtil.checkExistence(notification);
        
        this.notification = notification;
        return this;
    }
    
    public org.die_fabrik.lelib.wrapper.LeCharacteristicBuilder setUUID(UUID uuid) {
        this.uuid = uuid;
        return this;
    }
}