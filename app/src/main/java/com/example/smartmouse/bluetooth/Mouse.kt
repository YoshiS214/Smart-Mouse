package com.example.smartmouse.bluetooth

import android.content.Context

class Mouse(var context: Context): BLEActivity() {
    var mouse: MousePeripheral = MousePeripheral()
    var started = false

    fun changeState(x:Int, y:Int, z:Int, left:Boolean, right:Boolean, middle:Boolean){
        var displacement: IntArray = intArrayOf(x,y,z)
        var buttons: BooleanArray = booleanArrayOf(left, right, middle)

        mouse.sendData(displacement, buttons)
    }

    override fun setPeripheralProvider() {
        mouse.initialise(context)
    }

    override fun onDestroy() {
        super.onDestroy()

        if(mouse !=null){
            mouse.stop()
        }
    }

    fun stop() {
        if (mouse != null) {
            mouse.stop()
            started = false
        }
    }

    fun start(){
        mouse.start()
        started = true
    }

    fun isStarted(): Boolean{
        return started
    }

    fun isReady(): Boolean{
        return mouse.isReady()
    }

    fun getDevicesName(): Array<String>{
        return mouse.getDevicesName()
    }

    fun connect(name: String){
        mouse.connect(name)
    }

    fun connectedDeviceName():Array<String>{
        return mouse.connectedDeviceName()
    }

    fun storeData(){
        mouse.saveData()
    }
}