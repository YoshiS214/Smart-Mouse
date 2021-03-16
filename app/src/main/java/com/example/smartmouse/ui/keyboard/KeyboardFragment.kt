package com.example.smartmouse.ui.keyboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ToggleButton
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import com.example.smartmouse.R
import com.google.android.material.textfield.TextInputEditText

class KeyboardFragment : Fragment() {

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {

        val root = inflater.inflate(R.layout.fragment_keyboard, container, false)

        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val textInput: TextInputEditText = view.findViewById(R.id.input_text)
        val escKey: ToggleButton = view.findViewById(R.id.key_esc)
        val delKey: ToggleButton = view.findViewById(R.id.key_del)
        val shiftKey: ToggleButton = view.findViewById(R.id.key_shift)
        val ctrlKey: ToggleButton = view.findViewById(R.id.key_ctrl)
        val fnKey: ToggleButton = view.findViewById(R.id.key_fn)
        val altKey: ToggleButton = view.findViewById(R.id.key_alt)

        textInput.doAfterTextChanged {
            textInput.setText("")
        }
    }
}