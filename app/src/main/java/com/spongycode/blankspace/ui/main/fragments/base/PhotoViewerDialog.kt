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
import com.spongycode.blankspace.util.BitmapScaler
import com.spongycode.blankspace.util.Helper
import com.spongycode.blankspace.util.userdata
import java.io.ByteArrayOutputStream
import java.util.*
import kotlin.collections.HashMap


class PhotoViewerDialog : DialogFragment() {

    companion object {
        fun newInstance(photoViewUrl: String): PhotoViewerDialog {
            val args = Bundle()
            args.putString("PHOTO_VIEW_URL", photoViewUrl)
            val fragment = PhotoViewerDialog()
            fragment.arguments = args
            return fragment
        }
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.dialog_photo_viewer, container, false)
    }

    @SuppressLint("CutPasteId")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val photoViewUrl = arguments?.getString("PHOTO_VIEW_URL")
        Glide.with(requireActivity()).load(photoViewUrl)
            .into(view.findViewById(R.id.fullscreen_content))

    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )
    }

}