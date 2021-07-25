package com.spongycode.blankspace.ui.main.fragments.base

import android.accounts.NetworkErrorException
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
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
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.elconfidencial.bubbleshowcase.BubbleShowCase
import com.elconfidencial.bubbleshowcase.BubbleShowCaseBuilder
import com.elconfidencial.bubbleshowcase.BubbleShowCaseSequence
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.snackbar.Snackbar
import com.spongycode.blankspace.R
import com.spongycode.blankspace.databinding.FragmentMainBinding
import com.spongycode.blankspace.model.UserModel
import com.spongycode.blankspace.model.modelmemes.MemeModel
import com.spongycode.blankspace.storage.checkMemeIsFav
import com.spongycode.blankspace.storage.removeMeme
import com.spongycode.blankspace.storage.saveMemeToFavs
import com.spongycode.blankspace.ui.edit.EditActivity
import com.spongycode.blankspace.ui.main.MainActivity
import com.spongycode.blankspace.ui.main.MainActivity.Companion.firestore
import com.spongycode.blankspace.util.ClickListener
import com.spongycode.blankspace.util.NetworkCheck
import com.spongycode.blankspace.util.userdata
import java.io.ByteArrayOutputStream
import java.util.*
import kotlin.concurrent.schedule


@Suppress("DEPRECATION")
class MainFragment : Fragment() {

    private var _binding: FragmentMainBinding? = null
    private val binding get() = _binding!!
    private val memeViewModel = MainActivity.memeViewModel
    private var isShown = false

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
        val mainAdapter = MemeRecyclerAdapter(requireContext())

        try{
            if (NetworkCheck.hasInternetConnection((activity as MainActivity).application)) {
                if (memeViewModel.allMemeDb[memeViewModel.currentMemeCategory]!!.isEmpty()) {
                    memeViewModel.memeFun(memeViewModel.currentMemeCategory).observe(
                        viewLifecycleOwner, {
                            // set up and populate view
                            memeViewModel.allMemeDb[memeViewModel.currentMemeCategory]?.apply {
                                addAll(it)
                                toSet()
                                toList()
                            }
                            binding.rvMeme.adapter = mainAdapter
                            mainAdapter.mainDiffer
                                .submitList(memeViewModel.allMemeDb[memeViewModel.currentMemeCategory])
                            binding.rvMeme.adapter?.notifyDataSetChanged()
                        }
                    )
                }
            } else {
                Toast.makeText(context, "No internet connection", Toast.LENGTH_LONG).show()
            }
        } catch (e: NetworkErrorException) { Log.e("networkException", e.message!!) }

