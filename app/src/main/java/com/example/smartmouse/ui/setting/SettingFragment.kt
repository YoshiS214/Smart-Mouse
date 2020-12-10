package com.example.smartmouse.ui.setting

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.example.smartmouse.R
import com.example.smartmouse.bluetooth.BLE

class SettingFragment : Fragment(){
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_setting, container, false)

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val peripheralSwitch: Switch = view.findViewById(R.id.switch_peripheral)
        val connectButton: Button = view.findViewById(R.id.button_BLEconnect)

        peripheralSwitch.setOnCheckedChangeListener{ compoundButton: CompoundButton, b: Boolean ->
            if (b){
                compoundButton.setOnClickListener{
                    BLE.start()
                }
            }else{
                compoundButton.setOnClickListener{
                    BLE.stop()
                }
            }
        }

        connectButton.setOnClickListener {  }
    }
}