package com.example.smartmouse.ui.mouse

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import com.example.smartmouse.R

class Mouse3dFragment: Fragment() {

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_3dmouse, container, false)

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val AirMouseButton: Button = view.findViewById(R.id.button_airmouse)
        val MouseState: Button = view.findViewById(R.id.button_state)
        AirMouseButton.setOnClickListener { }
        MouseState.setOnClickListener {
            val homeFragment2 = Mouse2dFragment()
            val fragmentTransaction = fragmentManager?.beginTransaction()
            fragmentTransaction?.addToBackStack(null)
            fragmentTransaction?.replace(R.id.nav_host_fragment, homeFragment2)
            fragmentTransaction?.commit()
        }
    }
}