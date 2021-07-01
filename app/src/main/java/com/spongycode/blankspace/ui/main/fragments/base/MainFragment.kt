package com.spongycode.blankspace.ui.main.fragments.base

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.AnimatedVectorDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.ViewGroup
import android.widget.*
import androidx.annotation.Nullable
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.PopupMenu
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.imageview.ShapeableImageView
import com.spongycode.blankspace.R
import com.spongycode.blankspace.databinding.FragmentMainBinding
import com.spongycode.blankspace.model.UserModel
import com.spongycode.blankspace.model.modelmemes.MemeModel
import com.spongycode.blankspace.storage.*
import com.spongycode.blankspace.ui.edit.EditActivity
import com.spongycode.blankspace.ui.main.MainActivity
import com.spongycode.blankspace.ui.main.MainActivity.Companion.firestore
import com.spongycode.blankspace.util.ClickListener
import com.spongycode.blankspace.util.Helper
import com.spongycode.blankspace.util.userdata
import com.spongycode.blankspace.viewmodel.MemeViewModel
import java.io.ByteArrayOutputStream


@Suppress("DEPRECATION")
class MainFragment : Fragment() {

    private var _binding: FragmentMainBinding? = null
    private val binding get() = _binding!!
    private val memeViewModel = MainActivity.memeViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentMainBinding.inflate(inflater, container, false)

        memeViewModel.allMemeDb["Random"] = memeViewModel.randomMemeList
        memeViewModel.allMemeDb["Gaming"] = memeViewModel.gamingMemeList
        memeViewModel.allMemeDb["Coding"] = memeViewModel.codingMemeList
        memeViewModel.allMemeDb["Science"] = memeViewModel.scienceMemeList
        memeViewModel.allMemeDb["Member Edits"] = memeViewModel.memberEditsMemeList
        binding.currentCatTv.text = memeViewModel.currentMemeCategory

        if (memeViewModel.count == 0) {
            memeViewModel.memeFun(memeViewModel.currentMemeCategory).observe(
                viewLifecycleOwner, {
                    // set up and populate view
                    memeViewModel.allMemeDb[memeViewModel.currentMemeCategory]?.apply {
                        addAll(it)
                        toSet()
                        toList()
                    }
                    binding.rvMeme.adapter =
                        MemeRecyclerAdapter(requireContext(), memeViewModel.allMemeDb[memeViewModel.currentMemeCategory]!!)
                    binding.rvMeme.adapter?.notifyDataSetChanged()
                }
            )
            memeViewModel.count = 1
        }
        if (memeViewModel.count == 1) {
            binding.rvMeme.adapter = MemeRecyclerAdapter(
                requireContext(),
                memeViewModel.allMemeDb[memeViewModel.currentMemeCategory]!!
            )
            binding.rvMeme.adapter?.notifyDataSetChanged()

        }
        binding.rvMeme.edgeEffectFactory = object : RecyclerView.EdgeEffectFactory() {
            override fun createEdgeEffect(view: RecyclerView, direction: Int): EdgeEffect {
                return EdgeEffect(view.context).apply { color = resources.getColor(R.color.decent_green)
                }
            }
        }

