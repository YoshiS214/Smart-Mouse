package com.example.smartmouse.bluetooth

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity

abstract class BLEActivity : AppCompatActivity() {

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 0xb1e) {
            setPeripheralProvider()
        }
    }

    abstract fun setPeripheralProvider(): String?
}