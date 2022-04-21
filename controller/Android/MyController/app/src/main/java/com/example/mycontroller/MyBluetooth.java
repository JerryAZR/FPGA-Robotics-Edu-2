/**
 * This file is part of the TI-RSLK Bluetooth RC Car Controller Android
 * App, available at https://github.com/JerryAZR/FPGA-Robotics-Edu-2
 *
 * Copyright (C) 2022 Zerui An <anzerui@126.com / jerryazr@gmail.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.example.mycontroller;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.BluetoothSocket;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import androidx.collection.ArraySet;
import androidx.core.content.ContextCompat;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class MyBluetooth {

    public static final int BLUETOOTH_OK = 0;
    public static final int BLUETOOTH_NOT_AVAILABLE = 1;
    public static final int BLUETOOTH_NO_CONNECT = 2;
    public static final int BLUETOOTH_NO_SCAN = 3;
    public static final int BLUETOOTH_DISABLED = 4;
    public static final int UNKNOWN_ERROR = -1;

    public static final int STATE_DISCONNECTED = BluetoothProfile.STATE_DISCONNECTED;
    public static final int STATE_CONNECTING = BluetoothProfile.STATE_CONNECTING;
    public static final int STATE_CONNECTED = BluetoothProfile.STATE_CONNECTED;

    public static final String ERROR_TAG = "MY_ERROR";
    public static final String INFO_TAG = "MY_INFO";

    private static final long BLE_SCAN_PERIOD = 4000;

    public static final UUID MY_UUID = UUID.fromString("0001101-0000-1000-8000-00805F9B34FB");
    public static final UUID BLE_SERV_UUID = UUID.fromString("0000FFE0-0000-1000-8000-00805F9B34FB");
    public static final UUID BLE_CHAR_UUID = UUID.fromString("0000FFE1-0000-1000-8000-00805F9B34FB");
    public volatile int bluetoothState; // volatile just to be safe
    private BluetoothLeScanner leScanner;
    private PackageManager mPackageManager;
    public BluetoothDevice connectedDevice;
    public BluetoothSocket activeSocket;
    private BluetoothManager mBluetoothManager;
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothGatt mBluetoothGATT;
    private BluetoothGattCharacteristic mBluetoothGattCharacteristic;
    private ConnectThread mConnectThread;
    private BLEThread mBLEThread;
    private ScanCallback lastScanCallback;
    private boolean isBLE = false;

    public MyBluetooth() {
        bluetoothState = STATE_DISCONNECTED;
    }

    public void setManager(Context ctx) {
        if (ctx != null) {
            mBluetoothManager = (BluetoothManager) ctx.getSystemService(
                    Context.BLUETOOTH_SERVICE);
            mBluetoothAdapter = mBluetoothManager.getAdapter();
            mPackageManager = ctx.getPackageManager();
        }
    }

    public int getStatus(Context ctx) {
        if (ctx == null) {
            return UNKNOWN_ERROR;
        }

        if (mBluetoothAdapter == null) {
            return BLUETOOTH_NOT_AVAILABLE;
        }
        int permission_code;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            // Only necessary on recent devices
            permission_code = ContextCompat.checkSelfPermission(
                    ctx, Manifest.permission.BLUETOOTH_CONNECT);
            if (permission_code != PackageManager.PERMISSION_GRANTED) {
                return BLUETOOTH_NO_CONNECT;
            }
            permission_code = ContextCompat.checkSelfPermission(
                    ctx, Manifest.permission.BLUETOOTH_SCAN);
            if (permission_code != PackageManager.PERMISSION_GRANTED) {
                return BLUETOOTH_NO_SCAN;
            }
        }

        if (!mBluetoothAdapter.isEnabled()) {
            Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            ctx.startActivity(intent);
            return BLUETOOTH_DISABLED;
        }

        return BLUETOOTH_OK;
    }

    public Set<BluetoothDevice> getPairedDevices() {
        if (mBluetoothAdapter == null) {
            return new ArraySet<>();
        }

        try {
            return mBluetoothAdapter.getBondedDevices();
        } catch (SecurityException se) {
            return new ArraySet<>();
        }
    }

    public void getBLEDevices(ScanCallback callback) {
        if (mBluetoothAdapter == null) {
            return;
        }
        if (mPackageManager.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            leScanner = mBluetoothAdapter.getBluetoothLeScanner();
        }
        if (leScanner == null) {
            return;
        }
        lastScanCallback = callback;
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                ;// Log.i(INFO_TAG, "Scan period exceeded, stopping...");
                leScanner.stopScan(callback);
            }
        }, BLE_SCAN_PERIOD);
        leScanner.startScan(callback);
    }

    public void stopBLEScan() {
        if (leScanner == null || lastScanCallback == null) {
            return;
        }
        leScanner.stopScan(lastScanCallback);
    }

    public synchronized void connect(Context ctx, BluetoothDevice device) {
        if (bluetoothState == STATE_CONNECTING) {
            // Do nothing if a connection is being established.
            return;
        }

        bluetoothState = STATE_CONNECTING;
        // Start the thread to connect with the given device
        if (mConnectThread != null) {
            mConnectThread.close();
        }
        mConnectThread = new ConnectThread(ctx, device);
        isBLE = false; // connecting to classic device
        mConnectThread.start();
    }

    public void connect(Context ctx, String mac) {
        BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(mac);
        stopBLEScan();
        if (conditionalDisconnect(device)) {
            connect(ctx, device);
        }
    }

    public void connectBLE(Context ctx, String mac) {
        BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(mac);
        stopBLEScan();
        if (conditionalDisconnect(device)) {
            connectBLE(ctx, device);
        }
    }

    public synchronized void connectBLE(Context ctx, BluetoothDevice device) {
        if (bluetoothState == STATE_CONNECTING) {
            // Do nothing if a connection is being established.
            return;
        }
        Toast.makeText(ctx,
                "Connecting to " + device.getName(),
                Toast.LENGTH_SHORT).show();

        isBLE = true; // connecting to BLE device

        BluetoothGattCallback bluetoothGattCallback = new BluetoothGattCallback() {
            @Override
            public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
                super.onConnectionStateChange(gatt, status, newState);
                synchronized (MyBluetooth.this) {
                    bluetoothState = newState;
                }
                if (newState == STATE_CONNECTED) {
                    ;// Log.i(INFO_TAG, "Connected");
                    toast(ctx, "Connected to " + device.getName(),
                            Toast.LENGTH_SHORT);
                    connectedDevice = gatt.getDevice();
                    gatt.discoverServices();
                } else if (newState == STATE_DISCONNECTED) {
                    ;// Log.i(INFO_TAG, "Disconnected");
                    toast(ctx, gatt.getDevice().getName() + " disconnected",
                            Toast.LENGTH_SHORT);
                    gatt.close();
                    // No need to close the thread here because it will detect the change
                }
            }

            @Override
            public void onServicesDiscovered(BluetoothGatt gatt, int status) {
                super.onServicesDiscovered(gatt, status);
                List<BluetoothGattService> services = gatt.getServices();
                ;// Log.i(INFO_TAG, "onServicesDiscovered callback");
                for (BluetoothGattService service : services) {
                    ;// Log.i(INFO_TAG, "Service: " + service.getUuid().toString());
                }
                BluetoothGattService service = gatt.getService(BLE_SERV_UUID);
                List<BluetoothGattCharacteristic> charList =
                        service.getCharacteristics();
                for (BluetoothGattCharacteristic characteristic : charList) {
                    ;// Log.i(INFO_TAG, "Char: " + characteristic.getUuid().toString());
                }
                mBluetoothGattCharacteristic = service.getCharacteristic(BLE_CHAR_UUID);
                mBLEThread.setGatt(gatt);
                mBLEThread.setCharacteristic(mBluetoothGattCharacteristic);
            }

            @Override
            public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
                super.onCharacteristicWrite(gatt, characteristic, status);
                ;// Log.i(INFO_TAG, "onCharacteristicWrite status: " + status);
            }
        };

        try {
            mBluetoothGATT = device.connectGatt(ctx, false, bluetoothGattCallback);
        } catch (Exception e) {
            ;// Log.e(ERROR_TAG, "Failed to connect to BLE device " + device.getName(), e);
            return;
        }
        if (mBLEThread != null) {
            mBLEThread.close();
        }
        mBLEThread = new BLEThread();
        mBLEThread.start();
    }

    public void disconnect() {
        conditionalDisconnect(null);
    }

    public synchronized boolean conditionalDisconnect(BluetoothDevice device) {
        if (bluetoothState == STATE_CONNECTED && connectedDevice != null) {
            String connected_mac = connectedDevice.getAddress();
            String target_mac;
            if (device == null) {
                target_mac = null;
            } else {
                target_mac = device.getAddress();
            }
            if (connected_mac.equals(target_mac)) {
                ;// Log.i(INFO_TAG, "Already connected");
                return false;
            } else {
                // disconnect
                ;// Log.i(INFO_TAG, "Connected to another device. Disconnecting...");
                if (mConnectThread != null) {
                    mConnectThread.close();
                }
                // Also check ble state
                if (mBLEThread != null) {
                    mBLEThread.close();
                }
            }
        } else {
            ;// Log.i(INFO_TAG, "Nothing to disconnect");
        }
        return true; // can proceed with connection
    }

    public void send(int msg) {
        if (bluetoothState == STATE_CONNECTED) {
            if (isBLE) {
                // send msg to BLE device
                mBLEThread.send(msg);
            } else {
                // send msg to classic device
                mConnectThread.send(msg);
            }
        }
    }

    public void send(byte[] msg) {
        if (bluetoothState == STATE_CONNECTED) {
            if (isBLE) {
                // send msg to BLE device
                mBLEThread.send(msg);
            } else {
                // send msg to classic device
                mConnectThread.send(msg);
            }
        }
    }

    private static String timestamp() {
        Long time = System.currentTimeMillis();
        return time.toString();
    }

    public boolean ble_available() {
        return mPackageManager.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE);
    }

    private void toast(Context ctx, String msg, int duration) {
        Handler toastHandler = new Handler(Looper.getMainLooper());
        toastHandler.post(() -> Toast.makeText(ctx, msg, duration).show());
    }

    private class ConnectThread extends Thread {
        private final BluetoothDevice mDevice;
        private final Context ctx;
        private final AtomicBoolean running = new AtomicBoolean(false);
        private final AtomicInteger previousMsg = new AtomicInteger(0);
        private BluetoothSocket mSocket;
        private InputStream inStream;
        private OutputStream outStream;

        public ConnectThread(Context ctx, BluetoothDevice device) {
            this.mDevice = device;
            this.ctx = ctx;

            // Get a BluetoothSocket for a connection with the given BluetoothDevice
            try {
                mSocket = device.createRfcommSocketToServiceRecord(MY_UUID);
            } catch (IOException e) {
                ;// Log.e(ERROR_TAG, "Failed to get socket for device " + device.getName(), e);
            }
        }

        public void run() {
            String deviceName = mDevice.getName();
            toast(ctx, "Connecting to " + deviceName, Toast.LENGTH_SHORT);

            int scan_permission = PackageManager.PERMISSION_GRANTED;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                // Only necessary on recent devices
                scan_permission = ContextCompat.checkSelfPermission(
                        ctx,
                        Manifest.permission.BLUETOOTH_SCAN
                );
            }

            if (scan_permission == PackageManager.PERMISSION_GRANTED) {
                // Creating new connections to remote Bluetooth devices
                // should not be attempted while device discovery is in progress.
                mBluetoothAdapter.cancelDiscovery();
            } else {
                // That being said, we don't HAVE to call cancelDiscovery.
                // This is just for better performance.
                // So it's fine if we don't have the permission to do so.
                ;// Log.i(INFO_TAG, "Can't cancel discovery: permission denied.");
            }

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
                ;// Log.e(ERROR_TAG, "Failed to get IO stream", e);
                close();
            }

            while (running.get()) {
                try {
                    Thread.sleep(2000);
                    int cached = previousMsg.get();
                    outStream.write(cached);
                    ;// Log.i(INFO_TAG, timestamp() + ": Sending cached byte " + cached);
                } catch (InterruptedException interruptedException) {
                    ;// Log.i(INFO_TAG, "Interrupt during sleep", interruptedException);
                } catch (IOException ioException) {
                    ;// Log.e(ERROR_TAG, "Failed to send cached byte", ioException);
                    close();
                }
            }
        }

        public void send(int msg) {
            previousMsg.set(msg);

            if (mSocket.isConnected()) {
                try {
                    outStream.write(msg);
                    ;// Log.i(INFO_TAG, "Sending byte " + (msg & 0xFF) + " to " + mDevice.getName());
                } catch (IOException ioException) {
                    ;// Log.e(ERROR_TAG, "Failed to send byte", ioException);
                    close();
                }
            } else {
                ;// Log.i(INFO_TAG, "Socket already closed.");
                close();
            }
        }

        public void send(byte[] msg) {
            previousMsg.set(msg[0]);

            if (mSocket.isConnected()) {
                try {
                    outStream.write(msg);
                    ;// Log.i(INFO_TAG, "Sending bytes to " + mDevice.getName());
                } catch (IOException ioException) {
                    ;// Log.e(ERROR_TAG, "Failed to send bytes", ioException);
                    close();
                }
            } else {
                ;// Log.i(INFO_TAG, "Socket already closed.");
                close();
            }
        }

        public void close() {
            if (!running.get()) return; // Already closed
            try {
                mSocket.close();
                ;// Log.i(INFO_TAG, "Closing socket");
            } catch (Exception close_fail) {
                ;// Log.e(ERROR_TAG, "Failed to close socket", close_fail);
            }
            synchronized (MyBluetooth.this) {
                mConnectThread = null;
                bluetoothState = STATE_DISCONNECTED;
                connectedDevice = null;
                activeSocket = null;
            }
            running.set(false);
        }
    }

    private class BLEThread extends Thread {
        private final AtomicBoolean running;
        private final AtomicInteger previousMsg;
        private final AtomicBoolean charReady;

        private BluetoothGattCharacteristic characteristic;
        private BluetoothGatt gatt;

        public BLEThread() {
            running = new AtomicBoolean(false);
            previousMsg = new AtomicInteger(0);
            charReady = new AtomicBoolean(false);
        }

        public void setCharacteristic(BluetoothGattCharacteristic characteristic) {
            this.characteristic = characteristic;
            charReady.set(characteristic != null);
        }

        public void setGatt(BluetoothGatt gatt) {
            this.gatt = gatt;
        }

        @Override
        public void run() {
            running.set(true);
            while (running.get()) {
                try {
                    Thread.sleep(2000);
                    if (!charReady.get()) continue;
                    if (mBluetoothManager.getConnectionState(gatt.getDevice(),
                            BluetoothProfile.GATT_SERVER) == STATE_DISCONNECTED) {
                        ;// Log.i(INFO_TAG, "Device not connected");
                        break;
                    }
                    int cached = previousMsg.get();
                    ;// Log.i(INFO_TAG, timestamp() + ": BLE sending cached byte " + cached);
                    quickSend(cached);
                } catch (InterruptedException ie) {
                    ;// Log.i(INFO_TAG, "Interrupt during sleep", ie);
                } catch (Exception e) {
                    ;// Log.e(ERROR_TAG, "Failed to send cached byte", e);
                    break;
                }
            }
            close();
        }

        public void send(int msg) {
            previousMsg.set(msg);
            if (characteristic == null) {
                // Device is not ready. Don't send
                return;
            }
            try {
                quickSend(msg);
            } catch (Exception e) {
                ;// Log.e(ERROR_TAG, "Cannot send byte");
                close();
            }
        }

        public void send(byte[] msg) {
            previousMsg.set(msg[0]);
            if (characteristic == null) {
                // Device is not ready. Don't send
                return;
            }
            try {
                quickSend(msg);
            } catch (Exception e) {
                ;// Log.e(ERROR_TAG, "Cannot send bytes");
                close();
            }
        }

        private void quickSend(int msg) {
            characteristic.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE);
            byte[] bytearray = new byte[]{(byte) (msg & 0xFF)};
            characteristic.setValue(bytearray);
            if (!gatt.writeCharacteristic(characteristic)) {
                ;// Log.i(INFO_TAG, "Transmission failed");
            }
        }

        private void quickSend(byte[] msg) {
            characteristic.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE);
            characteristic.setValue(msg);
            if (!gatt.writeCharacteristic(characteristic)) {
                ;// Log.i(INFO_TAG, "Transmission failed");
            }
        }

        public void close() {
            if (!running.get()) return; // Already closed
            synchronized (MyBluetooth.this) {
                mBLEThread = null;
                bluetoothState = STATE_DISCONNECTED;
                connectedDevice = null;
                mBluetoothGattCharacteristic = null;
                mBluetoothGATT.close();
            }
            running.set(false);
        }
    }
}
