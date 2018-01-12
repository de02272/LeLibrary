package org.die_fabrik.lelib.server;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattServer;
import android.bluetooth.BluetoothGattServerCallback;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.AdvertiseData;
import android.bluetooth.le.AdvertiseSettings;
import android.bluetooth.le.BluetoothLeAdvertiser;
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
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

public abstract class LeServerService extends Service {
    
    /**
     * The logging TAG for this Object
     */
    protected final String TAG = this.getClass().getSimpleName();
    
    /**
     * The external published ServiceBinder
     * will be created during onBind, and destroyed during onUnbind
     */
    private LeServerServiceBinder leServerServiceBinder;
    
    /**
     * TRhe system wide BluetoothManager, created durin gonCreate
     */
    private BluetoothManager bluetoothManager;
    
    /**
     * The system wide BluetoothAdapter, created during onCreate()
     */
    private BluetoothAdapter bluetoothAdapter;
    
    /**
     * A Timer Object to schedule any TimerTasks (Timeout, etc). Created during onCreate()
     */
    private Timer timer;
    
    /**
     * The LeAdvertiserConfig Object which was used by the last start of the advertiser
     * necessary to restart the advertiser again
     * will be created in startAdvertiser and used in restartAdvertiser();
     */
    private LeAdvertiserConfig advertiserConfig;
    
    /**
     * The bluetoothAdvertiser OIbject created durcing onCreate(). Will be
     * used to start the advertiosing process.
     */
    private BluetoothLeAdvertiser bluetoothAdvertiser;
    
    /**
     * The callback object for the advertiser
     */
    private LeAdvertiserCallback leAdvertiseCallback;
    
    /**
     * The callback object for the gatt server
     */
    private LeGattServerCallback leGattServerCallback;
    
    /**
     * the Queue Manager for Notifications
     */
    private NotificationQueueManager notificationQueueManager;
    
    /**
     * The Gatt Server Object. created during startGatt()
     */
    private BluetoothGattServer gattServer;
    
    /**
     * The sessions list. Which clients are connected ?
     */
    private List<LeSession> leSessions = new ArrayList<>();
    /**
     * The timeout for an established connection, will be set during startGatt()
     */
    private int sessionTimeout;
    
    /**
     * A Helper method to look for a Characteristic within the gattServer Object
     *
     * @param UUID The UUID to look for
     * @return A BluetoothGattCharacteristic or null
     */
    private BluetoothGattCharacteristic findGattCharacteristic(UUID UUID) {
        if (gattServer != null) {
            List<BluetoothGattService> services = gattServer.getServices();
            for (BluetoothGattService service : services) {
                for (BluetoothGattCharacteristic characteristic : service.getCharacteristics()) {
                    if (characteristic.getUuid().equals(UUID)) {
                        return characteristic;
                    }
                }
            }
        }
        return null;
    }
    
    /**
     * A Helper method to find a Characteristic within the gattServer by it's dataClass
     *
     * @param dataClass the dataClass <? extends LeData> to look for
     * @return A BluetoothGattCharacteristic or null
     */
    private BluetoothGattCharacteristic findGattCharacteristic(Class<? extends LeData> dataClass) {
        if (gattServer != null) {
            for (LeService service : getLeProfile().getLeServices()) {
                for (LeCharacteristic leCharacteristic : service.getLeCharacteristics()) {
                    if (leCharacteristic.getDataClass().equals(dataClass)) {
                        return findGattCharacteristic(leCharacteristic.getUUID());
                    }
                }
            }
        }
        return null;
    }
    
    private LeCharacteristic findLeCharacteristic(UUID uuid) {
        for (LeService leService : getLeProfile().getLeServices()) {
            for (LeCharacteristic leCharacteristic : leService.getLeCharacteristics()) {
                if (leCharacteristic.getUUID().equals(uuid)) {
                    return leCharacteristic;
                }
            }
        }
        return null;
    }
    
    /**
     * A Helper method to find the Session for the given device
     *
     * @param device The device to look for
     * @return a LeSession Object or null
     */
    private LeSession findLeSession(BluetoothDevice device) {
        for (LeSession leSession : leSessions) {
            if (leSession.getDevice().equals(device)) {
                return leSession;
            }
        }
        return null;
    }
    
