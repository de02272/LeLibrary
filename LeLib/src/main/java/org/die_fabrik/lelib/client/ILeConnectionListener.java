package org.die_fabrik.lelib.client;

/**
 * Created by Michael on 11.01.2018.
 * tThe communication listener interface for the client side
 */

public interface ILeConnectionListener {
    void onConnDisconnect();
    
    void onConnDiscovered();
    
    void onConnDiscovering();
    
    void onConnTimeout();
}
