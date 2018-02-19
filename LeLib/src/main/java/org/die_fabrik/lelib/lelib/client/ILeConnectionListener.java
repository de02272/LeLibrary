package org.die_fabrik.lelib.lelib.client;

/**
 * Created by Michael on 11.01.2018.
 */

public interface ILeConnectionListener {
    void onConnDisconnect();
    
    void onConnDiscovered();
    
    void onConnDiscovering();
    
    void onConnTimeout();
}
