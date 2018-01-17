package org.die_fabrik.lelib.data;

import android.bluetooth.BluetoothDevice;

/**
 * Created by Michael on 13.01.2018.
 */

public interface ILeDataProvider {
    Class<? extends LeData>[] getDataClasses();
    
    LeData getLeData(Class<? extends LeData> dataClass, BluetoothDevice bluetoothDevice);
}
