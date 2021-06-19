package com.spongycode.blankspace.ui.main

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Point
import android.os.Build
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.navigation.NavHostController
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.viewpager.widget.ViewPager
import com.google.android.material.navigation.NavigationView
import com.google.android.material.tabs.TabLayout
import com.google.firebase.auth.FirebaseAuth
import com.spongycode.blankspace.R
import com.spongycode.blankspace.databinding.ActivityMainBinding
import com.spongycode.blankspace.ui.auth.AuthActivity
import com.spongycode.blankspace.ui.main.adapters.MainAdapter
import com.spongycode.blankspace.ui.main.fragments.base.TabLayoutFragment
import com.spongycode.blankspace.ui.main.fragments.drawer.favorite.FMemesFragment
import com.spongycode.blankspace.ui.main.fragments.drawer.favorite.FTemplatesFragment
import com.spongycode.blankspace.util.Constants.STORAGE_PERMISSION_CODE


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    lateinit var actionBarDrawerToggle: ActionBarDrawerToggle

    companion object {
        var width: Int? = null
        var height: Int? = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

        val fm = supportFragmentManager
//        fm
//            .beginTransaction()
//            .add(R.id.frameLayout, TabLayoutFragment())
//            .commit()


        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

//        setSupportActionBar(binding.)

        // Set app drawer
        actionBarDrawerToggle = ActionBarDrawerToggle(this, binding.drawerLayout, binding.toolbar, R.string.open_drawer, R.string.close_drawer)
        actionBarDrawerToggle.isDrawerIndicatorEnabled = true
//        binding.navigateUp.setOnClickListener { binding.drawerLayout.openDrawer(GravityCompat.START) }

        width = screenSizeInDp.x
        height = screenSizeInDp.y

        val navHostFragment = supportFragmentManager.findFragmentById(R.id.main_nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController

        binding.navigationView.setNavigationItemSelectedListener (object : NavigationView.OnNavigationItemSelectedListener{
            override fun onNavigationItemSelected(item: MenuItem): Boolean {
                when (item.itemId){
                    R.id.nav_fmemes -> {
                        navController.navigate(R.id.action_tabLayoutFragment_to_FMemesFragment)
                    }
                    R.id.nav_ftemplates -> {

                        navController.navigate(R.id.action_tabLayoutFragment_to_FTemplatesFragment)
                    }
                }
                return true
            }
        })

    }

    private fun checkPermission(permission: String){
     if (ContextCompat.checkSelfPermission(this@MainActivity, permission)
     == PackageManager.PERMISSION_DENIED){ // Request Permission
         ActivityCompat.requestPermissions(this, arrayOf(permission), STORAGE_PERMISSION_CODE)
     } else {
         Toast.makeText(this, "Permission granted", Toast.LENGTH_SHORT).show()
     }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == STORAGE_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this@MainActivity, "Camera Permission Granted", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this@MainActivity, "Camera Permission Denied", Toast.LENGTH_SHORT).show()
            }
        }
    }
}

// I was trying to do something extra, but didn't workout.

// extension property to get display metrics instance
val Activity.displayMetrics: DisplayMetrics
    get() {
        val displayMetrics = DisplayMetrics()
        if (Build.VERSION.SDK_INT >= 30) {
            display?.getRealMetrics(displayMetrics)
        } else {
            // getMetrica was deprecated in api level 30
            windowManager.defaultDisplay.getMetrics(displayMetrics)
        }
        return displayMetrics
    }

// Extension property to get screen widht an dheight in dp
val Activity.screenSizeInDp: Point
    get() {
        val point = Point()
        displayMetrics.apply {
            point.x = widthPixels
            point.y = heightPixels
        }
        return point
    }