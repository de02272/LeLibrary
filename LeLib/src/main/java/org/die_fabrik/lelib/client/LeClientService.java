package org.die_fabrik.lelib.client;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.os.ParcelUuid;
import android.util.Log;

import org.die_fabrik.lelib.LeUtil;
import org.die_fabrik.lelib.data.LeData;
import org.die_fabrik.lelib.wrapper.LeCharacteristic;
import org.die_fabrik.lelib.wrapper.LeProfile;
import org.die_fabrik.lelib.wrapper.LeService;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

/**
 * LeClientService
 * This version supports only a single connection to a server
 * When binding this service you will a binder which odders you several possibilities:
 * Start/Stop Scanne. the Scan will scan the environment for Le advertiser
 * Connect/disconnect: To Establish or cancel the connection to a dedicated server
 * read/write LeData Object can be moved from client to server and vice versa.
 * Be aware that the server can only send infomration to the client, when the client is asking for that
 */
public abstract class LeClientService extends Service {
    
    /**
     * The logging TAG for this Object
     */
    protected final String TAG = this.getClass().getSimpleName();
    /**
     * The list of the actual scan results. will be cleared after each start of the scanner
     */
    private final List<ScanResult> scanResults = new ArrayList<>();
    /**
     * The local copy of the Binder instance, created by onBind(), nulled by onUnbind()
     * You can check whether this service is bound to anyone by checking this Object for null
     */
    private LeClientServiceBinder leClientServiceBinder;
    /**
     * The system wide bluetoothManager Object (created during onCreate())
     */
    private BluetoothManager bluetoothManager;
    /**
     * The BluetoothAdapter (created during onCreate())
     */
    private BluetoothAdapter bluetoothAdapter;
    /**
     * The Scanner for active advertisers (created during startScan, nulled by StopScan or ScanCallback)
     */
    private BluetoothLeScanner leScanner;
    /**
     * the instance of the QueueManager will be created during onCreate()
     * The encapsulated Thread needs an interrupt during onDestroy();
     */
    private QueueManager queueManager;
    /**
     * The gatt gateway Object. Will be created during connect and destroyed during disconnect
     * (or any other method that cancels the connection)
     */
    private BluetoothGatt gatt;
    /**
     * The callback Object for an established connection
     */
    private LeGattCallback leGattCallback;
    /**
     * the callback Object for the scan process
     */
    private LeScanCallback leScanCallback;
    /**
     * the Timeout Object to connect a device. will be engaged during connect(BluetoothDevice);
     */
    private LeConnectionTimeout leConnectionTimeout;
    /**
     * a Timer instance to start TimerTasks
     */
    private Timer timer;
    /**
     * list of discovered Services from the server side(set by onServicesDiscovered, cleared by disconnect)
     */
    private List<BluetoothGattService> discoveredServices;
    /**
     * The timeout Object to stop the Scan Process. will be created during StartScan();
     */
    private LeScanTimeout leScanTimeout;
    
    /**
     * Helper method to find a characteristic by its UUID from the discovered Services.
     *
     * @param uuId the UUID to look for
     * @return the characteristic with the given UUID or null
     */
    private BluetoothGattCharacteristic findCharacteristic(UUID uuId) {
        if (discoveredServices != null && discoveredServices.size() > 0) {
            for (BluetoothGattService service : discoveredServices) {
                for (BluetoothGattCharacteristic characteristic : service.getCharacteristics()) {
                    if (characteristic.getUuid().equals(uuId)) {
                        return characteristic;
                    }
                }
            }
        } else {
            Log.e(TAG, "no discovered services available");
        }
        return null;
    }
    
    /**
     * A Helper method to find a GattDescriptor by the uuid
     *
     * @param uuid the uuid from the descriptor
     * @return an instance od LeCharacteristic or null;
     */
    private BluetoothGattDescriptor findDescriptorByUuid(UUID uuid) {
        for (BluetoothGattService service : gatt.getServices()) {
            for (BluetoothGattCharacteristic characteristic : service.getCharacteristics()) {
                BluetoothGattDescriptor descriptor = characteristic.getDescriptor(uuid);
                if (descriptor != null) {
                    return descriptor;
                }
            }
        }
        return null;
    }
    
