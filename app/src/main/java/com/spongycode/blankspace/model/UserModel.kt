package com.spongycode.blankspace.model

import java.io.Serializable

data class UserModel(
    var userId: String = "",
    var email: String = "",
    var username: String = "",
    var imageUrl: String = "",
    var status: String = ""
): Serializable