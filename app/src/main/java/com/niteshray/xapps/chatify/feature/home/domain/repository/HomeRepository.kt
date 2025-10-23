package com.niteshray.xapps.chatify.feature.home.domain.repository

import com.niteshray.xapps.chatify.feature.auth.domain.model.User
import com.niteshray.xapps.chatify.feature.home.domain.model.FriendRequest
import com.niteshray.xapps.chatify.feature.home.domain.model.RequestStatus

interface HomeRepository {
    suspend fun searchUserByUsername(username: String): Result<User?>
    suspend fun getCurrentUserData(userId: String): Result<User>
    
    // Friend Request operations
    suspend fun sendFriendRequest(fromUserId: String, toUserId: String, fromUsername: String, fromUserName: String): Result<Unit>
    suspend fun getFriendRequests(userId: String): Result<List<FriendRequest>>
    suspend fun acceptFriendRequest(requestId: String, currentUserId: String, friendId: String): Result<Unit>
    suspend fun declineFriendRequest(requestId: String): Result<Unit>
    suspend fun getConnectedFriends(userId: String): Result<List<User>>
    suspend fun checkFriendshipStatus(currentUserId: String, otherUserId: String): Result<RequestStatus?>
}
