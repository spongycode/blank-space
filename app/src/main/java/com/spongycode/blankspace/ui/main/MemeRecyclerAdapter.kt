package com.spongycode.blankspace.ui.main

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.spongycode.blankspace.R

class MemeRecyclerAdapter(private val context: Context, private val memeList: List<MemeModel>) :
    RecyclerView.Adapter<MemeRecyclerAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.meme_layout, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val meme = memeList[position]
        holder.title.text = meme.title
        Glide.with(holder.itemView.context.applicationContext).load(meme.url).into(holder.image)
    }

    override fun getItemCount(): Int {
        return memeList.size
    }

    inner class ViewHolder internal constructor(view: View) : RecyclerView.ViewHolder(view) {
        internal var title: TextView = view.findViewById(R.id.meme_title)
        internal var image: ImageView = view.findViewById(R.id.meme_iv)
    }
}
