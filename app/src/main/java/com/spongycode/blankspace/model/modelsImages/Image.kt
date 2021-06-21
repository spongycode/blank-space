package com.spongycode.blankspace.model.modelsImages

import android.text.BoringLayout

data class Image(
    var box_count: Int = 0,
    var height: Int = 0,
    var id: String = "",
    var name: String = "",
    var url: String = "",
    var width: Int = 0,
    var fav: Boolean = false
)