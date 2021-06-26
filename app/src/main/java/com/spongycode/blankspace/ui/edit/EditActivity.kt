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
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import com.bumptech.glide.Glide
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.storage.FirebaseStorage
import com.spongycode.blankspace.R
import com.spongycode.blankspace.ui.edit.fragments.PropertiesBSFragment
import com.spongycode.blankspace.ui.edit.fragments.TextEditorDialogFragment
import com.spongycode.blankspace.ui.main.fragments.base.MemberEditsDialog
import com.spongycode.blankspace.util.Helper
import com.spongycode.blankspace.util.userdata
import ja.burhanrashid52.photoeditor.*
import java.io.File
import java.util.*


@Suppress("DEPRECATION")
class EditActivity : AppCompatActivity(), PropertiesBSFragment.Properties {

    private val pickImage = 100
    private var imageRouteClear = true
    lateinit var mPhotoEditorView: PhotoEditorView
    lateinit var mPhotoEditor: PhotoEditor
    private var mPropertiesBSFragment: PropertiesBSFragment? = null

    lateinit var memeAddTextBG: ImageButton
    lateinit var memeAddText: ImageButton
    lateinit var memeSave: ImageButton
    lateinit var memeUndo: ImageButton
    lateinit var memeRedo: ImageButton
    lateinit var memeBrush: ImageButton
    lateinit var memeEraser: ImageButton
    lateinit var memeUpload: ImageButton

