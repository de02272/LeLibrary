package org.die_fabrik.lelib.client;

import android.bluetooth.le.ScanResult;

import java.util.List;

/**
 * Created by Michael on 11.01.2018.
 * Scan Listener (Client side)
 */

public interface ILeScanListener {
    void onScanBatchResults(List<ScanResult> results);
    
    void onScanFailed(int errorCode);
    
    void onScanResult(int callbackType, ScanResult result);
    
    void onScanStarted(long timeout);
    
    void onScanStopped(List<ScanResult> scanResults);
    
    void onScanTimeout();
}
