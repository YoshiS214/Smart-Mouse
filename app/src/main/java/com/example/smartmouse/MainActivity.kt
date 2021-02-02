package com.example.smartmouse

import android.app.Application
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.smartmouse.bluetooth.BLE
import com.example.smartmouse.bluetooth.Mouse
import com.example.smartmouse.ui.setting.SettingFragment
import com.google.android.material.navigation.NavigationView
import kotlinx.coroutines.internal.artificialFrame

class MainActivity : AppCompatActivity(){
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var mouse: Mouse


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
        val navView: NavigationView = findViewById(R.id.nav_view)
        val navController = findNavController(R.id.nav_host_fragment)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.

        mouse = Mouse(this)
        mouse.setPeripheralProvider()

        appBarConfiguration = AppBarConfiguration(setOf(
            R.id.nav_mouse, R.id.nav_cross, R.id.nav_keyboard, R.id.nav_touchpad, R.id.nav_setting), drawerLayout)

        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)


    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    fun getMouse(): Mouse{
        return mouse
    }

}