package org.die_fabrik.lelib.client;

import android.bluetooth.le.ScanResult;

import org.die_fabrik.lelib.data.LeData;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Michael on 11.01.2018.
 */

public class LeClientListeners {
    /**
     * The list of registered connectionListeners
     */
    private static final List<ILeConnectionListener> connectionListeners = new ArrayList<>();
    
    /**
     * The list of the registered scan listeners
     */
    private static final List<ILeScanListener> scanListeners = new ArrayList<>();
    
    /**
     * The list of the registered communications listeners
     */
    private static final List<ILeCommunicationListener> communicationListeners = new ArrayList<>();
    
    /**
     * The logging TAG for this (static) Object
     */
    private static String TAG = "LeClientListeners";
    
    public static void clearAllIsteners() {
        connectionListeners.clear();
        scanListeners.clear();
        connectionListeners.clear();
    }
    
    public static void onComCommandQueued() {
        List<ILeCommunicationListener> listeners = new ArrayList<>();
        synchronized (connectionListeners) {
            listeners.addAll(communicationListeners);
        }
        for (ILeCommunicationListener listener : listeners)
            listener.onComCommandQueued();
    }
    
    static void onComCommandSent(boolean success, int identifier) {
        List<ILeCommunicationListener> listeners = new ArrayList<>();
        synchronized (connectionListeners) {
            listeners.addAll(communicationListeners);
        }
        for (ILeCommunicationListener listener : listeners) {
            listener.onComCommandSent(success, identifier);
        }
    }
    
    static void onComNotificationReceived(LeData leData) {
        List<ILeCommunicationListener> listeners = new ArrayList<>();
        synchronized (connectionListeners) {
            listeners.addAll(communicationListeners);
        }
        for (ILeCommunicationListener listener : listeners) {
            listener.onComNotificationReceived(leData);
        }
    }
    
    static void onComRead(LeData leData) {
        List<ILeCommunicationListener> listeners = new ArrayList<>();
        synchronized (connectionListeners) {
            listeners.addAll(communicationListeners);
        }
        for (ILeCommunicationListener listener : listeners) {
            listener.onComRead(leData);
        }
    }
    
    static void onComWrite(LeData leData) {
        List<ILeCommunicationListener> listeners = new ArrayList<>();
        synchronized (connectionListeners) {
            listeners.addAll(communicationListeners);
        }
        for (ILeCommunicationListener listener : listeners) {
            listener.onComWrite(leData);
        }
    }
    
    static void onConnDisconnect() {
        List<ILeConnectionListener> listeners = new ArrayList<>();
        synchronized (connectionListeners) {
            listeners.addAll(connectionListeners);
        }
        for (ILeConnectionListener listener : listeners) {
            listener.onConnDisconnect();
        }
    }
    
    /**
     * calling the registered callback for onConnDiscovered()
     */
    static void onConnDiscovered() {
        List<ILeConnectionListener> listeners = new ArrayList<>();
        synchronized (connectionListeners) {
            listeners.addAll(connectionListeners);
        }
        for (ILeConnectionListener listener : listeners) {
            listener.onConnDiscovered();
        }
    }
    
    static void onConnDiscovering() {
        List<ILeConnectionListener> listeners = new ArrayList<>();
        synchronized (connectionListeners) {
            listeners.addAll(connectionListeners);
        }
        for (ILeConnectionListener listener : listeners) {
            listener.onConnDiscovering();
        }
    }
    
    /**
     * calling the registered callbacks for onConnTimeout
     */
    static void onConnTimeout() {
        List<ILeConnectionListener> listeners = new ArrayList<>();
        synchronized (connectionListeners) {
            listeners.addAll(connectionListeners);
        }
        for (ILeConnectionListener listener : listeners) {
            listener.onConnTimeout();
        }
    }
    
    public static void onScanBatchResults(List<ScanResult> results) {
        List<ILeScanListener> listeners = new ArrayList<>();
        synchronized (scanListeners) {
            listeners.addAll(scanListeners);
        }
        for (ILeScanListener listener : listeners) {
            listener.onScanBatchResults(results);
        }
    }
    
    static void onScanFailed(int errorCode) {
        List<ILeScanListener> listeners = new ArrayList<>();
        synchronized (scanListeners) {
            listeners.addAll(scanListeners);
        }
        for (ILeScanListener listener : listeners) {
            listener.onScanFailed(errorCode);
        }
    }
    
    public static void onScanResult(int callbackType, ScanResult result) {
        List<ILeScanListener> listeners = new ArrayList<>();
        synchronized (scanListeners) {
            listeners.addAll(scanListeners);
        }
        for (ILeScanListener listener : listeners) {
            listener.onScanResult(callbackType, result);
        }
    }
    
    static void onScanStarted(long timeout) {
        List<ILeScanListener> listeners = new ArrayList<>();
        synchronized (scanListeners) {
            listeners.addAll(scanListeners);
        }
        for (ILeScanListener listener : listeners) {
            listener.onScanStarted(timeout);
        }
    }
    
    static void onScanStopped(List<ScanResult> scanResults) {
        List<ILeScanListener> listeners = new ArrayList<>();
        synchronized (scanListeners) {
            listeners.addAll(scanListeners);
        }
        for (ILeScanListener listener : listeners) {
            listener.onScanStopped(scanResults);
        }
    }
    
    static void onScanTimeout() {
        List<ILeScanListener> listeners = new ArrayList<>();
        synchronized (scanListeners) {
            listeners.addAll(scanListeners);
        }
        for (ILeScanListener listener : listeners) {
            listener.onScanTimeout();
        }
    }
    
    /**
     * adds a listener to the list of connectionListeners
     * will do nothing if already registered
     *
     * @param listener the listener to add
     */
    public static void registerListener(ILeConnectionListener listener) {
        synchronized (connectionListeners) {
            if (!connectionListeners.contains(listener)) {
                connectionListeners.add(listener);
            }
        }
    }
    
    /**
     * adds a listener to the list of scanListeners
     * will do nothing if already registered
     *
     * @param listener the listener to add
     */
    public static void registerListener(ILeScanListener listener) {
        synchronized (scanListeners) {
            if (!scanListeners.contains(listener)) {
                scanListeners.add(listener);
            }
        }
    }
    
    /**
     * adds a listener to the list of scanListeners
     * will do nothing if already registered
     *
     * @param listener the listener to add
     */
    public static void registerListener(ILeCommunicationListener listener) {
        synchronized (communicationListeners) {
            if (!communicationListeners.contains(listener)) {
                communicationListeners.add(listener);
            }
        }
    }
    
    /**
     * removes a listener form the list of connectionListeners
     * does nothing if the listener was not registered before
     *
     * @param listener the listener to remove
     */
    public static void unregisterListener(ILeConnectionListener listener) {
        synchronized (connectionListeners) {
            if (connectionListeners.contains(listener)) {
                connectionListeners.remove(listener);
            }
        }
    }
    
    /**
     * removes a listener form the list of scanListeners
     * does nothing if the listener was not registered before
     *
     * @param listener the listener to remove
     */
    public static void unregisterListener(ILeScanListener listener) {
        synchronized (scanListeners) {
            if (scanListeners.contains(listener)) {
                scanListeners.remove(listener);
            }
        }
    }
    
    /**
     * removes a listener form the list of communicationListeners
     * does nothing if the listener was not registered before
     *
     * @param listener the listener to remove
     */
    public static void unregisterListener(ILeCommunicationListener listener) {
        synchronized (communicationListeners) {
            if (communicationListeners.contains(listener)) {
                communicationListeners.remove(listener);
            }
        }
    }
}
