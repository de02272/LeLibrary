package org.die_fabrik.lelib.data;

import android.bluetooth.BluetoothDevice;

/**
 * Created by Michael on 13.01.2018.
 * The interface for a data provider
 */

public interface ILeDataProvider {
    Class<? extends LeData>[] getLeDataClasses();
    
    LeData getLeData(Class<? extends LeData> dataClass, BluetoothDevice bluetoothDevice);
}
