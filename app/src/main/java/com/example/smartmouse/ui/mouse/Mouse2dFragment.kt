package com.example.smartmouse.ui.mouse

import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import com.example.smartmouse.MainActivity
import com.example.smartmouse.R
import com.example.smartmouse.Sensor
import com.example.smartmouse.bluetooth.Mouse
import java.util.*
import kotlin.math.roundToInt
import kotlin.properties.Delegates

class Mouse2dFragment : Fragment() {
    private lateinit var mouse: Mouse
    private lateinit var mainActivity: MainActivity
    private lateinit var sensor: Sensor
    private lateinit var handler: Handler
    private var speed by Delegates.notNull<Int>()
    private lateinit var timer: Timer
    private var scroll: Int = 0

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mainActivity = activity as MainActivity
        mouse = mainActivity.getMouse()
        sensor = mainActivity.getSensor()
        handler = Handler()
        speed = mainActivity.getSpeed()
        timer = Timer()
        return inflater.inflate(R.layout.fragment_2dmouse, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val leftButton: Button = view.findViewById(R.id.button_left)
        val rightButton: Button = view.findViewById(R.id.button_right)
        val upButton: Button = view.findViewById(R.id.button_up)
        val downButton: Button = view.findViewById(R.id.button_down)
        val mouseState: Button = view.findViewById(R.id.button_state)

        var measure: TimerTask = object : TimerTask() {
            override fun run() {
                var tmp: Pair<FloatArray, FloatArray> = sensor.getDisplacement()
                var device = mainActivity.getDevice()
                if (device != null) {
                    mouse.changeState(
                        (tmp.first[0] * speed).roundToInt(),
                        (tmp.first[1] * speed).roundToInt(),
                        scroll,
                        leftButton.isPressed,
                        rightButton.isPressed,
                        false,
                        device
                    )
                }
                scroll = 0
            }
        }

        upButton.setOnClickListener {
            scroll += 10
        }

        downButton.setOnClickListener {
            scroll -= 10
        }
        sensor.enableSensor()

        timer.scheduleAtFixedRate(measure, 0, 10)

        mouseState.setOnClickListener { // Return 3D mouse fragment
            fragmentManager?.popBackStack()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        timer.cancel()
        sensor.disableSensor()
    }

    override fun onStop() {
        super.onStop()
        timer.cancel()
        sensor.disableSensor()
    }
}