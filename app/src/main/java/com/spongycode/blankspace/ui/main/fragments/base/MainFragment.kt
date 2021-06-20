package com.spongycode.blankspace.ui.main.fragments.base

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.imageview.ShapeableImageView
import com.spongycode.blankspace.R
import com.spongycode.blankspace.api.ApiInterface
import com.spongycode.blankspace.databinding.FragmentMainBinding
import com.spongycode.blankspace.model.modelmemes.MemeList
import com.spongycode.blankspace.model.modelmemes.MemeModel
import com.spongycode.blankspace.storage.saveMemeToFavs
import com.spongycode.blankspace.ui.edit.EditActivity
import com.spongycode.blankspace.ui.main.MainActivity
import com.spongycode.blankspace.util.ClickListener
import com.spongycode.blankspace.util.Constants.TAG
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class MainFragment : Fragment() {

    private var _binding: FragmentMainBinding? = null
    private val binding get() = _binding!!

    lateinit var apiInterface: Call<MemeList?>

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

        binding.spCategory.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                Toast.makeText(
                    requireContext(),
                    parent?.getItemAtPosition(position).toString(),
                    Toast.LENGTH_LONG
                ).show()
                fetchMemeByCategory(parent?.getItemAtPosition(position).toString())
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
        }

        return binding.root
    }

    private fun fetchMemeByCategory(category: String) {

        if (category == "Member Edits"){
            return
        }

        when(category){
            "Random" -> apiInterface = ApiInterface.create().getMemesRandom()
            "Coding" -> apiInterface = ApiInterface.create().getMemesProgram()
            "Science" -> apiInterface = ApiInterface.create().getMemesScience()
            "Gaming" -> apiInterface = ApiInterface.create().getMemesGaming()
        }


        apiInterface.enqueue(object : Callback<MemeList?> {
            override fun onResponse(call: Call<MemeList?>, response: Response<MemeList?>) {

                if (response.body() != null) {
                    val memeList: MutableList<MemeModel> = mutableListOf()
                    for (i in response.body()!!.memes!!) {
                        memeList.add(i)
                    }

                    val linearLayoutManager =
                        LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)
                    binding.rvMeme.layoutManager = linearLayoutManager
                    binding.rvMeme.adapter = MemeRecyclerAdapter(requireActivity(), memeList)
                    val adapter = binding.rvMeme.adapter
                    adapter?.notifyDataSetChanged()

                } else {
                    Toast.makeText(requireActivity(), "Error fetching", Toast.LENGTH_LONG).show()

                }

            }

            override fun onFailure(call: Call<MemeList?>, t: Throwable) {
                Log.d(TAG, "Error Fetching: ${t.printStackTrace()}")
                Toast.makeText(requireActivity(), t.toString(), Toast.LENGTH_LONG).show()
            }
        })
    }

    inner class MemeRecyclerAdapter(private val context: Context, private val memeList: List<MemeModel>) :
        RecyclerView.Adapter<MemeRecyclerAdapter.ViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.meme_layout, parent, false)
            return ViewHolder(view)
        }


        @SuppressLint("ClickableViewAccessibility")
        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val meme: MemeModel = memeList[position]
            holder.title.text = meme.title
            Glide.with(holder.itemView.context.applicationContext).load(meme.url).into(holder.image)
            holder.like.setImageResource( if (meme.like) R.drawable.ic_heart_sign else R.drawable.ic_hearth )
            holder.image.setOnTouchListener(TapListener(meme))
            holder.share.setOnClickListener {
                // there's a prblm reading the uri after writing, i will try a different approach tomorrow or later tonight
                val myIntent = Intent()
                val uri = MainActivity().getBitmapFromView((activity as MainActivity), (holder.image.drawable as BitmapDrawable).bitmap)
                myIntent.apply {
                    action = Intent.ACTION_SEND
                    type = "image/*"
                    putExtra(Intent.EXTRA_STREAM, uri)
                }
                val chooser = Intent.createChooser(myIntent, "share file")
                val resInfoList: List<ResolveInfo> = context.packageManager
                    .queryIntentActivities(chooser, PackageManager.MATCH_DEFAULT_ONLY)

                for (resolveInfo in resInfoList) {
                    val packageName = resolveInfo.activityInfo.packageName
                    context.grantUriPermission(
                        packageName,
                        uri,
                        Intent.FLAG_GRANT_WRITE_URI_PERMISSION or Intent.FLAG_GRANT_READ_URI_PERMISSION
                    )
                }

                (activity as MainActivity).startActivity(chooser)

            }
            holder.download.setOnClickListener { MainActivity().saveImage((activity as MainActivity), holder.image.drawable, meme.title) }
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