package com.example.smartmouse.ui.keyboard

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ToggleButton
import androidx.fragment.app.Fragment
import com.example.smartmouse.MainActivity
import com.example.smartmouse.R
import com.example.smartmouse.bluetooth.Keyboard
import com.example.smartmouse.bluetooth.Mouse
import com.example.smartmouse.bluetooth.bDevice
import com.google.android.material.textfield.TextInputEditText
import kotlinx.android.synthetic.main.fragment_keyboard.*
import java.util.*

class KeyboardFragment : Fragment() {
    private lateinit var mouse: Mouse
    private lateinit var keyboard: Keyboard
    private lateinit var mainActivity: MainActivity
    private lateinit var timer: Timer
    private lateinit var handler: Handler
    private var initialiseError: String? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? { // Stop mouse server and start keyboard
        mainActivity = activity as MainActivity
        mouse = mainActivity.getMouse()
        if (mouse.isStarted()) {
            mouse.stop()
        }
        mainActivity.updateMouse(mouse)
        keyboard = mainActivity.getKeyboard()
        timer = Timer()
        handler = Handler(Looper.getMainLooper())
        val root = inflater.inflate(R.layout.fragment_keyboard, container, false)
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val textInput: TextInputEditText = view.findViewById(R.id.input_text)
        val escKey: Button = view.findViewById(R.id.key_esc)
        val delKey: Button = view.findViewById(R.id.key_del)
        val shiftKey: ToggleButton = view.findViewById(R.id.key_shift)
        val ctrlKey: ToggleButton = view.findViewById(R.id.key_ctrl)
        val fnKey: Button = view.findViewById(R.id.key_fn)
        val altKey: ToggleButton = view.findViewById(R.id.key_alt)

        initialiseError = keyboard.setPeripheralProvider()
        if (initialiseError == null) {
            keyboard.start()
            mainActivity.updateKeyboard(keyboard)
        }

        var read: TimerTask = object : TimerTask() { // Get text typed and send character one by one and replace with ""
            override fun run() {
                var device = mainActivity.getDevice()
                var temp = ""
                if (device != null) {
                    temp = input_text.text.toString()
                    if (temp != "") {
                        handler.post { textInput.setText("") }
                        temp.forEach { x ->
                            keyboard.keyChange("", device)
                            keyboard.keyChange(x.toString(), device)
                        }
                    }
                }
            }
        }
        if (initialiseError == null) {
            escKey.setOnClickListener {
                var device: bDevice? = mainActivity.getDevice()
                if (device != null) {
                    keyboard.keyChange("", device)
                    keyboard.keyChange("esc", device)
                }
            }

            delKey.setOnClickListener {
                var device: bDevice? = mainActivity.getDevice()
                if (device != null) {
                    keyboard.keyChange("", device)
                    keyboard.keyChange("del", device)
                }
            }

            fnKey.setOnClickListener {
                var device: bDevice? = mainActivity.getDevice()
                if (device != null) {
                    keyboard.keyChange("", device)
                    keyboard.keyChange("fn", device)
                }
            }

            textInput.setOnKeyListener { v, keyCode, event ->
                when (event.action) {
                    KeyEvent.KEYCODE_ENTER -> {
                        var device: bDevice? = mainActivity.getDevice()
                        if (device != null) {
                            keyboard.keyChange("", device)
                            keyboard.keyChange("ent", device)
                        }
                        true
                    }
                    KeyEvent.KEYCODE_BACK -> {
                        var device: bDevice? = mainActivity.getDevice()
                        if (device != null) {
                            keyboard.keyChange("", device)
                            keyboard.keyChange("back", device)
                        }
                        true
                    }
                    else -> {
                        false
                    }
                }
            }

            shiftKey.setOnCheckedChangeListener { buttonView, isChecked ->
                keyboard.changeModifiersState("shift", isChecked)
            }
            ctrlKey.setOnCheckedChangeListener { buttonView, isChecked ->
                keyboard.changeModifiersState("ctrl", isChecked)
            }
            altKey.setOnCheckedChangeListener { buttonView, isChecked ->
                keyboard.changeModifiersState("alt", isChecked)
            }

            timer.scheduleAtFixedRate(read, 0, 20) // Read and send values every 20 milliseconds
        }
    }

    override fun onStop() { // Stop keyboard server and start mouse server
        super.onStop()
        if (initialiseError == null) {
            timer.cancel()
            keyboard.stop()
            mainActivity.updateKeyboard(keyboard)
            mouse.setPeripheralProvider()
            mouse.start()
            mainActivity.updateMouse(mouse)
        }
    }
}