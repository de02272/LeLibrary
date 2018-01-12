package org.die_fabrik.lelib.server;

import android.bluetooth.le.AdvertiseSettings;

import org.die_fabrik.lelib.LeUtil;

public class LeAdvertiserConfigBuilder {
    private String bluetoothName;
    private int advertiseMode;
    private int timeout;
    private boolean connectible;
    private int txPowerLevel;
    private boolean includeDeviceName;
    private int payloadId;
    private byte[] payload;
    
    
    
    public LeAdvertiserConfig createLeAdvertiserConfig() {
        return new LeAdvertiserConfig(bluetoothName, advertiseMode, timeout, connectible, txPowerLevel, includeDeviceName, payloadId, payload);
    }
    
    public LeAdvertiserConfigBuilder setAdvertiseMode(int advertiseMode) {
        LeUtil.checkValue(advertiseMode, AdvertiseSettings.ADVERTISE_MODE_BALANCED,
                AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY,
                AdvertiseSettings.ADVERTISE_MODE_LOW_POWER);
        
        this.advertiseMode = advertiseMode;
        return this;
    }
    
    public LeAdvertiserConfigBuilder setBluetoothName(String bluetoothName) {
        this.bluetoothName = bluetoothName;
        return this;
    }
    
    public LeAdvertiserConfigBuilder setConnectible(boolean connectible) {
        this.connectible = connectible;
        return this;
    }
    
    public LeAdvertiserConfigBuilder setIncludeDeviceName(boolean includeDeviceName) {
        this.includeDeviceName = includeDeviceName;
        return this;
    }
    
    public LeAdvertiserConfigBuilder setPayload(byte[] payload) {
        this.payload = payload;
        return this;
    }
    
    public LeAdvertiserConfigBuilder setPayloadId(int payloadId) {
        LeUtil.checkRange(payloadId, 0, Integer.MAX_VALUE);
        this.payloadId = payloadId;
        return this;
    }
    
    public LeAdvertiserConfigBuilder setTimeout(int timeout) {
        LeUtil.checkRange(timeout, 0, Integer.MAX_VALUE);
        this.timeout = timeout;
        return this;
    }
    
    
    public LeAdvertiserConfigBuilder setTxPowerLevel(int txPowerLevel) {
        LeUtil.checkValue(txPowerLevel, AdvertiseSettings.ADVERTISE_TX_POWER_ULTRA_LOW,
                AdvertiseSettings.ADVERTISE_TX_POWER_MEDIUM,
                AdvertiseSettings.ADVERTISE_TX_POWER_HIGH,
                AdvertiseSettings.ADVERTISE_TX_POWER_LOW);
        
        this.txPowerLevel = txPowerLevel;
        return this;
    }
    
}