    /**
     * Helper method to find a LeCHaracteristic by it's UUID
     *
     * @param uuId
     * @return
     */
    private LeCharacteristic findLeCharacteristic(UUID uuId) {
        LeProfile leProfile = getLeProfile();
        for (LeService leService : leProfile.getLeServices()) {
            for (LeCharacteristic leCharacteristic : leService.getLeCharacteristics()) {
                if (leCharacteristic.getUUID().equals(uuId)) {
                    return leCharacteristic;
                }
            }
        }
        return null;
    }
    
    /**
     * Helper method to find a LeCharacteristic by its dataClass from the LeProfile()
     *
     * @param dataClass the dataClass to look for
     * @return the LeCharacteristic with the given DataClass;
     */
    private LeCharacteristic findLeCharacteristic(Class<? extends LeData> dataClass) {
        LeProfile leProfile = getLeProfile();
        for (LeService leService : leProfile.getLeServices()) {
            for (LeCharacteristic leCharacteristic : leService.getLeCharacteristics()) {
                if (leCharacteristic.getDataClass().equals(dataClass)) {
                    return leCharacteristic;
                }
            }
        }
        return null;
    }
    
    /**
     * The abstract method to retrieve the LeProfile (and its Service/Characteristic/Descriptor Structure)
     * Be aware that Client and Server are using the same LeProfile - when creating
     * different modules - use a library to build the profile
     *
     * @return The LeProfile
     */
    protected abstract LeProfile getLeProfile();
    
    /**
     * The onBind callback from android.
     *
     * @param intent not used
     * @return the public binder to this service(see leClientBinder)
     */
    @Override
    public IBinder onBind(Intent intent) {
        Log.v(TAG, "onBind()");
        if (leClientServiceBinder == null) {
            leClientServiceBinder = new LeClientServiceBinder();
        }
        return leClientServiceBinder;
    }
    
    /**
     * Called by the system when the service is first created.  Do not call this method directly.
     */
    @Override
    public void onCreate() {
        Log.v(TAG, "onCreate()");
        super.onCreate();
        bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        bluetoothAdapter = bluetoothManager.getAdapter();
        queueManager = new QueueManager();
        leGattCallback = new LeGattCallback();
        leScanCallback = new LeScanCallback();
        timer = new Timer();
        LeUtil.logAdapterCapabilities(bluetoothAdapter);
    }
    
    /**
     * Called by the system to notify a Service that it is no longer used and is being removed.  The
     * service should clean up any resources it holds (threads, registered
     * receivers, etc) at this point.  Upon return, there will be no more calls
     * in to this Service object and it is effectively dead.  Do not call this method directly.
     */
    @Override
    public void onDestroy() {
        Log.v(TAG, "onDestroy()");
        queueManager.onDestroy(); // will shutdown the QueueManager
        super.onDestroy();
    }
    
    /**
     * Called when all clients have disconnected from a particular interface
     * published by the service.  The default implementation does nothing and
     * returns false.
     *
     * @param intent The Intent that was used to bind to this service,
     *               as given to {@link Context#bindService
     *               Context.bindService}.  Note that any extras that were included with
     *               the Intent at that point will <em>not</em> be seen here.
     * @return Return true if you would like to have the service's
     * {@link #onRebind} method later called when new clients bind to it.
     */
    @Override
    public boolean onUnbind(Intent intent) {
        Log.v(TAG, "onUnbind()");
        leClientServiceBinder.stopScan(); // stops scan
        leClientServiceBinder = null;
        return false;
    }
    
    /**
     * This is the class which instance will be given to the UI via onBind();
     */
    public class LeClientServiceBinder extends Binder {
        /**
         * The logging TAG for this Object
         */
        private final String TAG = this.getClass().getSimpleName();
    
        /**
         * @param deviceAddress the max address
         * @param timeout       the timeout to establish this connection
         * @return true when the request to connect was queued successfully
         */
        public boolean connect(String deviceAddress, long timeout) {
            BluetoothDevice device = bluetoothAdapter.getRemoteDevice(deviceAddress);
            if (device != null) {
                return connect(device, timeout);
            }
            return false;
        }
        
        /**
         * To connect this service with a dedicated server (Parameters will follow)
         *
         * @return whether the request was queued successfully
         */
        public boolean connect(BluetoothDevice device, long timeout) {
            leConnectionTimeout = new LeConnectionTimeout();
            timer.schedule(leConnectionTimeout, timeout);
            gatt = device.connectGatt(LeClientService.this, false, leGattCallback);
            return gatt != null;
        }
        
