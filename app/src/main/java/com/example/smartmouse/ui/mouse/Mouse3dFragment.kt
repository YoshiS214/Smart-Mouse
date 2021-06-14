package com.example.smartmouse.ui.mouse

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.MotionEvent
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

class Mouse3dFragment : Fragment() {
    private lateinit var mouse: Mouse
    private lateinit var mainActivity: MainActivity
    private lateinit var sensor: Sensor
    private lateinit var handler: Handler
    private var speed by Delegates.notNull<Int>()
    private lateinit var timer: Timer
    private var active: Boolean = false

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
        return inflater.inflate(R.layout.fragment_3dmouse, container, false)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val airMouseButton: Button = view.findViewById(R.id.button_airmouse)
        val mouseState: Button = view.findViewById(R.id.button_state)

        var measure: TimerTask = object : TimerTask() {
            override fun run() {
                if (active) {
                    var tmp: Pair<FloatArray, FloatArray> = sensor.getDisplacement()
                    var device = mainActivity.getDevice()
                    if (device != null) {
                        mouse.changeState(
                            (tmp.first[0] * speed).roundToInt(),
                            (tmp.first[1] * speed).roundToInt(),
                            (tmp.first[2] * speed).roundToInt(),
                            (tmp.second[2] > +Math.PI / 4), // If rotated anticlockwise, left click
                            (tmp.second[2] < -Math.PI / 4), // If rotated clockwise, right click
                            false,
                            device
                        )
                    }
                }

            }
        }

        mouseState.setOnClickListener { // Navigate to 2D mouse fragment
            val homeFragment2 = Mouse2dFragment()
            val fragmentTransaction = fragmentManager?.beginTransaction()
            fragmentTransaction?.addToBackStack(null)
            fragmentTransaction?.replace(R.id.nav_host_fragment, homeFragment2)
            fragmentTransaction?.commit()
        }

        airMouseButton.setOnTouchListener { v, event -> // Only when button is pressed, measure motion and send values
            try {
                if (event.action == MotionEvent.ACTION_UP) {
                    timer.cancel()
                    active = false
                    sensor.disableSensor()
                } else if (event.action == MotionEvent.ACTION_DOWN) {
                    sensor.reset()
                    sensor.enableSensor()
                    timer.scheduleAtFixedRate(measure, 0, 10) // Every 10 milliseconds, send value
                    active = true
                }
                true
            } catch (e: Exception) {
                false
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        timer.cancel()
        active = false
        sensor.disableSensor()
    }

    override fun onStop() {
        super.onStop()
        timer.cancel()
        active = false
        sensor.disableSensor()
    }
}