package com.example.mycontroller.ui.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.example.mycontroller.MainViewModel;
import com.example.mycontroller.MyBluetooth;
import com.example.mycontroller.R;
import com.example.mycontroller.databinding.FragmentBluetoothBinding;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

public class BluetoothFragment extends Fragment {

    private FragmentBluetoothBinding binding;
    private Button refreshBtn;
    private TextView textView;
    private ListView deviceListView;

    private static final int REQUEST_BLUETOOTH_CONNECT = 0;
    private static final String DEVICE_NAME = "primary";
    private static final String DEVICE_MAC = "secondary";

    private MyBluetooth myBluetooth;
    private SimpleAdapter mSimpleAdapter;
    private BroadcastReceiver bluetoothReceiver;
    ArrayList<HashMap<String,String>> deviceList;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentBluetoothBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        textView = binding.textHome;
        deviceListView = binding.deviceList;

        refreshBtn = binding.refreshBtn;
        refreshBtn.setOnClickListener(v -> generateDeviceList());

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

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        getActivity().unregisterReceiver(bluetoothReceiver);
        binding = null;
    }

    private boolean generateDeviceList() {

        int return_code = myBluetooth.requestPermission(getContext(), getActivity());

        switch (return_code) {
            case MyBluetooth.BLUETOOTH_OK:
                // All is well
                break;
            case MyBluetooth.BLUETOOTH_NOT_AVAILABLE:
                textView.setText("Bluetooth not available.");
                return false;
            case MyBluetooth.BLUETOOTH_NO_PERMISSION:
                return false;
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

        textView.setText("Please select your device.");

        Set<BluetoothDevice> pairedDevices = myBluetooth.getPairedDevices();
        deviceList = new ArrayList<HashMap<String,String>>();
        HashMap<String,String> item;
        for (BluetoothDevice device : pairedDevices) {
            item = new HashMap<String, String>();
            item.put(DEVICE_NAME, device.getName());
            item.put(DEVICE_MAC, device.getAddress());
            deviceList.add(item);
        }

        mSimpleAdapter = new SimpleAdapter(
                getContext(),
                deviceList,
                R.layout.two_line_list_item,
                new String[] {DEVICE_NAME, DEVICE_MAC},
                new int[] {R.id.item_line_1, R.id.item_line_2});

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
}