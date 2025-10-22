package com.niteshray.xapps.chatify.feature.home.data.repository

import com.google.firebase.database.*
import com.google.firebase.firestore.FirebaseFirestore
import com.niteshray.xapps.chatify.feature.auth.domain.model.User
import com.niteshray.xapps.chatify.feature.home.domain.repository.HomeRepository
import kotlinx.coroutines.tasks.await

class HomeRepositoryImpl : HomeRepository {

    // Realtime Database - For presence/online status (real-time updates)
    private val realtimeDb = FirebaseDatabase.getInstance().reference
    
    // Firestore - For user profiles (complex queries, structured data)
    private val firestore = FirebaseFirestore.getInstance()

    /**
     * FIRESTORE: Get all users (better for complex queries, pagination, filtering)
     */
    override suspend fun getAllUsers(currentUserId: String): Result<List<User>> {
        return try {
            // FIRESTORE: Query user profiles
            val snapshot = firestore.collection("users")
                .get()
                .await()
            
            println("DEBUG: Total users in Firestore: ${snapshot.size()}")

            val allUsers = mutableListOf<User>()
            
            // Get user profiles from Firestore
            snapshot.documents.forEach { doc ->
                val uid = doc.getString("uid") ?: ""
                val name = doc.getString("name") ?: ""
                val email = doc.getString("email") ?: ""
                
                println("DEBUG: User found - UID: $uid, Name: '$name', Email: $email")
                
                // Exclude current user
                if (uid != currentUserId && name.isNotEmpty()) {
                    val user = User(
                        uid = uid,
                        name = name,
                        email = email
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

    /**
     * FIRESTORE: Get user profile data
     */
    override suspend fun getCurrentUserData(userId: String): Result<User> {
        return try {
            // FIRESTORE: Get user profile
            val userDoc = firestore.collection("users")
                .document(userId)
                .get()
                .await()
            
            val user = User(
                uid = userDoc.getString("uid") ?: "",
                name = userDoc.getString("name") ?: "",
                email = userDoc.getString("email") ?: ""
            )
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