        /**
         * Cancel a connection
         */
        public void disconnect() {
            if (leConnectionTimeout != null) {
                leConnectionTimeout.cancel();
            }
            if (gatt != null) {
                gatt.disconnect();
                gatt = null;
            }
            queueManager.clearQueue();
            discoveredServices = null;
        }
    
        public int getCommandBufferSize() {
            return queueManager.queue.size();
        }
        
        /**
         * reads an object fom the server
         *
         * @param dataClass the dataClass to read
         * @return whether the request was queued successfully
         */
        public void read(Class<? extends LeData> dataClass, int identifier) {
            QueuedRead queuedRead = new QueuedRead(dataClass, identifier);
            queueManager.addCommand(queuedRead);
        }
        
        /**
         * asks the server to enable or disable notifications for the dataClass
         *
         * @param dataClass the dataClass to receive notifications
         * @param enable    true - request to enable, false request to disable
         */
        public boolean setNotification(Class<? extends LeData> dataClass, boolean enable, int identifier) {
            LeCharacteristic leCharacteristic = findLeCharacteristic(dataClass);
            if (leCharacteristic != null) {
                if (leCharacteristic.getNotificationGattDescriptor() != null) {
                    UUID notificationDescriptorUuid = leCharacteristic.getNotificationGattDescriptor().getUuid();
    
                    if (enable) {
                        QueuedEnableNotification queuedEnableNotification = new QueuedEnableNotification(dataClass, identifier);
                        queueManager.addCommand(queuedEnableNotification);
                    } else {
                        QueuedDisableNotification queuedDisableNotification = new QueuedDisableNotification(dataClass, identifier);
                        queueManager.addCommand(queuedDisableNotification);
                    }
                    return true;
                } else {
                    Log.e(TAG, "The LeCharacteristic: " + leCharacteristic.getName() + " does not support notifications");
                }
            } else {
                Log.e(TAG, "can not find a LeCharacteristic with dataClass: " + dataClass.getSimpleName());
            }
            return false;
        }
        
        /**
         * @param reportDelay         Delay of report in milliseconds. Set to 0 to be notified of
         *                            results immediately. Values &gt; 0 causes the scan results to be queued up and
         *                            delivered after the requested delay or when the internal buffers fill up.
         * @param scanMode            he scan mode can be one of {@link ScanSettings#SCAN_MODE_LOW_POWER},
         *                            {@link ScanSettings#SCAN_MODE_BALANCED} or
         *                            {@link ScanSettings#SCAN_MODE_LOW_LATENCY}.
         * @param timeout             the ttimeout for find new devices
         * @param onlyProfileServices when true only device which support the service from LeProfile will be found
         * @return whether the scan process was successfully initiated
         */
        public boolean startScan(int reportDelay, int scanMode, long timeout, boolean onlyProfileServices) {
            Log.v(TAG, "startScan(reportDelay: " + reportDelay + ", scanMode: "
                    + LeUtil.getScanMode(LeClientService.this, scanMode) + ", timeout: " + timeout
                    + ", scan: " + (onlyProfileServices ? "only know advertising uuids" : "all advertiser") + ")");
            if (bluetoothAdapter != null) {
                // creates a scanner
                if (leScanner == null) {
                    leScanner = bluetoothAdapter.getBluetoothLeScanner();
                }
                
                // build settings
                ScanSettings.Builder settingsBuilder = new ScanSettings.Builder();
                settingsBuilder.setReportDelay(reportDelay)
                        .setScanMode(scanMode);
                
                // build  a filter
                List<ScanFilter> filters = new ArrayList<>();
                if (onlyProfileServices) {
                    ScanFilter.Builder filterBuilder = new ScanFilter.Builder();
                    UUID[] uuids = getLeProfile().getAdvertisingUuids();
                    for (UUID service : uuids) {
                        Log.v(TAG, "adding filter for : " + service.toString());
                        filterBuilder.setServiceUuid(new ParcelUuid(service));
                        filters.add(filterBuilder.build());
                    }
                }
                
                if (timeout > 0) {
                    leScanTimeout = new LeScanTimeout();
                    timer.schedule(leScanTimeout, timeout);
                }
    
                scanResults.clear();
                leScanner.startScan(filters, settingsBuilder.build(), leScanCallback);
                LeClientListeners.onScanStarted(timeout);
                return true;
                
            } else {
                Log.e(TAG, "bluetoothAdapter==null");
            }
            return false;
        }
        
