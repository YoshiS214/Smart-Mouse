package com.example.smartmouse

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.view.Menu
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.smartmouse.bluetooth.Keyboard
import com.example.smartmouse.bluetooth.Mouse
import com.example.smartmouse.bluetooth.bDevice
import com.google.android.material.navigation.NavigationView

class MainActivity : AppCompatActivity() {
    private lateinit var appBarConfig: AppBarConfiguration
    private lateinit var mouse: Mouse
    private lateinit var keyboard: Keyboard
    private var selectedDevice: bDevice? = null   //Device wanted to control
    private lateinit var sensor: Sensor
    private var speed: Int = 0  // Speed of cursor moving
    private var hidEnabled: Boolean = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val toolbar: Toolbar = findViewById(R.id.toolbar)

        val deviceSpinner: Spinner = findViewById(R.id.spinner_selectDevice)        // Spinner to choose device to control
        var adapter =
            ArrayAdapter(applicationContext, R.layout.spinner_style, arrayOf("No device found"))
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        deviceSpinner.adapter = adapter

        val handler: Handler = Handler()
        var runnable: Runnable = Runnable { }
        runnable = Runnable {       // Every 5 sec, refresh the list of connected device
            var devices: Array<bDevice> = mouse.connectedDevice()
            var names = devices.map { x -> x.name }
            adapter = ArrayAdapter(applicationContext, R.layout.spinner_style, names)
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            deviceSpinner.adapter = adapter
            handler.postDelayed(runnable, 5000)
        }
        handler.post(runnable)

        deviceSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?, position: Int, id: Long
            ) {     // On item selected, find the bluetooth device whose name is equal to selected item
                val spinnerParent = parent as Spinner
                val item = spinnerParent.selectedItem as String
                selectedDevice = mouse.connectedDevice().find { x -> x.name == item }!!
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
        }

        setSupportActionBar(toolbar)

        // Set up drawer and navigation
        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
        val navView: NavigationView = findViewById(R.id.nav_view)
        val navController = findNavController(R.id.nav_host_fragment)
        appBarConfig = AppBarConfiguration(
            setOf(
                R.id.nav_mouse,
                R.id.nav_cross,
                R.id.nav_keyboard,
                R.id.nav_touchpad,
                R.id.nav_setting
            ), drawerLayout
        )
        setupActionBarWithNavController(navController, appBarConfig)
        navView.setupWithNavController(navController)

        mouse = Mouse(this)
        keyboard = Keyboard(this)

        sensor = Sensor(this)
        // Load value of speed stored and put 10 if null
        speed = if (DataStore.getMouseSpeed(applicationContext) != null) DataStore.getMouseSpeed(
            applicationContext
        )!! else 10
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inputMethodManager: InputMethodManager? =
            getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager?.hideSoftInputFromWindow(this.currentFocus?.windowToken, 0)
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment)
        val nameText = findViewById<TextView>(R.id.text_computerName)       // Name of selected device is shown in drawer
        val macText = findViewById<TextView>(R.id.text_mac)                 // Mac address of selected device is shown in drawer
        nameText.text = selectedDevice?.name
        macText.text = selectedDevice?.address
        return navController.navigateUp(appBarConfig) || super.onSupportNavigateUp()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (mouse.isStarted()) {
            mouse.stop()
        }
        if (keyboard.isStarted()) {
            keyboard.stop()
        }
    }

    fun getMouse(): Mouse {
        return mouse
    }

    fun updateMouse(mouse: Mouse) {
        this.mouse = mouse
    }

    fun getKeyboard(): Keyboard {
        return keyboard
    }

    fun updateKeyboard(keyboard: Keyboard) {
        this.keyboard = keyboard
    }

    fun isHidEnabled(): Boolean{
        return hidEnabled
    }

    fun enableHid(){
        hidEnabled = true
    }

    fun disableHid(){
        hidEnabled = false
    }

    fun getDevice(): bDevice? {
        return selectedDevice
    }

    fun getSensor(): Sensor {
        return sensor
    }

    fun getSpeed(): Int {
        return speed
    }

    fun updateSpeed(new: Int) {
        speed = new
        DataStore.writeMouseSpeed(applicationContext, new)
    }

}