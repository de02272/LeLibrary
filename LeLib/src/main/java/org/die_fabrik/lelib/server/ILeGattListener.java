package org.die_fabrik.lelib.server;

import android.bluetooth.BluetoothDevice;

import org.die_fabrik.lelib.data.LeData;

/**
 * Created by Michael on 12.01.2018.
 */

public interface ILeGattListener {
    void onGattConnected(BluetoothDevice device);
    
    void onGattDisconnected(BluetoothDevice device);
    
    void onGattNotificationSent();
    
    void onGattWritten(LeData leData, BluetoothDevice device);
    
    void onGattWrittenFailure(LeData leData, BluetoothDevice device);
}
