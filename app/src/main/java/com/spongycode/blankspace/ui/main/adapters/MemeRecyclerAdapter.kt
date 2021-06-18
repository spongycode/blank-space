package com.spongycode.blankspace.ui.main.adapters

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.spongycode.blankspace.R
import com.spongycode.blankspace.model.modelmemes.MemeModel
import com.spongycode.blankspace.storage.saveMemeToFavs
import com.spongycode.blankspace.ui.edit.EditActivity
import com.spongycode.blankspace.util.ClickListener

class MemeRecyclerAdapter(private val context: Context, private val memeList: List<MemeModel>) :
    RecyclerView.Adapter<MemeRecyclerAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.meme_layout, parent, false)
        return ViewHolder(view)
    }


    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val meme: MemeModel = memeList[position]
        holder.title.text = meme.title
        Glide.with(holder.itemView.context.applicationContext).load(meme.url).into(holder.image)
        holder.image.setOnTouchListener(TapListener(meme))
    }

    override fun getItemCount() = memeList.size

    inner class ViewHolder internal constructor(view: View) : RecyclerView.ViewHolder(view) {
        internal var title: TextView = view.findViewById(R.id.meme_title)
        internal var image: ImageView = view.findViewById(R.id.meme_iv)
    }

    inner class TapListener(private val meme: MemeModel): ClickListener(context){
        override fun onLong() {
            val myIntent = Intent(context, EditActivity::class.java)
            myIntent.putExtra("imageurl", meme.url)
            context.startActivity(myIntent)
        }

        override fun onDouble() {
            saveMemeToFavs(meme)
        }
    }
}
