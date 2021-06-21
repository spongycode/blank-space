package com.rick.chatapp.chat.model

data class ChatMessage(
    var messageId: String = "",
    var messageText: String = "",
    var messageSenderID: String = "",
    var messageReceiverID: String = "",
    var messageTime: Long = 1L
)