package org.die_fabrik.ledemo.client.ui;

import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;

import org.die_fabrik.ledemo.client.ClientService;
import org.die_fabrik.ledemo.le.data.IntegerData;
import org.die_fabrik.lelib.client.ILeCommunicationListener;
import org.die_fabrik.lelib.client.ILeConnectionListener;
import org.die_fabrik.lelib.client.LeClientListeners;
import org.die_fabrik.lelib.client.LeClientService;
import org.die_fabrik.lelib.data.LeData;
import org.die_fabrik.library.R;

public class DeviceActivity extends AppCompatActivity {
    private final String TAG = this.getClass().getSimpleName();
    private LeClientService.LeClientServiceBinder binder;
    private String deviceAddress;
    private ServiceConnection serviceConnection;
    private SeekBar sb;
    private TextView tv;
    
    private void connect() {
        binder.connect(deviceAddress, 5000);
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(serviceConnection);
    }
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device);
        tv = (TextView) findViewById(R.id.tv);
        sb = (SeekBar) findViewById(R.id.sb);
        sb.setOnSeekBarChangeListener(new OnSbChangedListener());
        deviceAddress = getIntent().getStringExtra(ScanActivity.DEVICE_ADDRESS);
        getSupportActionBar().setSubtitle("Device: " + deviceAddress);
        serviceConnection = new ServiceConnection();
        Intent intent = new Intent(this, ClientService.class);
        bindService(intent, serviceConnection, BIND_AUTO_CREATE);
        LeClientListeners.registerListener(new ConnectionListener());
        LeClientListeners.registerListener(new CommunicationListener());
    }
    
    private class ServiceConnection implements android.content.ServiceConnection {
        
        /**
         * Called when a connection to the Service has been established, with
         * the {@link IBinder} of the communication channel to the
         * Service.
         *
         * @param name    The concrete component name of the service that has
         *                been connected.
         * @param iBinder The IBinder of the Service's communication channel,
         */
        @Override
        public void onServiceConnected(ComponentName name, IBinder iBinder) {
            Log.v(TAG, "onServiceConnected()");
            binder = (LeClientService.LeClientServiceBinder) iBinder;
            connect();
        }
        
        /**
         * Called when a connection to the Service has been lost.  This typically
         * happens when the process hosting the service has crashed or been killed.
         * This does <em>not</em> remove the ServiceConnection itself -- this
         * binding to the service will remain active, and you will receive a call
         * to {@link #onServiceConnected} when the Service is next running.
         *
         * @param name The concrete component name of the service whose
         *             connection has been lost.
         */
        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.v(TAG, "onServiceDisconnected()");
            binder = null;
        }
    }
    
    private class OnSbChangedListener implements SeekBar.OnSeekBarChangeListener {
        /**
         * Notification that the progress level has changed. Clients can use the fromUser parameter
         * to distinguish user-initiated changes from those that occurred programmatically.
         *
         * @param seekBar  The SeekBar whose progress has changed
         * @param progress The current progress level. This will be in the range min..max where min
         *                 and max were set by {@link ProgressBar#setMin(int)} and
         *                 {@link ProgressBar#setMax(int)}, respectively. (The default values for
         *                 min is 0 and max is 100.)
         * @param fromUser True if the progress change was initiated by the user.
         */
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            Log.v(TAG, "onProgressChanged() progress: " + progress + ", fromUser: " + fromUser);
            if (fromUser) {
                IntegerData integerData = new IntegerData(progress);
                binder.write(integerData, 1);
            }
            tv.setText(String.valueOf(progress));
        }
        
        /**
         * Notification that the user has started a touch gesture. Clients may want to use this
         * to disable advancing the seekbar.
         *
         * @param seekBar The SeekBar in which the touch gesture began
         */
        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
        
        }
        
        /**
         * Notification that the user has finished a touch gesture. Clients may want to use this
         * to re-enable advancing the seekbar.
         *
         * @param seekBar The SeekBar in which the touch gesture began
         */
        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
        
        }
    }
    
    private class ConnectionListener implements ILeConnectionListener {
        @Override
        public void onConnectionDisconnect() {
            Log.i(TAG, "onConnectionDisconnect()");
        }
        
        @Override
        public void onConnectionDiscovered() {
            Log.i(TAG, "onConnectionDiscovered()");
            binder.read(IntegerData.class, 2);
            binder.setNotification(IntegerData.class, true, 3);
        }
        
        @Override
        public void onConnectionDiscovering() {
            Log.v(TAG, "onConnectionDiscovering()");
        }
        
        @Override
        public void onConnectionTimeout() {
            Log.w(TAG, "onConnectionTimeout()");
        }
    }
    
    /**
     * Take care of popping the fragment back stack or finishing the activity
     * as appropriate.
     */
    @Override
    public void onBackPressed() {
        binder.disconnect();
        super.onBackPressed();
    }
    
    private class CommunicationListener implements ILeCommunicationListener {
        /**
         * called when the last command (read/wrirte/setNotification)
         * was sent to the other side
         *
         * @param success    whether the process was initiated successfully
         * @param identifier the Identifier which was given by the Ui when initiating this command
         */
        @Override
        public void onCommunicationCommandSent(boolean success, int identifier) {
            Log.v(TAG, "onCommunicationCommandSent() success: " + success + ", identifier: " + identifier);
        }
        
        /**
         * called when the client received a Notification with the LeData Object
         *
         * @param leData The object received via Notification
         */
        @Override
        public void onCommunicationNotificationReceived(LeData leData) {
            Log.v(TAG, "onCommunicationNotificationReceived() dataClass: " + leData.getClass().getSimpleName());
            if (leData.getClass().equals(IntegerData.class)) {
                IntegerData integerData = (IntegerData) leData;
                sb.setProgress(integerData.getVal());
            }
        }
        
        /**
         * called when a read request was successfully sent back to the client
         *
         * @param leData dataObject
         */
        @Override
        public void onCommunicationRead(LeData leData) {
            Log.v(TAG, "onCommunicationRead() dataClass: " + leData.getClass().getSimpleName());
            if (leData.getClass().equals(IntegerData.class)) {
                IntegerData integerData = (IntegerData) leData;
                sb.setProgress(integerData.getVal());
            }
        }
        
        /**
         * called when the last write transaction was finished
         *
         * @param leData the LeData which was sent to the server
         */
        @Override
        public void onCommunicationWrite(LeData leData) {
            Log.v(TAG, "onCommunicationWrite() dataClass: " + leData.getClass().getSimpleName());
        }
    }
}
