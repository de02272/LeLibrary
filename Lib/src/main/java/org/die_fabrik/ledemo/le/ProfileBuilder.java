package org.die_fabrik.ledemo.le;

import android.bluetooth.BluetoothGattService;
import android.content.Context;

import org.die_fabrik.ledemo.le.data.IntegerData;
import org.die_fabrik.lelib.wrapper.ELeCharacteristicAccess;
import org.die_fabrik.lelib.wrapper.LeCharacteristic;
import org.die_fabrik.lelib.wrapper.LeProfile;
import org.die_fabrik.lelib.wrapper.LeService;

/**
 * Created by Michael on 12.01.2018.
 * Here you can build the services which can be used to provide them or to consume them
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
    public LeProfile buildProfile(Context ctx) {
        LeCharacteristic c0 = LeCharacteristic.getBuilder()
                .setName("INTEGER VALUE")
                .setAccess(ELeCharacteristicAccess.BOTH)
                .setDataClass(IntegerData.class)
                .setNotification(true)
                .create();
        
        
        LeService s0 = LeService.getBuilder()
                .setName("TEST SERVICE")
                .setAdvertisingService(true)
                .setLeCharacteristics(c0)
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