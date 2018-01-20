package org.die_fabrik.ledemo.client;

import org.die_fabrik.ledemo.le.ProfileBuilder;
import org.die_fabrik.lelib.client.LeClientService;
import org.die_fabrik.lelib.wrapper.LeProfile;

/**
 * Created by Michael on 12.01.2018.
 */

public class ClientService extends LeClientService {
    private LeProfile profile;
    
    /**
     * Called by the system when the service is first created.  Do not call this method directly.
     */
    @Override
    public void onCreate() {
        super.onCreate();
        profile = new ProfileBuilder().buildProfile(this);
    }
    
    /**
     * The abstract method to retrieve the LeProfile (and its Service/Characteristic/Descriptor Structure)
     * Be aware that Client and Server are using the same LeProfile - when creating
     * different modules - use a library to build the profile
     *
     * @return The LeProfile
     */
    @Override
    protected LeProfile getLeProfile() {
        return profile;
    }
}
