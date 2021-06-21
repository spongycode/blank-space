package com.spongycode.blankspace.model.modelsImages

import android.text.BoringLayout

data class Image(
    val box_count: Int,
    val height: Int,
    val id: String,
    val name: String,
    val url: String,
    val width: Int,
    var fav: Boolean
)