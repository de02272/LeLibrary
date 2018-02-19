package org.die_fabrik.lelib.client;

import org.die_fabrik.lelib.data.LeData;

/**
 * Created by Michael on 11.01.2018.
 */

public interface ILeCommunicationListener {
    /**
     * informs the listener that a new command (read/write/enable/diable notification) was
     * queued
     */
    void onComCommandQueued();
    
    /**
     * called when the last command (read/wrirte/setNotification)
     * was sent to the other side
     *  @param success    whether the process was initiated successfully
     *
     */
    void onComCommandSent(boolean success);
    
    void onComCommandTimeout(LeClientService.QueuedCommand command);
    
    void onComLongNotificationIndicated(Class<? extends LeData> dataClass);
    
    /**
     * called when the client received a Notification with the LeData Object
     *
     * @param leData The object received via Notification
     */
    void onComNotificationReceived(LeData leData);
    
    /**
     * called when a read request was successfully sent back to the client
     * @param leData dataObject
     */
    void onComRead(LeData leData);
    
    /**
     * called when the last write transaction was finished
     * @param leData the LeData which was sent to the server
     */
    void onComWrite(LeData leData);
}
