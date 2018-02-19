package org.die_fabrik.lelib.lelib.data;

import android.bluetooth.BluetoothDevice;

import org.die_fabrik.lelib.data.LeData;

/**
 * Created by Michael on 13.01.2018.
 */

public interface ILeDataProvider {
    Class<? extends LeData>[] getLeDataClasses();
    
    LeData getLeData(Class<? extends LeData> dataClass, BluetoothDevice bluetoothDevice);
}
