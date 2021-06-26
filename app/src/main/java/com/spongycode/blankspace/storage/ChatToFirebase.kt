package com.spongycode.blankspace.storage

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.toObject
import com.spongycode.blankspace.model.UserModel
import com.spongycode.blankspace.ui.main.MainActivity.Companion.usersCollectionReference
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

fun requestCurrentUser(): LiveData<List<UserModel>> {
    val user = MutableLiveData<List<UserModel>>()
    val list = mutableListOf<UserModel>()
    CoroutineScope(Dispatchers.IO).launch {
        try {
            val querySnapshot = usersCollectionReference
                .whereEqualTo("userId", FirebaseAuth.getInstance().currentUser!!.uid)
                .get()
                .await()
            // this query only contains one element which is the user whose id's matches our current
            list.add(querySnapshot.documents[0].toObject<UserModel>()!!)
        } catch (e: Exception){
            Log.d("user Exception", "failed to get current user")
        }
    }
    Log.d("user", "Stupid fuck: $user")
    user.value = list
    return user
}

fun requestAllUsers(): LiveData<List<UserModel>> {
    val usersLiveData = MutableLiveData<List<UserModel>>()
    val userList = mutableListOf<UserModel>()
    CoroutineScope(Dispatchers.IO).launch {
        try {
            val querySnapshot = usersCollectionReference.get().await()
            for (dc in querySnapshot){
                val user = dc.toObject<UserModel>()
                userList.add(user)
                Log.d("user", "antonio: $user")
            }
        } catch (e: FirebaseException){
            Log.d("users", "failed to download users, ${e.printStackTrace()}")
        }
    }
    userList.sortBy { it.username }
    usersLiveData.value = userList
    return usersLiveData
}
