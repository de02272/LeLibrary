package org.die_fabrik.lelib.lelib.wrapper;

import android.content.Context;
import android.util.Log;

import org.die_fabrik.lelib.wrapper.LeProfileBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Created by Michael on 11.01.2018.
 */

public class LeProfile extends LeObject {
    private final LeService[] leServices;
    private final UUID[] advertisingUuids;
    
    public LeProfile(String name, LeService[] leServices) {
        super(name);
        this.leServices = leServices;
        List<UUID> uuids = new ArrayList<>();
        for (LeService leService : leServices) {
            if (leService.isAdvertisingService()) {
                uuids.add(leService.getUUID());
            }
            leService.setLeProfile(this);
        }
        this.advertisingUuids = uuids.toArray(new UUID[uuids.size()]);
    }
    
    public static org.die_fabrik.lelib.wrapper.LeProfileBuilder getBuilder() {
        return new LeProfileBuilder();
    }
    
    public UUID[] getAdvertisingUuids() {
        return advertisingUuids;
    }
    
    public LeService[] getLeServices() {
        return leServices;
    }
    
    public void log(Context ctx, String tag) {
        
        Log.v(tag, "************************************************************************************");
        Log.v(tag, "* ProfileName: " + getName());
        Log.v(tag, "* ");
        for (UUID uuid : advertisingUuids) {
            Log.v(tag, "* advertising UUID: " + uuid.toString());
        }
        Log.v(tag, "* ");
        if (leServices != null) {
            for (LeService service : leServices) {
                service.log(ctx, tag);
            }
        } else {
            Log.e(tag, "no services available");
        }
        
        Log.v(tag, "************************************************************************************");
        Log.v(tag, "* LeLib Demonstration application                                                  *");
        Log.v(tag, "* (c) 2018 by Michael Küthe, die Fabrik, Hamburg, Germany                          *");
        Log.v(tag, "* contact:de02272@gmail.com or via +49 172 5644617                                 *");
        Log.v(tag, "************************************************************************************");
    }
}
