package com.niteshray.xapps.chatify.feature.auth.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import com.niteshray.xapps.chatify.feature.auth.domain.model.User
import com.niteshray.xapps.chatify.feature.auth.domain.repository.AuthRepository
import kotlinx.coroutines.tasks.await

class AuthRepositoryImpl : AuthRepository {

    private val auth = FirebaseAuth.getInstance()
    
    // Realtime Database - For presence/online status (real-time updates)
    private val realtimeDb = FirebaseDatabase.getInstance().reference
    
    // Firestore - For user profiles (complex queries, structured data)
    private val firestore = FirebaseFirestore.getInstance()

    override suspend fun login(email: String, password: String): Result<FirebaseUser> {
        return try {
            val authResult = auth.signInWithEmailAndPassword(email, password).await()
            authResult.user?.let {
                // Update online status in Realtime DB (real-time presence)
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
                // Save user profile to Firestore
                saveUserToFirestore(user)
                
                // Initialize online status in Realtime DB
                updateOnlineStatus(firebaseUser.uid, true)
                
                Result.success(firebaseUser)
            } ?: Result.failure(Exception("User is null"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun saveUserToFirestore(user: User): Result<Unit> {
        return try {
            // FIRESTORE: Store user profile (structured data, queryable)
            val userData = hashMapOf(
                "uid" to user.uid,
                "name" to user.name,
                "email" to user.email,
                "profilePictureUrl" to user.profilePictureUrl,
                "friends" to user.friends,
                "createdAt" to System.currentTimeMillis()
            )
            
            println("DEBUG: Saving user to Firestore - UID: ${user.uid}, Name: ${user.name}")
            
            firestore.collection("users")
                .document(user.uid)
                .set(userData)
                .await()
                
            println("DEBUG: User saved successfully to Firestore")
            
            // REALTIME DB: Store presence data (real-time updates)
            val presenceData = hashMapOf(
                "uid" to user.uid,
                "isOnline" to user.isOnline,
                "lastSeen" to user.lastSeen
            )
            
            realtimeDb.child("presence")
                .child(user.uid)
                .setValue(presenceData)
                .await()
                
            println("DEBUG: User presence initialized in Realtime DB")
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
            // Update online status to false in Realtime DB
            updateOnlineStatus(uid, false)
        }
        auth.signOut()
    }

    /**
     * REALTIME DATABASE: Update online/offline status
     * Perfect for presence system with real-time updates
     */
    private suspend fun updateOnlineStatus(uid: String, isOnline: Boolean) {
        try {
            val presenceData = hashMapOf<String, Any>(
                "isOnline" to isOnline,
                "lastSeen" to System.currentTimeMillis()
            )
            
            realtimeDb.child("presence")
                .child(uid)
                .updateChildren(presenceData)
                .await()
                
            println("DEBUG: Updated presence for $uid - Online: $isOnline")
        } catch (e: Exception) {
            println("DEBUG: Error updating presence: ${e.message}")
        }
    }
}
