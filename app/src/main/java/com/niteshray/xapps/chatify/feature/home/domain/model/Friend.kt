package com.niteshray.xapps.chatify.feature.home.domain.model

data class Friend(
    val uid: String = "",
    val name: String = "",
    val email: String = "",
    val lastMessage: String = "",
    val lastMessageTime: Long = 0L
)
