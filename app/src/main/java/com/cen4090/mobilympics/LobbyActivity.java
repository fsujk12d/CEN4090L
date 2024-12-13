package com.cen4090.mobilympics;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pManager.Channel;
import android.net.wifi.p2p.WifiP2pManager.PeerListListener;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

/* Lobby Activity - This Activity will show a list of Players, the selected game,
 * some game options if applicable, a Discover button to find other players, etc */

public class LobbyActivity extends BaseGameActivity implements GameSelectDialog.OnGameSelectedListener{
    private Button discoverButton;
    private ListView listView;
    private Button disconnectButton;
    private Button selectGameButton;
    private Button startGameButton;
    private String selectedGame = null;
    private boolean isHost = false;

    private WifiP2pManager manager;
    private WifiP2pManager.Channel channel;
    private BroadcastReceiver receiver;
    private IntentFilter intentFilter;

    private List<WifiP2pDevice> peers = new ArrayList<>();
    private String[] deviceNameArray;
    private WifiP2pDevice[] deviceArray;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_lobby);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        /*-- Initialize UI Components --*/
        discoverButton = findViewById(R.id.discover_button);
        disconnectButton = findViewById(R.id.disconnect_button);
        listView = findViewById(R.id.device_list);
        selectGameButton = findViewById(R.id.select_game_button);
        selectGameButton.setEnabled(false);
        startGameButton = findViewById(R.id.start_game_button);
        startGameButton.setEnabled(false);

        /*-- Initialize WifiP2p Components --*/
        manager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        channel = manager.initialize(this, getMainLooper(), null);

        /*-- Set Up Intent Filter --*/
        intentFilter = new IntentFilter();
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);

        /*-- Initialize Broadcast Receiver  --*/
        receiver = new WiFiDirectBroadcastReceiver(manager, channel, this);

        /*-- Set Up Click Listeners --*/
        discoverButton.setOnClickListener(v -> setDiscoverButton());
        disconnectButton.setOnClickListener(v -> setDisconnectButton());
        selectGameButton.setOnClickListener(v -> showGameSelectDialog());
        startGameButton.setEnabled(false);
        startGameButton.setOnClickListener(v -> launchSelectedGame());

        /*-- Set Up Device List Click Listener --*/
        listView.setOnItemClickListener((parent, view, position, id) -> {
            connectToDevice(deviceArray[position]);
        });
    }

    @SuppressLint("MissingPermission")
    private void setDiscoverButton(){
        if (checkPermissions()) {
            manager.discoverPeers(channel, new WifiP2pManager.ActionListener() {
                @Override
                public void onSuccess() {
                    Toast.makeText(getApplicationContext(), "Discovery Started", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onFailure(int reason) {
                    Toast.makeText(getApplicationContext(), "Discovery Failed: " + reason, Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void setDisconnectButton(){
        if (wifiService != null){
            wifiService.disconnect();
            if (manager != null && channel != null){
                manager.removeGroup(channel, new WifiP2pManager.ActionListener() {
                    @Override
                    public void onSuccess() {
                        Toast.makeText(getApplicationContext(), "Disconnected", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onFailure(int reason) {
                        Toast.makeText(getApplicationContext(), "Disconnect Failed: " + reason, Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }
    }

    private void showGameSelectDialog(){
        GameSelectDialog dialog = new GameSelectDialog();
        dialog.show(getSupportFragmentManager(), "GameSelectDialog");
    }

    @Override
    public void onGameSelected(String selectedGame) {
        if (!isHost) {
            return;
        }

        this.selectedGame = selectedGame;

        startGameButton.setEnabled(true);
        final String btnText = "Selected: " + this.selectedGame ;
        selectGameButton.setText(btnText);
        startGameButton.setEnabled(true);
    }

    public void launchSelectedGame(){
        if (selectedGame == null){
            Toast.makeText(this, "Please select a game first!", Toast.LENGTH_SHORT).show();
        }

        if (wifiService != null){
            wifiService.sendData("START_GAME:" + selectedGame);
        }

        launchGame(selectedGame);
    }

    @SuppressLint("MissingPermission")
    private void connectToDevice(WifiP2pDevice device){
        WifiP2pConfig config = new WifiP2pConfig();
        config.deviceAddress = device.deviceAddress;

        if (checkPermissions()){
            manager.connect(channel, config, new WifiP2pManager.ActionListener() {
                @Override
                public void onSuccess() {
                    Toast.makeText(getApplicationContext(), "Connected to " + device.deviceName, Toast.LENGTH_SHORT).show();
                }


                @Override
                public void onFailure(int reason) {
                    Toast.makeText(getApplicationContext(), "Connection Failed: " + reason, Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    public WifiP2pManager.ConnectionInfoListener connectionInfoListener = new WifiP2pManager.ConnectionInfoListener() {
        @Override
        public void onConnectionInfoAvailable(WifiP2pInfo info) {
            if (info == null){
                Toast.makeText(getApplicationContext(), "Connection Info is null", Toast.LENGTH_SHORT).show();
                return;
            }

            final InetAddress groupOwnerAddress = info.groupOwnerAddress;

            if (info.groupFormed){
                isHost = info.isGroupOwner;
                if (isHost){
                    runOnUiThread(() -> {
                        selectGameButton.setEnabled(true);
                        Toast.makeText(getApplicationContext(), "You are the host - select a game", Toast.LENGTH_SHORT).show();
                    });
                    if (wifiService != null){
                        wifiService.setupAsHost();
                    }
                } else {
                    if (groupOwnerAddress == null){
                        Toast.makeText(getApplicationContext(), "Group owner address is null", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    runOnUiThread(() -> {
                        Toast.makeText(getApplicationContext(), "You are a client", Toast.LENGTH_SHORT).show();
                    });
                    if (wifiService != null){
                        wifiService.setupAsClient(groupOwnerAddress);
                    }
                }
            }
        }
    };

    public WifiP2pManager.PeerListListener peerListListener = new WifiP2pManager.PeerListListener() {
        @Override
        public void onPeersAvailable(WifiP2pDeviceList peerList) {
            if (!peerList.getDeviceList().equals(peers)) {
                peers.clear();
                peers.addAll(peerList.getDeviceList());

                deviceNameArray = new String[peerList.getDeviceList().size()];
                deviceArray = new WifiP2pDevice[peerList.getDeviceList().size()];

                int index = 0;
                for (WifiP2pDevice device : peerList.getDeviceList()) {
                    deviceNameArray[index] = device.deviceName;
                    deviceArray[index] = device;
                    index++;
                }

                ArrayAdapter<String> adapter = new ArrayAdapter<>(getApplicationContext(),
                        android.R.layout.simple_list_item_1, deviceNameArray);
                listView.setAdapter(adapter);
            }

            if (peers.size() == 0) {
                Toast.makeText(getApplicationContext(), "No Devices Found", Toast.LENGTH_SHORT).show();
            }
        }
    };

    private boolean checkPermissions() {
        // Array of permissions needed
        String[] REQUIRED_PERMISSIONS = new String[] {
                android.Manifest.permission.ACCESS_FINE_LOCATION,
                android.Manifest.permission.NEARBY_WIFI_DEVICES,
                android.Manifest.permission.ACCESS_WIFI_STATE,
                android.Manifest.permission.CHANGE_WIFI_STATE,
                android.Manifest.permission.INTERNET
        };

        // Check if we have all permissions
        boolean allPermissionsGranted = true;
        for (String permission : REQUIRED_PERMISSIONS) {
            if (ActivityCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                allPermissionsGranted = false;
                break;
            }
        }

        // If we don't have all permissions, request them
        if (!allPermissionsGranted) {
            ActivityCompat.requestPermissions(
                    this,
                    REQUIRED_PERMISSIONS,
                    1 // request code - you can use this in onRequestPermissionsResult
            );
            return false;
        }

        return true;
    }

    protected void onGameDataReceived(String data) {

        if (data.startsWith("START_GAME:")) {
            String gameName = data.substring(11);
            launchGame(gameName);
        } else {
            Toast.makeText(this, "Received: " + data, Toast.LENGTH_SHORT).show();
        }
    }

    private void launchGame(String gameName){
        Intent intent = null;

        switch (gameName){
            case "TicTacToe":
                intent = new Intent(this, TicTacToeActivity.class);
                break;
            case "Mancala":
                intent = new Intent(this, MancalaActivity.class);
                break;
            case "Checkers":
                intent = new Intent(this, CheckersActivity.class);
                break;
            case "Chess":
                intent = new Intent(this, ChessActivity.class);
                break;
            case "DotsAndBoxes":
                intent = new Intent(this, DotsNBoxesActivity.class);
                break;
        }

        if (intent != null){
            intent.putExtra("isHost", isHost);
            startActivity(intent);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(receiver, intentFilter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(receiver);
    }


}