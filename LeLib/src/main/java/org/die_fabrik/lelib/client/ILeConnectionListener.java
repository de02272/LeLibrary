package org.die_fabrik.lelib.client;

/**
 * Created by Michael on 11.01.2018.
 */

public interface ILeConnectionListener {
    void onConnectionDisconnect();
    
    void onConnectionDiscovered();
    
    void onConnectionDiscovering();
    
    void onConnectionTimeout();
}
