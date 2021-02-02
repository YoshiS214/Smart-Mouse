package com.example.smartmouse

import android.app.Application
import com.example.smartmouse.bluetooth.Mouse

class GlobalVariable: Application() {
    private lateinit var mouse: Mouse

    override fun onCreate() {
        super.onCreate()
    }

    fun setMouse(m: Mouse){
        mouse = m
    }

    fun getMouse(): Mouse{
        return mouse
    }
}