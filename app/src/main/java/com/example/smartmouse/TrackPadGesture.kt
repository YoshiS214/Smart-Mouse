package com.example.smartmouse

import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent
import android.widget.Toast
import com.example.smartmouse.MainActivity
import com.example.smartmouse.bluetooth.Mouse
import com.example.smartmouse.bluetooth.bDevice

class TrackPadGesture(mainActivity: MainActivity) : GestureDetector.SimpleOnGestureListener() {
    private var mouse : Mouse = mainActivity.getMouse()
    private var velocity : Int = mainActivity.getSpeed()
    private var mainActivity: MainActivity = mainActivity
    private var drag : Boolean = false

    override fun onScroll(
        e1: MotionEvent?,
        e2: MotionEvent?,
        distanceX: Float,
        distanceY: Float
    ): Boolean {
        var device: bDevice? = mainActivity.getDevice()
        if (device != null) {
            if (e2 != null) {
                if(e2.pointerCount != 1){
                    mouse.changeState(0, 0, (distanceY*velocity/-200).toInt(),
                        left = false,
                        right = false,
                        middle = false, device)
                }else{
                    mouse.changeState((distanceX*velocity/-10).toInt(), (distanceY*velocity/-10).toInt(), 0,
                        left = drag,
                        right = false,
                        middle = false, device)
                }
            }

        }
        return super.onScroll(e1, e2, distanceX, distanceY)
    }

    override fun onDoubleTap(e: MotionEvent?): Boolean {
        drag = !drag
        Toast.makeText(mainActivity.applicationContext, (if(drag)"Drag enabled" else "Drag disabled"), Toast.LENGTH_SHORT).show()
        return super.onDoubleTap(e)
    }

    override fun onSingleTapUp(e: MotionEvent?): Boolean {
        var device: bDevice? = mainActivity.getDevice()
        if (device != null) {
            mouse.changeState(
                0, 0, 0,
                left = true,
                right = false,
                middle = false, device
            )
            mouse.nullValue(device)
        }
        return super.onSingleTapUp(e)
    }

    fun onMultiTap(){
        var device: bDevice? = mainActivity.getDevice()
        if (device != null) {
            mouse.changeState(
                0, 0, 0,
                left = false,
                right = true,
                middle = false, device
            )
            mouse.nullValue(device)
        }
    }
}