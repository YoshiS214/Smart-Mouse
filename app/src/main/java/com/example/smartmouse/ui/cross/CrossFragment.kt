

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
import kotlin.math.roundToInt

class CrossFragment: Fragment(){
    private lateinit var mouse: Mouse
    private lateinit var mainActivity: MainActivity
    var speed: Int = 1

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mainActivity = activity as MainActivity
        mouse = mainActivity.getMouse()
        speed = mainActivity.getSpeed()
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

        upButton.setOnClickListener{
            var device = mainActivity.getDevice()
            if (device != null){
                mouse.changeState(0,- 2*speed,0, left = false, right = false, middle = false, device)
            }
        }

        downButton.setOnClickListener {
            var device = mainActivity.getDevice()
            if (device != null) {
                mouse.changeState(0,2*speed,0, left = false, right = false, middle = false, device)
            }
        }

        leftButton.setOnClickListener {
            var device = mainActivity.getDevice()
            if (device != null) {
                mouse.changeState(-2*speed,0,0, left = false, right = false, middle = false, device)
            }
        }

        rightButton.setOnClickListener {
            var device = mainActivity.getDevice()
            if (device != null) {
                mouse.changeState(2*speed,0,0, left = false, right = false, middle = false, device)
            }
        }

        leftClick.setOnClickListener {
            var device = mainActivity.getDevice()
            if (device != null) {
                mouse.changeState(0,0,0, left = true, right = false, middle = false, device)
            }
        }

        rightClick.setOnClickListener {
            var device = mainActivity.getDevice()
            if (device != null) {
                mouse.changeState(0,0,0, left = false, right = true, middle = false, device)
            }
        }
    }
}