package com.niteshray.xapps.chatify.feature.auth.domain.repository

import com.google.firebase.auth.FirebaseUser
import com.niteshray.xapps.chatify.feature.auth.domain.model.User

interface AuthRepository {
    suspend fun login(email: String, password: String): Result<FirebaseUser>
    suspend fun signup(email: String, password: String, name: String, username: String): Result<FirebaseUser>
    suspend fun saveUserToFirestore(user: User): Result<Unit>
    suspend fun isUsernameAvailable(username: String): Boolean
    suspend fun getCurrentUser(): FirebaseUser?
    suspend fun logout()
}
