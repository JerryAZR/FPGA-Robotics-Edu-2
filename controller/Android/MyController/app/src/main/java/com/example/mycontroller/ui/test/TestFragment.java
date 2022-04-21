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

package com.example.mycontroller.ui.test;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.mycontroller.MainViewModel;
import com.example.mycontroller.MyBluetooth;
import com.example.mycontroller.databinding.FragmentTestBinding;

public class TestFragment extends Fragment {

    private FragmentTestBinding binding;
    private MyBluetooth myBluetooth;

    public View.OnClickListener btnClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (myBluetooth == null || myBluetooth.bluetoothState != MyBluetooth.STATE_CONNECTED) {
                Toast.makeText(
                        getContext(),
                        "Device not connected",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            int msg = Integer.parseInt(((Button)v).getText().toString());
            Toast.makeText(
                    getContext(),
                    "Sending number " + msg + " to device",
                    Toast.LENGTH_SHORT).show();
            myBluetooth.send(msg);
        }
    };

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentTestBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // get my bluetooth object
        MainViewModel sharedVM = new ViewModelProvider(requireActivity()).get(MainViewModel.class);
        myBluetooth = sharedVM.myBluetooth;

        // set button on-click events
        binding.btn0.setOnClickListener(btnClick);
        binding.btn1.setOnClickListener(btnClick);
        binding.btn2.setOnClickListener(btnClick);
        binding.btn3.setOnClickListener(btnClick);
        binding.btn4.setOnClickListener(btnClick);
        binding.btn5.setOnClickListener(btnClick);
        binding.btn6.setOnClickListener(btnClick);
        binding.btn7.setOnClickListener(btnClick);
        binding.btn8.setOnClickListener(btnClick);
        binding.btn9.setOnClickListener(btnClick);
        binding.btn10.setOnClickListener(btnClick);
        binding.btn11.setOnClickListener(btnClick);
        binding.btn12.setOnClickListener(btnClick);
        binding.btn13.setOnClickListener(btnClick);
        binding.btn14.setOnClickListener(btnClick);
        binding.btn15.setOnClickListener(btnClick);

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}