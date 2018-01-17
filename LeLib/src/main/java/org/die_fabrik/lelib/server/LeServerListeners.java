package org.die_fabrik.lelib.server;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.AdvertiseSettings;
import android.util.Log;

import org.die_fabrik.lelib.data.ILeDataProvider;
import org.die_fabrik.lelib.data.LeData;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Michael on 12.01.2018.
 */

public class LeServerListeners {
    /**
     * The list of registered connectionListeners
     */
    private static final List<ILeAdvertiseListener> advertiseListeners = new ArrayList<>();
    
    /**
     * The list of registered connectionListeners
     */
    private static final List<ILeGattListener> gattListeners = new ArrayList<>();
    
    /**
     * The list of DataProviders
     */
    private static final List<ILeDataProvider> dataProviders = new ArrayList<>();
    
    /**
     * The logging TAG for this (static) Object
     */
    private static String TAG = "LeServerListeners";
    
    public static ILeDataProvider findDataProvider(Class<? extends LeData> cls) {
        for (ILeDataProvider provider : dataProviders) {
            for (Class<? extends LeData> dataClass : provider.getLeDataClasses()) {
                if (dataClass.equals(cls)) {
                    return provider;
                }
            }
        }
        return null;
    }
    
    public static void onAdvertiserStartFailure(int errorCode) {
        List<ILeAdvertiseListener> listeners = new ArrayList<>();
        synchronized (advertiseListeners) {
            listeners.addAll(advertiseListeners);
        }
        Log.v(TAG, "calling onAdvertiserStartFailure(errorCode) on " + listeners.size() + " listeners");
        for (ILeAdvertiseListener listener : listeners) {
            listener.onAdvertiserStartFailure(errorCode);
        }
    }
    
    public static void onAdvertiserStartSuccess(AdvertiseSettings settingsInEffect) {
        List<ILeAdvertiseListener> listeners = new ArrayList<>();
        synchronized (advertiseListeners) {
            listeners.addAll(advertiseListeners);
        }
        Log.v(TAG, "calling onAdvertiserStartSuccess() on " + listeners.size() + " listeners");
        for (ILeAdvertiseListener listener : listeners) {
            listener.onAdvertiserStartSuccess(settingsInEffect);
        }
    }
    
    public static void onGattConnected(BluetoothDevice device) {
        List<ILeGattListener> listeners = new ArrayList<>();
        synchronized (gattListeners) {
            listeners.addAll(gattListeners);
        }
        Log.v(TAG, "calling onGattConnected() on " + listeners.size() + " listeners");
        for (ILeGattListener listener : listeners) {
            listener.onGattConnected(device);
        }
    }
    
    public static void onGattDisconnected(BluetoothDevice device) {
        List<ILeGattListener> listeners = new ArrayList<>();
        synchronized (gattListeners) {
            listeners.addAll(gattListeners);
        }
        Log.v(TAG, "calling onGattDisconnected() on " + listeners.size() + " listeners");
        for (ILeGattListener listener : listeners) {
            listener.onGattDisconnected(device);
        }
    }
    
    public static void onGattNotificationSent() {
        List<ILeGattListener> listeners = new ArrayList<>();
        synchronized (gattListeners) {
            listeners.addAll(gattListeners);
        }
        Log.v(TAG, "calling onGattNotificationSent() on " + listeners.size() + " listeners");
        for (ILeGattListener listener : listeners) {
            listener.onGattNotificationSent();
        }
    }
    
    public static void onGattWritten(LeData leData, BluetoothDevice device) {
        List<ILeGattListener> listeners = new ArrayList<>();
        synchronized (gattListeners) {
            listeners.addAll(gattListeners);
        }
        Log.v(TAG, "calling onGattWritten() on " + listeners.size() + " listeners");
        for (ILeGattListener listener : listeners) {
            listener.onGattWritten(leData, device);
        }
    }
    
    public static void onGattWrittenFailure(LeData leData, BluetoothDevice device) {
        List<ILeGattListener> listeners = new ArrayList<>();
        synchronized (gattListeners) {
            listeners.addAll(gattListeners);
        }
        Log.v(TAG, "calling onGattWrittenFailure() on " + listeners.size() + " listeners");
        for (ILeGattListener listener : listeners) {
            listener.onGattWrittenFailure(leData, device);
        }
    }
    
    /**
     * adds a provider to the list of dataProviders
     * does nothing if the provider was registered before
     *
     * @param provider the provider to add
     */
    public static void registerDataProvider(ILeDataProvider provider) {
        synchronized (dataProviders) {
            if (!dataProviders.contains(provider)) {
                dataProviders.add(provider);
            }
        }
    }
    
    /**
     * adds a listener to the list of advertiseListeners
     * will do nothing if already registered
     *
     * @param listener the listener to add
     */
    public static void registerListener(ILeGattListener listener) {
        synchronized (gattListeners) {
            if (!gattListeners.contains(listener)) {
                gattListeners.add(listener);
            }
        }
    }
    
    /**
     * adds a listener to the list of advertiseListeners
     * will do nothing if already registered
     *
     * @param listener the listener to add
     */
    public static void registerListener(ILeAdvertiseListener listener) {
        synchronized (advertiseListeners) {
            if (!advertiseListeners.contains(listener)) {
                advertiseListeners.add(listener);
            }
        }
    }
    
    /**
     * removes a provider form the list of dataProviders
     * does nothing if the provider was not registered before
     *
     * @param provider the provider to remove
     */
    public static void unregisterDataProvider(ILeDataProvider provider) {
        synchronized (dataProviders) {
            if (dataProviders.contains(provider)) {
                dataProviders.remove(provider);
            }
        }
    }
    
    /**
     * removes a listener form the list of advertiseListeners
     * does nothing if the listener was not registered before
     *
     * @param listener the listener to remove
     */
    public static void unregisterListener(ILeGattListener listener) {
        synchronized (gattListeners) {
            if (gattListeners.contains(listener)) {
                gattListeners.remove(listener);
            }
        }
    }
    
    /**
     * removes a listener form the list of advertiseListeners
     * does nothing if the listener was not registered before
     *
     * @param listener the listener to remove
     */
    public static void unregisterListener(ILeAdvertiseListener listener) {
        synchronized (advertiseListeners) {
            if (advertiseListeners.contains(listener)) {
                advertiseListeners.remove(listener);
            }
        }
    }
}
