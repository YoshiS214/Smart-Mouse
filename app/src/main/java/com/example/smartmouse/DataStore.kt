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


            return try {
                var data: SharedPreferences = context.getSharedPreferences("ble", Context.MODE_PRIVATE)
                var gson: Gson = Gson()

                var map: Map<String, String> = data.all as Map<String, String>
                var devices = map.values.map { x -> gson.fromJson(x, bDevice::class.java) }
                devices.toTypedArray() as Array<bDevice>
            }catch (e: Exception){
                arrayOf<bDevice>()
            }
        }

        fun writeBleDevice(context: Context, device: bDevice){
            var data: SharedPreferences = context.getSharedPreferences("ble", Context.MODE_PRIVATE)
            var gson: Gson = Gson()
            data.edit().putString(device.address, gson.toJson(device)).commit()
        }


        fun getMouseSpeed(context: Context): Int?{
            var data: SharedPreferences = context.getSharedPreferences("mouse", Context.MODE_PRIVATE)
            var gson: Gson = Gson()

            return try{
                gson.fromJson(data.getString("speed", ""), Int::class.java)
            }catch (e: Exception){
                return null
            }
        }

        fun writeMouseSpeed(context: Context, speed: Int){
            var data: SharedPreferences = context.getSharedPreferences("mouse", Context.MODE_PRIVATE)
            var gson: Gson = Gson()
            data.edit().putString("speed", gson.toJson(speed)).commit()

        }
    }
}