package org.die_fabrik.ledemo.client.ui;

import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
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

import java.util.Timer;
import java.util.TimerTask;

public class DeviceActivity extends AppCompatActivity {
    private final static long timeout = 5000;
    private final String TAG = this.getClass().getSimpleName();
    private LeClientService.LeClientServiceBinder binder;
    private String deviceAddress;
    private ServiceConnection serviceConnection;
    private SeekBar sb;
    private TextView tv;
    private ConnectionListener connectionListener;
    private CommunicationListener communicationListener;
    private Timer timer;
    private PeriodicNotification task;
    private boolean overRideFromUser;
    private TextView bufTv;
    private TextView descWriteTv;
    private TextView descReadTv;
    private TextView notificationsReceivedTv;
    private TextView charaWriteTv;
    private TextView charaReadTv;
    
    private void connect() {
        binder.connect(deviceAddress, timeout);
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
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        timer = new Timer();
        setContentView(R.layout.activity_device);
        bufTv = (TextView) findViewById(R.id.buffer_size_tv);
        bufTv.setText("0");
        tv = (TextView) findViewById(R.id.tv);
        sb = (SeekBar) findViewById(R.id.sb);
        sb.setEnabled(false);
        sb.setOnSeekBarChangeListener(new OnSbChangedListener());
    
        charaReadTv = (TextView) findViewById(R.id.chara_read_tv);
        charaWriteTv = (TextView) findViewById(R.id.chara_write_tv);
        notificationsReceivedTv = (TextView) findViewById(R.id.notification_tv);
        descReadTv = (TextView) findViewById(R.id.desc_read_tv);
        descWriteTv = (TextView) findViewById(R.id.desc_write_tv);
        
        
        deviceAddress = getIntent().getStringExtra(ScanActivity.DEVICE_ADDRESS);
        getSupportActionBar().setSubtitle("Device: " + deviceAddress);
        serviceConnection = new ServiceConnection();
        Intent intent = new Intent(this, ClientService.class);
        bindService(intent, serviceConnection, BIND_AUTO_CREATE);
        connectionListener = new ConnectionListener();
        LeClientListeners.registerListener(connectionListener);
        communicationListener = new CommunicationListener();
        LeClientListeners.registerListener(communicationListener);
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (binder != null) {
            binder.disconnect();
        }
        LeClientListeners.unregisterListener(connectionListener);
        LeClientListeners.unregisterListener(communicationListener);
        
        unbindService(serviceConnection);
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        switch (id) {
            
            
            case R.id.action_start_periodic_changes:
                if (task == null) {
                    Log.v(TAG, "start periodic");
                    task = new PeriodicNotification(0, 0, 100, 1);
                    timer.schedule(task, 0, 200);
                } else {
                    Log.v(TAG, "stop periodic");
                    task.cancel();
                    timer.purge();
                    task = null;
                }
                return true;
            
            default:
                return super.onOptionsItemSelected(item);
        }
        
        
    }
    
    private void refreshBuf(final LeClientService.LeClientServiceBinder binder) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                bufTv.setText(String.valueOf(binder.getCommandBufferSize()));
                charaReadTv.setText(String.valueOf(binder.getCharacteristicReads()));
                charaWriteTv.setText(String.valueOf(binder.getCharacteristicWrites()));
                notificationsReceivedTv.setText(String.valueOf(binder.getNotificationsReceived()));
                descReadTv.setText(String.valueOf(binder.getDescriptorReads()));
                descWriteTv.setText(String.valueOf(binder.getDescriptorWrites()));
            }
        });
        
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
            if (fromUser || overRideFromUser) {
                IntegerData integerData = new IntegerData(progress);
                overRideFromUser = false;
                binder.write(integerData, timeout);
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
        public void onConnDisconnect() {
            Log.i(TAG, "onConnDisconnect()");
            finish();
        }
        
        @Override
        public void onConnDiscovered() {
            Log.i(TAG, "onConnDiscovered()");
            binder.clearStat();
            binder.read(IntegerData.class, timeout);
            binder.setNotification(IntegerData.class, true, timeout);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    sb.setEnabled(true);
                }
            });
            
        }
        
        @Override
        public void onConnDiscovering() {
            Log.v(TAG, "onConnDiscovering()");
        }
    
        @Override
        public void onConnTimeout() {
            Log.w(TAG, "onConnTimeout()");
            finish();
        }
    }
    
    private class CommunicationListener implements ILeCommunicationListener {
        /**
         * informs the listener that a new command (read/write/enable/diable notification) was
         * queued
         */
        @Override
        public void onComCommandQueued() {
            refreshBuf(binder);
        }
        
        /**
         * called when the last command (read/wrirte/setNotification)
         * was sent to the other side
         *
         * @param success whether the process was initiated successfully
         */
        @Override
        public void onComCommandSent(boolean success) {
            Log.v(TAG, "onComCommandSent() success: " + success);
            refreshBuf(binder);
        }
    
        @Override
        public void onComCommandTimeout(LeClientService.QueuedCommand command) {
        
        }
    
        @Override
        public void onComLongNotificationIndicated(Class<? extends LeData> dataClass) {
        
        }
        
        /**
         * called when the client received a Notification with the LeData Object
         *
         * @param leData The object received via Notification
         */
        @Override
        public void onComNotificationReceived(LeData leData) {
            Log.v(TAG, "onComNotificationReceived() dataClass: " + leData.getClass().getSimpleName());
            if (leData.getClass().equals(IntegerData.class)) {
                IntegerData integerData = (IntegerData) leData;
                sb.setProgress(integerData.getVal());
            }
            refreshBuf(binder);
        }
        
        /**
         * called when a read request was successfully sent back to the client
         *
         * @param leData dataObject
         */
        @Override
        public void onComRead(LeData leData) {
            Log.v(TAG, "onComRead() dataClass: " + leData.getClass().getSimpleName());
            if (leData.getClass().equals(IntegerData.class)) {
                IntegerData integerData = (IntegerData) leData;
                sb.setProgress(integerData.getVal());
            }
            refreshBuf(binder);
        }
        
        /**
         * called when the last write transaction was finished
         *
         * @param leData the LeData which was sent to the server
         */
        @Override
        public void onComWrite(LeData leData) {
            Log.v(TAG, "onComWrite() dataClass: " + leData.getClass().getSimpleName());
            refreshBuf(binder);
        }
    }
    
    private class PeriodicNotification extends TimerTask {
        protected final String TAG = this.getClass().getSimpleName();
        private final int step;
        private final int max;
        private final int min;
        private int act;
        private boolean down = false;
        
        public PeriodicNotification(int start, int min, int max, int step) {
            this.act = start;
            this.min = min;
            this.max = max;
            this.step = step;
        }
        
        /**
         * The action to be performed by this timer task.
         */
        @Override
        public void run() {
            int n = 0;
            if (down) {
                n = act - step;
                if (n < min) {
                    n = min;
                    down = false;
                }
            } else {
                n = act + step;
                if (n > max) {
                    n = max;
                    down = true;
                }
            }
            Log.v(TAG, "periodic change " + n);
            overRideFromUser = true;
            sb.setProgress(n);
            act = n;
        }
    }
}
