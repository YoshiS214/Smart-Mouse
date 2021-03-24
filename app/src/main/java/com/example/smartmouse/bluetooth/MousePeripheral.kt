package com.example.smartmouse.bluetooth

import android.content.Context
import android.util.Log
import java.io.ByteArrayOutputStream
import kotlin.experimental.and
import kotlin.experimental.or
import kotlin.math.absoluteValue
import kotlin.math.sign

class MousePeripheral: BLE() {
    private var previousReport: ByteArray = byteArrayOf(0, 0, 0, 0)
    private val reportMapHeader: ByteArray = byteArrayOf(USAGE_PAGE(1), 0x01)
    private val reportMapMouse: ByteArray = byteArrayOf(
        /*
        USAGE_PAGE(1),      0x01,             //Generic Desktop
        USAGE(1),           0x02,             // Mouse
        COLLECTION(1),      0x01,             // Application
        REPORT_ID(1),       0x01,             //  Report Id
        USAGE(1),           0x01,             //  Pointer
        COLLECTION(1),      0x00,             //  Physical
        USAGE_PAGE(1),      0x09,             //   Buttons
        USAGE_MINIMUM(1),   0x01,
        USAGE_MAXIMUM(1),   0x03,
        LOGICAL_MINIMUM(1), 0x00,             //   0
        LOGICAL_MAXIMUM(1), 0x01,             //   1
        REPORT_COUNT(1),    0x03,             //   3 bits (Buttons)
        REPORT_SIZE(1),     0x01,
        INPUT(1),           0x02,             //   Data, Variable, Absolute
        REPORT_COUNT(1),    0x01,             //   5 bits (Padding)
        REPORT_SIZE(1),     0x05,
        INPUT(1),           0x01,             //   Constant
        USAGE_PAGE(1),      0x01,             //   Generic Desktop
        USAGE(1),           0x30,             //   X
        USAGE(1),           0x31,             //   Y
        USAGE(1),           0x38,             //   Wheel
        LOGICAL_MINIMUM(1), 0x81.toByte(),    //   -127
        LOGICAL_MAXIMUM(1), 0x7F,             //   127
        REPORT_SIZE(1),     0x08,             //   8 bits
        REPORT_COUNT(1),    0x03,             //   3 x 8 bits = 3 bytes
        INPUT(1),           0x06,             //   Data, Variable, Relative
        END_COLLECTION(0),
        END_COLLECTION(0),

         */
        USAGE_PAGE(1), 0x01,  // Generic Desktop
        USAGE(1), 0x02,  // Mouse
        COLLECTION(1), 0x01,  // Application
        USAGE(1), 0x01,  //  Pointer
        COLLECTION(1), 0x00,  //  Physical
        USAGE_PAGE(1), 0x09,  //   Buttons
        USAGE_MINIMUM(1), 0x01,
        USAGE_MAXIMUM(1), 0x03,
        LOGICAL_MINIMUM(1), 0x00,
        LOGICAL_MAXIMUM(1), 0x01,
        REPORT_COUNT(1), 0x03,  //   3 bits (Buttons)
        REPORT_SIZE(1), 0x01,
        INPUT(1), 0x02,  //   Data, Variable, Absolute
        REPORT_COUNT(1), 0x01,  //   5 bits (Padding)
        REPORT_SIZE(1), 0x05,
        INPUT(1), 0x01,  //   Constant
        USAGE_PAGE(1), 0x01,  //   Generic Desktop
        USAGE(1), 0x30,  //   X
        USAGE(1), 0x31,  //   Y
        USAGE(1), 0x38,  //   Wheel
        LOGICAL_MINIMUM(1), 0x81.toByte(),  //   -127
        LOGICAL_MAXIMUM(1), 0x7f,  //   127
        REPORT_SIZE(1), 0x08,  //   Three bytes
        REPORT_COUNT(1), 0x03,
        INPUT(1), 0x06,  //   Data, Variable, Relative
        END_COLLECTION(0),
        END_COLLECTION(0)
    )
    private val reportMapKeyboard: ByteArray = byteArrayOf(
        USAGE(1),               0x06,                 // Keyboard
        COLLECTION(1),          0x01,                 // Application
        USAGE_PAGE(1),          0x07,                 // keyboard/Keypad
        REPORT_ID(1),           0x03,                 //   Report Id
        USAGE_MINIMUM(1),       0xE0.toByte(),
        USAGE_MAXIMUM(1),       0xE7.toByte(),
        LOGICAL_MINIMUM(1),     0x00,
        LOGICAL_MAXIMUM(1),     0x01,
        REPORT_SIZE(1),         0x01,                 //   1 byte (Modifier)
        REPORT_COUNT(1),        0x08,
        INPUT(1),               0x02,                 //   Data,Var,Abs,No Wrap,Linear,Preferred State,No Null Position
        REPORT_COUNT(1),        0x01,                 //   1 byte (Reserved)
        REPORT_SIZE(1),         0x08,
        INPUT(1),               0x01,                 //   Const,Array,Abs,No Wrap,Linear,Preferred State,No Null Position
        REPORT_COUNT(1),        0x05,                 //   5 bits (Num lock, Caps lock, Scroll lock, Compose, Kana)
        REPORT_SIZE(1),         0x01,
        USAGE_PAGE(1),          0x08,                 //   LEDs
        USAGE_MINIMUM(1),       0x01,                 //   Num Lock
        USAGE_MAXIMUM(1),       0x05,                 //   Kana
        OUTPUT(1),              0x02,                 //   Data,Var,Abs,No Wrap,Linear,Preferred State,No Null Position,Non-volatile
        REPORT_COUNT(1),        0x01,                 //   3 bits (Padding)
        REPORT_SIZE(1),         0x03,
        OUTPUT(1),              0x01,                 //   Const,Array,Abs,No Wrap,Linear,Preferred State,No Null Position,Non-volatile
        REPORT_COUNT(1),        0x06,                 //   6 bytes (Keys)
        REPORT_SIZE(1),         0x08,
        LOGICAL_MINIMUM(1),     0x00,
        LOGICAL_MAXIMUM(1),     0xFF.toByte(),          //   255 keys
        USAGE_PAGE(1),          0x07,                 //   keyboard/Keypad
        USAGE_MINIMUM(1),       0x00,
        USAGE_MAXIMUM(1),       0x65,
        INPUT(1),               0x00,                 //   Data,Array,Abs,No Wrap,Linear,Preferred State,No Null Position
        END_COLLECTION(0),
    )

