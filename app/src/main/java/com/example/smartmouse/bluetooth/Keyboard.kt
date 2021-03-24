package com.example.smartmouse.bluetooth

import android.content.Context
import kotlin.properties.Delegates

class Keyboard(var context: Context): BLEActivity() {
    private lateinit var keyboard: KeyboardPeripheral
    private var started by Delegates.notNull<Boolean>()
    
    init {
        keyboard = KeyboardPeripheral()
    }

    override fun setPeripheralProvider() {
        keyboard.initiallise(context)
    }

    fun keyChange(key: String, device: bDevice){
        keyboard.sendKey(key, device)
    }

    fun changeModifiersState(key: String, state: Boolean){
        keyboard.changeModifiersState(key, state)
    }

    override fun onDestroy() {
        super.onDestroy()

        if(keyboard !=null){
            keyboard.stop()
        }
    }

    fun stop() {
        if (keyboard != null) {
            keyboard.stop()
            started = false
        }
    }

    fun start(){
        keyboard.start()
        started = true
    }

    fun isStarted(): Boolean{
        return started
    }

    fun isReady(): Boolean{
        return keyboard.isReady()
    }

    fun getDevicesName(): Array<String>{
        return keyboard.getDevicesName()
    }

    fun connect(name: String){
        keyboard.connect(name)
    }

    fun connectedDevice():Array<bDevice>{
        return keyboard.connectedDevice()
    }

}