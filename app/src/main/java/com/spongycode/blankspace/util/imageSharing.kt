package com.spongycode.blankspace.util

import android.content.Intent
import android.net.Uri
import android.provider.MediaStore
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.NavHostFragment.findNavController
import com.spongycode.blankspace.R
import com.spongycode.blankspace.model.UserModel

val gallery = Intent(
    Intent.ACTION_PICK,
    MediaStore.Images.Media.INTERNAL_CONTENT_URI
)

fun loadImage(mUri : Uri, fragment: Fragment, sender: UserModel, receiver: UserModel?){
    val bundle = bundleOf(Constants.ImageShare to mUri.toString(), "s" to sender, "r" to receiver)
    findNavController(fragment).navigate(R.id.imageShareFragment, bundle)
}