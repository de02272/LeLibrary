package org.die_fabrik.lelib.wrapper;

import android.util.Log;

import java.util.UUID;

/**
 * Created by Michael on 03.01.2018.
 */

public abstract class LeAutoUuIdObject extends LeObject {
    private final static String[] uuids = new String[]
            {"aa1ed291-8bc4-4a22-9095-a5db56fbbb9d",
                    "156d6504-6b57-4102-8a5c-a089559011e4",
                    "f7b9248f-1e6b-4c76-aede-3ad05bda3101",
                    "cbebfd29-0c59-4b24-8363-5544f0f3326b",
                    "9d907af3-3346-4d87-974e-761b9d45ec7a",
                    "1cb21cfd-25a8-4c2d-88d0-f9c923505af3",
                    "399c068d-dc85-406e-92ff-bd5e7da2e432",
                    "bacbe3ca-2e5a-49ab-80db-f2dc9d648ca0",
                    "8b29b20f-5dc8-467f-9ed9-03e37e89ad5f",
                    "86309ef6-49c3-46ed-a5c1-e11cc8eccd02",
                    "9c5bd7b9-99a5-442c-ac4e-f6c71f440f1c",
                    "1abd4a2d-5b56-40b6-826d-0f9c73889a44",
                    "b19d86e6-ece0-4e4f-bb10-3a34c5e9108b",
                    "97131e35-02df-494d-b94e-4fb9e1949ad9",
                    "34c445b3-70d1-44a4-ad62-88ed1e2e1e9c",
                    "2735ef33-68ca-4ee9-a49c-3a2510ced7f7",
                    "ac64bfeb-2d9e-4ab1-b734-a2233e5efc09",
                    "fdf1722f-550f-4b03-a3fd-7d2aa5b60461",
                    "1acbaa4d-a8e2-4bd2-84b9-a9b2ce3015e0",
                    "4d0406fd-a3ba-4920-b108-36b68a6abd73",
                    "3bdb5682-24da-4501-b5cd-756025f645ab",
                    "6753162f-d4a7-4946-b18e-6776840fa249",
                    "a7c60842-a51b-4b14-9518-2cf8f93b696b",
                    "7523ea2b-f7e9-4304-b9b4-86780b2937ce",
                    "078b0d8f-bca8-4967-b509-8c4604935857",
                    "ebcf4719-ee26-4478-97eb-3ee91999f563",
                    "f6309adc-3df9-467f-b94d-97fc460da908",
                    "d473599b-5638-44fa-b968-966784adf166",
                    "f4bb7d97-dd89-4911-a03c-4da4bbc765c8",
                    "cc355fe7-ae0d-42da-abac-5e5eb73423e9",
                    "9042f547-bb74-4ada-8ecf-f5a650931cf0",
                    "209f7365-8453-4770-86d2-448d0812b012",
                    "9d44bad2-7e06-4053-a4e4-9d14c3da5ce1",
                    "242cc2e3-cc12-4eec-a7ed-a8dcbf4b0fdc",
                    "fa797046-0865-4273-9535-7bf2b3f824a1",
                    "e2bdab7b-c05b-4dab-8a05-e5789d5f12b2",
                    "b670107f-2eee-4d52-b677-66e85d8be831",
                    "6af4a97f-97ad-403f-a365-626fe81c68e5",
                    "bf27d816-2a62-4e9b-9329-79bd11b3c9bc",
                    "e633290f-a452-4a20-ad4d-c20d0742a2fa",
                    "606c7def-f4c9-491d-92c5-f6bba89ec6a8",
                    "5272a3d8-0e61-46cd-82ff-18881b9ed07f",
                    "bf246b20-f421-41a1-8628-cd5c73b87914",
                    "7de4f9fd-a0eb-4f91-8436-b28d4067962b",
                    "d2afd4e5-c397-439b-8c47-927b8b278425",
                    "96317e74-16dc-4193-98db-921ac289020b",
                    "b263e003-b56d-43f7-a787-941bb0e440c0",
                    "ca53f1e6-0321-4bcd-be1a-6af8a85fe9d7",
                    "c0911644-3309-4f71-99d1-77e35e5e325b",
                    "7e7227b7-b68b-4fa3-ba07-0a80483a7288",
                    "c58ff7d7-6ab2-4047-992e-4b3a36bf949c",
                    "27611c69-6914-4f5c-a411-8903c128f525",
                    "300558de-28e7-4729-b9bc-17aa9b28fc59",
                    "491a9797-1cab-425b-8b18-640d407197ca",
                    "49e0ce74-17f7-43ce-a48e-2ea209a5b656",
                    "73756601-1828-4d14-be13-b0e9fed39d92",
                    "2df113ed-6b57-4a2e-bf37-717071a321c6",
                    "3e86e1d6-be94-4b54-bc37-ee47bd482691",
                    "8701a3bd-8bc4-4adf-8ef5-14fb24fb823a",
                    "a318ed05-f7b7-4e31-a955-35864e38ed38",
                    "8b94ef19-70cd-42fb-9d3a-39d2c32aeaee",
                    "156d69f3-9c0a-44d4-b575-253008820f8f",
                    "fe73f837-2983-40c6-b2ab-44b35a2665e6",
                    "1bdeef98-ad12-4648-9a68-27e03299df66",
                    "e13cf899-f904-41fa-b433-5e2b8f705bd4",
                    "e20c89a4-62d4-4e61-aec0-141623caea12",
                    "ac45389b-a734-4a60-a694-f92ea714e09b",
                    "fef931a2-2f09-4e6c-86ec-f1d1cc41c560",
                    "84270d39-cf07-4ad7-96c8-ce1e88405e56",
                    "02ba5441-d0af-40a8-b233-21ba4641976e",
                    "b0f95fd4-f638-4c44-841e-b5d2431d8971",
                    "78baba7a-07fa-4717-8aef-748bf3997267",
                    "0e0797ef-fc51-4f69-b365-9eb672c66a8d",
                    "11048681-cb89-4202-9f98-39739df300c5",
                    "fe298628-1e5f-4a71-be39-4ad85948ec32",
                    "4b245833-9f19-4a32-aebc-bbc16dc17b24",
                    "e358b319-657d-4a2f-8a2d-dfc77251c012",
                    "87b851f8-62ab-4053-b7c0-1e8c02166501",
                    "863be5f0-07b4-43a5-9b88-0d1bebf9781b",
                    "d9de006f-16be-4593-b9d5-cc885852d056",
                    "884dd99b-f28e-4c87-8a25-e2079e5bfbe8",
                    "34415498-e5bf-4f59-bf15-b55357fc1975",
                    "c0266409-acbb-4c49-8fc6-d4cbe0e212bf",
                    "2daf0cc7-e7bd-438e-b825-dc639560eda2",
                    "bb6bde28-1ebe-4e8c-9633-83edd12d222f",
                    "9a371d83-eb02-4ec0-94f9-6a0bb7a47fa8",
                    "472cafc5-09b9-4060-8585-18e1e00d6fcd",
                    "7e2051a4-6a5e-4333-978e-9e1a6d5c0228",
                    "dd2b1306-f2f7-4ffd-b277-6ca4647abc25",
                    "402be5f2-c5f2-4dc0-a2b7-ab3bd9c0b383",
                    "0f9068bf-b15d-465c-9f71-9ecacbb664ea",
                    "5f7e575c-6a58-47b6-84bf-ea7ef5c1e383",
                    "f0d78128-6805-48a3-9650-706ba5f9bc18",
                    "2ab762e8-e9ad-4165-bc8a-076ff53634e1",
                    "a33ac7ee-8ab7-43bc-be89-0e2fb4160349",
                    "23b65b73-a4cf-4304-afa7-71f291150ab5",
                    "4fc82f91-85ff-48d3-8a7f-567282d044da",
                    "9dc9af33-6dfa-4f04-a477-48f44f30745b"};
    private static int uuidCnt = 0;
    protected final String TAG = this.getClass().getSimpleName();
    private final UUID UUID;
    
    
    public LeAutoUuIdObject(String name, java.util.UUID UUID) {
        super(name);
        
        if (UUID == null) {
            this.UUID = getNextUUID();
            Log.v(TAG, "provides the LeAutoUuIdObject with the UUID: " + this.UUID.toString() + " from position: " + (getUuidCnt() - 1));
        } else {
            this.UUID = UUID;
            Log.v(TAG, "provides the LeAutoUuIdObject with the given UUID: " + UUID.toString());
        }
        
    }
    
    public LeAutoUuIdObject(String name) {
        super(name);
        this.UUID = getNextUUID();
        Log.v(TAG, "provides the LeAutoUuIdObject with the UUID: " + this.UUID.toString() + " from position: " + (getUuidCnt() - 1));
    }
    
    public static int getIndexOfUUID(UUID UUID) {
        String s = UUID.toString();
        return getIndexOfUuid(s);
    }
    
    public static int getIndexOfUuid(String uuid) {
        String tmp = null;
        for (int i = 0; i < uuids.length; i++) {
            tmp = uuids[i];
            if (uuid.equals(tmp)) {
                return i;
            }
        }
        return -1;
    }
    
    public static UUID getNextUUID() {
        if (uuidCnt >= uuids.length) {
            throw new ArrayIndexOutOfBoundsException("The default uuid list is empty");
        }
        UUID answer = java.util.UUID.fromString(uuids[uuidCnt]);
        uuidCnt++;
        return answer;
    }
    
    public static int getUuidCnt() {
        return uuidCnt;
    }
    
    public java.util.UUID getUUID() {
        return UUID;
    }
}
