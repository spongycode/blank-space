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
import androidx.annotation.Nullable
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.google.android.material.imageview.ShapeableImageView
import com.spongycode.blankspace.R
import com.spongycode.blankspace.databinding.FragmentMainBinding
import com.spongycode.blankspace.databinding.MemeLayoutBinding
import com.spongycode.blankspace.model.modelmemes.MemeModel
import com.spongycode.blankspace.storage.saveMemeToFavs
import com.spongycode.blankspace.ui.edit.EditActivity
import com.spongycode.blankspace.ui.main.MainActivity
import com.spongycode.blankspace.util.ClickListener
import java.io.ByteArrayOutputStream

class FMemesFragment: Fragment() {

    private var _binding: FragmentMainBinding? = null
    private val binding get() = _binding!!
    private lateinit var itemBinding: MemeLayoutBinding
    private val memeViewModel = MainActivity.memeViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentMainBinding.inflate(inflater, container, false)

        binding.apply {

            memeViewModel.savedMemeLiveData.observe(viewLifecycleOwner, {
                val memeList = mutableListOf<MemeModel>()
                memeList.addAll(it)
                memeList.toSet()
                memeList.toList()
                Log.d("memeF", "meme: $memeList")
                rvMeme.adapter = MemeRecyclerAdapter(requireContext(), memeList)
                rvMeme.adapter?.notifyDataSetChanged()
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
        }

        inner class TapListener(private val meme: MemeModel): ClickListener(context){
            override fun onLong() {
                val myIntent = Intent(context, EditActivity::class.java)
                myIntent.putExtra("imageurl", meme.url)
                context.startActivity(myIntent)
            }

            override fun onDouble() {
                saveMemeToFavs(meme)
                meme.like = !meme.like
                // this rebuilds the whole rv, we need to implement a diffUtil.
//                binding.rvMeme.adapter?.notifyDataSetChanged()
            }
        }
    }
}