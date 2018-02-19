package org.die_fabrik.lelib.lelib.wrapper;

import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.content.Context;
import android.util.Log;

import org.die_fabrik.lelib.LeUtil;
import org.die_fabrik.lelib.data.LeData;
import org.die_fabrik.lelib.wrapper.ELeCharacteristicAccess;
import org.die_fabrik.lelib.wrapper.ELeNotification;
import org.die_fabrik.lelib.wrapper.LeCharacteristicBuilder;

import java.util.List;

/**
 * Created by Michael on 11.01.2018.
 * <p>
 * a description of a BluetoothCharacteristic
 */

public class LeCharacteristic extends LeAutoUuIdObject {
    
    private final ELeNotification notification;
    private final Class<? extends LeData> dataClass;
    private final ELeCharacteristicAccess access;
    private final BluetoothGattCharacteristic bluetoothGattCharacteristic;
    private final BluetoothGattDescriptor notificationGattDescriptor;
    // will be set when this object is bound to a LeService Object
    private LeService leService;
    
    LeCharacteristic(String name, java.util.UUID UUID, ELeNotification notification, Class<? extends LeData> dataClass, ELeCharacteristicAccess access) {
        super(name, UUID);
        this.notification = notification;
        this.dataClass = dataClass;
        this.access = access;
        
        switch (access) {
            
            case READ:
                int prop = BluetoothGattCharacteristic.PROPERTY_READ;
                if (notification != ELeNotification.NONE) {
                    prop = prop | BluetoothGattCharacteristic.PROPERTY_NOTIFY;
                }
                bluetoothGattCharacteristic = new BluetoothGattCharacteristic(getUUID(), prop
                        , BluetoothGattCharacteristic.PERMISSION_READ);
                break;
            case WRITE:
                prop = BluetoothGattCharacteristic.PROPERTY_WRITE;
                if (notification != ELeNotification.NONE) {
                    prop = prop | BluetoothGattCharacteristic.PROPERTY_NOTIFY;
                }
                bluetoothGattCharacteristic = new BluetoothGattCharacteristic(getUUID(), prop
                        , BluetoothGattCharacteristic.PERMISSION_WRITE);
                break;
            case BOTH:
                prop = BluetoothGattCharacteristic.PROPERTY_WRITE | BluetoothGattCharacteristic.PROPERTY_READ;
                if (notification != ELeNotification.NONE) {
                    prop = prop | BluetoothGattCharacteristic.PROPERTY_NOTIFY;
                }
                bluetoothGattCharacteristic = new BluetoothGattCharacteristic(getUUID()
                        , prop
                        , BluetoothGattCharacteristic.PERMISSION_READ | BluetoothGattCharacteristic.PERMISSION_WRITE);
                break;
            default:
                bluetoothGattCharacteristic = null;
        }
        
        if ((notification != ELeNotification.NONE) && bluetoothGattCharacteristic != null) {
            notificationGattDescriptor = new BluetoothGattDescriptor(getNextUUID(), BluetoothGattDescriptor.PERMISSION_WRITE);
            bluetoothGattCharacteristic.addDescriptor(notificationGattDescriptor);
        } else {
            notificationGattDescriptor = null;
        }
    }
    
    public static org.die_fabrik.lelib.wrapper.LeCharacteristicBuilder getBuilder() {
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
    
    public ELeNotification getNotification() {
        return notification;
    }
    
    public BluetoothGattDescriptor getNotificationGattDescriptor() {
        return notificationGattDescriptor;
    }
    
    public void log(Context ctx, String tag) {
        Log.v(tag, "*   CharacteristicName: " + getName());
        Log.v(tag, "*   UUID: " + getUUID());
        Log.v(tag, "*   dataClass: " + dataClass.getSimpleName());
        Log.v(tag, "*   access: " + access.name());
        Log.v(tag, "*   properties: " + LeUtil.getProperties(ctx, bluetoothGattCharacteristic.getProperties()));
        Log.v(tag, "*   permissions: " + LeUtil.getCharacteristicPermissions(ctx, bluetoothGattCharacteristic.getPermissions()));
        Log.v(tag, "*   notification: " + notification);
        List<BluetoothGattDescriptor> notificationDescriptors = bluetoothGattCharacteristic.getDescriptors();
        if (notificationDescriptors != null && notificationDescriptors.size() > 0) {
            for (BluetoothGattDescriptor descriptor : notificationDescriptors) {
                Log.v(tag, "*    NotificationDescriptor: " + descriptor.getUuid());
                Log.v(tag, "*    permissions: " + LeUtil.getCharacteristicPermissions(ctx, descriptor.getPermissions()));
            }
        } else {
            Log.i(tag, "*    no descriptor");
        }
    }
}
