package com.spongycode.blankspace.ui.main.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.textview.MaterialTextView
import com.spongycode.blankspace.R
import com.spongycode.blankspace.databinding.FragmentGenerateBinding
import com.spongycode.blankspace.databinding.ImageItemBinding
import com.spongycode.blankspace.model.modelsImages.Image
import com.spongycode.blankspace.ui.main.MainActivity
import com.spongycode.blankspace.viewmodel.ImageViewModel
import com.squareup.picasso.Picasso

class GenerateFragment : Fragment() {

    private var _binding: FragmentGenerateBinding? = null
    private val binding get() = _binding!!
    private lateinit var itemBinding: ImageItemBinding
    private lateinit var imageViewModel: ImageViewModel
    private var width: Int? = MainActivity.width
    private var height: Int? = MainActivity.height

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        imageViewModel = ViewModelProvider(this).get(ImageViewModel::class.java)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentGenerateBinding.inflate(inflater, container, false)

        imageViewModel.imageLiveData.observe(
            viewLifecycleOwner, {
               // set up and populate view
                val memeList = mutableListOf<Image>()
                memeList.addAll(it)
                memeList.toSet()
                memeList.toList()
                binding.list.adapter = GenerateFragmentAdapter(memeList)
                binding.list.adapter?.notifyDataSetChanged()
            }
        )

        return binding.root
    }

    inner class GenerateFragmentAdapter(private val listImages: List<Image>): RecyclerView.Adapter<GenerateFragmentAdapter.GenerateFragmentViewHolder>(){
        inner class GenerateFragmentViewHolder(binding: ImageItemBinding): RecyclerView.ViewHolder(binding.root){
            internal var image: ShapeableImageView = itemBinding.image
            internal var title: MaterialTextView = itemBinding.textView
        }

        override fun onCreateViewHolder(
            parent: ViewGroup,
            viewType: Int
        ): GenerateFragmentViewHolder {
            itemBinding = ImageItemBinding.inflate(LayoutInflater.from(parent.context))
            return GenerateFragmentViewHolder(itemBinding)
        }

        override fun onBindViewHolder(
            holder: GenerateFragmentViewHolder,
            position: Int
        ) {
            val image = listImages.get(position)
            holder.title.text = image.name
            Picasso.get().load(image.url)
                .error(R.drawable.meme)
                .into(holder.image)
            Log.w("display", "1- $width, $height")
        }

        override fun getItemCount() = listImages.size
    }
}