    var mStorage = FirebaseStorage.getInstance()
    val mStorageRef = mStorage.reference
    private var downloadUri: Uri? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit)

        val url = intent?.getStringExtra("imageurl")
        mPhotoEditorView = findViewById(R.id.photo_editor_view)
        mPropertiesBSFragment = PropertiesBSFragment()
        mPropertiesBSFragment!!.setPropertiesChangeListener(this)

        memeAddTextBG = findViewById(R.id.meme_add_text_with_bg)
        memeAddText = findViewById(R.id.meme_add_text_no_bg)
        memeSave = findViewById(R.id.save_local)
        memeUndo = findViewById(R.id.meme_undo)
        memeRedo = findViewById(R.id.meme_redo)
        memeBrush = findViewById(R.id.meme_brush)
        memeEraser = findViewById(R.id.meme_eraser)
        memeEraser = findViewById(R.id.upload_edit)

        Helper.buttonEffect(memeAddTextBG, "#FF03DAC5")
        Helper.buttonEffect(memeAddText, "#FF03DAC5")
        Helper.buttonEffect(memeSave, "#FF863BF1")
        Helper.buttonEffect(memeUndo, "#9E693F")
        Helper.buttonEffect(memeRedo, "#9E693F")
        Helper.buttonEffect(memeBrush, "#FF03DAC5")
        Helper.buttonEffect(memeEraser, "#FF03DAC5")


        if (url == "none") {
            // Pick Image
            val gallery = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI)
            startActivityForResult(gallery, pickImage)
        } else {
            Glide.with(this).load(url).into(mPhotoEditorView.source)
        }


        val mTextRobotoTf = ResourcesCompat.getFont(this, R.font.impact)
        mPhotoEditor = PhotoEditor.Builder(this, mPhotoEditorView)
            .setPinchTextScalable(true)
            .setDefaultTextTypeface(mTextRobotoTf)
            .build()


        memeUndo.setOnClickListener {
            mPhotoEditor.undo()
        }


        memeBrush.setOnClickListener {
            mPhotoEditor.setBrushDrawingMode(true)
            showBottomSheetDialogFragment(mPropertiesBSFragment)
            memeBrush.background.setTint(
                ContextCompat.getColor(
                    applicationContext,
                    R.color.teal_200
                )
            )
            memeEraser.background.setTint(ContextCompat.getColor(applicationContext, R.color.white))
        }

        memeEraser.setOnClickListener {
            mPhotoEditor.brushEraser()
            memeEraser.background.setTint(
                ContextCompat.getColor(
                    applicationContext,
                    R.color.teal_200
                )
            )
            memeBrush.background.setTint(ContextCompat.getColor(applicationContext, R.color.white))
        }

        memeRedo.setOnClickListener {
            mPhotoEditor.redo()
        }


        memeAddTextBG.setOnClickListener {
            val typeface: Typeface? = ResourcesCompat.getFont(this, R.font.impact)
            val textStyleBuilder: TextStyleBuilder = TextStyleBuilder()
            textStyleBuilder.withTextSize(40F)
            textStyleBuilder.withTextColor(resources.getColor(R.color.black))
            textStyleBuilder.withTextFont(typeface!!)
            textStyleBuilder.withBackgroundColor(resources.getColor(R.color.white_trans))
            mPhotoEditor.addText("Hold to Edit", textStyleBuilder)

            memeEraser.background.setTint(ContextCompat.getColor(applicationContext, R.color.white))
            memeBrush.background.setTint(ContextCompat.getColor(applicationContext, R.color.white))

        }
        memeAddText.setOnClickListener {
            val typeface: Typeface? = ResourcesCompat.getFont(this, R.font.impact)
            val textStyleBuilder: TextStyleBuilder = TextStyleBuilder()
            textStyleBuilder.withTextSize(40F)
            textStyleBuilder.withTextColor(resources.getColor(R.color.black))
            textStyleBuilder.withTextFont(typeface!!)
            mPhotoEditor.addText("Hold to Edit", textStyleBuilder)

            memeEraser.background.setTint(ContextCompat.getColor(applicationContext, R.color.white))
            memeBrush.background.setTint(ContextCompat.getColor(applicationContext, R.color.white))

        }


        findViewById<ImageButton>(R.id.save_local).setOnClickListener {
            saveOrUploadImageLocal(uploadStatus = false)
        }

        findViewById<ImageButton>(R.id.upload_edit).setOnClickListener {
            saveOrUploadImageLocal(uploadStatus = true)
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

            override fun onAddViewListener(viewType: ViewType?, numberOfAddedViews: Int) = Unit

            override fun onRemoveViewListener(viewType: ViewType?, numberOfAddedViews: Int) = Unit

            override fun onStartViewChangeListener(viewType: ViewType?) {
            }

            override fun onStopViewChangeListener(viewType: ViewType?) {
            }
        })


    }

    private fun saveOrUploadImageLocal(uploadStatus: Boolean = false) {
        val tShot = System.currentTimeMillis()
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            mPhotoEditor.saveAsFile(
                Environment.getExternalStorageDirectory().toString() + "/${tShot}.jpg",
                object : PhotoEditor.OnSaveListener {
                    override fun onSuccess(imagePath: String) {
                        if (uploadStatus) {
                            val mUri: Uri = Uri.fromFile(
                                File(
                                    Environment.getExternalStorageDirectory()
                                        .toString() + "/${tShot}.jpg"
                                )
                            )
                            MemberEditsDialog.newInstance(
                                userdata.afterLoginUserData.imageUrl,
                                mUri
                            )
                                .show(supportFragmentManager, "hello")
//                            postFire(mUri)
                        } else {
                            Toast.makeText(applicationContext, "Image Saved", Toast.LENGTH_LONG)
                                .show()
                        }

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
            )
        }
    }


    private fun showBottomSheetDialogFragment(fragment: BottomSheetDialogFragment?) {
        if (fragment == null || fragment.isAdded) {
            return
        }
        fragment.show(supportFragmentManager, fragment.tag)
    }


    @SuppressLint("ResourceAsColor")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK && requestCode == pickImage && data != null) {
            imageRouteClear = false

            val imageUri: Uri = data.data!!


            mPhotoEditorView = findViewById(R.id.photo_editor_view)
            Glide.with(this).load(imageUri.toString()).into(mPhotoEditorView.source)


            imageRouteClear = true
        }
    }

    override fun onColorChanged(colorCode: Int) {
        mPhotoEditor.setBrushColor(colorCode)
    }

    override fun onOpacityChanged(opacity: Int) {
        mPhotoEditor.setOpacity(opacity)
    }

    override fun onBrushSizeChanged(brushSize: Int) {
        mPhotoEditor.setBrushSize(brushSize.toFloat())
    }
}