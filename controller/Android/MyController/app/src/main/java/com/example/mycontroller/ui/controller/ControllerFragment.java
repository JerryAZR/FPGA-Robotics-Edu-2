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

package com.example.mycontroller.ui.controller;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.example.mycontroller.MainViewModel;
import com.example.mycontroller.MyBluetooth;
import com.example.mycontroller.databinding.FragmentControllerBinding;

import io.github.controlwear.virtual.joystick.android.JoystickView;

public class ControllerFragment extends Fragment {

    private FragmentControllerBinding binding;
    private MyBluetooth myBluetooth;
    private String left_info = "Left: +0";
    private String right_info = "; Right: +0";
    private int left_reading = 0;
    private int right_reading = 0;
    private double max_turn_spd = 250.0;
    private double max_nav_spd = 750.0;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentControllerBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        MainViewModel sharedVM = new ViewModelProvider(requireActivity()).get(MainViewModel.class);
        myBluetooth = sharedVM.myBluetooth;

        binding.joystickLeft.setOnMoveListener(new JoystickView.OnMoveListener() {
            @Override
            public void onMove(int angle, int strength) {
                if (angle < 180) {
                    left_info = "Left: +" + strength;
                    left_reading = strength;
                } else {
                    left_info = "Left: -" + strength;
                    left_reading = -strength;
                }
                binding.textController.setText(left_info + right_info);
                if (myBluetooth == null
                        || myBluetooth.bluetoothState != MyBluetooth.STATE_CONNECTED) {
                    return;
                }
                myBluetooth.send(makeCmd());
            }
        });

        binding.joystickRight.setOnMoveListener(new JoystickView.OnMoveListener() {
            @Override
            public void onMove(int angle, int strength) {
                if (angle < 90) {
                    right_info = "; Right: +" + strength;
                    right_reading = strength;
                } else {
                    right_info = "; Right: -" + strength;
                    right_reading = -strength;
                }
                binding.textController.setText(left_info + right_info);
                if (myBluetooth == null
                        || myBluetooth.bluetoothState != MyBluetooth.STATE_CONNECTED) {
                    return;
                }
                myBluetooth.send(makeCmd());
            }
        });

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private byte[] makeCmd() {
        int left_cmd = 0x80;
        int right_cmd = 0xC0;
        double left_spd;
        double right_spd;
        int left_spd_int;
        int right_spd_int;
        byte[] bytearray;
        left_spd = left_reading * max_nav_spd + right_reading * max_turn_spd;
        right_spd = left_reading * max_nav_spd - right_reading * max_turn_spd;
        left_spd_int = (int) (left_spd / 3200);
        right_spd_int = (int) (right_spd / 3200);
        if (left_spd > 0) {
            left_cmd = left_cmd | (left_spd_int & 0x1F);
        } else {
            left_cmd = left_cmd | 0x20 | (-left_spd_int & 0x1F);
        }
        if (right_spd > 0) {
            right_cmd = right_cmd | (right_spd_int & 0x1F);
        } else {
            right_cmd = right_cmd | 0x20 | (-right_spd_int & 0x1F);
        }
        bytearray = new byte[]{(byte) (left_cmd & 0xFF), (byte) (right_cmd & 0xFF)};
        return bytearray;
    }
}