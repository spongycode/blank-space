package com.spongycode.blankspace.ui.edit

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Typeface
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.res.ResourcesCompat
import com.bumptech.glide.Glide
import com.spongycode.blankspace.R
import com.spongycode.blankspace.ui.edit.fragments.TextEditorDialogFragment
import ja.burhanrashid52.photoeditor.*
import java.util.*

class EditActivity : AppCompatActivity() {

    private val pickImage = 100
    private var imageRouteClear = true
    lateinit var mPhotoEditorView: PhotoEditorView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit)

        val url = intent?.getStringExtra("imageurl")
        mPhotoEditorView = findViewById(R.id.photo_editor_view)

        if (url == "none") {
            // Pick Image
            val gallery = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI)
            startActivityForResult(gallery, pickImage)
        } else {
            Glide.with(this).load(url).into(mPhotoEditorView.source)
        }


        val mTextRobotoTf = ResourcesCompat.getFont(this, R.font.impact)
        val mPhotoEditor = PhotoEditor.Builder(this, mPhotoEditorView)
            .setPinchTextScalable(true)
            .setDefaultTextTypeface(mTextRobotoTf)
            .build()


        findViewById<ImageButton>(R.id.meme_undo).setOnClickListener {
            mPhotoEditor.undo()
        }

        findViewById<ImageButton>(R.id.meme_redo).setOnClickListener {
            mPhotoEditor.redo()
        }


        findViewById<ImageButton>(R.id.meme_add_text_with_bg).setOnClickListener {
            val typeface: Typeface? = ResourcesCompat.getFont(this, R.font.impact)
            val textStyleBuilder: TextStyleBuilder = TextStyleBuilder()
            textStyleBuilder.withTextSize(40F)
            textStyleBuilder.withTextColor(resources.getColor(R.color.black))
            textStyleBuilder.withTextFont(typeface!!)
            textStyleBuilder.withBackgroundColor(resources.getColor(R.color.white_trans))
            mPhotoEditor.addText("Hold to Edit", textStyleBuilder)
        }
        findViewById<ImageButton>(R.id.meme_add_text_no_bg).setOnClickListener {
            val typeface: Typeface? = ResourcesCompat.getFont(this, R.font.impact)
            val textStyleBuilder: TextStyleBuilder = TextStyleBuilder()
            textStyleBuilder.withTextSize(40F)
            textStyleBuilder.withTextColor(resources.getColor(R.color.black))
            textStyleBuilder.withTextFont(typeface!!)
            mPhotoEditor.addText("Hold to Edit", textStyleBuilder)
        }





        findViewById<ImageButton>(R.id.save_local).setOnClickListener {
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                mPhotoEditor.saveAsFile(
                    Environment.getExternalStorageDirectory().toString() + "/blank_meme.jpg",
                    object : PhotoEditor.OnSaveListener {
                        override fun onSuccess(imagePath: String) {
                            Toast.makeText(applicationContext, "Image Saved", Toast.LENGTH_LONG).show()
                        }

                        override fun onFailure(exception: Exception) {
                            Toast.makeText(
                                applicationContext,
                                exception.toString(),
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    })
            } else {
                val PERMISSIONS_STORAGE = arrayOf(
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                )
                val REQUEST_EXTERNAL_STORAGE = 1
                ActivityCompat.requestPermissions(
                    this,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
                );
            }
        }




        mPhotoEditor.setOnPhotoEditorListener(object : OnPhotoEditorListener {
            override fun onEditTextChangeListener(rootView: View?, text: String?, colorCode: Int) {

                val textEditorDialogFragment =
                    TextEditorDialogFragment.show(
                        this@EditActivity as AppCompatActivity,
                        text!!,
                        colorCode
                    )
                textEditorDialogFragment.setOnTextEditorListener { inputText, colorCode ->
                    val styleBuilder = TextStyleBuilder()
                    styleBuilder.withTextColor(colorCode)
                    mPhotoEditor.editText(rootView!!, inputText, styleBuilder)
                }
            }

            override fun onAddViewListener(viewType: ViewType?, numberOfAddedViews: Int) {
                Unit
            }

            override fun onRemoveViewListener(viewType: ViewType?, numberOfAddedViews: Int) {
                Unit
            }

            override fun onStartViewChangeListener(viewType: ViewType?) {
                Unit
            }

            override fun onStopViewChangeListener(viewType: ViewType?) {
                Unit
            }
        })


    }


    @SuppressLint("ResourceAsColor")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK && requestCode == pickImage && data != null) {
            imageRouteClear = false

            val imageUri: Uri = data.data!!

            Toast.makeText(this, imageUri.toString(), Toast.LENGTH_LONG).show()

            mPhotoEditorView = findViewById(R.id.photo_editor_view)
            Glide.with(this).load(imageUri.toString()).into(mPhotoEditorView.source)


            imageRouteClear = true
        }
    }
}