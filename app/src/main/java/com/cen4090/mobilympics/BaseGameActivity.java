package com.cen4090.mobilympics;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;

import androidx.appcompat.app.AppCompatActivity;

public abstract class BaseGameActivity extends AppCompatActivity {

    protected WiFiDirectService wifiService;
    private boolean serviceBound = false;

    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            WiFiDirectService.LocalBinder binder = (WiFiDirectService.LocalBinder) service;
            wifiService = binder.getService();
            serviceBound = true;
            wifiService.setDataCallback(data -> onGameDataReceived(data));
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            serviceBound = false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = new Intent(this, WiFiDirectService.class);
        bindService(intent, connection, BIND_AUTO_CREATE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (serviceBound) {
            unbindService(connection);
        }
    }

    protected abstract void onGameDataReceived(String data);
}