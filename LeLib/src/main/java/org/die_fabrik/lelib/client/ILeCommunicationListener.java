package org.die_fabrik.lelib.client;

import org.die_fabrik.lelib.data.LeData;

/**
 * Created by Michael on 11.01.2018.
 */

public interface ILeCommunicationListener {
    /**
     * called when the last command (read/wrirte/setNotification)
     * was sent to the other side
     *
     * @param success    whether the process was initiated successfully
     * @param identifier the Identifier which was given by the Ui when initiating this command
     */
    void onCommunicationCommandSent(boolean success, int identifier);
    
    /**
     * called when the client received a Notification with the LeData Object
     *
     * @param leData The object received via Notification
     */
    void onCommunicationNotificationReceived(LeData leData);
    
    /**
     * called when a read request was successfully sent back to the client
     * @param leData dataObject
     */
    void onCommunicationRead(LeData leData);
    
    /**
     * called when the last write transaction was finished
     * @param leData the LeData which was sent to the server
     */
    void onCommunicationWrite(LeData leData);
}
