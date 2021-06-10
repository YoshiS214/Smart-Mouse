package com.example.smartmouse.ui.setting

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.DialogInterface
import android.os.Bundle
import android.os.Handler
import android.text.Html
import android.text.method.LinkMovementMethod
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.text.HtmlCompat
import androidx.fragment.app.Fragment
import com.example.smartmouse.MainActivity
import com.example.smartmouse.R
import com.example.smartmouse.bluetooth.Keyboard
import com.example.smartmouse.bluetooth.Mouse
import kotlinx.android.synthetic.main.fragment_setting.*
import kotlinx.coroutines.*
import java.lang.Runnable

class SettingFragment : Fragment() {
    lateinit var mouse: Mouse
    lateinit var keyboard: Keyboard
    lateinit var mainActivity: MainActivity

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mainActivity = activity as MainActivity
        mouse = mainActivity.getMouse()
        keyboard = mainActivity.getKeyboard()
        return inflater.inflate(R.layout.fragment_setting, container, false)
    }

    @SuppressLint("UseSwitchCompatOrMaterialCode")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val peripheralSwitch: Switch = view.findViewById(R.id.switch_peripheral)
        val connectButton: Button = view.findViewById(R.id.button_BLEconnect)
        val deviceNameSpinner: Spinner = view.findViewById(R.id.spinner_DeviceName)
        val mouseSeekBar: SeekBar = view.findViewById(R.id.seekbar_mouse)
        val resetButton: Button = view.findViewById(R.id.button_reset)
        val errorText: TextView = view.findViewById(R.id.text_error)
        val howToText: TextView = view.findViewById(R.id.text_howTo)
        val FAQsText: TextView = view.findViewById(R.id.text_FAQs)
        val termsOfUseText: TextView = view.findViewById(R.id.text_termsOfUse)
        val contactText: TextView = view.findViewById(R.id.text_contact)
        var adapter = ArrayAdapter(
            mainActivity.applicationContext,
            R.layout.spinner_style,
            arrayOf("No device found")
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        deviceNameSpinner.adapter = adapter
        val handler: Handler = Handler()
        var runnable: Runnable = Runnable { }
        var initialiseError: String? = null
        runnable = Runnable { // Refresh list of known devices every 1 second
            if (peripheralSwitch.isChecked) {
                adapter = ArrayAdapter(
                    mainActivity.applicationContext,
                    R.layout.spinner_style,
                    mouse.getDevicesName()
                )
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                deviceNameSpinner.adapter = adapter
            }
            handler.postDelayed(runnable, 1000)
        }

        var deviceName: String = ""

        handler.post(runnable)

        howToText.text = HtmlCompat.fromHtml("<a href=\"https://github.com/YoshiS214/Smart-Mouse/wiki/\">Guide to the app</a>", HtmlCompat.FROM_HTML_MODE_LEGACY)
        howToText.movementMethod = LinkMovementMethod.getInstance()

        deviceNameSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?, position: Int, id: Long
            ) { // Choose a device to connect from list of known devices
                val spinnerParent = parent as Spinner
                val item = spinnerParent.selectedItem as String
                deviceName = item
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
        }

        peripheralSwitch.setOnCheckedChangeListener { compoundButton: CompoundButton, b: Boolean ->
            if (b) { // When turning on, try to start mouse server
                compoundButton.setOnClickListener {
                    GlobalScope.launch(Dispatchers.IO){
                        initialiseError = mouse.setPeripheralProvider()
                        if (initialiseError == null) {
                            mouse.start()
                            mainActivity.updateMouse(mouse)
                            mainActivity.enableHid()
                        } else {
                            errorText.text = initialiseError
                            peripheralSwitch.isChecked = false
                        }
                    }
                }
            } else { // When turing off, stop mouse server
                compoundButton.setOnClickListener {
                    mouse.stop()
                    mainActivity.updateMouse(mouse)
                    mainActivity.disableHid()
                }
            }
        }

        connectButton.setOnClickListener {
            mouse.connect(deviceName)
            mainActivity.updateMouse(mouse)
        }

        mouseSeekBar.progress = mainActivity.getSpeed()

        mouseSeekBar.setOnSeekBarChangeListener(
            object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(
                    seekBar: SeekBar?,
                    progress: Int,
                    fromUser: Boolean
                ) { // Change speed of cursor moving
                    mainActivity.updateSpeed(progress)
                }

                override fun onStartTrackingTouch(seekBar: SeekBar?) {
                }

                override fun onStopTrackingTouch(seekBar: SeekBar?) {
                }
            }
        )

        resetButton.setOnClickListener { // Reset stored setting data
            AlertDialog.Builder(view.context).apply {
                setTitle("Reset")
                setMessage("Are you sure to delete all setting?\n(You will need to do pairing again.)")
                setPositiveButton("Yes", DialogInterface.OnClickListener { _, _ ->
                    mouse.stop()
                    adapter = ArrayAdapter(
                        mainActivity.applicationContext,
                        android.R.layout.simple_spinner_item,
                        mouse.getDevicesName()
                    )
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    deviceNameSpinner.adapter = adapter
                    peripheralSwitch.isChecked = false
                    mouse.setPeripheralProvider()
                    mainActivity.updateMouse(mouse)
                })
                setNegativeButton("Cancel", null)
                show()
            }
        }
        peripheralSwitch.isChecked = mainActivity.isHidEnabled()
    }
}