package com.example.smartmouse.bluetooth

import android.content.Context
import android.util.Log
import kotlin.experimental.and
import kotlin.experimental.or
import kotlin.math.absoluteValue
import kotlin.math.sign

class MousePeripheral: BLE() {
    var previousReport: ByteArray = byteArrayOf()


    fun initialise(context: Context){
        var error: String? = super.initialise(context, booleanArrayOf(true, false, false), 10)
        if (error != null){
            Log.d("MOUSE INITIALISE", error)
        }
    }

    fun sendData(displacement: IntArray, buttons: BooleanArray){ // displacement = [x, y, z], buttons = [left, right, middle]
        val parameter: IntArray = intArrayOf(1,2,4)
        var report: ByteArray = byteArrayOf(0,0,0,0)

        for (x in displacement.indices){
            if (displacement[x].absoluteValue > 127){
                displacement[x] = displacement[x].sign * 127
            }
        }
        for (x in buttons.indices){
            if (buttons[x]){
                report[0] = report[0] or parameter[x].toByte()
            }
        }
        report[0] = report[0] and 7
        for (x in displacement.indices){
            report[x+1] = displacement[x].toByte()
        }
        if (previousReport.count { it.toInt() == 0 } != previousReport.size || report.count { it.toInt() == 0} == report.size){
            report.copyInto(previousReport)
            addHidInput(report)
        }
    }

    override fun isReady(): Boolean{
        return super.isReady()
    }

    override fun getDevicesName(): Array<String> {
        return super.getDevicesName()
    }

    override fun connect(name: String) {
        super.connect(name)
    }

    override fun connectedDeviceName(): Array<String> {
        return super.connectedDeviceName()
    }

    override fun saveData() {
        super.saveData()
    }

}