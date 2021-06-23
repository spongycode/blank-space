package com.spongycode.blankspace.ui.main.fragments.base

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.app.ProgressDialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.DialogFragment
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import com.bumptech.glide.Glide
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.Task
import com.google.firebase.Timestamp
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.UploadTask
import com.spongycode.blankspace.R
import com.spongycode.blankspace.model.modelmemes.MemeModel
import com.spongycode.blankspace.ui.auth.fragments.SignInFragment.Companion.firestore
import com.spongycode.blankspace.util.BitmapScaler
import com.spongycode.blankspace.util.Helper
import com.spongycode.blankspace.util.userdata
import java.io.ByteArrayOutputStream
import java.util.*
import kotlin.collections.HashMap


class MemberEditsDialog : DialogFragment() {

    private val pickImage = 100
    var mStorage = FirebaseStorage.getInstance()
    val mStorageRef = mStorage.reference
    private var downloadUri: Uri? = null

    companion object {
        fun newInstance(imageUrl: String): MemberEditsDialog {
            val args = Bundle()
            args.putString("IMAGE_URL", imageUrl)
            val fragment = MemberEditsDialog()
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.setCancelable(false)
        dialog.setCanceledOnTouchOutside(false)
        return dialog
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.dialog_member_edits, container, false)
    }

    @SuppressLint("CutPasteId")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val testStringArgValue = arguments?.getString("IMAGE_URL")
        Glide.with(requireActivity()).load(testStringArgValue)
            .into(view.findViewById(R.id.member_edits_iv))
        view.findViewById<Button>(R.id.member_edits_btn_post).isEnabled = false
        view.findViewById<Button>(R.id.member_edits_btn_post).alpha = 0.5f
        Helper.buttonEffect( view.findViewById<Button>(R.id.member_edits_btn_post), "#C665F37D")
        view.findViewById<Button>(R.id.member_edits_btn).setOnClickListener {
            val gallery = Intent(
                Intent.ACTION_PICK,
                MediaStore.Images.Media.INTERNAL_CONTENT_URI
            )
            startActivityForResult(gallery, pickImage)
        }
        view.findViewById<Button>(R.id.member_edits_btn_post).setOnClickListener {
            postToFirestore()
            dismiss()
        }

    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == pickImage && data != null && data.data != null) {

            val circularProgressDrawable = CircularProgressDrawable(requireContext())
            circularProgressDrawable.strokeWidth = 10f
            circularProgressDrawable.centerRadius = 50f
            circularProgressDrawable.start()

            Glide.with(requireActivity()).asBitmap().load("")
                .placeholder(circularProgressDrawable)
                .into(requireView().findViewById(R.id.member_edits_post_image))

            requireView().findViewById<Button>(R.id.member_edits_btn).visibility = GONE

            val imageUri: Uri = data.data!!
            val extension: String =
                imageUri.toString().substring(imageUri.toString().lastIndexOf("."))
            val ref = mStorageRef.child("pics/${System.currentTimeMillis()}")
            val uploadTask: UploadTask
            uploadTask = ref.putFile(imageUri)
            if (extension.toLowerCase(Locale.ROOT).trim() != ".gif") {
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
                downloadUri = task

                Glide.with(requireActivity()).load(imageUri)
                    .into(requireView().findViewById(R.id.member_edits_post_image))
                requireView().findViewById<Button>(R.id.member_edits_btn_post).alpha = 1f
                requireView().findViewById<Button>(R.id.member_edits_btn_post).isEnabled = true
            }
        }
    }


    private fun getImageByteArray(imageUri: Uri): Any {
        val originalBitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val source =
                ImageDecoder.createSource(
                    (activity as AppCompatActivity).contentResolver,
                    imageUri
                )
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


    private fun postToFirestore() {
        val progressDialog = ProgressDialog(requireContext())
        progressDialog.setTitle("Uploading...")
        progressDialog.setMessage("Working on it, Please Wait")
        progressDialog.show()


        val eachMeme = MemeModel(
            title = requireView().findViewById<TextView>(R.id.m_e_edit_text).text.toString(),
            url = downloadUri.toString(),
            userId = userdata.afterLoginUserData.userId,
            timestamp = Timestamp.now()
        )
        firestore.collection("memberEdits")
            .add(eachMeme)
            .addOnCompleteListener {
                progressDialog.hide()
            }
    }


}