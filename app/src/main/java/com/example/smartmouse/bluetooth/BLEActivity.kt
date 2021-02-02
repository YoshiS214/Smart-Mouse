package com.example.smartmouse.bluetooth

import android.app.Application
import android.content.Intent
import android.os.Bundle
import android.os.PersistableBundle
import androidx.appcompat.app.AppCompatActivity

abstract class BLEActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?, persistentState: PersistableBundle?) {
        super.onCreate(savedInstanceState, persistentState)


        //if (!BLE.isEnabled()){
        //    BLE.enableBLE()
        //}

        //setPeripheralProvider()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 0xb1e){
            setPeripheralProvider()
        }
    }

    abstract fun setPeripheralProvider()
}