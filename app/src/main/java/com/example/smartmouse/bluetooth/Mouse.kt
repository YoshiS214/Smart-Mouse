package com.example.smartmouse.bluetooth

import android.content.Context
import kotlin.properties.Delegates

class Mouse(var context: Context) : BLEActivity() {
    private lateinit var mouse: MousePeripheral
    private var started by Delegates.notNull<Boolean>()

    init {
        mouse = MousePeripheral()
        started = false
    }

    fun changeState(
        x: Int,
        y: Int,
        z: Int,
        left: Boolean,
        right: Boolean,
        middle: Boolean,
        device: bDevice
    ) {
        var displacement: IntArray = intArrayOf(x, y, z)
        var buttons: BooleanArray = booleanArrayOf(left, right, middle)

        mouse.sendData(displacement, buttons, device)
    }

    fun nullValue(device: bDevice){
        mouse.sendData(intArrayOf(0,0,0), booleanArrayOf(false, false, false), device)
    }

    override fun setPeripheralProvider(): String? {
        return mouse.initialise(context)
    }

    override fun onDestroy() {
        super.onDestroy()

        if (mouse != null) {
            mouse.stop()
        }
    }

    fun stop() {
        if (mouse != null) {
            mouse.stop()
            started = false
        }
    }

    fun start() {
        mouse.start()
        started = true
    }

    fun isStarted(): Boolean {
        return started
    }

    fun isReady(): Boolean {
        return mouse.isReady()
    }

    fun getDevicesName(): Array<String> {
        return mouse.getDevicesName()
    }

    fun connect(name: String) {
        mouse.connect(name)
    }

    fun connectedDevice(): Array<bDevice> {
        return mouse.connectedDevice()
    }
}