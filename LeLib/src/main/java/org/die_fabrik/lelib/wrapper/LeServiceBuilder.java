package org.die_fabrik.lelib.wrapper;

import android.bluetooth.BluetoothGattService;

import org.die_fabrik.lelib.LeUtil;

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
    
    public LeServiceBuilder setAdvertisingService(boolean advertisingService) {
        this.advertisingService = advertisingService;
        return this;
    }
    
    public LeServiceBuilder setLeCharacteristics(LeCharacteristic... leCharacteristics) {
        this.leCharacteristics = leCharacteristics;
        return this;
    }
    
    public LeServiceBuilder setName(String name) {
        LeUtil.checkExistence(name);
        this.name = name;
        return this;
    }
    
    /**
     *
     * @param serviceType one of BluetoothGattService.SERVICE_TYPE_PRIMARY or BluetoothGattService.SERVICE_TYPE_SECONDARY
     * @return the builder
     */
    public LeServiceBuilder setServiceType(int serviceType) {
        LeUtil.checkValue(serviceType, BluetoothGattService.SERVICE_TYPE_PRIMARY, BluetoothGattService.SERVICE_TYPE_SECONDARY);
        
        this.serviceType = serviceType;
        return this;
    }
    
    public LeServiceBuilder setUUID(UUID uuid) {
        this.uuid = uuid;
        return this;
    }
}