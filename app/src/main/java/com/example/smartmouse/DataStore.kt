package com.example.smartmouse

import android.bluetooth.BluetoothDevice
import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.google.gson.Gson
import java.lang.Exception

class DataStore {

    companion object{
        fun getBleDevices(context: Context): Array<BluetoothDevice>{
            var data: SharedPreferences = context.getSharedPreferences("ble", Context.MODE_PRIVATE)
            var gson: Gson = Gson()

            var devices = try {
                gson.fromJson(data.getString("devices", ""), Array<BluetoothDevice>::class.java)
            }catch (e: Exception){
                arrayOf()
            }
            if (devices.isNotEmpty()){
                Log.d("data", "読み込まれよ")
            }
            return devices
        }

        fun writeBleDevices(context: Context, devices: Array<BluetoothDevice>){
            var data: SharedPreferences = context.getSharedPreferences("ble", Context.MODE_PRIVATE)
            var gson: Gson = Gson()
            data.edit().putString("devices", gson.toJson(devices)).commit()
            Log.d("data", "書き込んだよ")
        }
    }
}