    protected abstract LeData getLeData(Class<? extends LeData> dataClass, LeSession leSession);
    
    /**
     * The abstract method to retrieve the LeProfile (and its Service/Characteristic/Descriptor Structure)
     * Be aware that Client and Server are using the same LeProfile - when creating
     * different modules - use a library to build the profile
     *
     * @return The LeProfile
     */
    protected abstract LeProfile getLeProfile();
    
    @Override
    public IBinder onBind(Intent intent) {
        Log.v(TAG, "onUnbind()");
        leServerServiceBinder = new LeServerServiceBinder();
        return leServerServiceBinder;
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
        bluetoothAdvertiser = bluetoothAdapter.getBluetoothLeAdvertiser();
        timer = new Timer();
        LeUtil.logAdapterCapabilities(bluetoothAdapter);
        leAdvertiseCallback = new LeAdvertiserCallback();
        leGattServerCallback = new LeGattServerCallback();
        notificationQueueManager = new NotificationQueueManager();
    }
    
    /**
     * Called by the system to notify a Service that it is no longer used and is being removed.  The
     * service should clean up any resources it holds (threads, registered
     * receivers, etc) at this point.  Upon return, there will be no more calls
     * in to this Service object and it is effectively dead.  Do not call this method directly.
     */
    @Override
    public void onDestroy() {
        Log.v(TAG, "onUnbind()");
        super.onDestroy();
        notificationQueueManager.onDestroy();
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
        leServerServiceBinder = null;
        return false;
    }
    
    /**
     * This helper method adds a LeSession object to the list of registered Session and start it's timeout
     *
     * @param leSession the session to register
     */
    private void registerSession(LeSession leSession) {
        leSessions.add(leSession);
        leSession.timeoutStart();
    }
    
    private void restartAdvertiser() {
        Log.v(TAG, "restarting Advertiser");
        leServerServiceBinder.stopAdvertising();
        leServerServiceBinder.startAdvertising(advertiserConfig);
        
    }
    
    /**
     * This helper method removes a session from the list of registered session
     * and stops it's timeout
     *
     * @param leSession the session to remove
     */
    private void unregisterSession(LeSession leSession) {
        leSession.timeoutStop();
        leSessions.remove(leSession);
    }
    
    public class LeServerServiceBinder extends Binder {
        /**
         * The logging TAG for this Object
         */
        protected final String TAG = this.getClass().getSimpleName();
        
        
        public void disconnect(BluetoothDevice bluetoothDevice) {
            Log.v(TAG, "disconnect");
        }
        
        public boolean sendNotification(LeData leData, boolean confirm) {
            Log.i(TAG, "sendNotification()");
            byte[] leValue = new byte[0];
            try {
                leValue = leData.getLeValue();
                notificationQueueManager.addCommand(new Notification(leData.getClass(), leValue, confirm));
                return true;
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            return false;
            
        }
        
        public void startAdvertising(LeAdvertiserConfig config) {
            Log.i(TAG, "startAdvertising()");
            
            if (config == null) {
                int z = 0;
                throw new IllegalArgumentException("Missing LeAdvertiserConfig");
            } else {
                bluetoothAdapter.setName(config.getBluetoothName());
                
                AdvertiseSettings settings = new AdvertiseSettings.Builder()
                        .setAdvertiseMode(config.getAdvertiseMode())
                        .setTimeout(config.getTimeout())
                        .setConnectable(config.isConnectible())
                        .setTxPowerLevel(config.getTxPowerLevel())
                        .build();
                
                AdvertiseData.Builder dataBuilder1 = new AdvertiseData.Builder();
                for (UUID adUuid : getLeProfile().getAdvertisingUuids()) {
                    dataBuilder1.addServiceUuid(new ParcelUuid(adUuid));
                }
                dataBuilder1.setIncludeDeviceName(config.isIncludeDeviceName());
                
                
                AdvertiseData scanResponse = new AdvertiseData.Builder()
                        .setIncludeTxPowerLevel(false)
                        .addManufacturerData(config.getPayloadId(), config.getPayload())
                        .build();
                
                
                bluetoothAdvertiser.startAdvertising(settings, dataBuilder1.build(), scanResponse, leAdvertiseCallback);
                advertiserConfig = config; // for the next restart
            }
        }
        
        public void startGatt(int sessionTimeout) {
            Log.i(TAG, "startGatt");
            
            LeServerService.this.sessionTimeout = sessionTimeout;
            
            gattServer = bluetoothManager.openGattServer(LeServerService.this, leGattServerCallback);
            List<BluetoothGattService> loadedServices = gattServer.getServices();
            for (LeService leService : getLeProfile().getLeServices()) {
                BluetoothGattService service = leService.getBluetoothGattService();
                if (!loadedServices.contains(service)) {
                    gattServer.addService(service);
                }
            }
        }
        
        public void stopAdvertising() {
            Log.i(TAG, "stopAdvertising");
            bluetoothAdvertiser.stopAdvertising(leAdvertiseCallback);
        }
        
        public void stopGatt() {
            Log.i(TAG, "stopGatt");
            if (gattServer != null) {
                for (LeSession bluetoothDevice : leSessions) {
                    gattServer.cancelConnection(bluetoothDevice.getDevice());
                }
                gattServer.close();
                gattServer = null;
            }
        }
    }
    
