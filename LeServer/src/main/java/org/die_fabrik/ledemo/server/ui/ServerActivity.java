package org.die_fabrik.ledemo.server.ui;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.AdvertiseSettings;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;

import org.die_fabrik.ledemo.le.data.IntegerData;
import org.die_fabrik.ledemo.server.ServerService;
import org.die_fabrik.lelib.LeUtil;
import org.die_fabrik.lelib.data.ILeDataProvider;
import org.die_fabrik.lelib.data.LeData;
import org.die_fabrik.lelib.server.ILeGattListener;
import org.die_fabrik.lelib.server.LeAdvertiserConfig;
import org.die_fabrik.lelib.server.LeServerListeners;
import org.die_fabrik.lelib.server.LeServerService;
import org.die_fabrik.leserver.R;

public class ServerActivity extends AppCompatActivity implements ILeDataProvider {
    /**
     * The logging TAG for this Object
     */
    protected final String TAG = this.getClass().getSimpleName();
    
    private TextView tv;
    private SeekBar sb;
    private LeServerService.LeServerBinder binder;
    private BleServiceConnector serviceConnection;
    private GattListener gattListener;
    
    @Override
    public Class<? extends LeData>[] getDataClasses() {
        return new Class[]{IntegerData.class};
    }
    
    @Override
    public LeData getLeData(Class<? extends LeData> dataClass, BluetoothDevice bluetoothDevice) {
        Log.v(TAG, "LeData requested");
        return new IntegerData(sb.getProgress());
    }
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        tv = (TextView) findViewById(R.id.tv);
        sb = (SeekBar) findViewById(R.id.sb);
        
        
        sb.setOnSeekBarChangeListener(new OnSbChangedListener());
        
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        LeServerListeners.registerDataProvider(this);
        serviceConnection = new BleServiceConnector();
        Intent gattServiceIntent = new Intent(this, ServerService.class);
        bindService(gattServiceIntent, serviceConnection, BIND_AUTO_CREATE);
        gattListener = new GattListener();
        LeServerListeners.registerListener(gattListener);
        
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }
    
    @Override
    protected void onDestroy() {
        unbindService(serviceConnection);
        super.onDestroy();
        
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        
        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }
        
        return super.onOptionsItemSelected(item);
    }
    
    private final class BleServiceConnector implements ServiceConnection {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            binder = (LeServerService.LeServerBinder) service;
            LeAdvertiserConfig cfg = LeAdvertiserConfig.getBuilder()
                    .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY)
                    .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_HIGH)
                    .setBluetoothName("SERVER")
                    .setConnectible(true)
                    .setTimeout(0)
                    .setIncludeDeviceName(true).create();
            
            binder.startAdvertising(cfg);
            binder.startGatt(0);
            
            Log.i(TAG, "Bluetooth LE Services onServiceConnected");
            
            
        }
        
        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            binder = null;
            Log.i(TAG, "Bluetooth LE Services onDisconnected");
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
            Log.v(TAG, "onProgressChanged( progress: " + progress + ", fromUser: " + fromUser);
            IntegerData integerData = new IntegerData(progress);
            binder.sendNotification(integerData);
            
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
    
    private class GattListener implements ILeGattListener {
        @Override
        public void onGattConnected(BluetoothDevice device) {
        
        }
        
        @Override
        public void onGattDisconnected(BluetoothDevice device) {
        
        }
        
        @Override
        public void onGattNotificationSent() {
        
        }
        
        @Override
        public void onGattWritten(LeData leData, BluetoothDevice device) {
            IntegerData integerData = (IntegerData) leData;
            Log.v(TAG, "onWritten with IntegerData: " + integerData.getVal());
            LeUtil.logHexValue(integerData.getLeValue(), TAG);
            sb.setProgress(integerData.getVal());
        }
        
        @Override
        public void onGattWrittenFailure(LeData leData, BluetoothDevice device) {
        
        }
    }
}
