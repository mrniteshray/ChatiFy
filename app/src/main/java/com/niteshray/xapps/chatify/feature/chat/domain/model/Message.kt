package com.niteshray.xapps.chatify.feature.chat.domain.model

data class Message(
    val id: String = "",
    val chatId: String = "",
    val senderId: String = "",
    val receiverId: String = "",
    val message: String = "",
    val timestamp: Long = 0L,
    val isRead: Boolean = false
)