    private class LeAdvertiserCallback extends android.bluetooth.le.AdvertiseCallback {
        
        /**
         * The logging TAG for this Object
         */
        protected final String TAG = this.getClass().getSimpleName();
        
        /**
         * Callback when advertising could not be started.
         *
         * @param errorCode Error code (see ADVERTISE_FAILED_* constants) for advertising start
         *                  failures.
         */
        @Override
        public void onStartFailure(int errorCode) {
            super.onStartFailure(errorCode);
            Log.e(TAG, "onStartFailure for LeAdvertiser with " + LeUtil.getAdvertiseFailure(LeServerService.this, errorCode));
            LeServerListeners.onAdvertiserStartFailure(errorCode);
        }
        
        /**
         * Callback triggered in response to {@link BluetoothLeAdvertiser#startAdvertising} indicating
         * that the advertising has been started successfully.
         *
         * @param settingsInEffect The actual settings used for advertising, which may be different from
         *                         what has been requested.
         */
        @Override
        public void onStartSuccess(AdvertiseSettings settingsInEffect) {
            super.onStartSuccess(settingsInEffect);
            Log.v(TAG, "onAdvertiserStartSuccess with " + settingsInEffect);
            LeServerListeners.onAdvertiserStartSuccess(settingsInEffect);
        }
    }
    
    private class LeGattServerCallback extends BluetoothGattServerCallback {
        /**
         * The logging TAG for this Object
         */
        private final String TAG = this.getClass().getSimpleName();
        
        /**
         * A remote client has requested to read a local characteristic.
         * <p>
         * <p>An application must call {@link BluetoothGattServer#sendResponse}
         * to complete the request.
         * <p>
         * <p>
         * TODO cause we do not know when it's over, we can not trigger a LeServerListener callback? How to handle that?
         *
         * @param device         The remote device that has requested the read operation
         * @param requestId      The Id of the request
         * @param offset         Offset into the value of the characteristic
         * @param characteristic Characteristic to be read
         */
        @Override
        public void onCharacteristicReadRequest(BluetoothDevice device, int requestId, int offset, BluetoothGattCharacteristic characteristic) {
            super.onCharacteristicReadRequest(device, requestId, offset, characteristic);
            Log.v(TAG, "onCharacteristicReadRequest(device: " + device + ", requestId: " + requestId +
                    ", offset: " + offset + " characteristic: " + characteristic.getUuid().toString() + ")");
            
            byte[] leValue = null;
            int answerStatus = BluetoothGatt.GATT_FAILURE;
            int answerOffset = offset;
            
            LeSession leSession = findLeSession(device);
            if (leSession != null) {
                leSession.timeoutProlongation();
                LeCharacteristic leCharacteristic = findLeCharacteristic(characteristic.getUuid());
                if (leCharacteristic != null) {
                    
                    if (offset == 0) { // new request
                        
                        try {
                            LeData leData = LeServerService.this.getLeData(leCharacteristic.getDataClass(), leSession);
                            if (leData != null) {
                                leValue = leData.getLeValue();
                            }
                            leSession.getOutgoing().setLeValue(leValue);
                            answerStatus = BluetoothGatt.GATT_SUCCESS;
                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                        }
                    } else { // split request
                        leValue = Arrays.copyOfRange(leSession.getOutgoing().getLeValue(), offset, leSession.getOutgoing().getLeValue().length);
                        answerStatus = BluetoothGatt.GATT_SUCCESS;
                    }
                } else {
                    answerOffset = 0;
                    Log.e(TAG, "no LeCharacteristic found for UUID: " + characteristic.getUuid().toString());
                }
            } else {
                answerOffset = 0;
                Log.e(TAG, "no session found for device: " + device);
            }
            gattServer.sendResponse(device, requestId, answerStatus, answerOffset, leValue);
        }
        
