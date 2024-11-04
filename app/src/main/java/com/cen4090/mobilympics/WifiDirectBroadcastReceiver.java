package com.cen4090.mobilympics;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.NetworkInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pManager;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;

import com.cen4090.mobilympics.MainActivity;

import java.util.Collection;

public class WiFiDirectBroadcastReceiver extends BroadcastReceiver {
    private WifiP2pManager manager;
    private WifiP2pManager.Channel channel;
    private MainActivity activity;

    public WiFiDirectBroadcastReceiver(WifiP2pManager manager, WifiP2pManager.Channel channel,
                                       MainActivity activity) {
        this.manager = manager;
        this.channel = channel;
        this.activity = activity;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (action != null) {
            switch (action) {
                case WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION:
                    int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);
                    if (state == WifiP2pManager.WIFI_P2P_STATE_ENABLED) {
                        // Wifi P2P is enabled
                    } else {
                        // Wifi P2P is not enabled
                    }
                    break;

                case WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION:
                    if (ActivityCompat.checkSelfPermission(this.activity.getBaseContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(this.activity.getBaseContext(), android.Manifest.permission.NEARBY_WIFI_DEVICES) != PackageManager.PERMISSION_GRANTED) {
                        // TODO: Consider calling
                        //    ActivityCompat#requestPermissions
                        // here to request the missing permissions, and then overriding
                        //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                        //                                          int[] grantResults)
                        // to handle the case where the user grants the permission. See the documentation
                        // for ActivityCompat#requestPermissions for more details.
                        return;
                    }
                    manager.requestPeers(channel, peers -> showPeerDialog(peers.getDeviceList()));
                    break;

                case WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION:
                    NetworkInfo networkInfo = intent.getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);
                    if (networkInfo != null && networkInfo.isConnected()) {
                        manager.requestConnectionInfo(channel, info -> {
                            if (info.groupFormed) {
                                Intent gameIntent = new Intent(activity, TicTacToeActivity.class);
                                gameIntent.putExtra("IS_HOST", info.isGroupOwner);
                                activity.startActivity(gameIntent);
                            }
                        });
                    }
                    break;
            }
        }
    }

    private void showPeerDialog(Collection<WifiP2pDevice> devices) {
        String[] deviceNames = devices.stream()
                .map(device -> device.deviceName)
                .toArray(String[]::new);

        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle("Select Opponent")
                .setItems(deviceNames, (dialog, which) -> {
                    WifiP2pDevice device = (WifiP2pDevice) devices.toArray()[which];
                    connectToPeer(device);
                })
                .show();
    }

    private void connectToPeer(WifiP2pDevice device) {
        WifiP2pConfig config = new WifiP2pConfig();
        config.deviceAddress = device.deviceAddress;

        if (ActivityCompat.checkSelfPermission(this.activity.getBaseContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(this.activity.getBaseContext(), android.Manifest.permission.NEARBY_WIFI_DEVICES) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        manager.connect(channel, config, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                // Wait for WIFI_P2P_CONNECTION_CHANGED_ACTION
            }

            @Override
            public void onFailure(int reason) {
                Toast.makeText(activity, "Connection failed", Toast.LENGTH_SHORT).show();
            }
        });
    }
}