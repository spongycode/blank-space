package com.spongycode.blankspace.ui.main

import android.app.Activity
import android.content.Intent
import android.graphics.Point
import android.os.Build
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.viewpager.widget.ViewPager
import com.google.android.material.tabs.TabLayout
import com.google.firebase.auth.FirebaseAuth
import com.spongycode.blankspace.R
import com.spongycode.blankspace.databinding.ActivityMainBinding
import com.spongycode.blankspace.ui.auth.AuthActivity
import com.spongycode.blankspace.ui.main.adapters.MainAdapter


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    lateinit var tabLayout: TabLayout
    lateinit var viewPager: ViewPager

    companion object {
        var width: Int? = null
        var height: Int? = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)//

        setSupportActionBar(binding.toolbar)

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

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.signOut -> {
                FirebaseAuth.getInstance().signOut()
                val intent = Intent(this, AuthActivity::class.java)
                startActivity(intent)
                this.finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
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