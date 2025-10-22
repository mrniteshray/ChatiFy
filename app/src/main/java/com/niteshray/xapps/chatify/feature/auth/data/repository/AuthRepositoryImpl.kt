package com.niteshray.xapps.chatify.feature.auth.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.niteshray.xapps.chatify.feature.auth.domain.model.User
import com.niteshray.xapps.chatify.feature.auth.domain.repository.AuthRepository
import kotlinx.coroutines.tasks.await

class AuthRepositoryImpl : AuthRepository {

    private val auth = FirebaseAuth.getInstance()
    
    // Firestore - For user profiles (complex queries, structured data)
    private val firestore = FirebaseFirestore.getInstance()

    override suspend fun login(email: String, password: String): Result<FirebaseUser> {
        return try {
            val authResult = auth.signInWithEmailAndPassword(email, password).await()
            authResult.user?.let {
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
                    email = email
                )
                // Save user profile to Firestore
                saveUserToFirestore(user)
                
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
                "createdAt" to System.currentTimeMillis()
            )
            
            println("DEBUG: Saving user to Firestore - UID: ${user.uid}, Name: ${user.name}")
            
            firestore.collection("users")
                .document(user.uid)
                .set(userData)
                .await()
                
            println("DEBUG: User saved successfully to Firestore")
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
        auth.signOut()
    }
}
