package com.spongycode.blankspace.model.modelmemes


data class MemeModel(
    var title: String = "",
    var url: String = "",
    var userId: String = "",
    var like: Boolean = false,
    var timestamp: com.google.firebase.Timestamp? = null
)
