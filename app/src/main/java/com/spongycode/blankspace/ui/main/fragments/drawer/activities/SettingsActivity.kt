package com.spongycode.blankspace.ui.main.fragments.drawer.activities

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.MenuItem
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.bumptech.glide.Glide
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.Task
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import com.spongycode.blankspace.R
import com.spongycode.blankspace.ui.auth.fragments.SignInFragment.Companion.firestore
import com.spongycode.blankspace.util.BitmapScaler
import com.spongycode.blankspace.util.Helper
import com.spongycode.blankspace.util.userdata
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.util.*


class SettingsActivity : AppCompatActivity() {

    lateinit var username: EditText
    lateinit var email: TextView
    lateinit var btnSubmit: Button
    lateinit var editProPic: TextView
    lateinit var ivProPic: ImageView
    lateinit var progressBar: ProgressBar
    private var currImageUrl: String = userdata.afterLoginUserData.imageUrl

    private val pickImage = 100

    private var storageReference: StorageReference? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)


        val toolbar: Toolbar = findViewById<View>(R.id.tool_settings) as Toolbar
        setSupportActionBar(toolbar)

        toolbar.setNavigationIcon(R.drawable.ic_baseline_arrow_back_ios_24)

        storageReference = FirebaseStorage.getInstance().reference
        ivProPic = findViewById(R.id.iv_profile_pic)
        Glide.with(this).load(userdata.afterLoginUserData.imageUrl).into(ivProPic)

        progressBar = findViewById(R.id.progress_image)
        progressBar.visibility = GONE


        username = findViewById(R.id.textInputEditText)
        username.setText(userdata.afterLoginUserData.username)
        email = findViewById(R.id.textInputEditText01)
        email.text = userdata.afterLoginUserData.email

        btnSubmit = findViewById(R.id.submitButton)

        btnSubmit.setOnClickListener {
            updateProfile(newImageUrl = currImageUrl, newUsername = username.text.toString())
        }

        editProPic = findViewById(R.id.edit_profile_pic)
        editProPic.setOnClickListener {
            val gallery = Intent(
                Intent.ACTION_PICK,
                MediaStore.Images.Media.INTERNAL_CONTENT_URI
            )
            startActivityForResult(gallery, pickImage)

        }


    }

    private fun updateProfile(newImageUrl: String, newUsername: String) {
        CoroutineScope(Dispatchers.IO).launch {
            if (newImageUrl != userdata.afterLoginUserData.imageUrl) {
                userdata.afterLoginUserData.imageUrl = newImageUrl
                firestore.collection("users").document(userdata.afterLoginUserData.userId)
                    .update("imageUrl", newImageUrl)
                    .addOnCompleteListener {
                        Glide.with(applicationContext).load(userdata.afterLoginUserData.imageUrl)
                            .into(ivProPic)
                        Toast.makeText(
                            applicationContext,
                            "Profile Pic Updated",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    .await()
            }
            if (newUsername != userdata.afterLoginUserData.username) {
                try {
                    if (Helper.isUniqueUsername(newUsername)) {
                        val userRef = firestore.collection("users")
                            .document(userdata.afterLoginUserData.userId)
                        userRef.update("username", newUsername)
                            .addOnCompleteListener {
                                userdata.afterLoginUserData.username = newUsername
                                username.setText(userdata.afterLoginUserData.username)
                                Toast.makeText(
                                    applicationContext,
                                    "Username changed",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                    } else {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(
                                applicationContext,
                                "Pick unique username",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }

                } catch (e: Exception) {
                    Unit
                }


            }


        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)


        if (resultCode == RESULT_OK && requestCode == pickImage && data != null && data.data != null) {
            progressBar.visibility = VISIBLE
            val imageUri: Uri = data.data!!
            val extension: String =
                imageUri.toString().substring(imageUri.toString().lastIndexOf("."))
            val ref = storageReference!!.child("profilepics/${System.currentTimeMillis()}")
            val uploadTask: UploadTask
            uploadTask = if (extension.toLowerCase(Locale.ROOT).trim() != ".gif") {
                val imageByteArray = getImageByteArray(imageUri)
                ref.putBytes(imageByteArray as ByteArray)
            } else {
                ref.putFile(imageUri)
            }

            uploadTask.continueWithTask(Continuation<UploadTask.TaskSnapshot, Task<Uri>> { task ->
                if (!task.isSuccessful) {
                    task.exception?.let {
                        throw it
                    }
                }
                return@Continuation ref.downloadUrl
            }).addOnSuccessListener { task ->
                val downloadUri = task
                currImageUrl = downloadUri.toString()
                progressBar.visibility = GONE
                try {
                    Glide.with(this).load(currImageUrl).into(ivProPic)
                } catch (e: Exception) {

                }
            }

        }

    }


    private fun getImageByteArray(imageUri: Uri): Any {
        val originalBitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val source = ImageDecoder.createSource(contentResolver, imageUri)
            ImageDecoder.decodeBitmap(source)
        } else {
            MediaStore.Images.Media.getBitmap(contentResolver, imageUri)
        }
        val scaledBitmap = BitmapScaler.scaleToFitHeight(originalBitmap, 1000)
        val byteOutputStream = ByteArrayOutputStream()
        scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 25, byteOutputStream)
        return byteOutputStream.toByteArray()
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
        }
        return super.onOptionsItemSelected(item)
    }
}