    fun initialise(context: Context){
        var error: String? = super.initialise(context, booleanArrayOf(true, true, false), 10)
        if (error != null){
            Log.d("MOUSE INITIALISE", error)
        }
    }

    fun sendData(displacement: IntArray, buttons: BooleanArray, device: bDevice){ // displacement = [x, y, z], buttons = [left, right, middle]
        /*
        val parameter: IntArray = intArrayOf(1, 2, 4)
        var report: ByteArray = byteArrayOf(0, 0, 0, 0)

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
            report[x + 1        ] = displacement[x].toByte()
        }
        if (previousReport.count { it.toInt() == 0 } != previousReport.size || report.count { it.toInt() == 0} == report.size){
            report.copyInto(previousReport)
            addInputReports(Report(device,report))
        }

         */
        var dx = displacement[0]
        var dy = displacement[1]
        var wheel = displacement[2]
        if (dx > 127) dx = 127
        if (dx < -127) dx = -127
        if (dy > 127) dy = 127
        if (dy < -127) dy = -127
        if (wheel > 127) wheel = 127
        if (wheel < -127) wheel = -127
        var button: Byte = 0
        if (buttons[0]) {
            button = button or 1
        }
        if (buttons[1]) {
            button = button or 2
        }
        if (buttons[2]) {
            button = button or 4
        }
        val report = ByteArray(4)
        report[0] = (button and 7) as Byte
        report[1] = dx.toByte()
        report[2] = dy.toByte()
        report[3] = wheel.toByte()
        if (previousReport[0].equals(0) && previousReport[1].equals(0) && previousReport[2].equals(0) && previousReport[3].equals(0) && report[0].equals(0) && report[1].equals(0) && report[2].equals(0) && report[3].equals(0)) {
            return
        }
        previousReport[0] = report[0]
        previousReport[1] = report[1]
        previousReport[2] = report[2]
        previousReport[3] = report[3]
        addInputReports(Report(device, report))
    }

    override fun getReportMap(): ByteArray {
        /*
        var output = ByteArrayOutputStream()
        output.write(reportMapMouse)
        return  output.toByteArray()

         */
        return reportMapMouse
    }

    override fun getOutputReport(output: ByteArray) {
        Log.d("BLE", output.decodeToString())
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

    override fun connectedDevice(): Array<bDevice> {
        return super.connectedDevice()
    }

}