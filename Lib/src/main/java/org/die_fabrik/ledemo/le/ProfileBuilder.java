package org.die_fabrik.ledemo.le;

import android.bluetooth.BluetoothGattService;
import android.content.Context;

import org.die_fabrik.ledemo.le.data.RemoteDeviceNameData;
import org.die_fabrik.lelib.wrapper.ELeCharacteristicAccess;
import org.die_fabrik.lelib.wrapper.LeCharacteristic;
import org.die_fabrik.lelib.wrapper.LeProfile;
import org.die_fabrik.lelib.wrapper.LeService;

/**
 * Created by Michael on 12.01.2018.
 */

public class ProfileBuilder {
    /**
     * The logging TAG for this Object
     */
    protected final String TAG = this.getClass().getSimpleName();
    
    /**
     * automatic usage of uuids
     *
     * @return
     * @throws Exception
     */
    public LeProfile buildProfile(Context ctx) throws Exception {
        LeCharacteristic c0 = LeCharacteristic.getBuilder()
                .setName("CHAT HISTORY")
                .setAccess(ELeCharacteristicAccess.READ)
                .setDataClass(RemoteDeviceNameData.class)
                .create();
        
        LeCharacteristic c1 = LeCharacteristic.getBuilder()
                .setName("REMOTE DEVICE NAME CHARACTERISTIC")
                .setAccess(ELeCharacteristicAccess.WRITE)
                .setDataClass(RemoteDeviceNameData.class)
                .create();
        
        LeService s0 = LeService.getBuilder()
                .setName("CHAT SERVICE")
                .setAdvertisingService(true)
                .setLeCharacteristics(c0, c1)
                .setServiceType(BluetoothGattService.SERVICE_TYPE_PRIMARY)
                .create();
        
        LeProfile p0 = LeProfile.getBuilder()
                .setName("PROFILE")
                .setLeServices(s0)
                .create();
        
        p0.log(ctx, TAG);
        return p0;
    }
}