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

package com.example.mycontroller.ui.bluetooth;

import android.Manifest;
import android.app.ListActivity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContract;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mycontroller.MainViewModel;
import com.example.mycontroller.MyBluetooth;
import com.example.mycontroller.R;
import com.example.mycontroller.databinding.FragmentBluetoothBinding;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class BluetoothFragment extends Fragment {

    private FragmentBluetoothBinding binding;
    private TextView textView;
    private ListView deviceListView;

    private static final String DEVICE_NAME = "primary";
    private static final String DEVICE_MAC = "secondary";

    private MyBluetooth myBluetooth;
    private SimpleAdapter mSimpleAdapter;
    private SimpleAdapter leDeviceListAdapter;
    private BroadcastReceiver bluetoothReceiver;
    private ArrayList<HashMap<String,String>> deviceList;
    private ArrayList<HashMap<String,String>> leDeviceList;
    private HashSet<BluetoothDevice> leDeviceSet;
    private ActivityResultLauncher<String[]> requestPermissionLauncher;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentBluetoothBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        textView = binding.textHome;
        deviceListView = binding.deviceList;

        binding.refreshBtn.setOnClickListener(v -> generateDeviceList());
        binding.bleBtn.setOnClickListener(v -> generateBLEDeviceList());

        // Get the shared bluetooth object
        MainViewModel sharedVM = new ViewModelProvider(requireActivity()).get(MainViewModel.class);
        myBluetooth = sharedVM.myBluetooth;
        myBluetooth.setManager(getContext());

        generateDeviceList();

        // Set up a broadcast receiver so that the device list is refreshed
        // when bluetooth is turned on/off
        bluetoothReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                final String action = intent.getAction();

                if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
                    int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE,
                            BluetoothAdapter.ERROR);
                    if (state == BluetoothAdapter.STATE_ON ||
                        state == BluetoothAdapter.STATE_OFF) {
                        generateDeviceList();
                    }
                }
            }
        };
        IntentFilter filter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
        getActivity().registerReceiver(bluetoothReceiver, filter);

        // Also refresh the device list on permission grant
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            requestPermissionLauncher = registerForActivityResult(
                    new ActivityResultContracts.RequestMultiplePermissions(),
                    v -> generateDeviceList()
            );
            String[] desiredPermissions = new String[]{
                    Manifest.permission.BLUETOOTH_CONNECT,
                    Manifest.permission.BLUETOOTH_SCAN,
                    Manifest.permission.ACCESS_FINE_LOCATION
            };
            getBluetoothPermissions(desiredPermissions);
        }

        return root;
    }

    private void getBluetoothPermissions(String[] permissions) {
        int return_code;
        ArrayList<String> requests = new ArrayList<>();
        // Check if permission is already granted
        for (String pm : permissions) {
            return_code = ContextCompat.checkSelfPermission(getContext(), pm);
            if (return_code != PackageManager.PERMISSION_GRANTED) {
                requests.add(pm);
            }
        }
        // Request for permission if not already granted
        if (requests.size() > 0) {
            requestPermissionLauncher.launch(requests.toArray(new String[0]));
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        getActivity().unregisterReceiver(bluetoothReceiver);
        binding = null;
    }

    @NonNull
    private ArrayList<HashMap<String,String>> makeDeviceList(@NonNull Set<BluetoothDevice> deviceSet) {
        ArrayList<HashMap<String,String>> _deviceList = new ArrayList<>();
        HashMap<String,String> item;
        for (BluetoothDevice device : deviceSet) {
            item = new HashMap<String, String>();
            item.put(DEVICE_NAME, device.getName());
            item.put(DEVICE_MAC, device.getAddress());
            _deviceList.add(item);
        }
        return _deviceList;
    }

    private SimpleAdapter makeAdapter(List<HashMap<String,String>> list) {
        SimpleAdapter adapter = new SimpleAdapter(
                getContext(),
                list,
                R.layout.two_line_list_item,
                new String[] {DEVICE_NAME, DEVICE_MAC},
                new int[] {R.id.item_line_1, R.id.item_line_2});
        return adapter;
    }

    private boolean generateDeviceList() {
        if (myBluetooth.ble_available()) {
            // stop any ongoing ble scan
            myBluetooth.stopBLEScan();
        }

        int return_code = myBluetooth.getStatus(getContext());

        switch (return_code) {
            case MyBluetooth.BLUETOOTH_OK:
                // All is well
                break;
            case MyBluetooth.BLUETOOTH_NOT_AVAILABLE:
                textView.setText("Bluetooth not available.");
                return false;
            case MyBluetooth.BLUETOOTH_NO_CONNECT:
                textView.setText("Permission not granted.");
                return false;
            case MyBluetooth.BLUETOOTH_NO_SCAN:
                ;// Log.i("MY_INFO", "Scan permission not granted");
                break;
            case MyBluetooth.BLUETOOTH_DISABLED:
                textView.setText("Bluetooth not enabled.");
                Toast.makeText(
                        getContext(),
                        "Please enable Bluetooth and try again.",
                        Toast.LENGTH_LONG
                ).show();
                deviceListView.setAdapter(null);
                return false;
            default:
                Toast.makeText(
                        getContext(),
                        "Unknown Error: " + return_code,
                        Toast.LENGTH_SHORT
                ).show();
                return false;
        }

        textView.setText(R.string.bluetooth_classic_header);

        Set<BluetoothDevice> pairedDevices = myBluetooth.getPairedDevices();
        deviceList = makeDeviceList(pairedDevices);
        mSimpleAdapter = makeAdapter(deviceList);

        deviceListView.setAdapter(mSimpleAdapter);
        deviceListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                HashMap<String,String> item;
                item = deviceList.get(position);
                String device_mac = item.get(DEVICE_MAC);
                myBluetooth.connect(getContext(), device_mac);
            }
        });

        return true;
    }

    private boolean generateBLEDeviceList() {
        if (!myBluetooth.ble_available()) {
            Toast.makeText(
                    getContext(),
                    "Bluetooth low-energy is not available on this device",
                    Toast.LENGTH_LONG
            ).show();
            return false;
        }
        // stop any ongoing ble scan
        myBluetooth.stopBLEScan();

        int return_code = myBluetooth.getStatus(getContext());

        switch (return_code) {
            case MyBluetooth.BLUETOOTH_OK:
                // All is well
                break;
            case MyBluetooth.BLUETOOTH_NOT_AVAILABLE:
                textView.setText("Bluetooth not available.");
                return false;
            case MyBluetooth.BLUETOOTH_NO_CONNECT:
                textView.setText("Permission not granted.");
                return false;
            case MyBluetooth.BLUETOOTH_NO_SCAN:
                ;// Log.i("MY_INFO", "Scan permission not granted");
                break;
            case MyBluetooth.BLUETOOTH_DISABLED:
                textView.setText("Bluetooth not enabled.");
                Toast.makeText(
                        getContext(),
                        "Please enable Bluetooth and try again.",
                        Toast.LENGTH_LONG
                ).show();
                deviceListView.setAdapter(null);
                return false;
            default:
                Toast.makeText(
                        getContext(),
                        "Unknown Error: " + return_code,
                        Toast.LENGTH_SHORT
                ).show();
                return false;
        }

        textView.setText(R.string.bluetooth_le_header);

        // Set up BLE scan callback
        leDeviceSet = new HashSet<>();
        deviceListView.setAdapter(null);
        ScanCallback leScanCallback = new ScanCallback() {
            @Override
            public void onScanResult(int callbackType, ScanResult result) {
                super.onScanResult(callbackType, result);
                BluetoothDevice leDevice = result.getDevice();
                if (leDevice.getName() != null && !leDeviceSet.contains(leDevice)) {
                    leDeviceSet.add(leDevice);
                    ;// Log.i(MyBluetooth.INFO_TAG, leDevice.getName() + "  " + leDevice.getAddress());
                    leDeviceList = makeDeviceList(leDeviceSet);
                    leDeviceListAdapter = makeAdapter(leDeviceList);
                    deviceListView.setAdapter(leDeviceListAdapter);
                }
            }
        };
        // Set on click listener
        deviceListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                HashMap<String,String> item;
                item = leDeviceList.get(position);
                String device_mac = item.get(DEVICE_MAC);
                myBluetooth.connectBLE(getContext(), device_mac);
            }
        });
        myBluetooth.getBLEDevices(leScanCallback);

        return true;
    }
}