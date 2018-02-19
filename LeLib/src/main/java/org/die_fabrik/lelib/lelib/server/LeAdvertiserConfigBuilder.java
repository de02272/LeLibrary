package org.die_fabrik.lelib.lelib.server;

import android.bluetooth.le.AdvertiseSettings;

import org.die_fabrik.lelib.LeUtil;
import org.die_fabrik.lelib.server.LeAdvertiserConfig;

public class LeAdvertiserConfigBuilder {
    private String bluetoothName;
    private int advertiseMode;
    private int timeout;
    private boolean connectible;
    private int txPowerLevel;
    private boolean includeDeviceName;
    private int payloadId;
    private byte[] payload;
    
    
    public LeAdvertiserConfig create() {
        LeUtil.checkValue(advertiseMode, AdvertiseSettings.ADVERTISE_MODE_BALANCED,
                AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY,
                AdvertiseSettings.ADVERTISE_MODE_LOW_POWER);
        LeUtil.checkRange(payloadId, 0, Integer.MAX_VALUE);
        LeUtil.checkRange(timeout, 0, Integer.MAX_VALUE);
        LeUtil.checkValue(txPowerLevel, AdvertiseSettings.ADVERTISE_TX_POWER_ULTRA_LOW,
                AdvertiseSettings.ADVERTISE_TX_POWER_MEDIUM,
                AdvertiseSettings.ADVERTISE_TX_POWER_HIGH,
                AdvertiseSettings.ADVERTISE_TX_POWER_LOW);
        
        return new LeAdvertiserConfig(bluetoothName, advertiseMode, timeout, connectible, txPowerLevel, includeDeviceName, payloadId, payload);
    }
    
    public org.die_fabrik.lelib.server.LeAdvertiserConfigBuilder setAdvertiseMode(int advertiseMode) {
        LeUtil.checkValue(advertiseMode, AdvertiseSettings.ADVERTISE_MODE_BALANCED,
                AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY,
                AdvertiseSettings.ADVERTISE_MODE_LOW_POWER);
        
        this.advertiseMode = advertiseMode;
        return this;
    }
    
    public org.die_fabrik.lelib.server.LeAdvertiserConfigBuilder setBluetoothName(String bluetoothName) {
        this.bluetoothName = bluetoothName;
        return this;
    }
    
    public org.die_fabrik.lelib.server.LeAdvertiserConfigBuilder setConnectible(boolean connectible) {
        this.connectible = connectible;
        return this;
    }
    
    public org.die_fabrik.lelib.server.LeAdvertiserConfigBuilder setIncludeDeviceName(boolean includeDeviceName) {
        this.includeDeviceName = includeDeviceName;
        return this;
    }
    
    public org.die_fabrik.lelib.server.LeAdvertiserConfigBuilder setPayload(byte[] payload) {
        this.payload = payload;
        return this;
    }
    
    public org.die_fabrik.lelib.server.LeAdvertiserConfigBuilder setPayloadId(int payloadId) {
        LeUtil.checkRange(payloadId, 0, Integer.MAX_VALUE);
        this.payloadId = payloadId;
        return this;
    }
    
    public org.die_fabrik.lelib.server.LeAdvertiserConfigBuilder setTimeout(int timeout) {
        LeUtil.checkRange(timeout, 0, Integer.MAX_VALUE);
        this.timeout = timeout;
        return this;
    }
    
    
    public org.die_fabrik.lelib.server.LeAdvertiserConfigBuilder setTxPowerLevel(int txPowerLevel) {
        LeUtil.checkValue(txPowerLevel, AdvertiseSettings.ADVERTISE_TX_POWER_ULTRA_LOW,
                AdvertiseSettings.ADVERTISE_TX_POWER_MEDIUM,
                AdvertiseSettings.ADVERTISE_TX_POWER_HIGH,
                AdvertiseSettings.ADVERTISE_TX_POWER_LOW);
        
        this.txPowerLevel = txPowerLevel;
        return this;
    }
    
}