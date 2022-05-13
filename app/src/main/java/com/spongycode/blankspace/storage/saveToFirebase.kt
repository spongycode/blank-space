package com.spongycode.blankspace.storage

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import com.spongycode.blankspace.model.modelmemes.MemeModel
import com.spongycode.blankspace.model.modelsImages.Image
import com.spongycode.blankspace.ui.auth.AuthActivity

const val TAG: String = "firebase"
val imageCollection = Firebase.firestore.collection("userImages")
val memeList = mutableListOf<MemeModel>()
val imageList = mutableListOf<Image>()

fun saveMemeToFavs(meme: MemeModel) {
    var title =
        meme.url // changing to url from title as sometimes title is blank as also always not unique
    val re = Regex("[^A-Za-z0-9 ]")
    title = re.replace(title, "") // remove all special characters
    meme.let {
        imageCollection
            .document("${AuthActivity().firebaseAuth.currentUser?.email}/favMemes/$title")
            .set(meme, SetOptions.merge())
    }
}

fun saveTemplate(image: Image) {
    var name =
        image.url  // changing to url from title as sometimes title is blank as also always not unique
    val re = Regex("[^A-Za-z0-9 ]")
    name = re.replace(name, "") // remove all special characters
    image.let {
        imageCollection
            .document("${AuthActivity().firebaseAuth.currentUser?.email}/favTemplates/$name")
            .set(image, SetOptions.merge())
    }
}
fun removeTemplate(image: Image) {
    var name =
        image.url  // changing to url from title as sometimes title is blank as also always not unique
    val re = Regex("[^A-Za-z0-9 ]")
    name = re.replace(name, "") // remove all special characters
    image.let {
        imageCollection
            .document("${AuthActivity().firebaseAuth.currentUser?.email}/favTemplates/$name")
            .delete()
    }
}

fun removeMeme(meme: MemeModel) {
    var name =
        meme.url  // changing to url from title as sometimes title is blank as also always not unique
    val re = Regex("[^A-Za-z0-9 ]")
    name = re.replace(name, "") // remove all special characters
    meme.let {
        imageCollection
            .document("${AuthActivity().firebaseAuth.currentUser?.email}/favMemes/$name")
            .delete()
    }
}

fun getMemeFromFavs(): LiveData<List<MemeModel>> {
    val memeLiveData: MutableLiveData<List<MemeModel>> = MutableLiveData()
    Firebase.firestore.collection("userImages/${AuthActivity().firebaseAuth.currentUser?.email}/favMemes")
        .limit(25)
        .addSnapshotListener { snapshot, error ->
            error?.let {
                Log.d(TAG, error.message!!)
            }

            memeList.clear()
            snapshot?.let {
                for (m in it) {
                    val meme = m.toObject<MemeModel>()
                    memeList.add(meme)
                    Log.d("memeF", "meme: $meme")
                }
            }
        }
    memeLiveData.value = memeList
    return memeLiveData
}

fun getTemplateFromFavs(): LiveData<List<Image>> {
    val imageLiveData: MutableLiveData<List<Image>> = MutableLiveData()
    Firebase.firestore.collection("userImages/${AuthActivity().firebaseAuth.currentUser?.email}/favTemplates")
        .addSnapshotListener { snapshot, error ->
            error?.let {
                Log.d(TAG, error.message!!)
            }

            imageList.clear()
            snapshot?.let {
                for (i in it) {
                    val image = i.toObject<Image>()
                    imageList.add(image)
                    Log.d("image", "image: $image")
                }
            }
        }
    imageLiveData.value = imageList
    return imageLiveData
}
fun checkTemplateIsFav(img: Image) {
    Firebase.firestore.collection("userImages/${AuthActivity().firebaseAuth.currentUser?.email}/favTemplates")
        .whereEqualTo("url", img.url)
        .get()
        .addOnCompleteListener { task ->
            if (task.isSuccessful) {
                if (task.result!!.size() > 0) {
                    img.fav = true
                }
            }
        }
}

fun checkMemeIsFav(meme: MemeModel) {
    Firebase.firestore.collection("userImages/${AuthActivity().firebaseAuth.currentUser?.email}/favMemes")
        .whereEqualTo("url", meme.url)
        .get()
        .addOnCompleteListener { task ->
            if (task.isSuccessful) {
                if (task.result!!.size() > 0) {
                    meme.like = true
                }
            }
        }
}
