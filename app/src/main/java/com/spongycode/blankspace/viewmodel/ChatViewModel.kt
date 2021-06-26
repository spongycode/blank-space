package com.spongycode.blankspace.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.spongycode.blankspace.model.UserModel
import com.spongycode.blankspace.model.modelChat.ChatMessage
import com.spongycode.blankspace.storage.requestAllUsers
import com.spongycode.blankspace.storage.requestCurrentUser

class ChatViewModel: ViewModel() {

    var currentText = ""
    val listMessages = mutableListOf<ChatMessage>()
    val listUsers = mutableListOf<UserModel>()

    val user: LiveData<List<UserModel>> = requestCurrentUser()
    val userLiveData: LiveData<List<UserModel>> = requestAllUsers()


}