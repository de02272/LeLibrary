package org.die_fabrik.lelib.wrapper;

import android.bluetooth.BluetoothGattCharacteristic;
import android.content.Context;
import android.util.Log;

import org.die_fabrik.lelib.LeUtil;
import org.die_fabrik.lelib.data.LeData;

/**
 * Created by Michael on 11.01.2018.
 * <p>
 * a description of a BluetoothCharacteristic
 */

public class LeCharacteristic extends LeAutoUuIdObject {
    private final Class<? extends LeData> dataClass;
    private final ELeCharacteristicAccess access;
    private final BluetoothGattCharacteristic bluetoothGattCharacteristic;
    // will be set when this object is bound to a LeService Object
    private LeService leService;
    
    LeCharacteristic(String name, java.util.UUID UUID, Class<? extends LeData> dataClass, ELeCharacteristicAccess access) {
        super(name, UUID == null ? getNextUUID() : UUID);
        this.dataClass = dataClass;
        this.access = access;
        
        switch (access) {
            
            case READ:
                bluetoothGattCharacteristic = new BluetoothGattCharacteristic(getUUID(),
                        BluetoothGattCharacteristic.PERMISSION_READ, BluetoothGattCharacteristic.PROPERTY_READ);
                break;
            case WRITE:
                bluetoothGattCharacteristic = new BluetoothGattCharacteristic(getUUID(),
                        BluetoothGattCharacteristic.PERMISSION_WRITE,
                        BluetoothGattCharacteristic.PROPERTY_WRITE);
                
                break;
            case BOTH:
                bluetoothGattCharacteristic = new BluetoothGattCharacteristic(getUUID(),
                        BluetoothGattCharacteristic.PERMISSION_READ | BluetoothGattCharacteristic.PERMISSION_WRITE,
                        BluetoothGattCharacteristic.PROPERTY_READ | BluetoothGattCharacteristic.PROPERTY_WRITE);
                break;
            default:
                bluetoothGattCharacteristic = null;
        }
        
        
    }
    
    public static LeCharacteristicBuilder getBuilder() {
        return new LeCharacteristicBuilder();
    }
    
    public ELeCharacteristicAccess getAccess() {
        return access;
    }
    
    public BluetoothGattCharacteristic getBluetoothGattCharacteristic() {
        return bluetoothGattCharacteristic;
    }
    
    public Class<? extends LeData> getDataClass() {
        return dataClass;
    }
    
    public LeService getLeService() {
        return leService;
    }
    
    public void setLeService(LeService leService) {
        this.leService = leService;
    }
    
    public void log(Context ctx, String tag) {
        Log.v(tag, "*   CharacteristicName: " + getName());
        Log.v(tag, "*   UUID: " + getUUID());
        Log.v(tag, "*   dataClass: " + dataClass.getSimpleName());
        Log.v(tag, "*   access: " + access.name());
        Log.v(tag, "*   properties: " + LeUtil.getProperties(ctx, bluetoothGattCharacteristic.getProperties()));
        Log.v(tag, "*   permissions: " + LeUtil.getCharacteristicPermissions(ctx, bluetoothGattCharacteristic.getPermissions()));
    }
}
