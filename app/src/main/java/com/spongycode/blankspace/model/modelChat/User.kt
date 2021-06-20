package com.spongycode.blankspace.model.modelChat

import android.os.Parcelable
import java.io.Serializable

data class User (
    var userId: String = "",
    var name: String = "",
    var profilePicture: String = "",
    var status: String = ""
): Serializable