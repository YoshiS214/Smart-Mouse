package com.example.smartmouse.ui.mouse

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import com.example.smartmouse.R

class Mouse2dFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_2dmouse, container, false)

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val LeftButton: Button = view.findViewById(R.id.button_left)
        val RightButton: Button = view.findViewById(R.id.button_right)
        val UpButton: Button = view.findViewById(R.id.button_up)
        val DownButton: Button = view.findViewById(R.id.button_down)
        val MouseState: Button = view.findViewById(R.id.button_state)
        LeftButton.setOnClickListener { }
        RightButton.setOnClickListener {  }
        UpButton.setOnClickListener {  }
        DownButton.setOnClickListener {  }
        MouseState.setOnClickListener {
            fragmentManager?.popBackStack()
        }
    }
}