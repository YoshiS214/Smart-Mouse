package com.example.smartmouse.bluetooth

import android.content.Context
import android.util.Log
import kotlin.experimental.or

class KeyboardPeripheral: BLE() {
    private val emptyReport = byteArrayOf(0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0)
    private val reportMapKeyboard = byteArrayOf(
        USAGE_PAGE(1),          0x01,  // Generic Desktop Ctrls
        USAGE(1),               0x06,  // Keyboard
        COLLECTION(1),          0x01,  // Application
        USAGE_PAGE(1),          0x07,  //   Kbrd/Keypad
        USAGE_MINIMUM(1),       0xE0.toByte(),
        USAGE_MAXIMUM(1),       0xE7.toByte(),
        LOGICAL_MINIMUM(1),     0x00,
        LOGICAL_MAXIMUM(1),     0x01,
        REPORT_SIZE(1),         0x01,  //   1 byte (Modifier)
        REPORT_COUNT(1),        0x08,
        INPUT(1),               0x02,  //   Data,Var,Abs,No Wrap,Linear,Preferred State,No Null Position
        REPORT_COUNT(1),        0x01,  //   1 byte (Reserved)
        REPORT_SIZE(1),         0x08,
        INPUT(1),               0x01,  //   private,Array,Abs,No Wrap,Linear,Preferred State,No Null Position
        REPORT_COUNT(1),        0x05,  //   5 bits (Num lock, Caps lock, Scroll lock, Compose, Kana)
        REPORT_SIZE(1),         0x01,
        USAGE_PAGE(1),          0x08,  //   LEDs
        USAGE_MINIMUM(1),       0x01,  //   Num Lock
        USAGE_MAXIMUM(1),       0x05,  //   Kana
        OUTPUT(1),              0x02,  //   Data,Var,Abs,No Wrap,Linear,Preferred State,No Null Position,Non-volatile
        REPORT_COUNT(1),        0x01,  //   3 bits (Padding)
        REPORT_SIZE(1),         0x03,
        OUTPUT(1),              0x01,  //   private,Array,Abs,No Wrap,Linear,Preferred State,No Null Position,Non-volatile
        REPORT_COUNT(1),        0x06,  //   6 bytes (Keys)
        REPORT_SIZE(1),         0x08,
        LOGICAL_MINIMUM(1),     0x00,
        LOGICAL_MAXIMUM(1),     0x65,  //   101 keys
        USAGE_PAGE(1),          0x07,  //   Kbrd/Keypad
        USAGE_MINIMUM(1),       0x00,
        USAGE_MAXIMUM(1),       0x65,
        INPUT(1),               0x00,  //   Data,Array,Abs,No Wrap,Linear,Preferred State,No Null Position
        END_COLLECTION(0)
    )
    private val keyNoneModifier = 0
    private val ctrlKeyModifier = 1
    private val shiftKeyModifier = 2
    private val altKeyModifier = 4
    private val KEY_F1 = 0x3a
    private val KEY_F2 = 0x3b
    private val KEY_F3 = 0x3c
    private val KEY_F4 = 0x3d
    private val KEY_F5 = 0x3e
    private val KEY_F6 = 0x3f
    private val KEY_F7 = 0x40
    private val KEY_F8 = 0x41
    private val KEY_F9 = 0x42
    private val KEY_F10 = 0x43
    private val KEY_F11 = 0x44
    private val KEY_F12 = 0x45
    private val KEY_PRINT_SCREEN = 0x46
    private val KEY_SCROLL_LOCK = 0x47
    private val KEY_CAPS_LOCK = 0x39
    private val KEY_NUM_LOCK = 0x53
    private val KEY_INSERT = 0x49
    private val KEY_HOME = 0x4a
    private val KEY_PAGE_UP = 0x4b
    private val KEY_PAGE_DOWN = 0x4e
    private val KEY_RIGHT_ARROW = 0x4f
    private val KEY_LEFT_ARROW = 0x50
    private val KEY_DOWN_ARROW = 0x51
    private val KEY_UP_ARROW = 0x52

