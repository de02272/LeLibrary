package org.die_fabrik.lelib.client;

import android.util.Log;

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
    
    static void onCommunicationCommandSent(boolean success, int identifier) {
        List<ILeCommunicationListener> listeners = new ArrayList<>();
        synchronized (connectionListeners) {
            listeners.addAll(communicationListeners);
        }
        Log.v(TAG, "calling onConnectionConnected(BluetoothDevice) on " + listeners.size() + " listeners");
        for (ILeCommunicationListener listener : listeners) {
            listener.onCommunicationCommandSent(success, identifier);
        }
    }
    
    public static void onCommunicationNotificationReceived(LeData leData) {
        List<ILeCommunicationListener> listeners = new ArrayList<>();
        synchronized (connectionListeners) {
            listeners.addAll(communicationListeners);
        }
        Log.v(TAG, "calling onCommunicationNotificationReceived(LeData) on " + listeners.size() + " listeners");
        for (ILeCommunicationListener listener : listeners) {
            listener.onCommunicationNotificationReceived(leData);
        }
    }
    
    public static void onCommunicationRead(LeData leData) {
        List<ILeCommunicationListener> listeners = new ArrayList<>();
        synchronized (connectionListeners) {
            listeners.addAll(communicationListeners);
        }
        Log.v(TAG, "calling onCommunicationRead(LeData) on " + listeners.size() + " listeners");
        for (ILeCommunicationListener listener : listeners) {
            listener.onCommunicationRead(leData);
        }
    }
    
    public static void onCommunicationWrite(LeData leData) {
        List<ILeCommunicationListener> listeners = new ArrayList<>();
        synchronized (connectionListeners) {
            listeners.addAll(communicationListeners);
        }
        Log.v(TAG, "calling onCommunicationWrite(LeData) on " + listeners.size() + " listeners");
        for (ILeCommunicationListener listener : listeners) {
            listener.onCommunicationWrite(leData);
        }
    }
    
    static void onConnectionDisconnect() {
        List<ILeConnectionListener> listeners = new ArrayList<>();
        synchronized (connectionListeners) {
            listeners.addAll(connectionListeners);
        }
        Log.v(TAG, "calling onConnectionDisconnect() on " + listeners.size() + " listeners");
        for (ILeConnectionListener listener : listeners) {
            listener.onConnectionDisconnect();
        }
    }
    
    /**
     * calling the registered callback for onConnectionDiscovered()
     */
    static void onConnectionDiscovered() {
        List<ILeConnectionListener> listeners = new ArrayList<>();
        synchronized (connectionListeners) {
            listeners.addAll(connectionListeners);
        }
        Log.v(TAG, "calling onConnectionDiscovered() on " + listeners.size() + " listeners");
        for (ILeConnectionListener listener : listeners) {
            listener.onConnectionDiscovered();
        }
    }
    
    static void onConnectionDiscovering() {
        List<ILeConnectionListener> listeners = new ArrayList<>();
        synchronized (connectionListeners) {
            listeners.addAll(connectionListeners);
        }
        Log.v(TAG, "calling onConnectionDiscovering() on " + listeners.size() + " listeners");
        for (ILeConnectionListener listener : listeners) {
            listener.onConnectionDiscovering();
        }
    }
    
    /**
     * calling the registered callbacks for onConnectionTimeout
     */
    static void onConnectionTimeout() {
        List<ILeConnectionListener> listeners = new ArrayList<>();
        synchronized (connectionListeners) {
            listeners.addAll(connectionListeners);
        }
        Log.v(TAG, "calling onConnectionTimeout(BluetoothDevice) on " + listeners.size() + " listeners");
        for (ILeConnectionListener listener : listeners) {
            listener.onConnectionTimeout();
        }
    }
    
    
    
    static void onScanFailed(int errorCode) {
        List<ILeScanListener> listeners = new ArrayList<>();
        synchronized (scanListeners) {
            listeners.addAll(scanListeners);
        }
        Log.v(TAG, "calling onScanFailed() on " + listeners.size() + " listeners");
        for (ILeScanListener listener : listeners) {
            listener.onScanFailed(errorCode);
        }
    }
    
    static void onScanStarted(long timeout) {
        List<ILeScanListener> listeners = new ArrayList<>();
        synchronized (scanListeners) {
            listeners.addAll(scanListeners);
        }
        Log.v(TAG, "calling onScanStarted() on " + listeners.size() + " listeners");
        for (ILeScanListener listener : listeners) {
            listener.onScanStarted(timeout);
        }
    }
    
    static void onScanStopped() {
        List<ILeScanListener> listeners = new ArrayList<>();
        synchronized (scanListeners) {
            listeners.addAll(scanListeners);
        }
        Log.v(TAG, "calling onScanStopped() on " + listeners.size() + " listeners");
        for (ILeScanListener listener : listeners) {
            listener.onScanStopped();
        }
    }
    
    static void onScanTimeout() {
        List<ILeScanListener> listeners = new ArrayList<>();
        synchronized (scanListeners) {
            listeners.addAll(scanListeners);
        }
        Log.v(TAG, "calling onScanTimeout() on " + listeners.size() + " listeners");
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
