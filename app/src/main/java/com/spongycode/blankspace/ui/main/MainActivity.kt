package com.spongycode.blankspace.ui.main

import android.Manifest
import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.preference.PreferenceManager
import android.provider.MediaStore
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.NavHostFragment
import androidx.work.*
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage
import com.spongycode.blankspace.R
import com.spongycode.blankspace.databinding.ActivityMainBinding
import com.spongycode.blankspace.storage.PollWorker
import com.spongycode.blankspace.util.Constants.POLL_WORK
import com.spongycode.blankspace.util.Constants.PREF_LAST_RESULT_ID
import com.spongycode.blankspace.util.Constants.PREF_LAST_RESULT_ID_TEXT
import com.spongycode.blankspace.util.Constants.STORAGE_PERMISSION_CODE
import com.spongycode.blankspace.viewmodel.ChatViewModel
import com.spongycode.blankspace.viewmodel.ImageViewModel
import com.spongycode.blankspace.viewmodel.MemeViewModel
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    companion object {
        val firebaseAuth = FirebaseAuth.getInstance()
        val usersCollectionReference = Firebase.firestore.collection("users")
        val firestore = Firebase.firestore
        val storage: FirebaseStorage = Firebase.storage
        lateinit var imageViewModel: ImageViewModel
        lateinit var memeViewModel: MemeViewModel
        lateinit var chatViewModel: ChatViewModel
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        imageViewModel = ViewModelProvider(this).get(ImageViewModel::class.java)
        memeViewModel = ViewModelProvider(this).get(MemeViewModel::class.java)
        chatViewModel = ViewModelProvider(this).get(ChatViewModel::class.java)

        // navigation
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.main_nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController

        val workRequest = PeriodicWorkRequest
            .Builder(PollWorker::class.java, 1, TimeUnit.MINUTES)
            .build()
        WorkManager.getInstance()
            .enqueueUniquePeriodicWork(POLL_WORK, ExistingPeriodicWorkPolicy.KEEP, workRequest)

        binding.navigationView.setNavigationItemSelectedListener (object : NavigationView.OnNavigationItemSelectedListener{
            override fun onNavigationItemSelected(item: MenuItem): Boolean {
                when (item.itemId){
                    R.id.nav_home -> {
                        navController.navigate(R.id.tabLayoutFragment)
                        binding.drawerLayout.close()
                    }
                    R.id.nav_message -> {
                        if (navController.currentDestination?.label == "ChatScreenFragment") return false
                        else navController.navigate(R.id.tabLayoutChatFragment)
                        binding.drawerLayout.close()
                        binding.drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)

                    }
                    R.id.nav_fmemes -> {
                        if (navController.currentDestination?.label == "FMemesFragment") return false
                        else navController.navigate(R.id.action_tabLayoutFragment_to_FMemesFragment)
                        binding.drawerLayout.close()
                        binding.drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
                    }
                    R.id.nav_ftemplates -> {
                        if (navController.currentDestination?.label == "FTemplatesFragment") return false
                        else navController.navigate(R.id.action_tabLayoutFragment_to_FTemplatesFragment)
                        binding.drawerLayout.close()
                        binding.drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
                    }
                    R.id.nav_settings -> {
                        if (navController.currentDestination?.label == "SettingFragment") return false
                        else navController.navigate(R.id.action_tabLayoutFragment_to_settingFragment)
                        binding.drawerLayout.close()
                        binding.drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
                    }
                    R.id.nav_logout -> {
                        FirebaseAuth.getInstance().signOut()
                        navController.navigate(R.id.authActivity); navController.popBackStack(); this@MainActivity.finish() }
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
        } else { // Build < Q
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

object QueryPreferenc {

    fun getLastResultId(context: Context): String {
        return PreferenceManager.getDefaultSharedPreferences(context)
            .getString(PREF_LAST_RESULT_ID, "")!!
    }

    fun setLastResultId(context: Context, lastResultId: String) {
        PreferenceManager.getDefaultSharedPreferences(context).edit {
            putString(PREF_LAST_RESULT_ID, lastResultId)
        }
    }

    fun getLastResultIdText(context: Context): String {
        return PreferenceManager.getDefaultSharedPreferences(context)
            .getString(PREF_LAST_RESULT_ID_TEXT, "")!!
    }

    fun setLastResultIdText(context: Context, lastResultId: String) {
        PreferenceManager.getDefaultSharedPreferences(context).edit {
            putString(PREF_LAST_RESULT_ID_TEXT, lastResultId)
        }
    }
}
