    private var modifiers: BooleanArray = booleanArrayOf(false, false, false)  // ctrl, shift. alt
    
    
    fun initiallise(context: Context){
        var error: String? = super.initialise(context, booleanArrayOf(true, true, false), 20)
        if (error != null){
            Log.d("KEYBOARD INITIALISE", error)
        }
    }
    
    override fun getOutputReport(output: ByteArray) {
        Log.d("BLE", output.decodeToString())
    }

    override fun getReportMap(): ByteArray {
        return reportMapKeyboard
    }

    private fun getKeyCode(char: String?): Byte{
        return when (char) {
            "A", "a" -> 0x04
            "B", "b" -> 0x05
            "C", "c" -> 0x06
            "D", "d" -> 0x07
            "E", "e" -> 0x08
            "F", "f" -> 0x09
            "G", "g" -> 0x0a
            "H", "h" -> 0x0b
            "I", "i" -> 0x0c
            "J", "j" -> 0x0d
            "K", "k" -> 0x0e
            "L", "l" -> 0x0f
            "M", "m" -> 0x10
            "N", "n" -> 0x11
            "O", "o" -> 0x12
            "P", "p" -> 0x13
            "Q", "q" -> 0x14
            "R", "r" -> 0x15
            "S", "s" -> 0x16
            "T", "t" -> 0x17
            "U", "u" -> 0x18
            "V", "v" -> 0x19
            "W", "w" -> 0x1a
            "X", "x" -> 0x1b
            "Y", "y" -> 0x1c
            "Z", "z" -> 0x1d
            "!", "1" -> 0x1e
            "@", "2" -> 0x1f
            "#", "3" -> 0x20
            "$", "4" -> 0x21
            "%", "5" -> 0x22
            "^", "6" -> 0x23
            "&", "7" -> 0x24
            "*", "8" -> 0x25
            "(", "9" -> 0x26
            ")", "0" -> 0x27
            "\n" -> 0x28
            "\b" -> 0x2a
            "\t" -> 0x2b
            " " -> 0x2c
            "_", "-" -> 0x2d
            "+", "=" -> 0x2e
            "{", "[" -> 0x2f
            "}", "]" -> 0x30
            "|", "\\" -> 0x31
            ":", ";" -> 0x33
            "\"", "'" -> 0x34
            "~", "`" -> 0x35
            "<", "," -> 0x36
            ">", "." -> 0x37
            "?", "/" -> 0x38
            "del" -> 0x4C
            "esc" -> 0x29
            "ent" -> 0x28
            "back" -> 0x2A
            else -> 0
        }
    }

    private fun getModifier(char: String?): Byte{
        var modifier: Byte = when (char) {
            "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z", "!", "@", "#", "$", "%", "^", "&", "*", "(", ")", "_", "+", "{", "}", "|", ":", "\"", "~", "<", ">", "?" -> shiftKeyModifier.toByte()
            else -> 0
        }
        if (modifiers[0]){
            modifier = ctrlKeyModifier.toByte() or modifier
        }
        if (modifiers[1]){
            modifier = shiftKeyModifier.toByte() or modifier
        }
        if (modifiers[2]){
            modifier = altKeyModifier.toByte() or modifier
        }
        return modifier
    }

    fun sendKeyUp(device: bDevice){
        addInputReports(Report(device, emptyReport))
    }

    fun sendKey(key: String, device: bDevice){
        var report = ByteArray(8)
        report[0] = getModifier(key)
        report[2] = getKeyCode(key)
        addInputReports(Report(device, report))
        sendKeyUp(device)
    }

    fun changeModifiersState(key: String, newState: Boolean){
        when (key){
            "ctrl" -> modifiers[0] = newState
            "shift" ->modifiers[1] = newState
            "alt" -> modifiers[2] = newState
        }
    }
}