package com.spongycode.blankspace.ui.main.fragments

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.textview.MaterialTextView
import com.spongycode.blankspace.R
import com.spongycode.blankspace.databinding.FragmentGenerateBinding
import com.spongycode.blankspace.databinding.ImageItemBinding
import com.spongycode.blankspace.model.modelsImages.Image
import com.spongycode.blankspace.ui.edit.EditActivity
import com.spongycode.blankspace.ui.main.MainActivity
import com.spongycode.blankspace.ui.photoviewer.PhotoViewerActivity
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

        binding.list?.attachFab(binding.generateFab, activity as AppCompatActivity)

        binding.generateFab.setOnClickListener {
            val myIntent = Intent(requireContext(), EditActivity::class.java)
            myIntent.putExtra("imageurl", "none")
            this.startActivity(myIntent)
        }


        return binding.root
    }

    inner class GenerateFragmentAdapter(private val listImages: List<Image>) :
        RecyclerView.Adapter<GenerateFragmentAdapter.GenerateFragmentViewHolder>() {
        inner class GenerateFragmentViewHolder(binding: ImageItemBinding) :
            RecyclerView.ViewHolder(binding.root) {
            internal var image: ShapeableImageView = itemBinding.image
            internal var title: MaterialTextView = itemBinding.textView
            internal var save: MaterialButton = itemBinding.templateSave
            internal var edit: MaterialButton = itemBinding.templateEdit
        }

        override fun onCreateViewHolder(
            parent: ViewGroup,
            viewType: Int
        ): GenerateFragmentViewHolder {
            itemBinding = ImageItemBinding.inflate(LayoutInflater.from(parent.context))
            return GenerateFragmentViewHolder(itemBinding)
        }

        override fun onBindViewHolder(holder: GenerateFragmentViewHolder, position: Int) {
            val image = listImages.get(position)
            holder.title.text = image.name
            Picasso.get().load(image.url)
                .error(R.drawable.meme)
                .into(holder.image)
            Log.w("display", "1- $width, $height")
            holder.edit.setOnClickListener {
                val myIntent = Intent(requireContext(), EditActivity::class.java)
                myIntent.putExtra("imageurl", image.url)
                context?.startActivity(myIntent)
            }
            holder.image.setOnClickListener {
                val intent = Intent(context, PhotoViewerActivity::class.java)
                intent.putExtra("IMAGE_URL", image.url)
                context?.startActivity(intent)
            }
        }

        override fun getItemCount() = listImages.size
    }


    private fun RecyclerView.attachFab(fab: FloatingActionButton, activity: AppCompatActivity) {
        this.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (dy > 0) {
                    fab.hide()

                } else if (dy < 0) {
                    fab.show()
                    activity.supportActionBar!!.show()

                }
            }
        })
    }
}