        /**
         * Stops scanning for foreign devices
         *
         * @return
         */
        public void stopScan() {
            Log.v(TAG, "stopScan()");
            if (leScanTimeout != null) {
                leScanTimeout.cancel(); // clear the timeout
            }
            if (leScanner != null && leScanCallback != null) {
    
                leScanner.flushPendingScanResults(leScanCallback);
                
                leScanner.stopScan(leScanCallback);
            } else {
                Log.wtf(TAG, "How could this happen");
            }
            LeClientListeners.onScanStopped(scanResults);
        }
        
        /**
         * Sends Data to the server
         *
         * @param leData the data to transport
         */
        public void write(LeData leData, int identifier) {
            QueuedWrite queuedWrite = new QueuedWrite(leData, identifier);
            queueManager.addCommand(queuedWrite);
        }
    }
    
    /**
     * This callback implementation is needed for the ScanProcess and it's special issues
     */
    private class LeScanCallback extends android.bluetooth.le.ScanCallback {
        /**
         * The logging TAG for this Object
         */
        protected final String TAG = this.getClass().getSimpleName();
        
        
        /**
         * Callback when batch results are delivered.
         *
         * @param results List of scan results that are previously scanned.
         */
        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            super.onBatchScanResults(results);
            for (ScanResult scanResult : results) {
                if (!scanResults.contains(scanResult)) {
                    scanResults.add(scanResult);
                }
            }
            LeClientListeners.onScanBatchResults(results);
        }
        
        /**
         * Callback when scan could not be started.
         *
         * @param errorCode Error code (one of SCAN_FAILED_*) for scan failure.
         */
        @Override
        public void onScanFailed(int errorCode) {
            Log.v(TAG, "onScanFailed:" + LeUtil.getScanErrorCode(LeClientService.this, errorCode) + "(" + errorCode + ")");
            leScanTimeout.cancel();
            super.onScanFailed(errorCode);
            LeClientListeners.onScanFailed(errorCode);
            
        }
        
