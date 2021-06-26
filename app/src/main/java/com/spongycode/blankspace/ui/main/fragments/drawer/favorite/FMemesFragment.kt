package com.spongycode.blankspace.ui.main.fragments.drawer.favorite

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.addCallback
import androidx.annotation.Nullable
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.NavHostFragment
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.google.android.material.imageview.ShapeableImageView
import com.spongycode.blankspace.R
import com.spongycode.blankspace.databinding.FragmentFMemesBinding
import com.spongycode.blankspace.databinding.MemeLayoutBinding
import com.spongycode.blankspace.model.UserModel
import com.spongycode.blankspace.model.modelmemes.MemeModel
import com.spongycode.blankspace.storage.saveMemeToFavs
import com.spongycode.blankspace.ui.edit.EditActivity
import com.spongycode.blankspace.ui.main.MainActivity
import com.spongycode.blankspace.ui.main.MainActivity.Companion.firestore
import com.spongycode.blankspace.ui.main.fragments.base.PhotoViewerDialog
import com.spongycode.blankspace.util.ClickListener
import java.io.ByteArrayOutputStream
import java.util.*

class FMemesFragment: Fragment() {

    private var _binding: FragmentFMemesBinding? = null
    private val binding get() = _binding!!
    private lateinit var itemBinding: MemeLayoutBinding
    private val memeViewModel = MainActivity.memeViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentFMemesBinding.inflate(inflater, container, false)

        val toolbar: Toolbar = binding.toolFMemes
        (activity as AppCompatActivity?)!!.setSupportActionBar(toolbar)
        val navHostFragment = (activity as AppCompatActivity).supportFragmentManager.findFragmentById(R.id.main_nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController

        toolbar.setNavigationIcon(R.drawable.ic_nav_up)
        binding.toolFMemes.setNavigationOnClickListener {
            navController.navigate(R.id.action_FMemesFragment_to_tabLayoutFragment)
        }
        requireActivity().onBackPressedDispatcher.addCallback {
            navController.navigate(R.id.action_FMemesFragment_to_tabLayoutFragment)
        }
        binding.apply {

            memeViewModel.savedMemeLiveData.observe(viewLifecycleOwner, {
                val memeList = mutableListOf<MemeModel>()
                memeList.addAll(it)
                memeList.toSet()
                memeList.toList()
                Log.d("memeF", "meme: $memeList")
                rvFMeme.adapter = MemeRecyclerAdapter(requireContext(), memeList)
                rvFMeme.adapter?.notifyDataSetChanged()
            })

        }
        return binding.root
    }

    inner class MemeRecyclerAdapter(
        private val context: Context,
        private val memeList: List<MemeModel>
    ) :
        RecyclerView.Adapter<MemeRecyclerAdapter.ViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(
                R.layout.meme_layout,
                parent,
                false
            )
            return ViewHolder(view)
        }


        @SuppressLint("ClickableViewAccessibility")
        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val meme: MemeModel = memeList[position]

            if(meme.userId == ""){
                holder.memeSenderUsername.visibility= View.GONE
                holder.memeSenderImage.visibility= View.GONE
                holder.memePostTimeTv.visibility= View.GONE
            }else{
                val listDate = meme.timestamp!!.toDate().toString().split(":| ".toRegex()).map { it.trim() }
                val dateFinal = listDate[3] + ":" + listDate[4] + " on " + listDate[1] + " " + listDate[2]
                holder.memePostTimeTv.text = dateFinal
            }

            firestore.collection("users")
                .whereEqualTo("userId", meme.userId)
                .get()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        for (data in task.result!!) {
                            val imageUrl = data.toObject(UserModel::class.java).imageUrl
                            Glide.with(requireActivity()).load(imageUrl).into(holder.memeSenderImage)
                            holder.memeSenderUsername.text = data.toObject(UserModel::class.java).username
                        }
                    }
                }



            holder.title.text = meme.title
            Glide.with(holder.itemView.context.applicationContext).load(meme.url).into(holder.image)
            holder.like.setImageResource(if (meme.like) R.drawable.ic_heart_sign else R.drawable.ic_hearth)
            holder.image.setOnTouchListener(TapListener(meme))
            holder.share.setOnClickListener {

                Glide.with(requireActivity())
                    .asBitmap()
                    .load(meme.url)
                    .into(object : CustomTarget<Bitmap?>() {
                        override fun onResourceReady(
                            resource: Bitmap,
                            @Nullable transition: Transition<in Bitmap?>?
                        ) {
                            val os = ByteArrayOutputStream()
                            resource.compress( Bitmap.CompressFormat.PNG, 100, os)
                            val path: String = MediaStore.Images.Media.insertImage(
                                activity?.contentResolver,
                                resource,
                                null,
                                null
                            )

                            val myIntent = Intent()
                            myIntent.apply {
                                action = Intent.ACTION_SEND
                                type = "image/*"
                                putExtra(Intent.EXTRA_STREAM, Uri.parse(path))
                            }
                            val chooser = Intent.createChooser(myIntent, "share file")
                            requireActivity().startActivity(chooser)

                        }

                        override fun onLoadCleared(@Nullable placeholder: Drawable?) {}
                    })
            }
            holder.download.setOnClickListener { MainActivity().saveImage(
                (activity as MainActivity),
                holder.image.drawable,
                meme.title
            ) }
            holder.like.setOnClickListener { meme.like = !meme.like }
        }

        override fun getItemCount() = memeList.size

        inner class ViewHolder internal constructor(view: View) : RecyclerView.ViewHolder(view) {
            internal val title: TextView = view.findViewById(R.id.meme_title)
            internal val image: ImageView = view.findViewById(R.id.meme_iv)
            internal val like: ShapeableImageView = view.findViewById(R.id.like)
            internal val share: ShapeableImageView = view.findViewById(R.id.share)
            internal val download: ShapeableImageView = view.findViewById(R.id.download)
            internal val memeSenderImage: ImageView = view.findViewById(R.id.meme_sender_image)
            internal val memeSenderUsername: TextView = view.findViewById(R.id.meme_sender_username)
            internal val memePostTimeTv: TextView = view.findViewById(R.id.meme_post_time_tv)
        }

        inner class TapListener(private val meme: MemeModel): ClickListener(context){
            override fun onLong() {
                if (meme.gif){
                    Toast.makeText(requireContext(), "Editing GIFS not supported", Toast.LENGTH_LONG).show()
                }else{
                    val myIntent = Intent(context, EditActivity::class.java)
                    Toast.makeText(requireContext(), meme.url.toLowerCase(Locale.ROOT).trim(), Toast.LENGTH_LONG).show()
                    myIntent.putExtra("imageurl", meme.url)
                    context.startActivity(myIntent)
                }
            }

            override fun onDouble() {
                saveMemeToFavs(meme)
                meme.like = !meme.like
                // this rebuilds the whole rv, we need to implement a diffUtil.
//                binding.rvMeme.adapter?.notifyDataSetChanged()
            }
            override fun onSingle() {
                super.onSingle()
                PhotoViewerDialog.newInstance(meme.url).show(parentFragmentManager, "hello")
            }
        }
    }
}