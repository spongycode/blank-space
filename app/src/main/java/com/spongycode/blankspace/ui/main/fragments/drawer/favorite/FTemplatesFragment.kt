package com.spongycode.blankspace.ui.main.fragments.drawer.favorite

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.textview.MaterialTextView
import com.spongycode.blankspace.R
import com.spongycode.blankspace.databinding.FragmentGenerateBinding
import com.spongycode.blankspace.databinding.ImageItemBinding
import com.spongycode.blankspace.model.modelsImages.Image
import com.spongycode.blankspace.storage.saveTemplate
import com.spongycode.blankspace.ui.edit.EditActivity
import com.spongycode.blankspace.ui.main.MainActivity
import com.spongycode.blankspace.util.ClickListener
import com.spongycode.blankspace.viewmodel.ImageViewModel
import com.squareup.picasso.Picasso

class FTemplatesFragment: Fragment() {

    private var _binding: FragmentGenerateBinding? = null
    private val binding get() = _binding!!
    private lateinit var itemBinding: ImageItemBinding
    private val imageViewModel: ImageViewModel = MainActivity.imageViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentGenerateBinding.inflate(inflater, container, false)

        imageViewModel.savedImageLiveData.observe(
            viewLifecycleOwner, {
                // set up and populate view
                val imageList = mutableListOf<Image>()
                imageList.apply {
                    addAll(it)
                    toSet()
                    toList()
                    Log.d("image", "image: $imageList")
                    binding.list.adapter = FTemplateFragmentAdapter(imageList)
                    binding.list.adapter?.notifyDataSetChanged()
                }
            }
        )

        return binding.root
    }

    // Both this fragment and FMemes have their own adapter bcs in the future F Fragments and main
    // Fragments will have complete different UIs
    inner class FTemplateFragmentAdapter(private val listImages: List<Image>) :
        RecyclerView.Adapter<FTemplateFragmentAdapter.GenerateFragmentViewHolder>() {
        inner class GenerateFragmentViewHolder(binding: ImageItemBinding) :
            RecyclerView.ViewHolder(binding.root) {
            internal var image: ShapeableImageView = itemBinding.image
            internal var title: MaterialTextView = itemBinding.textView
            internal var star: ShapeableImageView = itemBinding.starSign
        }

        override fun onCreateViewHolder(
            parent: ViewGroup,
            viewType: Int
        ): GenerateFragmentViewHolder {
            itemBinding = ImageItemBinding.inflate(LayoutInflater.from(parent.context))
            return GenerateFragmentViewHolder(itemBinding)
        }

        private lateinit var image: Image
        @SuppressLint("ClickableViewAccessibility")
        override fun onBindViewHolder(holder: GenerateFragmentViewHolder, position: Int) {
            image = listImages.get(position)
            holder.title.text = image.name
            holder.star.visibility = if( image.fav) View.VISIBLE else View.INVISIBLE
            Picasso.get().load(image.url)
                .error(R.drawable.meme)
                .into(holder.image)
            holder.image.setOnTouchListener(TapListener(image))
        }

        override fun getItemCount() = listImages.size

        inner class TapListener(private val img: Image): ClickListener(this@FTemplatesFragment.requireContext()){
            override fun onLong() {
                val myIntent = Intent(requireContext(), EditActivity::class.java)
                myIntent.putExtra("imageurl", img.url)
                context?.startActivity(myIntent)
            }

            override fun onDouble() {
                saveTemplate(img)
                img.fav = !img.fav
            }
        }
    }


}