        /**
         * Callback when a BLE advertisement has been found.
         *
         * @param callbackType Determines how this callback was triggered. Could be one of
         *                     {@link ScanSettings#CALLBACK_TYPE_ALL_MATCHES},
         *                     {@link ScanSettings#CALLBACK_TYPE_FIRST_MATCH} or
         *                     {@link ScanSettings#CALLBACK_TYPE_MATCH_LOST}
         * @param result       A Bluetooth LE scan result.
         */
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);
            Log.v(TAG, "onScanResult: " + result.getDevice());
            if (!scanResults.contains(result)) {
                scanResults.add(result);
            }
            LeClientListeners.onScanResult(callbackType, result);
        }
    }
    
    /**
     * This Callback Implementation is used to manage the communication between client and Server
     */
    private class LeGattCallback extends BluetoothGattCallback {
        /**
         * The logging TAG for this Object
         */
        protected final String TAG = this.getClass().getSimpleName();
        
        /**
         * A helper method to constructLeData the LeDat Class from a received byte[]
         * hopefully the usage of reflexion is ok
         * used from onCharacteristicChanged & onCharacteristicRead
         *
         * @param leValue The byte[9 which is used to constructLeData the leData Object
         * @param cls     The class of the designated LeData Object
         * @return an instantiated LeData Object
         * @throws NoSuchMethodException
         * @throws IllegalAccessException
         * @throws InvocationTargetException
         * @throws InstantiationException
         */
        
        
        /**
         * Callback triggered as a result of a remote characteristic notification.
         *
         * @param gatt           GATT client the characteristic is associated with
         * @param characteristic Characteristic that has been updated as a result
         */
        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            super.onCharacteristicChanged(gatt, characteristic);
            Log.v(TAG, "onCharacteristicChanged characteristic: " + characteristic.getUuid().toString());
            LeCharacteristic leCharacteristic = findLeCharacteristic(characteristic.getUuid());
            if (leCharacteristic != null) {
                try {
                    byte[] leValue = characteristic.getValue();
                    LeData leData = LeUtil.createLeDataFromLeValue(leValue, leCharacteristic.getDataClass());
                    LeClientListeners.onComNotificationReceived(leData);
                    
                } catch (NoSuchMethodException | InvocationTargetException | InstantiationException | IllegalAccessException e) {
                    Log.e(TAG, "Failure during usage of Reflexion", e);
                }
            } else {
                Log.e(TAG, "can not find LeCharacteristic with UUID: " + characteristic.getUuid().toString());
            }
        }
        
        /**
         * Callback reporting the result of a characteristic read operation.
         *
         * @param gatt           GATT client invoked {@link BluetoothGatt#readCharacteristic}
         * @param characteristic Characteristic that was read from the associated
         *                       remote device.
         * @param status         {@link BluetoothGatt#GATT_SUCCESS} if the read operation
         */
        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicRead(gatt, characteristic, status);
            Log.v(TAG, "onCharacteristicRead characteristic: " + characteristic.getUuid().toString()
                    + ", status: " + LeUtil.getGattStatus(LeClientService.this, status));
            if (status == BluetoothGatt.GATT_SUCCESS) {
                LeCharacteristic leCharacteristic = findLeCharacteristic(characteristic.getUuid());
                if (leCharacteristic != null) {
                    try {
                        byte[] leValue = characteristic.getValue();
                        LeData leData = LeUtil.createLeDataFromLeValue(leValue, leCharacteristic.getDataClass());
                        LeClientListeners.onComRead(leData);
                        
                    } catch (NoSuchMethodException | InvocationTargetException | InstantiationException | IllegalAccessException e) {
                        Log.e(TAG, "Failure during usage of Reflexion", e);
                    }
                } else {
                    Log.e(TAG, "can not find LeCharacteristic with UUID: " + characteristic.getUuid().toString());
                }
            } else {
                Log.e(TAG, "status!=GATT_SUCCESS");
            }
            QueuedCommand lastCommand = queueManager.nextCommand();
        }
        
        /**
         * Callback indicating the result of a characteristic write operation.
         * <p>
         * <p>If this callback is invoked while a reliable write transaction is
         * in progress, the value of the characteristic represents the value
         * reported by the remote device. An application should compare this
         * value to the desired value to be written. If the values don't match,
         * the application must abort the reliable write transaction.
         *
         * @param gatt           GATT client invoked {@link BluetoothGatt#writeCharacteristic}
         * @param characteristic Characteristic that was written to the associated
         *                       remote device.
         * @param status         The result of the write operation
         *                       {@link BluetoothGatt#GATT_SUCCESS} if the operation succeeds.
         */
        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicWrite(gatt, characteristic, status);
            Log.v(TAG, "onCharacteristicWrite characteristic: " + characteristic.getUuid().toString() +
                    ", status: " + LeUtil.getGattStatus(LeClientService.this, status)
                    + ", value.length: " + characteristic.getValue().length +
                    ", value: " + characteristic.getValue());
    
            if (status == BluetoothGatt.GATT_SUCCESS) {
                LeCharacteristic leCharacteristic = findLeCharacteristic(characteristic.getUuid());
                if (leCharacteristic != null) {
                    try {
                        byte[] leValue = characteristic.getValue();
                        LeData leData = LeUtil.createLeDataFromLeValue(leValue, leCharacteristic.getDataClass());
                        // TODO if this is a reliable transaction - we havbe to check the value against the value from lastCommand
                        // I'm not clear how to abort this transaction?
                        LeClientListeners.onComWrite(leData);
                        
                    } catch (NoSuchMethodException | InvocationTargetException | InstantiationException | IllegalAccessException e) {
                        Log.e(TAG, "Failure during usage of Reflexion", e);
                    }
                } else {
                    Log.e(TAG, "can not find LeCharacteristic with UUID: " + characteristic.getUuid().toString());
                }
            } else {
                Log.e(TAG, "status!=GATT_SUCCESS");
            }
            QueuedCommand lastCommand = queueManager.nextCommand();
        }
        
        /**
         * Callback indicating when GATT client has connected/disconnected to/from a remote
         * GATT server.
         *
         * @param gatt     GATT client
         * @param status   Status of the connect or disconnect operation.
         *                 {@link BluetoothGatt#GATT_SUCCESS} if the operation succeeds.
         * @param newState Returns the new connection state. Can be one of
         *                 {@link BluetoothProfile#STATE_DISCONNECTED} or
         *                 {@link BluetoothProfile#STATE_CONNECTED}
         */
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            super.onConnectionStateChange(gatt, status, newState);
            Log.i(TAG, "onConnectionStateChange() " + "status: "
                    + LeUtil.getGattStatus(LeClientService.this, status) + ", newState: "
                    + LeUtil.getGattState(LeClientService.this, newState));
            if (status == BluetoothGatt.GATT_SUCCESS) {
                if (newState == BluetoothProfile.STATE_CONNECTED) {
                    boolean discovering = gatt.discoverServices();
                    if (discovering) {
                        LeClientListeners.onConnDiscovering();
                    } else {
                        // TODO ist das nötig?
                        LeClientListeners.onConnDisconnect();
                        leClientServiceBinder.disconnect();
                    }
                } else { // disconnected with a reason
                    LeClientListeners.onConnDisconnect();
                    queueManager.clearQueue();
                }
            } else {
                //TODO prüfen ob das reicht, oder ob außerdem noch LeClientListeners.onConnDisconnect(); aufgerufen werden muss.
                leClientServiceBinder.disconnect();
                LeClientListeners.onConnDisconnect();
            }
        }
        
        /**
         * Callback reporting the result of a descriptor read operation.
         *
         * @param gatt       GATT client invoked {@link BluetoothGatt#readDescriptor}
         * @param descriptor Descriptor that was read from the associated
         *                   remote device.
         * @param status     {@link BluetoothGatt#GATT_SUCCESS} if the read operation
         */
        @Override
        public void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            super.onDescriptorRead(gatt, descriptor, status);
        }
        
        /**
         * Callback indicating the result of a descriptor write operation.
         *
         * @param gatt       GATT client invoked {@link BluetoothGatt#writeDescriptor}
         * @param descriptor Descriptor that was writte to the associated
         *                   remote device.
         * @param status     The result of the write operation
         *                   {@link BluetoothGatt#GATT_SUCCESS} if the operation succeeds.
         */
        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            super.onDescriptorWrite(gatt, descriptor, status);
            QueuedCommand lastCommand = queueManager.nextCommand();
        }
        
        /**
         * Callback invoked when the list of remote services, characteristics and descriptors
         * for the remote device have been updated, ie new services have been discovered.
         *
         * @param gatt   GATT client invoked {@link BluetoothGatt#discoverServices}
         * @param status {@link BluetoothGatt#GATT_SUCCESS} if the remote device
         */
        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            super.onServicesDiscovered(gatt, status);
            Log.v(TAG, "onServicesDiscovered() status: "
                    + LeUtil.getGattStatus(LeClientService.this, status));
            // cancel the timeout for an ongoing connection process
            leConnectionTimeout.cancel();
            
            if (status == BluetoothGatt.GATT_SUCCESS) {
                discoveredServices = gatt.getServices();
                LeClientListeners.onConnDiscovered();
                
            } else {
                Log.e(TAG, "something was wrong - will disconnect");
                leClientServiceBinder.disconnect();
                // TODO is it necessary to do the callback here, would disconnect() do the same in onConnectionStateChanged?
                LeClientListeners.onConnDisconnect();
            }
        }
    }
    
    
    /**
     * This class manages the serialization of commands
     * Gatt (and even Gattserver) is getting unstable when sending new request before the
     * last Request was done. Therefore we build a queue and send the command serialized to the other side...
     */
    private final class QueueManager {
        
        /**
         * The Thread to handle the queue independent from any other thread
         * will be created in constructor. Will be terminated and removed during onDestroy();
         */
        private final QueueManagerThread queueManagerThread;
        
        /**
         * The logging TAG for this Object
         */
        private final String TAG = this.getClass().getSimpleName();
        
        /**
         * The list of stored commands
         */
        private final List<QueuedCommand> queue = new ArrayList<>();
        
        /**
         * The locking Object will be used to hold the Thread, and assure that the nextCommand will
         * not be sent before intended to.
         */
        private final Object lock = new Object();
        
        /**
         * Holds a link to the last executed command. Will be set to null from nextCommand(),
         * will be set to value from Thread
         */
        private volatile QueuedCommand lastCommand;
        
        /**
         * This constructor builds and starts the QueueManagerThread
         */
        private QueueManager() {
            queueManagerThread = new QueueManagerThread();
            queueManagerThread.start();
        }
        
        /**
         * adds a command to the commandQueue
         * proves whether the command can be executed immediately
         *
         * @param queuedCommand
         */
        void addCommand(QueuedCommand queuedCommand) {
            Log.v(TAG, "adding " + queuedCommand.getClass().getSimpleName() + " to the queue at position: " + queue.size());
            synchronized (queue) {
                queue.add(queuedCommand);
            }
            proveNextCommand();
            LeClientListeners.onComCommandQueued();
        }
        
        /**
         * This method clear the commandQueue without sending these commands to the server
         * This will be called when a connection disconnects
         */
        public void clearQueue() {
            synchronized (queue) {
                queue.clear();
            }
        }
        
        /**
         * this will cause the QueueManagerThread to execute the next command
         */
        QueuedCommand nextCommand() {
            if (queue.size() < 1) {
                Log.v(TAG, "nextCommand() will fall asleep");
            } else {
                Log.v(TAG, "nextCommand() will execute next Command");
            }
            QueuedCommand answer = lastCommand;
            lastCommand = null;
            wakeUp();
            return answer;
        }
        
        /**
         * This should be called when the LeClientService is on shutdown (onDestroy() :-) )
         */
        private void onDestroy() {
            queueManagerThread.interrupt();
            wakeUp();
        }
        
        /**
         * checks whether the lastCommand is cancelled or done
         */
        private void proveNextCommand() {
            if (lastCommand == null) {
                nextCommand();
            }
        }
        
        /**
         * threats the Thread to prove whether he could execute the next Commands
         */
        private void wakeUp() {
            synchronized (lock) {
                lock.notify();
            }
        }
        
        private class QueueManagerThread extends Thread {
            /**
             * The logging TAG for this Object
             */
            private final String TAG = this.getClass().getSimpleName();
            
            /**
             * If this thread was constructed using a separate
             * <code>Runnable</code> run object, then that
             * <code>Runnable</code> object's <code>run</code> method is called;
             * otherwise, this method does nothing and returns.
             * <p>
             * Subclasses of <code>Thread</code> should override this method.
             *
             * @see #start()
             * @see #stop()
             */
            @Override
            public void run() {
                Log.v(TAG, "entering the loop");
                while (!isInterrupted()) {
    
                    if (lastCommand == null && queue.size() > 0) {
                        synchronized (queue) {
                            lastCommand = queue.remove(0); // getting the oldest Object
                        }
                        boolean success = lastCommand.execute(gatt);
                        LeClientListeners.onComCommandSent(success, lastCommand.getIdentifier());
                    }
                    synchronized (lock) {
                        try {
                            lock.wait();
                        } catch (InterruptedException e) {
                            //Log.e(TAG, "expected exception", e);
                            Log.w(TAG, "interrupted QueueManagerThread");
                        }
                    }
                }
                Log.i(TAG, "QueueManagerThread is terminating()");
            }
        }
    }
    
    public abstract class QueuedCommand {
        /**
         * The logging TAG for this Object
         */
        protected final String TAG = this.getClass().getSimpleName();
        
        /**
         * The Identifier given by the calling ui thread
         */
        private final int identifier;
        
        protected QueuedCommand(int identifier) {
            this.identifier = identifier;
        }
        
        /**
         * This method should implement all the things which are necessary to
         * send a command to the server
         *
         * @param gatt The (connected) gatt Object
         * @return true, if the read operation was initiated successfully
         */
        abstract boolean execute(BluetoothGatt gatt);
        
        public int getIdentifier() {
            return identifier;
        }
    }
    
    private class QueuedRead extends QueuedCommand {
        private final Class<? extends LeData> dataClass;
        
        QueuedRead(Class<? extends LeData> dataClass, int identifier) {
            super(identifier);
            this.dataClass = dataClass;
        }
        
        @Override
        boolean execute(BluetoothGatt gatt) {
            if (gatt != null) {
                LeCharacteristic leCharacteristic = findLeCharacteristic(dataClass);
                if (leCharacteristic != null) {
                    BluetoothGattCharacteristic characteristic = findCharacteristic(leCharacteristic.getUUID());
                    if (characteristic != null) {
                        return gatt.readCharacteristic(characteristic);
                    } else {
                        Log.e(TAG, "can not find a Characteristic with UUID: " + leCharacteristic.getUUID().toString());
                    }
                } else {
                    Log.e(TAG, "can not find a LeCharacteristic with dataClass: " + dataClass.getSimpleName());
                }
            } else {
                Log.e(TAG, "gatt==null");
            }
            return false;
        }
    }
    
    private class QueuedWrite extends QueuedCommand {
        private final LeData leData;
        
        QueuedWrite(LeData leData, int identifier) {
            super(identifier);
            this.leData = leData;
        }
        
        @Override
        boolean execute(BluetoothGatt gatt) {
            if (gatt != null) {
                LeCharacteristic leCharacteristic = findLeCharacteristic(leData.getClass());
                if (leCharacteristic != null) {
                    BluetoothGattCharacteristic characteristic = findCharacteristic(leCharacteristic.getUUID());
                    if (characteristic != null) {
                        byte[] leValue = new byte[0];
                        try {
                            leValue = leData.createLeValue();
                            characteristic.setValue(leValue);
                            return gatt.writeCharacteristic(characteristic);
                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                        }
                    } else {
                        Log.e(TAG, "can not find a Characteristic with UUID: " + leCharacteristic.getUUID().toString());
                    }
                } else {
                    Log.e(TAG, "can not find a LeCharacteristic with dataClass: " + leData.getClass().getSimpleName());
                }
            } else {
                Log.e(TAG, "gatt==null");
            }
            return false;
            
        }
    }
    
    private class QueuedEnableNotification extends QueuedCommand {
        private final Class<? extends LeData> dataClass;
        
        QueuedEnableNotification(Class<? extends LeData> dataClass, int identifier) {
            super(identifier);
            this.dataClass = dataClass;
        }
        
        @Override
        boolean execute(BluetoothGatt gatt) {
            if (gatt != null) {
                LeCharacteristic leCharacteristic = findLeCharacteristic(dataClass);
                if (leCharacteristic != null) {
    
                    BluetoothGattCharacteristic characteristic = findCharacteristic(leCharacteristic.getUUID());
                    if (characteristic != null) {
                        UUID descriptorUuid = leCharacteristic.getNotificationGattDescriptor().getUuid();
                        BluetoothGattDescriptor descriptor = findDescriptorByUuid(descriptorUuid);
                        descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                        gatt.setCharacteristicNotification(characteristic, true);
                        return gatt.writeDescriptor(descriptor);
    
                    } else {
                        Log.e(TAG, "can not find a Characteristic with UUID: " + leCharacteristic.getUUID().toString());
                    }
                } else {
                    Log.e(TAG, "can not find a LeCharacteristic with dataClass: " + dataClass.getSimpleName());
                }
            } else {
                Log.e(TAG, "gatt==null");
            }
            return false;
        }
    }
    
    private class QueuedDisableNotification extends QueuedCommand {
        private final Class<? extends LeData> dataClass;
        
        QueuedDisableNotification(Class<? extends LeData> dataClass, int identifier) {
            super(identifier);
            this.dataClass = dataClass;
        }
        
        @Override
        boolean execute(BluetoothGatt gatt) {
            if (gatt != null) {
                LeCharacteristic leCharacteristic = findLeCharacteristic(dataClass);
                if (leCharacteristic != null) {
    
                    BluetoothGattCharacteristic characteristic = findCharacteristic(leCharacteristic.getUUID());
                    if (characteristic != null) {
                        UUID descriptorUuid = leCharacteristic.getNotificationGattDescriptor().getUuid();
                        BluetoothGattDescriptor descriptor = findDescriptorByUuid(descriptorUuid);
                        descriptor.setValue(BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE);
                        gatt.setCharacteristicNotification(characteristic, false);
                        return gatt.writeDescriptor(descriptor);
    
                    } else {
                        Log.e(TAG, "can not find a Characteristic with UUID: " + leCharacteristic.getUUID().toString());
                    }
                } else {
                    Log.e(TAG, "can not find a LeCharacteristic with dataClass: " + dataClass.getSimpleName());
                }
            } else {
                Log.e(TAG, "gatt==null");
            }
            return false;
        }
    }
    
    
    private class LeConnectionTimeout extends TimerTask {
        /**
         * The logging TAG for this Object
         */
        protected final String TAG = this.getClass().getSimpleName();
        
        
        /**
         * The action to be performed by this timer task.
         */
        @Override
        public void run() {
            Log.v(TAG, "The timeout to establish a connection is achieved, will cancel the process");
            LeClientListeners.onConnTimeout();
            leClientServiceBinder.disconnect();
        }
    }
    
    private class LeScanTimeout extends TimerTask {
        /**
         * The action to be performed by this timer task.
         */
        @Override
        public void run() {
            Log.v(TAG, "the timeout for the scan process is achieved - will stop scanning");
            LeClientListeners.onScanTimeout();
            leClientServiceBinder.stopScan();
        }
    }
    
    
}
