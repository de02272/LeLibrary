package org.die_fabrik.lelib.server;

/**
 * Created by Michael on 12.01.2018.
 */

public class LeAdvertiserConfig {
    private final String bluetoothName;
    private final int advertiseMode;
    private final int timeout;
    private final boolean connectible;
    private final int txPowerLevel;
    private final boolean includeDeviceName;
    private final int payloadId;
    private final byte[] payload;
    
    LeAdvertiserConfig(String bluetoothName, int advertiseMode, int timeout, boolean connectible, int txPowerLevel, boolean includeDeviceName, int payloadId, byte[] payload) {
        this.bluetoothName = bluetoothName;
        this.advertiseMode = advertiseMode;
        this.timeout = timeout;
        this.connectible = connectible;
        this.txPowerLevel = txPowerLevel;
        this.includeDeviceName = includeDeviceName;
        this.payloadId = payloadId;
        this.payload = payload;
    }
    
    public int getAdvertiseMode() {
        return advertiseMode;
    }
    
    public String getBluetoothName() {
        return bluetoothName;
    }
    
    public byte[] getPayload() {
        return payload;
    }
    
    public int getPayloadId() {
        return payloadId;
    }
    
    public int getTimeout() {
        return timeout;
    }
    
    public int getTxPowerLevel() {
        return txPowerLevel;
    }
    
    public boolean isConnectible() {
        return connectible;
    }
    
    public boolean isIncludeDeviceName() {
        return includeDeviceName;
    }
}
