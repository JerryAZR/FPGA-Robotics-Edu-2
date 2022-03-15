package com.example.mycontroller;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.collection.ArraySet;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class MyBluetooth {

    public static final int BLUETOOTH_OK = 0;
    public static final int BLUETOOTH_NOT_AVAILABLE = 1;
    public static final int BLUETOOTH_NO_PERMISSION = 2;
    public static final int BLUETOOTH_DISABLED = 3;
    public static final int UNKNOWN_ERROR = -1;

    public static final int STATE_DISCONNECTED = 0;
    public static final int STATE_CONNECTING = 1;
    public static final int STATE_CONNECTED = 2;

    public static final String ERROR_TAG = "MY_ERROR";
    public static final String INFO_TAG = "MY_INFO";

    public static final UUID MY_UUID = UUID.fromString("0001101-0000-1000-8000-00805F9B34FB");

    BluetoothManager mBluetoothManager = null;
    BluetoothAdapter mBluetoothAdapter = null;

    private ConnectThread mConnectThread = null;

    public int bluetoothState;
    public BluetoothDevice connectedDevice = null;
    public BluetoothSocket activeSocket = null;

    public MyBluetooth() {
        bluetoothState = STATE_DISCONNECTED;
    }

    public void setManager(Context ctx) {
        if (ctx != null) {
            mBluetoothManager = (BluetoothManager) ctx.getSystemService(
                    Context.BLUETOOTH_SERVICE);
            mBluetoothAdapter = mBluetoothManager.getAdapter();
        }
    }

    public int requestPermission(Context ctx, Activity act) {
        if (ctx == null || act == null) {
            return UNKNOWN_ERROR;
        }

        if (mBluetoothAdapter == null) {
            return BLUETOOTH_NOT_AVAILABLE;
        }

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.S) {
            // Only necessary on recent devices
            int permission1 = ContextCompat.checkSelfPermission(
                    ctx, Manifest.permission.BLUETOOTH_CONNECT);
            int permission2 = ContextCompat.checkSelfPermission(
                    ctx, Manifest.permission.BLUETOOTH_SCAN);
            if (permission1 != PackageManager.PERMISSION_GRANTED ||
                    permission2 != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                        act,
                        new String[]{
                                Manifest.permission.BLUETOOTH_CONNECT,
                                Manifest.permission.BLUETOOTH_SCAN},
                        0);
                return BLUETOOTH_NO_PERMISSION;
            }
        }

        if (!mBluetoothAdapter.isEnabled()) {
            Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            ctx.startActivity(intent);
            return BLUETOOTH_DISABLED;
        }

        return BLUETOOTH_OK;
    }

    public int getStatus(Context ctx) {
        if (mBluetoothAdapter == null) {
            return BLUETOOTH_NOT_AVAILABLE;
        }

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.S) {
            // Only necessary on recent devices
            int permission1 = ContextCompat.checkSelfPermission(
                    ctx, Manifest.permission.BLUETOOTH_CONNECT);
            int permission2 = ContextCompat.checkSelfPermission(
                    ctx, Manifest.permission.BLUETOOTH_SCAN);
            if (permission1 != PackageManager.PERMISSION_GRANTED ||
                    permission2 != PackageManager.PERMISSION_GRANTED) {
                return BLUETOOTH_NO_PERMISSION;
            }
        }

        if (!mBluetoothAdapter.isEnabled()) {
            return BLUETOOTH_DISABLED;
        }

        return BLUETOOTH_OK;
    }

    public Set<BluetoothDevice> getPairedDevices() {
        if (mBluetoothAdapter == null) {
            return new ArraySet<>();
        }

        return mBluetoothAdapter.getBondedDevices();
    }

    public synchronized void connect(Context ctx, BluetoothDevice device) {
        if (bluetoothState == STATE_CONNECTING) {
            // Do nothing if a connection is being established.
            return;
        }
        if (bluetoothState == STATE_CONNECTED && connectedDevice != null) {
            String connected_mac = connectedDevice.getAddress();
            String target_mac = device.getAddress();
            if (connected_mac.equals(target_mac)) {
                Log.i(INFO_TAG, "Already connected");
                return;
            } else {
                // disconnect
                Log.i(INFO_TAG, "Connected to another device. Disconnecting...");
                if (mConnectThread != null) {
                    mConnectThread.close();
                }
            }
        }

        bluetoothState = STATE_CONNECTING;
        // Start the thread to connect with the given device
        mConnectThread = new ConnectThread(ctx, device);
        mConnectThread.start();
    }

    public void connect(Context ctx, String mac) {
        BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(mac);
        connect(ctx, device);
    }

    public void send(int msg) {
        mConnectThread.send(msg);
    }

    private class ConnectThread extends Thread {
        private BluetoothSocket mSocket;
        private final BluetoothDevice mDevice;
        private InputStream inStream;
        private OutputStream outStream;
        private final Context ctx;
        private final AtomicBoolean running = new AtomicBoolean(false);
        private final AtomicInteger previousMsg = new AtomicInteger(0);

        public ConnectThread(Context ctx, BluetoothDevice device) {
            this.mDevice = device;
            this.ctx = ctx;

            // Get a BluetoothSocket for a connection with the given BluetoothDevice
            try {
                mSocket = device.createRfcommSocketToServiceRecord(MY_UUID);
            } catch (IOException e) {
                Log.e(ERROR_TAG, "Failed to get socket for device " + device.getName(), e);
            }
        }

        public void run() {
            String deviceName = mDevice.getName();
            toast(ctx, "Connecting to " + deviceName, Toast.LENGTH_SHORT);

            // Creating new connections to remote Bluetooth devices
            // should not be attempted while device discovery is in progress.
            mBluetoothAdapter.cancelDiscovery();

            // Attempt to connect
            running.set(true);
            try {
                mSocket.connect();
                toast(ctx, "Connected to " + deviceName, Toast.LENGTH_SHORT);
                synchronized (MyBluetooth.this) {
                    bluetoothState = STATE_CONNECTED;
                    connectedDevice = mDevice;
                    activeSocket = mSocket;
                }
            } catch (Exception connect_fail) {
                toast(ctx, "Failed to connect to " + deviceName, Toast.LENGTH_SHORT);
                close();
            }

            try {
                inStream = mSocket.getInputStream();
                outStream = mSocket.getOutputStream();
            } catch (Exception e) {
                Log.e(ERROR_TAG, "Failed to get IO stream", e);
                close();
            }

            while (running.get()) {
                try {
                    Thread.sleep(2000);
                    outStream.write(previousMsg.get());
                } catch (InterruptedException interruptedException) {
                    Log.i(INFO_TAG, "Interrupt during sleep", interruptedException);
                } catch (IOException ioException) {
                    Log.e(ERROR_TAG, "Failed to send cached byte", ioException);
                    close();
                }
            }
        }

        public void send(int msg){
            previousMsg.set(msg);

            try {
                outStream.write(msg);
            } catch (IOException ioException) {
                Log.e(ERROR_TAG, "Failed to send byte", ioException);
                close();
            }
        }

        public void close() {
            try {
                mSocket.close();
                Log.i(INFO_TAG, "Closing socket");
            } catch (Exception close_fail) {
                Log.e(ERROR_TAG, "Failed to close socket", close_fail);
            }
            synchronized (MyBluetooth.this) {
                mConnectThread = null;
                bluetoothState = STATE_DISCONNECTED;
                connectedDevice = null;
                activeSocket = null;
            }
            running.set(false);
        }

        private void toast(Context ctx, String msg, int duration) {
            Handler toastHandler = new Handler(Looper.getMainLooper());
            toastHandler.post(() -> Toast.makeText(ctx, msg, duration).show());
        }
    }

}