        /**
         * A remote client has requested to write to a local characteristic.
         * <p>
         * <p>An application must call {@link BluetoothGattServer#sendResponse}
         * to complete the request.
         *
         * @param device         The remote device that has requested the write operation
         * @param requestId      The Id of the request
         * @param characteristic Characteristic to be written to.
         * @param preparedWrite  true, if this write operation should be queued for
         *                       later execution.
         * @param responseNeeded true, if the remote device requires a response
         * @param offset         The offset given for the value
         * @param value          The value the client wants to assign to the characteristic
         */
        @Override
        public void onCharacteristicWriteRequest(BluetoothDevice device, int requestId, BluetoothGattCharacteristic characteristic, boolean preparedWrite, boolean responseNeeded, int offset, byte[] value) {
            super.onCharacteristicWriteRequest(device, requestId, characteristic, preparedWrite, responseNeeded, offset, value);
            Log.v(TAG, "onCharacteristicWriteRequest(device: " + device + ", requestId: " + requestId +
                    " characteristic: " + characteristic.getUuid().toString() + ", preparedWrite: " + preparedWrite +
                    ", responseNeeded: " + responseNeeded + ", offset: " + offset + ", value.length: " + value.length + ")");
            byte[] leValue = value;
            int answerStatus = BluetoothGatt.GATT_FAILURE;
            int answerOffset = offset;
            LeData leData = null;
            
            LeSession leSession = findLeSession(device);
            if (leSession != null) {
                leSession.timeoutProlongation();
                LeCharacteristic leCharacteristic = findLeCharacteristic(characteristic.getUuid());
                if (leCharacteristic != null) {
                    
                    if (preparedWrite) {
                        //TODO check whether its better to rest the incoming buffer here or where else?
                        leSession.getIncoming().clear();
                        leSession.getIncoming().setDataClass(leCharacteristic.getDataClass());
                        leSession.getIncoming().addLeValue(value);
                        answerStatus = BluetoothGatt.GATT_SUCCESS;
                    } else {
                        try {
                            leData = LeUtil.createLeDataFromLeValue(value, leCharacteristic.getDataClass());
                            answerStatus = BluetoothGatt.GATT_SUCCESS;
                        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException | InstantiationException e) {
                            Log.e(TAG, "can not build LeData Object");
                            e.printStackTrace();
                        }
                    }
                } else {
                    Log.e(TAG, "no LeCharacteristic found for UUID: " + characteristic.getUuid().toString());
                }
            } else {
                Log.e(TAG, "no session found for device: " + device);
            }
            gattServer.sendResponse(device, requestId, answerStatus, answerOffset, leValue);
            if (answerStatus == BluetoothGatt.GATT_SUCCESS) {
                if (!preparedWrite) {
                    LeServerListeners.onGattWritten(leData, leSession.getDevice());
                }
            } else {
                // TODO any kind of failure message?
                LeServerListeners.onGattWrittenFailure(leData, leSession.getDevice());
            }
        }
        
