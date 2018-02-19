package org.die_fabrik.lelib.lelib.wrapper;

import android.bluetooth.BluetoothGattService;
import android.content.Context;
import android.util.Log;

import org.die_fabrik.lelib.LeUtil;
import org.die_fabrik.lelib.wrapper.LeCharacteristic;
import org.die_fabrik.lelib.wrapper.LeProfile;
import org.die_fabrik.lelib.wrapper.LeServiceBuilder;

/**
 * Created by Michael on 11.01.2018.
 */

public class LeService extends LeAutoUuIdObject {
    private final LeCharacteristic[] leCharacteristics;
    private final int serviceType;
    private final boolean advertisingService;
    private final BluetoothGattService bluetoothGattService;
    private LeProfile leProfile;
    
    public LeService(String name, java.util.UUID UUID, LeCharacteristic[] leCharacteristics, int serviceType, boolean advertisingService) {
        super(name, UUID);
        this.leCharacteristics = leCharacteristics;
        this.serviceType = serviceType;
        this.advertisingService = advertisingService;
        bluetoothGattService = new BluetoothGattService(getUUID(), serviceType);
        if (leCharacteristics != null) {
            for (LeCharacteristic leCharacteristic : leCharacteristics) {
                bluetoothGattService.addCharacteristic(leCharacteristic.getBluetoothGattCharacteristic());
                leCharacteristic.setLeService(this);
            }
        }
    }
    
    public BluetoothGattService getBluetoothGattService() {
        return bluetoothGattService;
    }
    
    public static org.die_fabrik.lelib.wrapper.LeServiceBuilder getBuilder() {
        return new LeServiceBuilder();
    }
    
    public LeCharacteristic[] getLeCharacteristics() {
        return leCharacteristics;
    }
    
    public LeProfile getLeProfile() {
        return leProfile;
    }
    
    public int getServiceType() {
        return serviceType;
    }
    
    public boolean isAdvertisingService() {
        return advertisingService;
    }
    
    public void log(Context ctx, String tag) {
        Log.v(tag, "*  ServiceName: " + getName());
        Log.v(tag, "*  UUID: " + getUUID().toString());
        Log.v(tag, "*  serviceType: " + LeUtil.getServiceType(getServiceType()));
        Log.v(tag, "*  includeInAdvertisement: " + advertisingService);
        if (leCharacteristics != null) {
            for (LeCharacteristic characteristic : leCharacteristics) {
                characteristic.log(ctx, tag);
            }
        } else {
            Log.w(tag, "*  no Characteristics");
        }
    }
    
    public void setLeProfile(LeProfile leProfile) {
        this.leProfile = leProfile;
    }
}
