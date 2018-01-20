package org.die_fabrik.ledemo.client.ui;

import android.Manifest;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import org.die_fabrik.ledemo.client.ClientService;
import org.die_fabrik.lelib.LeUtil;
import org.die_fabrik.lelib.client.ILeScanListener;
import org.die_fabrik.lelib.client.LeClientListeners;
import org.die_fabrik.lelib.client.LeClientService;
import org.die_fabrik.library.R;

import java.util.ArrayList;
import java.util.List;

import static android.support.v4.content.PermissionChecker.PERMISSION_DENIED;

public class ScanActivity extends AppCompatActivity {
    static final String DEVICE_ADDRESS = "DEVICE_ADDRESS";
    private static final int MY_PERMISSIONS_REQUEST = 1024;
    private final String TAG = this.getClass().getSimpleName();
    private final List<String> devices = new ArrayList<>();
    private ServiceConnection serviceConnection;
    private LeClientService.LeClientServiceBinder binder;
    private ScanListener scanListener;
    private ListView deviceList;
    private SwipeRefreshLayout srl;
    private ArrayAdapter<String> listAdapter;
    private Intent serviceIntent;
    
    /**
     * Called after {@link #onStop} when the current activity is being
     * re-displayed to the user (the user has navigated back to it).  It will
     * be followed by {@link #onStart} and then {@link #onResume}.
     * <p>
     * <p>For activities that are using raw {@link Cursor} objects (instead of
     * creating them through
     * {@link #managedQuery(Uri, String[], String, String[], String)},
     * this is usually the place
     * where the cursor should be requeried (because you had deactivated it in
     * {@link #onStop}.
     * <p>
     * <p><em>Derived classes must call through to the super class's
     * implementation of this method.  If they do not, an exception will be
     * thrown.</em></p>
     *
     * @see #onStop
     * @see #onStart
     * @see #onResume
     */
    @Override
    protected void onRestart() {
        super.onRestart();
        startScan();
    }
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LeClientListeners.clearAllIsteners();
        
        setContentView(R.layout.activity_scan);
        getSupportActionBar().setSubtitle("Scan devices");
        deviceList = (ListView) findViewById(R.id.device_lv);
        srl = (SwipeRefreshLayout) findViewById(R.id.device_srl);
        listAdapter = new ArrayAdapter<String>(this, R.layout.device_entry, R.id.device_tv, devices);
        deviceList.setAdapter(listAdapter);
        deviceList.setOnItemClickListener(new OnDeviceClickListener());
        srl.setOnRefreshListener(new SwipeRefreshListener());
        scanListener = new ScanListener();
        LeClientListeners.registerListener(scanListener);
        serviceConnection = new ServiceConnection();
        serviceIntent = new Intent(this, ClientService.class);
        
        int permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION);
        if (permissionCheck == PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, MY_PERMISSIONS_REQUEST);
        } else {
            bindService(serviceIntent, serviceConnection, BIND_AUTO_CREATE);
        }
    }
    
    /**
     * Callback for the result from requesting permissions. This method
     * is invoked for every call on {@link #requestPermissions(String[], int)}.
     * <p>
     * <strong>Note:</strong> It is possible that the permissions request interaction
     * with the user is interrupted. In this case you will receive empty permissions
     * and results arrays which should be treated as a cancellation.
     * </p>
     *
     * @param requestCode  The request code passed in {@link #requestPermissions(String[], int)}.
     * @param permissions  The requested permissions. Never null.
     * @param grantResults The grant results for the corresponding permissions
     *                     which is either {@link PackageManager#PERMISSION_GRANTED}
     *                     or {@link PackageManager#PERMISSION_DENIED}. Never null.
     * @see #requestPermissions(String[], int)
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST:
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    bindService(serviceIntent, serviceConnection, BIND_AUTO_CREATE);
                } else {
                    Log.e(TAG, "permission not granted");
                    this.finish();
                }
                break;
        }
    }
    
    @Override
    protected void onDestroy() {
        Log.v(TAG, "onDestroy()");
        super.onDestroy();
        LeClientListeners.unregisterListener(scanListener);
        unbindService(serviceConnection);
    }
    
    private void startScan() {
        Log.v(TAG, "startScan()");
        devices.clear();
        listAdapter.notifyDataSetChanged();
        srl.setRefreshing(true);
        binder.startScan(0, ScanSettings.SCAN_MODE_LOW_LATENCY, 5000, true);
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
            startScan();
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
    
    private class ScanListener implements ILeScanListener {
        @Override
        public void onScanBatchResults(List<ScanResult> results) {
            Log.v(TAG, "onScanBatchResults() with : " + results.size() + " results");
        }
        
        @Override
        public void onScanFailed(int errorCode) {
            Log.v(TAG, "onScanFailed() with errorCode: " + errorCode);
        }
        
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            Log.v(TAG, "onScanResult() with callbackType: " + LeUtil.getScanCallbackType(ScanActivity.this, callbackType) + ", device: " + result.getDevice().getAddress());
            String s = result.getDevice().getAddress();
            if (!devices.contains(s)) {
                devices.add(s);
            }
            listAdapter.notifyDataSetChanged();
        }
        
        @Override
        public void onScanStarted(long timeout) {
            Log.v(TAG, "onScanStarted() with timeout: " + timeout);
        }
        
        @Override
        public void onScanStopped(List<ScanResult> scanResults) {
            Log.v(TAG, "onScanStopped()");
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    srl.setRefreshing(false);
                }
            });
            
        }
        
        @Override
        public void onScanTimeout() {
            Log.v(TAG, "onScanTimeout()");
        }
    }
    
    private class SwipeRefreshListener implements SwipeRefreshLayout.OnRefreshListener {
        /**
         * Called when a swipe gesture triggers a refresh.
         */
        @Override
        public void onRefresh() {
            Log.v(TAG, "onRefresh()");
            if (binder != null) {
                startScan();
            }
            
        }
    }
    
    private class OnDeviceClickListener implements AdapterView.OnItemClickListener {
        
        
        /**
         * Callback method to be invoked when an item in this AdapterView has
         * been clicked.
         * <p>
         * Implementers can call getItemAtPosition(position) if they need
         * to access the data associated with the selected item.
         *
         * @param parent   The AdapterView where the click happened.
         * @param view     The view within the AdapterView that was clicked (this
         *                 will be a view provided by the adapter)
         * @param position The position of the view in the adapter.
         * @param id       The row id of the item that was clicked.
         */
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            Log.v(TAG, "onItemClick() position: " + position + ", id: " + id);
            String adr = devices.get(position);
            Intent intent = new Intent(ScanActivity.this, DeviceActivity.class);
            intent.putExtra(DEVICE_ADDRESS, adr);
            startActivity(intent);
        }
    }
}
