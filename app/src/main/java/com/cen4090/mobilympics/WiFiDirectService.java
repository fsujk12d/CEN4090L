package com.cen4090.mobilympics;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class WiFiDirectService extends Service {
    private WifiP2pManager manager;
    private WifiP2pManager.Channel channel;
    private ServerClass serverClass;
    private ClientClass clientClass;
    private boolean isHost;
    private Socket socket;

    public WiFiDirectService() {
    }

    public class LocalBinder extends Binder {
        public WiFiDirectService getService() {
            return WiFiDirectService.this;
        }
    }

    private final IBinder binder = new LocalBinder();

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        manager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        channel = manager.initialize(this, getMainLooper(), null);
    }

    public void sendData(String data){
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            if (isHost && serverClass != null){
                serverClass.write(data.getBytes());
            } else if (!isHost && clientClass != null){
                clientClass.write(data.getBytes());
            }
        });
    }

    public void setupAsHost() {
        isHost = true;
        serverClass = new ServerClass();
        serverClass.start();
    }

    public void setupAsClient(InetAddress hostAddress){
        isHost = false;
        clientClass = new ClientClass(hostAddress);
        clientClass.start();
    }

    public interface DataCallBack{
        void onDataReceived(String data);
    }

    private DataCallBack dataCallBack;

    public void setDataCallback(DataCallBack callBack){
        dataCallBack = callBack;
    }

    private class ServerClass extends Thread {
        ServerSocket serverSocket;
        InputStream inStream;
        OutputStream outStream;

        public void write(byte[] bytes){
            try {
                outStream.write(bytes);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void run() {
            try{
                serverSocket = new ServerSocket(8888);
                socket = serverSocket.accept();
                inStream = socket.getInputStream();
                outStream = socket.getOutputStream();
            }catch (IOException e){
                e.printStackTrace();
            }

            ExecutorService executor = Executors.newSingleThreadExecutor();
            Handler handler = new Handler(Looper.getMainLooper());

            executor.execute(() -> {
                byte[] buffer = new byte[1024];
                int bytes;

                while (socket != null) {
                    try {
                        bytes = inStream.read(buffer);
                        if (bytes > 0) {
                            final String receivedData = new String(buffer, 0, bytes);
                            handler.post(() -> {
                                if (dataCallBack != null) {
                                    dataCallBack.onDataReceived(receivedData);
                                }
                            });
                        }
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            });
        }

    }

    private class ClientClass extends Thread{
        String hostAdd;
        private InputStream inStream;
        private OutputStream outStream;

        public void write(byte[] bytes){
            try {
                outStream.write(bytes);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public ClientClass(InetAddress hostAddress) {
            hostAdd = hostAddress.getHostAddress();
            socket = new Socket();
        }

        @Override
        public void run() {
            try{
                socket.connect(new InetSocketAddress(hostAdd, 8888), 500);
                inStream = socket.getInputStream();
                outStream = socket.getOutputStream();

            } catch (IOException e) {
                e.printStackTrace();
            }

            ExecutorService executor = Executors.newSingleThreadExecutor();
            Handler handler = new Handler(Looper.getMainLooper());

            executor.execute(() -> {
                byte[] buffer = new byte[1024];
                int bytes;

                while (socket != null) {
                    try {
                        bytes = inStream.read(buffer);
                        if (bytes > 0) {
                            final String receivedData = new String(buffer, 0, bytes);
                            handler.post(() -> {
                                if (dataCallBack != null) {
                                    dataCallBack.onDataReceived(receivedData);
                                }
                            });
                        }
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            });
        }
    }

    public void disconnect() {
        if (socket != null){
            try{
                socket.close();
                socket = null;
            } catch (IOException e){
                e.printStackTrace();
            }
        }
        if (serverClass != null){
            serverClass.interrupt();
            serverClass = null;
        }
        if (clientClass != null){
            clientClass.interrupt();
            clientClass = null;
        }
    }
}