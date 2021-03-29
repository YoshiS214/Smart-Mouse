package com.example.smartmouse.bluetooth

import android.content.Context
import android.util.Log
import kotlin.experimental.and
import kotlin.experimental.or
import kotlin.math.absoluteValue
import kotlin.math.sign

class MousePeripheral : BLE() {
    private var previousReport: ByteArray = byteArrayOf(0, 0, 0, 0)
    private val reportMapMouse: ByteArray = byteArrayOf(
        USAGE_PAGE(1),      0x01,             //Generic Desktop
        USAGE(1),           0x02,             // Mouse
        COLLECTION(1),      0x01,             // Application
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
    )

    fun initialise(context: Context): String? {
        var error: String? = super.initialise(context, booleanArrayOf(true, true, false), 10)
        if (error != null) {
            Log.e("MOUSE INITIALISE", error)
        }
        return error
    }

    fun sendData(
        displacement: IntArray,
        buttons: BooleanArray,
        device: bDevice
    ) { // displacement = [x, y, z], buttons = [left, right, middle]
        val parameter: IntArray = intArrayOf(1, 2, 4)
        var report: ByteArray = byteArrayOf(0, 0, 0, 0)

        for (x in displacement.indices) {
            if (displacement[x].absoluteValue > 127) {
                displacement[x] = displacement[x].sign * 127
            }
        }
        for (x in buttons.indices) {
            if (buttons[x]) {
                report[0] = report[0] or parameter[x].toByte()
            }
        }
        report[0] = report[0] and 7
        for (x in displacement.indices) {
            report[x + 1] = displacement[x].toByte()
        }
        if (previousReport[0].equals(0) && previousReport[1].equals(0) && previousReport[2].equals(0) && previousReport[3].equals(
                0
            ) && report[0].equals(0) && report[1].equals(0) && report[2].equals(0) && report[3].equals(
                0
            )
        ) {
            return
        }
        report.copyInto(previousReport)
        addInputReports(Report(device, report))
    }

    override fun getReportMap(): ByteArray {
        return reportMapMouse
    }

    override fun getOutputReport(output: ByteArray) {
        Log.d("BLE", output.decodeToString())
    }
}