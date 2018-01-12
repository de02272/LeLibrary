package org.die_fabrik.ledemo.client;

import org.die_fabrik.lelib.client.LeClientService;
import org.die_fabrik.lelib.wrapper.LeProfile;

/**
 * Created by Michael on 12.01.2018.
 */

public class ClientService extends LeClientService {
    /**
     * The abstract method to retrieve the LeProfile (and its Service/Characteristic/Descriptor Structure)
     * Be aware that Client and Server are using the same LeProfile - when creating
     * different modules - use a library to build the profile
     *
     * @return The LeProfile
     */
    @Override
    protected LeProfile getLeProfile() {
        return null;
    }
}
