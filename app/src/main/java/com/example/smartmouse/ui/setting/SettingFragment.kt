package com.example.smartmouse.ui.setting

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import com.example.smartmouse.MainActivity
import com.example.smartmouse.R
import com.example.smartmouse.bluetooth.Mouse

class SettingFragment : Fragment(){
    lateinit var mouse: Mouse
    lateinit var mainActivity: MainActivity

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mainActivity = activity as MainActivity
        mouse = mainActivity.getMouse()
        return inflater.inflate(R.layout.fragment_setting, container, false)

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val peripheralSwitch: Switch = view.findViewById(R.id.switch_peripheral)
        val connectButton: Button = view.findViewById(R.id.button_BLEconnect)
        val deviceNameSpinner: Spinner = view.findViewById(R.id.spinner_DeviceName)
        var adapter = ArrayAdapter(mainActivity.applicationContext, android.R.layout.simple_spinner_item, arrayOf("No device found"))
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        deviceNameSpinner.adapter = adapter
        val handler: Handler = Handler()
        var runnable: Runnable = Runnable {  }
        runnable = Runnable {
            if (peripheralSwitch.isChecked){
                adapter = ArrayAdapter(mainActivity.applicationContext, android.R.layout.simple_spinner_item, mouse.getDevicesName())
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                deviceNameSpinner.adapter = adapter
            }
            handler.postDelayed(runnable, 30000)
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
                    mouse.start()
                }
            }else{
                compoundButton.setOnClickListener{
                    mouse.stop()
                    //BLE.disableBLE()
                }
            }
        }

        connectButton.setOnClickListener {
            mouse.connect(deviceName)
        }
    }

}