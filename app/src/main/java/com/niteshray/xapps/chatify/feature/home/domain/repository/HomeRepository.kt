package com.niteshray.xapps.chatify.feature.home.domain.repository

import com.niteshray.xapps.chatify.feature.auth.domain.model.User

interface HomeRepository {
    suspend fun getAllUsers(currentUserId: String): Result<List<User>>
    suspend fun getCurrentUserData(userId: String): Result<User>
}
