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
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import com.example.smartmouse.MainActivity
import com.example.smartmouse.R
import com.example.smartmouse.bluetooth.Keyboard
import com.example.smartmouse.bluetooth.Mouse
import com.example.smartmouse.bluetooth.bDevice
import com.google.android.material.textfield.TextInputEditText
import kotlinx.android.synthetic.main.fragment_keyboard.*
import java.util.*
import kotlin.math.roundToInt

class KeyboardFragment : Fragment() {
    private lateinit var  mouse: Mouse
    private lateinit var keyboard : Keyboard
    private lateinit var mainActivity: MainActivity
    private lateinit var timer: Timer
    private lateinit var handler: Handler

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        mainActivity = activity as MainActivity
        mouse = mainActivity.getMouse()
        mouse.stop()
        mainActivity.updateMouse(mouse)
        keyboard = mainActivity.getKeyboard()
        keyboard.setPeripheralProvider()
        keyboard.start()
        mainActivity.updateKeyboard(keyboard)
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

        var read: TimerTask = object: TimerTask(){
            override fun run() {
                var device = mainActivity.getDevice()
                var temp = ""
                if (device != null){
                    temp = input_text.text.toString()
                    if (temp != ""){
                        handler.post { textInput.setText("") }
                        temp.forEach { x ->
                            keyboard.keyChange("", device)
                            keyboard.keyChange(x.toString(), device)
                        }
                    }
                }
            }
        }
        
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

        timer.scheduleAtFixedRate(read, 0, 20)
    }

    override fun onStop() {
        super.onStop()
        timer.cancel()
        keyboard.stop()
        mainActivity.updateKeyboard(keyboard)
        mouse.setPeripheralProvider()
        mouse.start()
        mainActivity.updateMouse(mouse)
    }
}