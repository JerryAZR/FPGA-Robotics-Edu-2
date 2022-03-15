package com.example.mycontroller;

import android.content.Context;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class MainViewModel extends ViewModel {
    public final MyBluetooth myBluetooth = new MyBluetooth();
    private MutableLiveData<Integer> myBluetoothStatus = new MutableLiveData<Integer>();
}
