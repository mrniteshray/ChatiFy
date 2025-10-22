package com.niteshray.xapps.chatify.feature.home.data.repository

import com.google.firebase.database.*
import com.niteshray.xapps.chatify.feature.auth.domain.model.User
import com.niteshray.xapps.chatify.feature.home.domain.repository.HomeRepository
import kotlinx.coroutines.tasks.await

class HomeRepositoryImpl : HomeRepository {

    private val database = FirebaseDatabase.getInstance().reference

    override suspend fun getAllUsers(currentUserId: String): Result<List<User>> {
        return try {
            val snapshot = database.child("users").get().await()
            
            println("DEBUG: Total users in database: ${snapshot.childrenCount}")

            val allUsers = mutableListOf<User>()
            snapshot.children.forEach { doc ->
                val uid = doc.child("uid").getValue(String::class.java) ?: ""
                val name = doc.child("name").getValue(String::class.java) ?: ""
                val email = doc.child("email").getValue(String::class.java) ?: ""
                
                println("DEBUG: User found - UID: $uid, Name: '$name', Email: $email")
                
                // Exclude current user
                if (uid != currentUserId && name.isNotEmpty()) {
                    val user = User(
                        uid = uid,
                        name = name,
                        email = email,
                        profilePictureUrl = doc.child("profilePictureUrl").getValue(String::class.java) ?: "",
                        isOnline = doc.child("isOnline").getValue(Boolean::class.java) ?: false,
                        lastSeen = doc.child("lastSeen").getValue(Long::class.java) ?: 0L,
                        friends = doc.child("friends").children.mapNotNull { it.getValue(String::class.java) }
                    )
                    allUsers.add(user)
                }
            }

            println("DEBUG: Total users (excluding current user): ${allUsers.size}")
            Result.success(allUsers)
        } catch (e: Exception) {
            println("DEBUG: Error loading users: ${e.message}")
            e.printStackTrace()
            Result.failure(e)
        }
    }

    override suspend fun getCurrentUserData(userId: String): Result<User> {
        return try {
            val snapshot = database.child("users").child(userId).get().await()
            val user = User(
                uid = snapshot.child("uid").getValue(String::class.java) ?: "",
                name = snapshot.child("name").getValue(String::class.java) ?: "",
                email = snapshot.child("email").getValue(String::class.java) ?: "",
                profilePictureUrl = snapshot.child("profilePictureUrl").getValue(String::class.java) ?: "",
                isOnline = snapshot.child("isOnline").getValue(Boolean::class.java) ?: false,
                lastSeen = snapshot.child("lastSeen").getValue(Long::class.java) ?: 0L,
                friends = snapshot.child("friends").children.mapNotNull { it.getValue(String::class.java) }
            )
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
