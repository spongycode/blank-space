package com.spongycode.blankspace.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.spongycode.blankspace.model.UserModel
import com.spongycode.blankspace.model.modelChat.ChatMessage
import com.spongycode.blankspace.storage.receiveMessages
import com.spongycode.blankspace.storage.requestAllUsers
import com.spongycode.blankspace.storage.requestCurrentUser

class ChatViewModel: ViewModel() {

    // hold the value of the message being write
    var currentText = ""

    // hold the value of users retrieved from firebase
    val listUsers = mutableListOf<UserModel>()
    val userLiveData: LiveData<List<UserModel>> = requestAllUsers()

    // hold the value of the current user
    val user: LiveData<List<UserModel>> = requestCurrentUser()

    // hold the value of messages, retrieved from firebase

    // group chat
    val groupMessages = mutableListOf<ChatMessage>()
    val chatMessages = mutableListOf<ChatMessage>()
    fun receiveMessage(collection: String): LiveData<List<ChatMessage>>{
        val messages: LiveData<List<ChatMessage>>
        = receiveMessages(collection)
        return messages
    }

}