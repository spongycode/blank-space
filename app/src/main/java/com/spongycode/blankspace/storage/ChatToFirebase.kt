package com.spongycode.blankspace.storage

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import com.spongycode.blankspace.model.UserModel
import com.spongycode.blankspace.model.modelChat.ChatMessage
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

// and then this one requires parameters anyway

val message = mutableListOf<ChatMessage>()
fun receiveChatMessage(collection: String): LiveData<List<ChatMessage>>{
    val messagesLiveData = MutableLiveData<List<ChatMessage>>()
    CoroutineScope(Dispatchers.IO).launch{  // listen to every event at this collection
        // add every new messasge to the messages list
        Firebase.firestore.collection(collection)
            .orderBy("messageTime")
            .addSnapshotListener { querySnapshot, error ->

                // if error then log it
                error?.let {
                    Log.d("receiveMessage", error.message!!)
                }

                // clear the list for older messages and redownload all messages again
                message.clear()
                querySnapshot?.let {
                    for (document in it) {
                        val message = document.toObject<ChatMessage>()
                        messages.add(message)
                    }

                    message.sortByDescending { it.messageTime }
                }
            }
    }
    messagesLiveData.value = message
    return messagesLiveData
}

// i tried to use repository pattern, call receiveMessages from viewModel
// but there's a bug, and i have no more time to fix it
// i think i should separate the functions
val messages = mutableListOf<ChatMessage>()
fun receiveGroupMessage(collection: String): LiveData<List<ChatMessage>> {
    val messageLiveData = MutableLiveData<List<ChatMessage>>()
    CoroutineScope(Dispatchers.IO).launch{
        val a = Firebase.firestore
            .collection(collection)
        a
            .orderBy("messageTime")
            .addSnapshotListener { value, error ->
                error?.let {
                    Log.w("error", error)
                }

                value?.let {
                    messages.clear()
                    for (doc in it){
                        val message = doc.toObject<ChatMessage>()
                        messages.add(message)
                        Log.d("error", "data: ${messages}")
                    }
                    messages.sortByDescending { it.messageTime }
                }
            }
    }
    messageLiveData.value = messages
    return messageLiveData
}
