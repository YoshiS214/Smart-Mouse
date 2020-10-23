package com.example.smartmouse.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.ToggleButton
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.example.smartmouse.R

class HomeFragment : Fragment() {

    private lateinit var homeViewModel: HomeViewModel

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        homeViewModel =
                ViewModelProviders.of(this).get(HomeViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_home, container, false)
        val AirMouseButton: Button = root.findViewById(R.id.button_airmouse)
        val MouseState: ToggleButton = root.findViewById(R.id.toggle_mouse)
        AirMouseButton.setOnClickListener { }
        MouseState.setOnClickListener {  }
        return root
    }
}