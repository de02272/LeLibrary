package org.die_fabrik.lelib.lelib.server;

import android.bluetooth.le.AdvertiseSettings;

/**
 * Created by Michael on 12.01.2018.
 */

public interface ILeAdvertiseListener {
    void onAdvertiserStartFailure(int errorCode);
    
    void onAdvertiserStartSuccess(AdvertiseSettings settingsInEffect);
}
