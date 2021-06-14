package com.example.smartmouse.ui.touchpad

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.GestureDetectorCompat
import androidx.fragment.app.Fragment
import com.example.smartmouse.MainActivity
import com.example.smartmouse.R
import com.example.smartmouse.TrackPadGesture
import com.example.smartmouse.bluetooth.Mouse
import com.example.smartmouse.bluetooth.bDevice
import kotlin.math.absoluteValue
import kotlin.math.max

class TouchpadFragment : Fragment() {
    private lateinit var detector: GestureDetectorCompat
    private lateinit var gesture: TrackPadGesture
    private lateinit var mainActivity: MainActivity

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_touchpad, container, false)
        mainActivity = activity as MainActivity
        gesture = TrackPadGesture(mainActivity)
        detector = GestureDetectorCompat(mainActivity.applicationContext, gesture)
        return root
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val trackpad: TextView = view.findViewById(R.id.text_trackpad)
        var moved: Boolean = false
        var time: Long = 0L
        val speed: Int = mainActivity.getSpeed()
        var mouse: Mouse = mainActivity.getMouse()
        var drag: Boolean = false
        var previousPosition: Pair<Float, Float> = Pair(0F,0F)
        var distance: Pair<Float, Float> = Pair(0F,0F)
        var first: Boolean = true
        var multiTap: Boolean = false

        trackpad.setOnTouchListener { v, event ->
            /*
            if (event.action == MotionEvent.ACTION_POINTER_3_DOWN){
                gesture.onMultiTap()
            }
            detector.onTouchEvent(event)
             */
            Log.d("Trackpad", "\nfirst: "+first.toString()+"\nmoved: "+moved.toString()+"\npx: "+previousPosition.first.toString()+" py: "+previousPosition.second.toString()+"\nx: "+distance.first.toString()+" y: "+distance.second.toString())
            when(event.action){
                MotionEvent.ACTION_DOWN -> {
                    Log.d("Trackpad", "Single tap")
                    moved = false
                    drag = false
                    first = true
                    time = System.currentTimeMillis()
                    previousPosition = Pair(event.x, event.y)
                }
                MotionEvent.ACTION_POINTER_2_DOWN ->{
                    Log.d("Trackpad", "Multi tap")
                    moved = false
                    drag = false
                    first = true
                    multiTap = true
                    time = System.currentTimeMillis()
                    previousPosition = Pair(event.x, event.y)
                }
                MotionEvent.ACTION_UP -> {
                    if (System.currentTimeMillis() - time < 500 && !moved){
                        if (!multiTap){
                            gesture.onSingleTapUp(event)
                        }else{
                            gesture.onMultiTap()
                        }
                    }
                    moved = false
                    drag = false
                    first =true
                    multiTap = false
                }
                MotionEvent.ACTION_MOVE ->{
                    if(!first){
                        distance = Pair(event.x - previousPosition.first, event.y - previousPosition.second)
                    }
                    first = false
                    previousPosition = Pair(event.x, event.y)
                    if (max(distance.first.absoluteValue, distance.second.absoluteValue) > 1 && !moved){
                        moved = true
                        if (System.currentTimeMillis() - time > 1000){
                            Toast.makeText(mainActivity.applicationContext, "Drag enabled", Toast.LENGTH_SHORT).show()
                            drag = true
                        }
                    }
                    when(event.pointerCount){
                        1 -> {
                            var device: bDevice? = mainActivity.getDevice()
                            if (device != null && moved) {
                                mouse.changeState((distance.first*speed/10).toInt(), (distance.second*speed/10).toInt(), 0,
                                    left = drag,
                                    right = false,
                                    middle = false, device)
                            }
                        }
                        2 -> {
                            var device: bDevice? = mainActivity.getDevice()
                            if (device != null && moved) {
                                mouse.changeState(0, 0, (distance.second*speed/200).toInt(),
                                    left = false,
                                    right = false,
                                    middle = false, device)
                            }
                        }
                    }
                }
            }

            true
        }
    }
}