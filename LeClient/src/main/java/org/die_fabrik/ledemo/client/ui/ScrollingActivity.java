package org.die_fabrik.ledemo.client.ui;

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
import android.view.View;

import org.die_fabrik.ledemo.client.ClientService;
import org.die_fabrik.lelib.client.LeClientService;
import org.die_fabrik.library.R;

public class ScrollingActivity extends AppCompatActivity {
    /**
     * The logging TAG for this Object
     */
    protected final String TAG = this.getClass().getSimpleName();
    
    private LeClientService.LeClientServiceBinder binder;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scrolling);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        BleServiceConnector serviceConnection = new BleServiceConnector();
        Intent gattServiceIntent = new Intent(this, ClientService.class);
        bindService(gattServiceIntent, serviceConnection, BIND_AUTO_CREATE);
        
    }
    
    private final class BleServiceConnector implements ServiceConnection {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            binder = (LeClientService.LeClientServiceBinder) service;
            Log.i(TAG, "Bluetooth LE Services onServiceConnected");
            
            
        }
        
        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            binder = null;
            Log.i(TAG, "Bluetooth LE Services onDisconnected");
        }
    }
}
