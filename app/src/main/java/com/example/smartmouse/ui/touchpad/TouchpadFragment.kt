package com.example.smartmouse.ui.touchpad

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.view.GestureDetectorCompat
import androidx.fragment.app.Fragment
import com.example.smartmouse.MainActivity
import com.example.smartmouse.R
import com.example.smartmouse.TrackPadGesture
import com.example.smartmouse.bluetooth.Mouse

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

        trackpad.setOnTouchListener { v, event ->
            if (event.action == MotionEvent.ACTION_POINTER_2_DOWN){
                gesture.onMultiTap()
            }
            detector.onTouchEvent(event)
        }
    }
}