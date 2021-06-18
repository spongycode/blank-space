package com.spongycode.blankspace.storage

import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.spongycode.blankspace.model.modelmemes.MemeModel
import com.spongycode.blankspace.model.modelsImages.Image
import com.spongycode.blankspace.ui.auth.AuthActivity

fun saveMemeToFavs(meme: MemeModel){
    meme?.let {
        Firebase.firestore.collection("userImages")
            .document("${AuthActivity().firebaseAuth.currentUser?.email}/favMemes/${meme.title}")
            .set(meme, SetOptions.merge())
    }
}

fun saveTemplate(image: Image){
    image?.let {
        Firebase.firestore.collection("userImages")
            .document("${AuthActivity().firebaseAuth.currentUser?.email}/favTemplates/${image.name}")
            .set(image, SetOptions.merge())
    }
}