        if (!memeViewModel.allMemeDb[memeViewModel.currentMemeCategory]!!.isEmpty()) {
            binding.rvMeme.adapter = mainAdapter
            mainAdapter.mainDiffer
                .submitList(memeViewModel.allMemeDb[memeViewModel.currentMemeCategory])
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
                            Snackbar.make(
                                (activity as AppCompatActivity).findViewById(android.R.id.content),
                                "Loading Random Memes",
                                Snackbar.LENGTH_SHORT
                            ).show()
                        }
                    }
                    R.id.action_gaming -> {
                        if (memeViewModel.currentMemeCategory != item.title.toString()) {
                            memeViewModel.currentMemeCategory = item.title.toString()
                            binding.currentCatTv.text = item.title.toString()
                            memeFunObserve(item.title.toString())
                            Snackbar.make(
                                (activity as AppCompatActivity).findViewById(android.R.id.content),
                                "Loading Gaming Memes",
                                Snackbar.LENGTH_SHORT
                            ).show()
                        }
                    }
                    R.id.action_coding -> {
                        if (memeViewModel.currentMemeCategory != item.title.toString()) {
                            memeViewModel.currentMemeCategory = item.title.toString()
                            binding.currentCatTv.text = item.title.toString()
                            memeFunObserve(item.title.toString())
                            Snackbar.make(
                                (activity as AppCompatActivity).findViewById(android.R.id.content),
                                "Loading Coding Memes",
                                Snackbar.LENGTH_SHORT
                            ).show()
                        }
                    }
                    R.id.action_science -> {
                        if (memeViewModel.currentMemeCategory != item.title.toString()) {
                            memeViewModel.currentMemeCategory = item.title.toString()
                            binding.currentCatTv.text = item.title.toString()
                            memeFunObserve(item.title.toString())
                            Snackbar.make(
                                (activity as AppCompatActivity).findViewById(android.R.id.content),
                                "Loading Science Memes",
                                Snackbar.LENGTH_SHORT
                            ).show()
                        }
                    }
                    R.id.action_member_edits -> {
                        if (memeViewModel.currentMemeCategory != item.title.toString()) {
                            memeViewModel.currentMemeCategory = item.title.toString()
                            binding.currentCatTv.text = item.title.toString()
                            memeFunObserve(item.title.toString())
                            Snackbar.make(
                                (activity as AppCompatActivity).findViewById(android.R.id.content),
                                "Loading Member Edits",
                                Snackbar.LENGTH_SHORT
                            ).show()
                        }
                    }

                }
                true
            }
            popupMenu.show()
        }

        binding.rvMeme.attachFab(binding.fabMemberEdits, activity as AppCompatActivity)

        binding.fabMemberEdits.setOnClickListener {
            MemberEditsDialog.newInstance(userdata.afterLoginUserData.imageUrl, null)
                .show(parentFragmentManager, "hello")
        }
        return binding.root
    }

    var position = 1

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.rvMeme.addOnScrollListener(object : RecyclerView.OnScrollListener(){
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                val ll = binding.rvMeme.layoutManager as LinearLayoutManager
                if (newState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE){
                    val ll = binding.rvMeme.layoutManager as LinearLayoutManager
                    memeViewModel.position = ll.findLastVisibleItemPosition()

                    if (ll.findLastCompletelyVisibleItemPosition() == memeViewModel.allMemeDb[memeViewModel.currentMemeCategory]!!.size - 1
                        && memeViewModel.position == memeViewModel.allMemeDb[memeViewModel.currentMemeCategory]!!.size - 1
                    ){
                        // My commit today will be i don't know how, but it works
                        binding.progressBar.visibility = View.VISIBLE
                        val oldList = mutableListOf<MemeModel>()
                        val newList = mutableListOf<MemeModel>()
                        memeViewModel.memeFun(memeViewModel.currentMemeCategory).observe(
                            viewLifecycleOwner, {
                                // set up and populate view
                                newList.addAll(it)
                                newList.removeAll(memeViewModel.allMemeDb[memeViewModel.currentMemeCategory]!!)
                                for (meme in  memeViewModel.allMemeDb[memeViewModel.currentMemeCategory]!!) {
                                    for (me in newList){
                                        if (me.title == meme.title) {
                                            oldList.add(me)
                                        }
                                    }
                                }

                                newList.removeAll(oldList)
                                memeViewModel.allMemeDb[memeViewModel.currentMemeCategory]!!.addAll(newList)
                                MemeRecyclerAdapter(requireContext()).mainDiffer
                                    .submitList(memeViewModel.allMemeDb[memeViewModel.currentMemeCategory])
                                binding.rvMeme.scrollToPosition(memeViewModel.position)

                                Timer().schedule(300){
                                    binding.progressBar.visibility = View.INVISIBLE
                                }
                            }
                        )
                    } else {binding.progressBar.visibility = View.INVISIBLE}
                }
            }
        })

    }

    private fun memeFunObserve(category: String) {
        val mainAdapter = MemeRecyclerAdapter(requireContext())
        if (memeViewModel.allMemeDb[category]!!.isEmpty()){
            memeViewModel.memeFun(category).observe(
                viewLifecycleOwner, {
                    // set up and populate view
                    memeViewModel.allMemeDb[category]?.apply {
                        addAll(0, it)
                        toSet()
                        toList()
                        Log.d("meme", "meme: ${memeViewModel.allMemeDb[category]!!}")
                        binding.rvMeme.adapter = mainAdapter
                        mainAdapter.mainDiffer
                            .submitList(memeViewModel.allMemeDb[memeViewModel.currentMemeCategory])
                        binding.rvMeme.adapter?.notifyDataSetChanged()
                    }
                }
            )
        }else{
            binding.rvMeme.adapter = mainAdapter
            mainAdapter.mainDiffer
                .submitList(memeViewModel.allMemeDb[memeViewModel.currentMemeCategory]!!.toList())
        }
    }

    inner class MemeRecyclerAdapter(
        private val context: Context
    ) :
        RecyclerView.Adapter<MemeRecyclerAdapter.ViewHolder>() {

        private val mainDiffUtil = object : DiffUtil.ItemCallback<MemeModel>(){
            override fun areItemsTheSame(oldItem: MemeModel, newItem: MemeModel): Boolean {
                return oldItem.url == newItem.url
            }

            override fun areContentsTheSame(oldItem: MemeModel, newItem: MemeModel): Boolean {
                return oldItem.title == newItem.title
            }
        }

        val mainDiffer = AsyncListDiffer(this, mainDiffUtil)

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
//            val meme: MemeModel = memeList[position]
            val meme: MemeModel = mainDiffer.currentList.get(position)
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



            binding.rvMeme.viewTreeObserver
                .addOnGlobalLayoutListener {
                    if (!isShown) {
                        val showcaseMainIv: ImageView =
                            binding.rvMeme.getChildAt(0).findViewById<ImageView>(R.id.meme_iv)
                        val showCaseShareIv: ImageView =
                            binding.rvMeme.getChildAt(0).findViewById(R.id.share)
                        val showCaseFilter: ImageButton =
                            (activity as MainActivity).findViewById(R.id.pop)
                        val firstShowCaseBuilder = BubbleShowCaseBuilder(activity as MainActivity)
                            .title("Hold to Edit")
                            .description("Tap and Hold the image to edit them with you texts and more.")
                            .arrowPosition(BubbleShowCase.ArrowPosition.TOP)
                            .backgroundColor(Color.WHITE)
                            .textColor(Color.BLACK)
                            .titleTextSize(17)
                            .descriptionTextSize(15)
                            .image(resources.getDrawable(R.drawable.ic_create))
                            .showOnce("BUBBLE_SHOW_EDIT_ID")
                            .targetView(showcaseMainIv)
                        val secondShowCaseBuilder = BubbleShowCaseBuilder(activity as MainActivity)
                            .title("Double Tap to Favorite")
                            .description("Double tap on your favorite Memes and Templates to bookmark them and access easily.")
                            .arrowPosition(BubbleShowCase.ArrowPosition.TOP)
                            .backgroundColor(Color.WHITE)
                            .textColor(Color.BLACK)
                            .titleTextSize(17)
                            .descriptionTextSize(15)
                            .image(resources.getDrawable(R.drawable.ic_baseline_favorite_24))
                            .showOnce("BUBBLE_SHOW_FAV_ID")
                            .targetView(showcaseMainIv)
                        val thirdShowCaseBuilder = BubbleShowCaseBuilder(activity as MainActivity)
                            .title("Easy Share")
                            .description("Share Memes with your friends easily.")
                            .arrowPosition(BubbleShowCase.ArrowPosition.RIGHT)
                            .backgroundColor(Color.WHITE)
                            .textColor(Color.BLACK)
                            .titleTextSize(17)
                            .descriptionTextSize(15)
                            .image(resources.getDrawable(R.drawable.ic_baseline_share_24))
                            .showOnce("BUBBLE_SHOW_SHARE_ID")
                            .targetView(showCaseShareIv)
                        val fourthShowCaseBuilder = BubbleShowCaseBuilder(activity as MainActivity)
                            .title("Filter Categories")
                            .description("Access new memes in different categories.")
                            .arrowPosition(BubbleShowCase.ArrowPosition.TOP)
                            .backgroundColor(Color.WHITE)
                            .textColor(Color.BLACK)
                            .titleTextSize(17)
                            .descriptionTextSize(15)
                            .image(resources.getDrawable(R.drawable.ic_baseline_filter_list_24))
                            .showOnce("BUBBLE_SHOW_FILTER_ID")
                            .targetView(showCaseFilter)
                        BubbleShowCaseSequence()
                            .addShowCase(fourthShowCaseBuilder)
                            .addShowCase(firstShowCaseBuilder)
                            .addShowCase(secondShowCaseBuilder)
                            .addShowCase(thirdShowCaseBuilder)
                            .show()
                        isShown = true
                    }
                }
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
                                System.currentTimeMillis().toString(),
                                System.currentTimeMillis().toString()
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
                if(meme.url.substring(meme.url.lastIndexOf(".")).toLowerCase(Locale.ROOT).trim() != ".gif") {
                    MainActivity().saveImage(
                        (activity as MainActivity),
                        holder.image.drawable,
                        meme.title
                    )
                }else{
                    Toast.makeText(requireContext(), "Gif download not supported.", Toast.LENGTH_LONG).show()
                }
            }
            holder.like.setOnClickListener { meme.like = !meme.like }
        }

        override fun getItemCount() = mainDiffer.currentList.size

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
            internal val memeHeartAnimOut: ImageView = view.findViewById(R.id.meme_heart_anim_out_iv)
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
                onDoubleWorker(holder, meme)
            }

            override fun onSingle() {
                super.onSingle()
                PhotoViewerDialog.newInstance(meme.url).show(parentFragmentManager, "hello")
            }
        }

        private fun onDoubleWorker(holder: ViewHolder, meme: MemeModel) {
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
                holder.memeHeartAnimOut.alpha = 0.8f
                val drawable: Drawable = holder.memeHeartAnimOut.drawable
                val animatedVectorDrawable: AnimatedVectorDrawable =
                    drawable as AnimatedVectorDrawable
                animatedVectorDrawable.start()

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
                holder.memeHeartAnim.alpha = 0.8f
                val drawable: Drawable = holder.memeHeartAnim.drawable
                val animatedVectorDrawable: AnimatedVectorDrawable =
                    drawable as AnimatedVectorDrawable
                animatedVectorDrawable.start()
            }

            // this rebuilds the whole rv, we need to implement a diffUtil.
//                binding.rvMeme.adapter?.notifyDataSetChanged()


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