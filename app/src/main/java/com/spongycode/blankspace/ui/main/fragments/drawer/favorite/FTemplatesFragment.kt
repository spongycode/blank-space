package com.spongycode.blankspace.ui.main.fragments.drawer.favorite

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.drawable.AnimatedVectorDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.activity.addCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.NavHostFragment
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.textview.MaterialTextView
import com.spongycode.blankspace.R
import com.spongycode.blankspace.databinding.FragmentFTemplatesBinding
import com.spongycode.blankspace.databinding.ImageItemBinding
import com.spongycode.blankspace.model.modelsImages.Image
import com.spongycode.blankspace.storage.removeTemplate
import com.spongycode.blankspace.ui.edit.EditActivity
import com.spongycode.blankspace.ui.main.MainActivity
import com.spongycode.blankspace.ui.main.fragments.base.PhotoViewerDialog
import com.spongycode.blankspace.util.ClickListener
import com.spongycode.blankspace.viewmodel.ImageViewModel
import com.squareup.picasso.Picasso

class FTemplatesFragment: Fragment() {

    private var _binding: FragmentFTemplatesBinding? = null
    private val binding get() = _binding!!
    private lateinit var itemBinding: ImageItemBinding
    private val imageViewModel: ImageViewModel = MainActivity.imageViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentFTemplatesBinding.inflate(inflater, container, false)

        val toolbar: Toolbar = binding.toolFTemplates
        (activity as AppCompatActivity?)!!.setSupportActionBar(toolbar)
        val navHostFragment = (activity as AppCompatActivity).supportFragmentManager.findFragmentById(R.id.main_nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController
        toolbar.setNavigationIcon(R.drawable.ic_baseline_arrow_back_ios_24)
        binding.toolFTemplates.setNavigationOnClickListener {
            navController.navigate(R.id.action_FTemplatesFragment_to_tabLayoutFragment)
        }
        requireActivity().onBackPressedDispatcher.addCallback {
            navController.navigate(R.id.action_FTemplatesFragment_to_tabLayoutFragment)
        }



        imageViewModel.savedImageLiveData.observe(
            viewLifecycleOwner, {
                // set up and populate view
                val imageList = mutableListOf<Image>()
                imageList.apply {
                    addAll(it)
                    toSet()
                    toList()
                    Log.d("image", "image: $imageList")
                    binding.listFTemplates.adapter = FTemplateFragmentAdapter(imageList)
                    binding.listFTemplates.adapter?.notifyDataSetChanged()
                }
            }
        )

        return binding.root
    }

    // Both this fragment and FMemes have their own adapter bcs in the future F Fragments and main
    // Fragments will have complete different UIs
    inner class FTemplateFragmentAdapter(private val listImages: MutableList<Image>) :
        RecyclerView.Adapter<FTemplateFragmentAdapter.FTemplatesViewHolder>() {
        inner class FTemplatesViewHolder(binding: ImageItemBinding) :
            RecyclerView.ViewHolder(binding.root) {
            internal var image: ShapeableImageView = itemBinding.image
            internal var title: MaterialTextView = itemBinding.textView
            internal var star: ShapeableImageView = itemBinding.starSign
            internal var starAnim: ImageView = itemBinding.imageStarAnimIv
        }

        override fun onCreateViewHolder(
            parent: ViewGroup,
            viewType: Int
        ): FTemplatesViewHolder {
            itemBinding = ImageItemBinding.inflate(LayoutInflater.from(parent.context))
            return FTemplatesViewHolder(itemBinding)
        }

        private lateinit var image: Image
        @SuppressLint("ClickableViewAccessibility")
        override fun onBindViewHolder(holder: FTemplatesViewHolder, position: Int) {
            image = listImages.get(position)
            holder.title.text = image.name
            holder.star.visibility = if( image.fav) View.VISIBLE else View.INVISIBLE
            Picasso.get().load(image.url)
                .error(R.drawable.meme)
                .into(holder.image)
            holder.image.setOnTouchListener(TapListener(image, holder, listImages, position))
        }

        override fun getItemCount() = listImages.size

        inner class TapListener(
            private val img: Image,
            private val holder: FTemplatesViewHolder,
            private val listImages: MutableList<Image>,
            private val position: Int
        ): ClickListener(this@FTemplatesFragment.requireContext()){
            override fun onLong() {
                val myIntent = Intent(requireContext(), EditActivity::class.java)
                myIntent.putExtra("imageurl", img.url)
                context?.startActivity(myIntent)
            }
            override fun onDouble() {
                removeTemplate(img)
                holder.star.visibility = View.GONE
                listImages.removeAt(position)
                notifyItemRemoved(position)
                notifyItemRangeChanged(position, itemCount)
                img.fav = !img.fav
                holder.starAnim.alpha = 0.9f
                val drawable: Drawable = holder.starAnim.drawable
                val animatedVectorDrawable: AnimatedVectorDrawable =
                    drawable as AnimatedVectorDrawable
                animatedVectorDrawable.start()
            }
            override fun onSingle() {
                super.onSingle()
                PhotoViewerDialog.newInstance(img.url).show(parentFragmentManager, "hello")
            }
        }
    }


}
