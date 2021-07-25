package com.spongycode.blankspace.ui.main.fragments.drawer.profile

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.addCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.NavHostFragment
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import com.bumptech.glide.Glide
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.Task
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import com.spongycode.blankspace.R
import com.spongycode.blankspace.databinding.FragmentSettingsBinding
import com.spongycode.blankspace.ui.main.MainActivity.Companion.firestore
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

class SettingFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!
    lateinit var username: EditText
    lateinit var status: EditText
    lateinit var email: TextView
    lateinit var btnSubmit: Button
    lateinit var editProPic: TextView
    lateinit var ivProPic: ImageView
    private var currImageUrl: String = userdata.afterLoginUserData.imageUrl


    private val pickImage = 100

    private var storageReference: StorageReference? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)

        val toolbar: Toolbar = binding.toolSettings
        (activity as AppCompatActivity?)!!.setSupportActionBar(toolbar)

        val navHostFragment =
            (activity as AppCompatActivity).supportFragmentManager.findFragmentById(R.id.main_nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController

        toolbar.setNavigationIcon(R.drawable.ic_baseline_arrow_back_ios_24)
        binding.toolSettings.setNavigationOnClickListener {
            navController.navigate(R.id.action_settingFragment_to_tabLayoutFragment)
        }
        requireActivity().onBackPressedDispatcher.addCallback {
            navController.navigate(R.id.action_settingFragment_to_tabLayoutFragment)
        }
        storageReference = FirebaseStorage.getInstance().reference
        ivProPic = binding.ivProfilePic
        if(userdata.afterLoginUserData.imageUrl != ""){
            Glide.with(this).load(userdata.afterLoginUserData.imageUrl).into(ivProPic)
        }

        username = binding.textInputEditText
        username.setText(userdata.afterLoginUserData.username)
        status = binding.textInputStatus
        if (userdata.afterLoginUserData.status != "") {
            status.setText(userdata.afterLoginUserData.status)
        }
        email = binding.textInputEditText01
        email.text = userdata.afterLoginUserData.email

        btnSubmit = binding.submitButton
        enableBtn(false)
        Helper.buttonEffect(btnSubmit, "#C665F37D")


        btnSubmit.setOnClickListener {
            updateProfile(
                newImageUrl = currImageUrl,
                newUsername = username.text.toString(),
                newStatus = status.text.toString()
            )
        }

        editProPic = binding.editProfilePic
        editProPic.setOnClickListener {
            val gallery = Intent(
                Intent.ACTION_PICK,
                MediaStore.Images.Media.INTERNAL_CONTENT_URI
            )
            startActivityForResult(gallery, pickImage)
        }


        status.addTextChangedListener(object : TextWatcher {
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                val state =
                    (s.toString() != userdata.afterLoginUserData.status ||
                            username.text.toString() != userdata.afterLoginUserData.username ||
                            currImageUrl != userdata.afterLoginUserData.imageUrl)
                enableBtn(state)
            }

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
                // no use for now
            }

            override fun afterTextChanged(s: Editable) {
                // no use for now
            }
        })

        username.addTextChangedListener(object : TextWatcher {
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                val state =
                    (s.toString() != userdata.afterLoginUserData.username ||
                            status.text.toString() != userdata.afterLoginUserData.status ||
                            currImageUrl != userdata.afterLoginUserData.imageUrl)
                enableBtn(state)
            }

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
                // no use for now
            }

            override fun afterTextChanged(s: Editable) {
                // no use for now
            }
        })












        return binding.root
    }

    private fun enableBtn(state: Boolean) {
        btnSubmit.isEnabled = state
        if (!state) {
            btnSubmit.alpha = 0.5f
        }else{
            btnSubmit.alpha = 1f
        }
    }

    private fun updateProfile(newImageUrl: String, newUsername: String, newStatus: String) {
        CoroutineScope(Dispatchers.IO).launch {
            if (newImageUrl != userdata.afterLoginUserData.imageUrl || newStatus != userdata.afterLoginUserData.status) {
                userdata.afterLoginUserData.imageUrl = newImageUrl
                userdata.afterLoginUserData.status = newStatus
                firestore.collection("users").document(userdata.afterLoginUserData.userId)
                    .update("imageUrl", newImageUrl, "status", newStatus)
                    .addOnCompleteListener {
//                        Glide.with(requireActivity()).load(userdata.afterLoginUserData.imageUrl)
//                            .into(ivProPic)
//                        Toast.makeText(
//                            requireContext(),
//                            "Profile Pic Updated",
//                            Toast.LENGTH_SHORT
//                        ).show()
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
                                Snackbar.make(
                                    (activity as AppCompatActivity).findViewById(android.R.id.content),
                                    "Profile Updated.",
                                    Snackbar.LENGTH_SHORT
                                ).show()
                                enableBtn(false)
                            }
                    } else {
                        withContext(Dispatchers.Main) {
                            Snackbar.make(
                                (activity as AppCompatActivity).findViewById(android.R.id.content),
                                "Pick unique username.",
                                Snackbar.LENGTH_SHORT
                            ).show()
                        }
                    }
                } catch (e: Exception) {

                }
            } else {
                withContext(Dispatchers.Main) {
                    Snackbar.make(
                        (activity as AppCompatActivity).findViewById(android.R.id.content),
                        "Profile Updated.",
                        Snackbar.LENGTH_SHORT
                    ).show()
                    enableBtn(false)
                }
            }
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK && requestCode == pickImage && data != null && data.data != null) {
//            progressBar.visibility = VISIBLE
            val circularProgressDrawable = CircularProgressDrawable(requireContext())
            circularProgressDrawable.strokeWidth = 10f
            circularProgressDrawable.centerRadius = 50f
            circularProgressDrawable.start()
            Glide.with(this).load("").placeholder(circularProgressDrawable).into(ivProPic)


            val imageUri: Uri = data.data!!
//            val extension: String =
//                imageUri.toString().substring(imageUri.toString().lastIndexOf("."))
            val ref = storageReference!!.child("profilepics/${System.currentTimeMillis()}")
            val uploadTask: UploadTask
            uploadTask = if (true) {
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
//                progressBar.visibility = GONE
                try {
                    Glide.with(this).load(imageUri).placeholder(circularProgressDrawable).into(ivProPic)
                    enableBtn(true)
                } catch (e: Exception) {
                }
            }
        }
    }


    private fun getImageByteArray(imageUri: Uri): Any {
        val originalBitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val source =
                ImageDecoder.createSource((activity as AppCompatActivity).contentResolver, imageUri)
            ImageDecoder.decodeBitmap(source)
        } else {
            MediaStore.Images.Media.getBitmap(
                (activity as AppCompatActivity).contentResolver,
                imageUri
            )
        }
        val scaledBitmap = BitmapScaler.scaleToFitHeight(originalBitmap, 1000)
        val byteOutputStream = ByteArrayOutputStream()
        scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 25, byteOutputStream)
        return byteOutputStream.toByteArray()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            null
        }
        return super.onOptionsItemSelected(item)
    }

}




