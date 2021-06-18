package com.spongycode.blankspace.ui.main

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Point
import android.os.Build
import android.os.Bundle
import android.util.DisplayMetrics
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
import androidx.viewpager.widget.ViewPager
import com.google.android.material.navigation.NavigationView
import com.google.android.material.tabs.TabLayout
import com.google.firebase.auth.FirebaseAuth
import com.spongycode.blankspace.R
import com.spongycode.blankspace.databinding.ActivityMainBinding
import com.spongycode.blankspace.ui.auth.AuthActivity
import com.spongycode.blankspace.ui.main.adapters.MainAdapter
import com.spongycode.blankspace.util.Constants.STORAGE_PERMISSION_CODE


class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var binding: ActivityMainBinding
    lateinit var tabLayout: TabLayout
    lateinit var viewPager: ViewPager
    lateinit var actionBarDrawerToggle: ActionBarDrawerToggle

    companion object {
        var width: Int? = null
        var height: Int? = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)


        // Set app drawer
        actionBarDrawerToggle = ActionBarDrawerToggle(this, binding.drawerLayout, R.string.open_drawer, R.string.close_drawer)
        actionBarDrawerToggle.isDrawerIndicatorEnabled = true
//        binding.navigateUp.setOnClickListener { binding.drawerLayout.openDrawer(GravityCompat.START) }

        width = screenSizeInDp.x
        height = screenSizeInDp.y

        tabLayout = findViewById(R.id.tabLayout)
        viewPager = findViewById(R.id.viewPager)
        tabLayout.addTab(tabLayout.newTab().setText("Home"))
        tabLayout.addTab(tabLayout.newTab().setText("Generate"))
        tabLayout.tabGravity = TabLayout.GRAVITY_FILL
        val adapter = MainAdapter(
            this, supportFragmentManager,
            tabLayout.tabCount
        )
        viewPager.adapter = adapter

        viewPager.addOnPageChangeListener(TabLayout.TabLayoutOnPageChangeListener(tabLayout))
        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                viewPager.currentItem = tab.position
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {}
            override fun onTabReselected(tab: TabLayout.Tab) {}
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

//
//    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
//        menuInflater.inflate(R.menu.drawer_menu, menu)
//        return true
//    }
//
//    override fun onOptionsItemSelected(item: MenuItem): Boolean {
//        return when (item.itemId) {
//            R.id.signOut -> {
//                FirebaseAuth.getInstance().signOut()
//                val intent = Intent(this, AuthActivity::class.java)
//                startActivity(intent)
//                this.finish()
//                true
//            }
//            else -> super.onOptionsItemSelected(item)
//        }
//    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.nav_account -> { Toast.makeText(this, "Publication", Toast.LENGTH_LONG).show() }
        }
        binding.drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    override fun onBackPressed() {
        if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)){
            binding.drawerLayout.closeDrawer(GravityCompat.START)
        } else { super.onBackPressed() }
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