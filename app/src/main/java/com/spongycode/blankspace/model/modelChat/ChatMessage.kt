package com.spongycode.blankspace.model.modelChat

data class ChatMessage(
    var messageId: String = "",
    var messageText: String = "",
    var messageSenderID: String = "",
    var messageReceiverID: String = "",
    var timeStamp: Long = 1L
)