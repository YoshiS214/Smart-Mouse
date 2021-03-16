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
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.smartmouse.bluetooth.Mouse
import com.example.smartmouse.bluetooth.bDevice
import com.google.android.material.navigation.NavigationView

class MainActivity : AppCompatActivity(){
    private lateinit var appBarConfig: AppBarConfiguration
    private lateinit var mouse: Mouse
    private lateinit var selectedDevice: bDevice
    private lateinit var sensor: Sensor
    private var speed: Int = if (DataStore.getMouseSpeed(applicationContext) != null) DataStore.getMouseSpeed(applicationContext)!! else 10


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        val deviceSpinner: Spinner = findViewById(R.id.spinner_selectDevice)
        var adapter = ArrayAdapter(applicationContext, android.R.layout.simple_spinner_item, arrayOf("No device found"))
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        deviceSpinner.adapter = adapter
        val handler: Handler = Handler()
        var runnable: Runnable = Runnable {  }
        runnable = Runnable {
            var devices = mouse.connectedDevice()
            var names = devices.map { x -> x.name }
            adapter = ArrayAdapter(applicationContext, android.R.layout.simple_spinner_item, names)
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            deviceSpinner.adapter = adapter
            handler.postDelayed(runnable, 10000)
        }

        handler.post(runnable)

        deviceSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onItemSelected(parent: AdapterView<*>?,
                                        view: View?, position: Int, id: Long) {
                val spinnerParent = parent as Spinner
                val item = spinnerParent.selectedItem as String
                selectedDevice = mouse.connectedDevice().find { x -> x.name == item }!!

            }
            override fun onNothingSelected(parent: AdapterView<*>?) {

            }
        }
        setSupportActionBar(toolbar)

        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
        val navView: NavigationView = findViewById(R.id.nav_view)
        val navController = findNavController(R.id.nav_host_fragment)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.

        mouse = Mouse(this)
        mouse.setPeripheralProvider()

        sensor = Sensor()

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
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        val inputMethodManager: InputMethodManager? = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

        inputMethodManager?.hideSoftInputFromWindow(this.currentFocus?.windowToken, 0)

        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment)
        return navController.navigateUp(appBarConfig) || super.onSupportNavigateUp()
    }

    fun getMouse(): Mouse{
        return mouse
    }

    fun updateMouse(mouse: Mouse){
        this.mouse = mouse
    }

    fun getDevice(): bDevice{
        return selectedDevice
    }

    fun getSensor():Sensor{
        return sensor
    }

    fun getSpeed():Int{
        return speed
    }

    fun updateSpeed(new : Int){
        speed = new
        DataStore.writeMouseSpeed(applicationContext, new)
    }

    override fun onStop() {
        super.onStop()
        mouse.storeData()
    }

}