        binding.pop.setOnClickListener {
            val popupMenu = PopupMenu(requireContext(), binding.pop)
            popupMenu.menuInflater.inflate(R.menu.popup_menu, popupMenu.menu)
            popupMenu.setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.action_random -> {
                        if (memeViewModel.currentMemeCategory != item.title.toString()) {
                            memeViewModel.currentMemeCategory = item.title.toString()
                            binding.currentCatTv.text = item.title.toString()
                            memeFunObserve(item.title.toString())
                            Toast.makeText(
                                requireContext(),
                                "Loading random memes" + item.title,
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                    R.id.action_gaming -> {
                        if (memeViewModel.currentMemeCategory != item.title.toString()) {
                            memeViewModel.currentMemeCategory = item.title.toString()
                            binding.currentCatTv.text = item.title.toString()
                            memeFunObserve(item.title.toString())
                            Toast.makeText(
                                requireContext(),
                                "Loading gaming memes",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                    R.id.action_coding -> {
                        if (memeViewModel.currentMemeCategory != item.title.toString()) {
                            memeViewModel.currentMemeCategory = item.title.toString()
                            binding.currentCatTv.text = item.title.toString()
                            memeFunObserve(item.title.toString())
                            Toast.makeText(
                                requireContext(),
                                "Loading coding memes",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                    R.id.action_science -> {
                        if (memeViewModel.currentMemeCategory != item.title.toString()) {
                            memeViewModel.currentMemeCategory = item.title.toString()
                            binding.currentCatTv.text = item.title.toString()
                            memeFunObserve(item.title.toString())
                            Toast.makeText(
                                requireContext(),
                                "Loading science memes",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                    R.id.action_member_edits -> {
                        if (memeViewModel.currentMemeCategory != item.title.toString()) {
                            memeViewModel.currentMemeCategory = item.title.toString()
                            binding.currentCatTv.text = item.title.toString()
                            memeFunObserve(item.title.toString())
                            Toast.makeText(
                                requireContext(),
                                "Loading member edits",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }

                }
                true
            }
            popupMenu.show()
        }

        binding.rvMeme?.attachFab(binding.fabMemberEdits, activity as AppCompatActivity)

//        binding.spCategory.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
//            override fun onItemSelected(
//                parent: AdapterView<*>?,
//                view: View?,
//                position: Int,
//                id: Long
//            ) {
//                Toast.makeText(
//                    requireContext(),
//                    parent?.getItemAtPosition(position).toString(),
//                    Toast.LENGTH_LONG
//                ).show()
//                memeViewModel.memeFun(parent?.getItemAtPosition(position).toString()).observe(
//                    viewLifecycleOwner, {
//                        // set up and populate view
//                        val memeList = mutableListOf<MemeModel>()
//                        memeList.apply {
//                            addAll(it)
//                            toSet()
//                            toList()
//                            Log.d("meme", "meme: $memeList")
//                            binding.rvMeme.adapter = MemeRecyclerAdapter(requireContext(), memeList)
//                            binding.rvMeme.adapter?.notifyDataSetChanged()
//                        }
//                    }
//                )
//            }
//
//            override fun onNothingSelected(parent: AdapterView<*>?) {
//            }
//        }
        binding.fabMemberEdits.setOnClickListener {
            MemberEditsDialog.newInstance(userdata.afterLoginUserData.imageUrl, null)
                .show(parentFragmentManager, "hello")
        }
        return binding.root
    }

    private fun memeFunObserve(category: String) {
        if (memeViewModel.allMemeDb[category]!!.isEmpty()){
            memeViewModel.memeFun(category).observe(
                viewLifecycleOwner, {
                    // set up and populate view
                    memeViewModel.allMemeDb[category]?.apply {
                        addAll(0,it)
                        toSet()
                        toList()
                        Log.d("meme", "meme: ${memeViewModel.allMemeDb[category]!!}")
                        binding.rvMeme.adapter =
                            MemeRecyclerAdapter(requireContext(), memeViewModel.allMemeDb[category]!!)
                        binding.rvMeme.adapter?.notifyDataSetChanged()
                    }
                }
            )
        }else{
            binding.rvMeme.adapter =
                MemeRecyclerAdapter(requireContext(), memeViewModel.allMemeDb[category]!!)
            binding.rvMeme.adapter?.notifyDataSetChanged()
        }
    }

    // This should be in its own file (repository)
//    private fun fetchMemeByCategory(category: String) {
//
//        if (category == "Member Edits"){
//            return
//        }
//
//        when(category){
//            "Random" -> apiInterface = ApiInterface.create().getMemesRandom()
//            "Coding" -> apiInterface = ApiInterface.create().getMemesProgram()
//            "Science" -> apiInterface = ApiInterface.create().getMemesScience()
//            "Gaming" -> apiInterface = ApiInterface.create().getMemesGaming()
//        }
//
//
//        apiInterface.enqueue(object : Callback<MemeList?> {
//            override fun onResponse(call: Call<MemeList?>, response: Response<MemeList?>) {
//
//                if (response.body() != null) {
//                    val memeList: MutableList<MemeModel> = mutableListOf()
//                    for (i in response.body()!!.memes!!) {
//                        memeList.add(i)
//                    }
//
//                    val linearLayoutManager =
//                        LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)
//                    binding.rvMeme.layoutManager = linearLayoutManager
//                    binding.rvMeme.adapter = MemeRecyclerAdapter(requireActivity(), memeList)
//                    val adapter = binding.rvMeme.adapter
//                    adapter?.notifyDataSetChanged()
//
//                } else {
//                    Toast.makeText(requireActivity(), "Error fetching", Toast.LENGTH_LONG).show()
//
//                }
//
//            }
//
//            override fun onFailure(call: Call<MemeList?>, t: Throwable) {
//                Log.d(TAG, "Error Fetching: ${t.printStackTrace()}")
//                Toast.makeText(requireActivity(), t.toString(), Toast.LENGTH_LONG).show()
//            }
//        })
//    }

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
            if (meme.userId == "") {
                holder.memeSenderUsername.visibility = GONE
                holder.memeSenderImage.visibility = GONE
                holder.memePostTimeTv.visibility = GONE
            } else {
                val listDate =
                    meme.timestamp!!.toDate().toString().split(":| ".toRegex()).map { it.trim() }
                val dateFinal =
                    listDate[3] + ":" + listDate[4] + " on " + listDate[1] + " " + listDate[2]
                holder.memePostTimeTv.text = dateFinal
                firestore.collection("users")
                    .whereEqualTo("userId", meme.userId)
                    .get()
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            for (data in task.result!!) {
                                val imageUrl = data.toObject(UserModel::class.java).imageUrl
                                Glide.with(requireActivity()).load(imageUrl)
                                    .into(holder.memeSenderImage)
                                holder.memeSenderUsername.text =
                                    data.toObject(UserModel::class.java).username
                            }
                        }
                    }
            }

            checkMemeIsFav(meme)
            holder.like.setImageResource(if (meme.like) R.drawable.ic_baseline_favorite_24 else
                R.drawable.ic_baseline_favorite_border_24)

            holder.title.text = meme.title

            holder.memeLikeEntry.setOnClickListener {
                onDoubleWorker(holder, meme)
            }


            val circularProgressDrawable = CircularProgressDrawable(requireContext())
            circularProgressDrawable.strokeWidth = 10f
            circularProgressDrawable.centerRadius = 50f
            circularProgressDrawable.start()



            Glide.with(holder.itemView.context.applicationContext).load(meme.url)
                .placeholder(circularProgressDrawable)
                .into(holder.image)



            holder.image.setOnTouchListener(TapListener(meme, holder))
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
                            resource.compress(Bitmap.CompressFormat.PNG, 100, os)
                            val path: String = MediaStore.Images.Media.insertImage(
                                activity?.contentResolver,
                                resource,
                                "title",
                                "desc"
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
            holder.download.setOnClickListener {
                MainActivity().saveImage(
                    (activity as MainActivity),
                    holder.image.drawable,
                    meme.title
                )
            }
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
            internal val memeHeartAnim: ImageView = view.findViewById(R.id.meme_heart_anim_iv)
            internal val memeLikeEntry: ShapeableImageView = view.findViewById(R.id.like_entry)
            internal val memeLikeGone: ShapeableImageView = view.findViewById(R.id.like_gone)
        }

        inner class TapListener(private val meme: MemeModel, private val holder: ViewHolder) :
            ClickListener(context) {
            override fun onLong() {
                if (meme.gif) {
                    Toast.makeText(
                        requireContext(),
                        "Editing GIFS not supported",
                        Toast.LENGTH_LONG
                    ).show()
                } else {
                    val myIntent = Intent(context, EditActivity::class.java)
                    myIntent.putExtra("imageurl", meme.url)
                    context.startActivity(myIntent)
                }
            }

            override fun onDouble() {
                onDoubleWorker(holder,meme)
            }

            override fun onSingle() {
                super.onSingle()
                PhotoViewerDialog.newInstance(meme.url).show(parentFragmentManager, "hello")
            }
        }

        private fun onDoubleWorker(holder: ViewHolder,meme: MemeModel) {
            if (meme.like){
                meme.like = false
                removeMeme(meme)
                holder.like.setImageResource(0)
                holder.memeLikeEntry.alpha = 0f
                holder.memeLikeGone.alpha = 1f
                val drawableLittle: Drawable = holder.memeLikeGone.drawable
                val animatedVectorDrawableLittle: AnimatedVectorDrawable =
                    drawableLittle as AnimatedVectorDrawable
                animatedVectorDrawableLittle.start()

            }else{
                meme.like = true
                saveMemeToFavs(meme)
                holder.memeLikeEntry.alpha = 1f
                holder.memeLikeGone.alpha = 0f
                val drawableLittle: Drawable = holder.memeLikeEntry.drawable
                val animatedVectorDrawableLittle: AnimatedVectorDrawable =
                    drawableLittle as AnimatedVectorDrawable
                animatedVectorDrawableLittle.start()
                holder.like.setImageResource(R.drawable.ic_baseline_favorite_24)
            }

            // this rebuilds the whole rv, we need to implement a diffUtil.
//                binding.rvMeme.adapter?.notifyDataSetChanged()
            holder.memeHeartAnim.alpha = 0.8f
            val drawable: Drawable = holder.memeHeartAnim.drawable
            val animatedVectorDrawable: AnimatedVectorDrawable =
                drawable as AnimatedVectorDrawable
            animatedVectorDrawable.start()

        }
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