package com.spongycode.blankspace.model.modelChat

import java.io.Serializable

data class ChatScreenMessage(
    var messageId: String = "",
    var messageText: String = "",
    var messageTime: Long = 1L,
    var messageReceiverId: String = "",
    var nameReceiver: String = "",
    var profilePictureReceiver: String = "",
    var messageSenderId: String = "",
    var nameSender: String = "",
    var profilePictureSender: String = ""
): Serializable