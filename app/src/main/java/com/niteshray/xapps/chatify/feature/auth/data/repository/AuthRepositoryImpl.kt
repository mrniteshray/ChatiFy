package com.niteshray.xapps.chatify.feature.auth.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.FirebaseDatabase
import com.niteshray.xapps.chatify.feature.auth.domain.model.User
import com.niteshray.xapps.chatify.feature.auth.domain.repository.AuthRepository
import kotlinx.coroutines.tasks.await

class AuthRepositoryImpl : AuthRepository {

    private val auth = FirebaseAuth.getInstance()
    private val database = FirebaseDatabase.getInstance().reference

    override suspend fun login(email: String, password: String): Result<FirebaseUser> {
        return try {
            val authResult = auth.signInWithEmailAndPassword(email, password).await()
            authResult.user?.let {
                // Update online status
                updateOnlineStatus(it.uid, true)
                Result.success(it)
            } ?: Result.failure(Exception("User is null"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun signup(email: String, password: String, name: String): Result<FirebaseUser> {
        return try {
            val authResult = auth.createUserWithEmailAndPassword(email, password).await()
            authResult.user?.let { firebaseUser ->
                val user = User(
                    uid = firebaseUser.uid,
                    name = name,
                    email = email,
                    isOnline = true,
                    lastSeen = System.currentTimeMillis(),
                    friends = emptyList()
                )
                saveUserToFirestore(user)
                Result.success(firebaseUser)
            } ?: Result.failure(Exception("User is null"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun saveUserToFirestore(user: User): Result<Unit> {
        return try {
            val userData = hashMapOf(
                "uid" to user.uid,
                "name" to user.name,
                "email" to user.email,
                "profilePictureUrl" to user.profilePictureUrl,
                "isOnline" to user.isOnline,
                "lastSeen" to user.lastSeen,
                "friends" to user.friends
            )
            
            println("DEBUG: Saving user to database - UID: ${user.uid}, Name: ${user.name}, Email: ${user.email}")
            
            database.child("users")
                .child(user.uid)
                .setValue(userData)
                .await()
                
            println("DEBUG: User saved successfully to /users/${user.uid}")
            Result.success(Unit)
        } catch (e: Exception) {
            println("DEBUG: Error saving user: ${e.message}")
            e.printStackTrace()
            Result.failure(e)
        }
    }

    override suspend fun getCurrentUser(): FirebaseUser? {
        return auth.currentUser
    }

    override suspend fun logout() {
        auth.currentUser?.uid?.let { uid ->
            updateOnlineStatus(uid, false)
        }
        auth.signOut()
    }

    private suspend fun updateOnlineStatus(uid: String, isOnline: Boolean) {
        try {
            val updates = hashMapOf<String, Any>(
                "isOnline" to isOnline,
                "lastSeen" to System.currentTimeMillis()
            )
            database.child("users")
                .child(uid)
                .updateChildren(updates)
                .await()
        } catch (e: Exception) {
            // Handle silently or log
        }
    }
}
