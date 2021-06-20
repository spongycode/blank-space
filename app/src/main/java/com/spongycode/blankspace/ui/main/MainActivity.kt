package com.spongycode.blankspace.ui.main

import android.Manifest
import android.app.Activity
import android.content.ContentValues
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Point
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.DisplayMetrics
import android.util.Log
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.navigation.fragment.NavHostFragment
import com.google.android.material.navigation.NavigationView
import com.spongycode.blankspace.R
import com.spongycode.blankspace.databinding.ActivityMainBinding
import com.spongycode.blankspace.util.Constants.STORAGE_PERMISSION_CODE
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream


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

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setIcon(R.drawable.ic_troll_face)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_burger)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)


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
                    R.id.nav_home -> { navController.navigate(R.id.tabLayoutFragment) }
                    R.id.nav_message -> { navController.navigate(R.id.groupChatFragment) }
                    R.id.nav_fmemes -> { navController.navigate(R.id.FMemesFragment) }
                    R.id.nav_ftemplates -> { navController.navigate(R.id.FTemplatesFragment) }
                    R.id.nav_profile -> { navController.navigate(R.id.myProfileFragment) }
                    R.id.nav_settings -> { navController.navigate(R.id.settingFragment) }
                    R.id.nav_logout -> { navController.navigate(R.id.authActivity); this@MainActivity.finish() }
                }
                return true
            }
        })

    }

    fun saveImage(activity: Activity, image: Drawable, title: String) {

        checkPermission(activity)

        val bitmap = (image as BitmapDrawable).bitmap

        // File name
        val filename = "$title.jpg"

        // Output stream
        var outputStream: OutputStream? = null

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q){
            activity.applicationContext.contentResolver?.also { contentResolver ->
                val contentValues = ContentValues().apply {
                    // update file info
                    put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
                    put(MediaStore.MediaColumns.MIME_TYPE, "image/jpg")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
                }

                // Inserting the contentValues to contentReolver and getting the Uri
                val imageUri: Uri? =
                    contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)

                // Opening an outputStream with tue Uri
                outputStream = imageUri?.let { contentResolver.openOutputStream(it) }
            }
        } else { // Build > Q
            val imagesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
            val image = File(imagesDir, filename)
            outputStream = FileOutputStream(image)
        }

        outputStream?.use {
            // writing the file
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, it)
            Toast.makeText(activity.applicationContext, "saved to photos", Toast.LENGTH_SHORT).show()
        }
    }

    // doesn't work
    fun getBitmapFromView(activity: Activity, bmp: Bitmap?): Uri? {
        checkPermission(activity)
        val imageFolder = File(activity.applicationContext.cacheDir, "images")
        var uri: Uri? = null

        try {
            imageFolder.mkdir()
            val file = File(imageFolder, "shared_image.jpg")
            val outputStream = FileOutputStream(file)
            outputStream.flush()
            outputStream.close()
            uri = FileProvider.getUriForFile(activity.applicationContext, "com.rick.shareimage.fileprovider", file)
            bmp?.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
        } catch ( e: IOException){
            e.message?.let {
                Toast.makeText(activity.applicationContext, "failure", Toast.LENGTH_SHORT).show()
                Log.d("share", it)
            }
        }
        return uri
    }

    private fun checkPermission(activity: Activity){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            if (ContextCompat.checkSelfPermission(activity.applicationContext, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_DENIED || ContextCompat.checkSelfPermission(activity.applicationContext, Manifest.permission.READ_EXTERNAL_STORAGE)
            == PackageManager.PERMISSION_DENIED  ){ // Request Permission
                ActivityCompat.requestPermissions(activity,
                    arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE),
                    STORAGE_PERMISSION_CODE)
            }
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
                Toast.makeText(this@MainActivity, "Permission Granted", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this@MainActivity, "Permission Denied", Toast.LENGTH_SHORT).show()
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