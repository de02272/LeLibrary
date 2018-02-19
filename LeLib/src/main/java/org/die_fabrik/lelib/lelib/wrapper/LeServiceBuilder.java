package org.die_fabrik.lelib.lelib.wrapper;

import android.bluetooth.BluetoothGattService;

import org.die_fabrik.lelib.LeUtil;
import org.die_fabrik.lelib.wrapper.LeCharacteristic;
import org.die_fabrik.lelib.wrapper.LeService;

import java.util.UUID;

public class LeServiceBuilder {
    private String name;
    private UUID uuid;
    private LeCharacteristic[] leCharacteristics;
    private int serviceType;
    private boolean advertisingService;
    
    public LeService create() {
        LeUtil.checkExistence(name);
        LeUtil.checkValue(serviceType, BluetoothGattService.SERVICE_TYPE_PRIMARY, BluetoothGattService.SERVICE_TYPE_SECONDARY);
        
        return new LeService(name, uuid, leCharacteristics, serviceType, advertisingService);
    }
    
    public org.die_fabrik.lelib.wrapper.LeServiceBuilder setAdvertisingService(boolean advertisingService) {
        this.advertisingService = advertisingService;
        return this;
    }
    
    public org.die_fabrik.lelib.wrapper.LeServiceBuilder setLeCharacteristics(LeCharacteristic... leCharacteristics) {
        this.leCharacteristics = leCharacteristics;
        return this;
    }
    
    public org.die_fabrik.lelib.wrapper.LeServiceBuilder setName(String name) {
        LeUtil.checkExistence(name);
        this.name = name;
        return this;
    }
    
    /**
     * @param serviceType one of BluetoothGattService.SERVICE_TYPE_PRIMARY or BluetoothGattService.SERVICE_TYPE_SECONDARY
     * @return the builder
     */
    public org.die_fabrik.lelib.wrapper.LeServiceBuilder setServiceType(int serviceType) {
        LeUtil.checkValue(serviceType, BluetoothGattService.SERVICE_TYPE_PRIMARY, BluetoothGattService.SERVICE_TYPE_SECONDARY);
        
        this.serviceType = serviceType;
        return this;
    }
    
    public org.die_fabrik.lelib.wrapper.LeServiceBuilder setUUID(UUID uuid) {
        this.uuid = uuid;
        return this;
    }
}