        /**
         * Callback indicating when a remote device has been connected or disconnected.
         *
         * @param device   Remote device that has been connected or disconnected.
         * @param status   Status of the connect or disconnect operation.
         * @param newState Returns the new connection state. Can be one of
         *                 {@link BluetoothProfile#STATE_DISCONNECTED} or
         *                 {@link BluetoothProfile#STATE_CONNECTED}
         */
        @Override
        public void onConnectionStateChange(BluetoothDevice device, int status, int newState) {
            super.onConnectionStateChange(device, status, newState);
            Log.v(TAG, "onConnectionStateChange(device: " + device + ", status: "
                    + LeUtil.getGattStatus(LeServerService.this, status)
                    + ", newState: " + LeUtil.getGattState(LeServerService.this, newState) + ")");
            LeSession leSession;
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                leSession = new LeSession(device, sessionTimeout);
                registerSession(leSession);
                LeServerListeners.onGattConnected(leSession.getDevice());
                
                /**
                 * the Restart is necessary cause the Advertiser will stop(a little bit),
                 * once a connection is established (that's now!)
                 */
                restartAdvertiser();
                
            } else { // DISCONNECTED
                leSession = findLeSession(device);
                unregisterSession(leSession);
                LeServerListeners.onGattDisconnected(leSession.getDevice());
            }
        }
        
        /**
         * A remote client has requested to read a local descriptor.
         * <p>
         * <p>An application must call {@link BluetoothGattServer#sendResponse}
         * to complete the request.
         *
         * @param device     The remote device that has requested the read operation
         * @param requestId  The Id of the request
         * @param offset     Offset into the value of the characteristic
         * @param descriptor Descriptor to be read
         */
        @Override
        public void onDescriptorReadRequest(BluetoothDevice device, int requestId, int offset, BluetoothGattDescriptor descriptor) {
            super.onDescriptorReadRequest(device, requestId, offset, descriptor);
        }
        
        /**
         * A remote client has requested to write to a local descriptor.
         * <p>
         * <p>An application must call {@link BluetoothGattServer#sendResponse}
         * to complete the request.
         *
         * @param device         The remote device that has requested the write operation
         * @param requestId      The Id of the request
         * @param descriptor     Descriptor to be written to.
         * @param preparedWrite  true, if this write operation should be queued for
         *                       later execution.
         * @param responseNeeded true, if the remote device requires a response
         * @param offset         The offset given for the value
         * @param value          The value the client wants to assign to the descriptor
         */
        @Override
        public void onDescriptorWriteRequest(BluetoothDevice device, int requestId, BluetoothGattDescriptor descriptor, boolean preparedWrite, boolean responseNeeded, int offset, byte[] value) {
            super.onDescriptorWriteRequest(device, requestId, descriptor, preparedWrite, responseNeeded, offset, value);
        }
        
        /**
         * Execute all pending write operations for this device.
         * <p>
         * <p>An application must call {@link BluetoothGattServer#sendResponse}
         * to complete the request.
         *
         * @param device    The remote device that has requested the write operations
         * @param requestId The Id of the request
         * @param execute   Whether the pending writes should be executed (true) or
         */
        @Override
        public void onExecuteWrite(BluetoothDevice device, int requestId, boolean execute) {
            super.onExecuteWrite(device, requestId, execute);
            Log.v(TAG, "onExecuteWrite(device: " + device + ", requestId: " + requestId + ", execute: " + execute + ")");
            
            byte[] leValue = null;
            int answerStatus = BluetoothGatt.GATT_FAILURE;
            int answerOffset = 0;
            
            LeSession leSession = findLeSession(device);
            if (leSession != null) {
                if (execute) {
                    try {
                        leValue = leSession.getIncoming().getLeValue();
                        LeData leData = LeUtil.createLeDataFromLeValue(leSession.getIncoming().getLeValue(), leSession.getIncoming().getDataClass());
                        answerStatus = BluetoothGatt.GATT_SUCCESS;
                    } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException | InstantiationException e) {
                        Log.e(TAG, "can not build LeData Object");
                        e.printStackTrace();
                    }
                } else {
                    answerStatus = BluetoothGatt.GATT_SUCCESS;
                }
            } else {
                Log.e(TAG, "no session found for device: " + device);
            }
            gattServer.sendResponse(device, requestId, answerStatus, answerOffset, leValue);
        }
        
