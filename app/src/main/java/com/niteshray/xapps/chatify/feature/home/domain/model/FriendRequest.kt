package com.niteshray.xapps.chatify.feature.home.domain.model

data class FriendRequest(
    val id: String = "",
    val fromUserId: String = "",
    val fromUsername: String = "",
    val fromUserName: String = "",
    val toUserId: String = "",
    val status: RequestStatus = RequestStatus.PENDING,
    val timestamp: Long = 0L
)

enum class RequestStatus {
    PENDING,
    ACCEPTED,
    DECLINED
}
