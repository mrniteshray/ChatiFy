package com.niteshray.xapps.chatify.feature.auth.domain.model

data class User(
    val uid: String = "",
    val username: String = "", // Unique identifier for connecting
    val name: String = "",
    val email: String = ""
)
