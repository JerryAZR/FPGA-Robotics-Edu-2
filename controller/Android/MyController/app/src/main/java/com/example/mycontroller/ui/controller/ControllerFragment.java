package com.example.mycontroller.ui.controller;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.example.mycontroller.databinding.FragmentControllerBinding;

import io.github.controlwear.virtual.joystick.android.JoystickView;

public class ControllerFragment extends Fragment {

    private FragmentControllerBinding binding;
    private String left_info = "Left: +0";
    private String right_info = "; Right: +0";
    private int left_reading = 0;
    private int right_reading = 0;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentControllerBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

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
            }
        });

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}