

package com.example.smartmouse.ui.cross

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import com.example.smartmouse.MainActivity
import com.example.smartmouse.R
import com.example.smartmouse.bluetooth.BLE
import com.example.smartmouse.bluetooth.Mouse
import kotlinx.android.synthetic.main.fragment_cross.*

class CrossFragment: Fragment(){
    lateinit var mouse: Mouse

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mouse = (activity as MainActivity).getMouse()
        return inflater.inflate(R.layout.fragment_cross, container, false)

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val upButton: ImageView = view.findViewById(R.id.button_upArrow)
        val downButton: ImageView = view.findViewById(R.id.button_downArrow)
        val leftButton: ImageView = view.findViewById(R.id.button_leftArrow)
        val rightButton: ImageView = view.findViewById(R.id.button_rightArrow)
        val leftClick: ImageView = view.findViewById(R.id.button_leftClick)
        val rightClick: ImageView = view.findViewById(R.id.button_rightClick)
        val nameText: TextView = view.findViewById(R.id.text_ConnectedDevice)

        var names: Array<String> = mouse.connectedDeviceName()

        if (names.isEmpty()){
            nameText.text = "No device is connected"
        }else{
            for (x in names){
                nameText.text = nameText.text.toString() + x + ", "
            }
            nameText.text = nameText.text.toString() + "are connected"
        }

        upButton.setOnClickListener{
            mouse.changeState(0,- 20,0, left = false, right = false, middle = false)
        }

        downButton.setOnClickListener {
            mouse.changeState(0,20,0, left = false, right = false, middle = false)
        }

        leftButton.setOnClickListener {
            mouse.changeState(-20,0,0, left = false, right = false, middle = false)
        }

        rightButton.setOnClickListener {
            mouse.changeState(20,0,0, left = false, right = false, middle = false)
        }

        leftClick.setOnClickListener {
            mouse.changeState(0,0,0, left = true, right = false, middle = false)
        }

        rightClick.setOnClickListener {
            mouse.changeState(0,0,0, left = false, right = true, middle = false)
        }
    }
}