package com.example.smartmouse

import android.bluetooth.BluetoothDevice
import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.example.smartmouse.bluetooth.bDevice
import com.google.gson.Gson
import java.lang.Exception

class DataStore {

    companion object{
        fun getBleDevices(context: Context): Array<bDevice>{
            var data: SharedPreferences = context.getSharedPreferences("ble", Context.MODE_PRIVATE)
            var gson: Gson = Gson()

            /*
            if (devices.isNotEmpty()){
                Log.d("data", "読み込まれたよ")
            }
             */
            return try {
                gson.fromJson(data.getString("devices", ""), Array<BluetoothDevice>::class.java)
            }catch (e: Exception){
                arrayOf<bDevice>()
            }
        }

        fun writeBleDevices(context: Context, devices: Array<BluetoothDevice>){
            var data: SharedPreferences = context.getSharedPreferences("ble", Context.MODE_PRIVATE)
            var gson: Gson = Gson()
            data.edit().putString("devices", gson.toJson(devices)).commit()
            Log.d("data", "書き込んだよ")
        }
    }
}