package com.example.smartmouse.ui.setting

import android.app.AlertDialog
import android.content.DialogInterface
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import com.example.smartmouse.MainActivity
import com.example.smartmouse.R
import com.example.smartmouse.bluetooth.Keyboard
import com.example.smartmouse.bluetooth.Mouse

class SettingFragment : Fragment(){
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val peripheralSwitch: Switch = view.findViewById(R.id.switch_peripheral)
        val connectButton: Button = view.findViewById(R.id.button_BLEconnect)
        val deviceNameSpinner: Spinner = view.findViewById(R.id.spinner_DeviceName)
        val mouseSeekBar: SeekBar = view.findViewById(R.id.seekbar_mouse)
        val resetButton: Button = view.findViewById(R.id.button_reset)
        var adapter = ArrayAdapter(mainActivity.applicationContext, R.layout.spinner_style, arrayOf("No device found"))
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        deviceNameSpinner.adapter = adapter
        val handler: Handler = Handler()
        var runnable: Runnable = Runnable {  }
        runnable = Runnable {
            if (peripheralSwitch.isChecked){
                adapter = ArrayAdapter(mainActivity.applicationContext, R.layout.spinner_style, mouse.getDevicesName())
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                deviceNameSpinner.adapter = adapter
            }
            handler.postDelayed(runnable, 1000)
        }

        var deviceName: String = ""

        peripheralSwitch.isChecked = mouse.isStarted()

        handler.post(runnable)

        deviceNameSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onItemSelected(parent: AdapterView<*>?,
                                        view: View?, position: Int, id: Long) {
                val spinnerParent = parent as Spinner
                val item = spinnerParent.selectedItem as String

                deviceName = item
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {

            }
        }

        peripheralSwitch.setOnCheckedChangeListener{ compoundButton: CompoundButton, b: Boolean ->
            if (b){
                compoundButton.setOnClickListener{
                    //BLE.enableBLE()
                    mouse.setPeripheralProvider()
                    mouse.start()
                    mainActivity.updateMouse(mouse)
                    //keyboard.setPeripheralProvider()
                    //keyboard.start()
                    mainActivity.updateKeyboard(keyboard)
                }
            }else{
                compoundButton.setOnClickListener{
                    mouse.stop()
                    mainActivity.updateMouse(mouse)
                    //keyboard.stop()
                    mainActivity.updateKeyboard(keyboard)
                    //BLE.disableBLE()
                }
            }
        }

        connectButton.setOnClickListener {
            mouse.connect(deviceName)
            mainActivity.updateMouse(mouse)
            //keyboard.connect(deviceName)
            //mainActivity.updateKeyboard(keyboard)
            //keyboard.storeData()
        }

        mouseSeekBar.progress = mainActivity.getSpeed()

        mouseSeekBar.setOnSeekBarChangeListener(
            object : SeekBar.OnSeekBarChangeListener{
                override fun onProgressChanged(
                    seekBar: SeekBar?,
                    progress: Int,
                    fromUser: Boolean
                ) {
                    mainActivity.updateSpeed(progress)
                }

                override fun onStartTrackingTouch(seekBar: SeekBar?) {
                }

                override fun onStopTrackingTouch(seekBar: SeekBar?) {
                }
            }
        )

        resetButton.setOnClickListener {
            AlertDialog.Builder(view.context).apply {
                setTitle("Reset")
                setMessage("Are you sure to delete all setting?\n(You will need to do pairing again.)")
                setPositiveButton("Yes", DialogInterface.OnClickListener { _, _ ->
                    mouse.stop()
                    //keyboard.stop()
                    adapter = ArrayAdapter(mainActivity.applicationContext, android.R.layout.simple_spinner_item, mouse.getDevicesName())
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    deviceNameSpinner.adapter = adapter
                    peripheralSwitch.isChecked = false
                    mouse.setPeripheralProvider()
                    mainActivity.updateMouse(mouse)
                    //keyboard.setPeripheralProvider()
                    //mainActivity.updateKeyboard(keyboard)
                })
                setNegativeButton("Cancel", null)
                show()
            }
        }
    }

    override fun onStop() {
        super.onStop()
    }
}