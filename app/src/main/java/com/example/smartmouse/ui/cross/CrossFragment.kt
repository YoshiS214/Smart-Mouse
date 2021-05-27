package com.example.smartmouse.ui.cross

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import androidx.fragment.app.Fragment
import com.example.smartmouse.MainActivity
import com.example.smartmouse.R
import com.example.smartmouse.bluetooth.Mouse
import kotlin.math.roundToInt

class CrossFragment : Fragment() {
    private lateinit var mouse: Mouse
    private lateinit var mainActivity: MainActivity
    private var speed: Int = 1

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
        val upButton: ImageButton = view.findViewById(R.id.button_upArrow)
        val downButton: ImageButton = view.findViewById(R.id.button_downArrow)
        val leftButton: ImageButton = view.findViewById(R.id.button_leftArrow)
        val rightButton: ImageButton = view.findViewById(R.id.button_rightArrow)
        val leftClick: ImageButton = view.findViewById(R.id.button_leftClick)
        val rightClick: ImageButton = view.findViewById(R.id.button_rightClick)
        val upScroll: ImageButton = view.findViewById(R.id.button_upScroll)
        val downScroll: ImageButton = view.findViewById(R.id.button_downScroll)

        upButton.setOnClickListener {
            var device = mainActivity.getDevice()
            if (device != null) {
                mouse.changeState(
                    0,
                    -2 * speed,
                    0,
                    left = false,
                    right = false,
                    middle = false,
                    device
                )
            }
        }

        downButton.setOnClickListener {
            var device = mainActivity.getDevice()
            if (device != null) {
                mouse.changeState(
                    0,
                    2 * speed,
                    0,
                    left = false,
                    right = false,
                    middle = false,
                    device
                )
            }
        }

        leftButton.setOnClickListener {
            var device = mainActivity.getDevice()
            if (device != null) {
                mouse.changeState(
                    -2 * speed,
                    0,
                    0,
                    left = false,
                    right = false,
                    middle = false,
                    device
                )
            }
        }

        rightButton.setOnClickListener {
            var device = mainActivity.getDevice()
            if (device != null) {
                mouse.changeState(
                    2 * speed,
                    0,
                    0,
                    left = false,
                    right = false,
                    middle = false,
                    device
                )
            }
        }

        upScroll.setOnClickListener {
            var device = mainActivity.getDevice()
            if (device != null) {
                mouse.changeState(
                    0,
                    0,
                    (0.5 * speed).roundToInt(),
                    left = false,
                    right = false,
                    middle = false,
                    device
                )
            }
        }

        downScroll.setOnClickListener {
            var device = mainActivity.getDevice()
            if (device != null) {
                mouse.changeState(
                    0,
                    0,
                    (-0.5 * speed).roundToInt(),
                    left = false,
                    right = false,
                    middle = false,
                    device
                )
            }
        }


        leftClick.setOnClickListener {
            var device = mainActivity.getDevice()
            if (device != null) {
                mouse.changeState(1, 0, 0, left = true, right = false, middle = false, device)
            }
        }

        rightClick.setOnClickListener {
            var device = mainActivity.getDevice()
            if (device != null) {
                mouse.changeState(1, 0, 0, left = false, right = true, middle = false, device)
            }
        }
    }
}