        /**
         * Callback invoked when a notification or indication has been sent to
         * a remote device.
         * <p>
         * <p>When multiple notifications are to be sent, an application must
         * wait for this callback to be received before sending additional
         * notifications.
         *
         * @param device The remote device the notification has been sent to
         * @param status {@link BluetoothGatt#GATT_SUCCESS} if the operation was successful
         */
        @Override
        public void onNotificationSent(BluetoothDevice device, int status) {
            super.onNotificationSent(device, status);
        }
        
        /**
         * Indicates whether a local service has been added successfully.
         *
         * @param status  Returns {@link BluetoothGatt#GATT_SUCCESS} if the service
         *                was added successfully.
         * @param service The service that has been added
         */
        @Override
        public void onServiceAdded(int status, BluetoothGattService service) {
            super.onServiceAdded(status, service);
        }
    }
    
    /**
     * This class manages the serialization of commands
     * Gatt (and even Gattserver) is getting unstable when sending new request before the
     * last Request was done. Therefore we build a queue and send the command serialized to the other side...
     */
    private final class NotificationQueueManager {
        
        /**
         * The Thread to handle the queue independent from any other thread
         * will be created in constructor. Will be terminated and removed during onDestroy();
         */
        private final NotificationQueueManagerThread notificationQueueManagerThread;
        
        /**
         * The logging TAG for this Object
         */
        private final String TAG = this.getClass().getSimpleName();
        
        /**
         * The list of stored commands
         */
        private final List<Notification> queue = new ArrayList<>();
        
        /**
         * The locking Object will be used to hold the Thread, and assure that the nextNotification will
         * not be sent before intended to.
         */
        private final Object lock = new Object();
        
        /**
         * Holds a link to the last executed command. Will be set to null from nextNotification(),
         * will be set to value from Thread
         */
        private volatile Notification lastNotification;
        
        /**
         * This constructor builds and starts the NotificationQueueManagerThread
         */
        private NotificationQueueManager() {
            notificationQueueManagerThread = new NotificationQueueManagerThread();
            notificationQueueManagerThread.start();
        }
        
        /**
         * adds a command to the commandQueue
         * proves whether the command can be executed immediately
         *
         * @param queuedCommand
         */
        void addCommand(Notification queuedCommand) {
            Log.v(TAG, "adding " + queuedCommand.getClass().getSimpleName() + " to the queue at position: " + queue.size());
            synchronized (queue) {
                queue.add(queuedCommand);
            }
            proveNextCommand();
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
         * this will cause the NotificationQueueManagerThread to execute the next command
         */
        Notification nextNotification() {
            Log.v(TAG, "nextNotification()");
            Notification answer = lastNotification;
            lastNotification = null;
            wakeUp();
            return answer;
        }
        
        /**
         * This should be called when the LeClientService is on shutdown (onDestroy() :-) )
         */
        private void onDestroy() {
            notificationQueueManagerThread.interrupt();
            wakeUp();
        }
        
        /**
         * checks whether the lastCommand is cancelled or done
         */
        private void proveNextCommand() {
            if (lastNotification == null) {
                nextNotification();
            }
        }
        
        /**
         * threats the Thread to prove whether he could execute the next Commands
         */
        private void wakeUp() {
            lock.notify();
        }
        
        private class NotificationQueueManagerThread extends Thread {
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
                    
                    if (lastNotification == null) {
                        synchronized (queue) {
                            lastNotification = queue.remove(0); // getting the oldest Object
                        }
                        for (LeSession leSession : leSessions) {
                            for (Class<? extends LeData> cls : leSession.getNotificationClasses()) {
                                if (cls.equals(lastNotification.getCls())) {
                                    lastNotification.execute(gattServer, leSession.getDevice());
                                    break;
                                }
                            } // the break brings us here
                        }
                        LeServerListeners.onGattNotificationSent();
                    }
                    synchronized (lock) {
                        try {
                            lock.wait();
                        } catch (InterruptedException e) {
                            Log.e(TAG, "expected exception", e);
                        }
                    }
                }
                Log.i(TAG, "NotificationQueueManagerThread is terminating()");
            }
        }
    }
    
    private class Notification {
        private final Class<? extends LeData> cls;
        private final byte[] data;
        private final boolean confirm;
        private final BluetoothGattCharacteristic characteristic;
        
        private Notification(Class<? extends LeData> cls, byte[] data, boolean confirm) {
            this.cls = cls;
            this.data = data;
            this.confirm = confirm;
            characteristic = findGattCharacteristic(cls);
        }
        
        public boolean execute(BluetoothGattServer gattServer, BluetoothDevice bluetoothDevice) {
            if (characteristic != null) {
                return gattServer.notifyCharacteristicChanged(bluetoothDevice, characteristic, confirm);
            } else {
                Log.e(TAG, "can not send notification cause there is no registered characteristic to transport the data from type: " + cls.getSimpleName());
            }
            return false;
        }
        
        public Class<? extends LeData> getCls() {
            return cls;
        }
        
        public byte[] getData() {
            return data;
        }
        
    }
    
    private class LeSession {
        private final BluetoothDevice device;
        private final long timeout;
        private final LeValues incoming = new LeValues();
        private final LeValues outgoing = new LeValues();
        private final List<Class<? extends LeData>> notificationClasses = new ArrayList<>();
        private LeSessionTimeout leSessionTimeout;
        
        private LeSession(BluetoothDevice device, long timeout) {
            this.device = device;
            this.timeout = timeout;
            
        }
        
        private void addNotificationClass(Class<? extends LeData> cls) {
            if (!notificationClasses.contains(cls)) {
                notificationClasses.add(cls);
            }
        }
        
        BluetoothDevice getDevice() {
            return device;
        }
        
        LeValues getIncoming() {
            return incoming;
        }
        
        List<Class<? extends LeData>> getNotificationClasses() {
            return notificationClasses;
        }
        
        LeValues getOutgoing() {
            return outgoing;
        }
        
        long getTimeout() {
            return timeout;
        }
        
        void removeNotificationClass(Class<? extends LeData> cls) {
            if (notificationClasses.contains(cls)) {
                notificationClasses.remove(cls);
            }
        }
        
        void timeoutProlongation() {
            if (leSessionTimeout != null) {
                leSessionTimeout.cancel();
            }
            timeoutStart();
        }
        
        void timeoutStart() {
            leSessionTimeout = new LeSessionTimeout(device);
            timer.schedule(leSessionTimeout, timeout);
        }
        
        void timeoutStop() {
            if (leSessionTimeout != null) {
                leSessionTimeout.cancel();
            }
        }
    }
    
    private class LeSessionTimeout extends TimerTask {
        private final BluetoothDevice device;
        
        private LeSessionTimeout(BluetoothDevice device) {
            this.device = device;
        }
        
        /**
         * The action to be performed by this timer task.
         */
        @Override
        public void run() {
            Log.w(TAG, "session timeout is achieved - will cancel connection to " + device);
            leServerServiceBinder.disconnect(device);
        }
    }
    
    private class LeValues {
        private final List<byte[]> leValues = new ArrayList<>();
        private int leValuesLength = 0;
        private Class<? extends LeData> dataClass;
        private byte[] leValue;
        
        private LeValues() {
        
        }
        
        private void addLeValue(byte[] leValue) {
            if (leValue != null) {
                leValues.add(leValue);
                leValuesLength = leValuesLength + leValue.length;
            }
        }
        
        private void clear() {
            leValues.clear();
            leValuesLength = 0;
        }
        
        public Class<? extends LeData> getDataClass() {
            return dataClass;
        }
        
        public void setDataClass(Class<? extends LeData> dataClass) {
            this.dataClass = dataClass;
        }
        
        private byte[] getLeValue() {
            ByteBuffer bb = ByteBuffer.allocate(leValuesLength);
            for (byte[] leValue : leValues) {
                bb.put(leValue);
            }
            return bb.array();
        }
        
        public void setLeValue(byte[] leValue) {
            clear();
            addLeValue(leValue);
        }
    }
}