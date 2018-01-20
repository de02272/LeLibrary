package org.die_fabrik.ledemo.server;

import org.die_fabrik.ledemo.le.ProfileBuilder;
import org.die_fabrik.lelib.server.LeServerService;
import org.die_fabrik.lelib.wrapper.LeProfile;

/**
 * Created by Michael on 12.01.2018.
 */

public class ServerService extends LeServerService {
    private LeProfile profile = null;
    
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
    
    /**
     * Called by the system when the service is first created.  Do not call this method directly.
     */
    @Override
    public void onCreate() {
        profile = new ProfileBuilder().buildProfile(this);
        super.onCreate();
    
    }
}
