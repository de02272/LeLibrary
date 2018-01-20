# LeLibrary
a client server approach to make it more easy to manage a simple BLE Connection between devices.

The directory LeClient, LeServer and Lib are for evaluation reasons only
The core functionality is based in the directory LeLib.

Android >= API 22! This is due that the Ble support in previous version is neither good nor stable. 
This is not really an android issue, cause many of the older bluetooth hardware chips does not 
support Ble correctly.

The LeLib is capable working on on a headless android Things device (tested on Raspberry Pi3).

Use case:
establish and maintain BLE connections between two or more devices in a Client Server model.

Recommendation: When building such an app, divide your project within Android Studio into three 
Modules(Client, Server and Lib). The Client and the Server Module should present the regular apps 
which should be build. Here shoud the user interaction take place.
The Lib Module should be used to provide a "ProfileBuilder" to both Modules (Client & Server - they depend on Lib )
As an alternative to this method you can also build the two services within a single 
application. But I haven't tested that yet.

In the client & the Server module you should implement the LeClientService and the LeServerService as services.

The complexity from the Android API to the BleService, Characteristics and Descriptor is fade away due a Profile concept which contains 
only of Services and their depending Characteristics (have a look at the ProfileBuilder class in the demo).

To exchange data between Server & client you can implement subClasses of LeData. This should be a plain Pojo with two additional methods.
void constructLeData(byte[] leValue) to construct the Object from a byte[] and 
byte[] createLeValue() to retrieve the byte[] from a LeData Object. 
These LeData classes should also be positioned in the Lib Module - to allow both modules (server & client) to access these classes.
   

To control the behaviour of the services, both (the LeServerService & the LeClientService) provides 
a binder Object. Your Ui should bind these services and use the binder to control the behaviour of the service.
To react on communication events, for both Services a static Class named LeServerListener and 
LeClientListener is available. Use this static class to register Listeners for several event 
groups (ILeAdvertiseListener, ILeGattListener on the LeServerListener and 
ILeCommunicationListener, ILeConnectionListener and ILeScanListener in LeClientListener).

To provide Data to the LeServerService, you can either overwrite the getLeData method in 
LeServerService, or register ILeDataProviders in LeServerListener


ATTENTION. The LeServerService is already at a demo stage. The LeClientService is not that fare....

