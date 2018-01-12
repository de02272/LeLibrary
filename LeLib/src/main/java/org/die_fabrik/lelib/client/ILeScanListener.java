package org.die_fabrik.lelib.client;

/**
 * Created by Michael on 11.01.2018.
 */

public interface ILeScanListener {
    void onScanFailed(int errorCode);
    
    void onScanStarted(long timeout);
    
    void onScanStopped();
    
    void onScanTimeout();
}
