package com.niteshray.xapps.chatify.feature.auth.domain.model

data class User(
    val uid: String = "",
    val name: String = "",
    val email: String = "",
    val profilePictureUrl: String = "",
    val isOnline: Boolean = false,
    val lastSeen: Long = 0L,
    val friends: List<String> = emptyList() // List of friend UIDs
)
