package com.niteshray.xapps.chatify.feature.home.data.repository

import com.google.firebase.database.*
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.niteshray.xapps.chatify.feature.auth.domain.model.User
import com.niteshray.xapps.chatify.feature.home.domain.model.FriendRequest
import com.niteshray.xapps.chatify.feature.home.domain.model.RequestStatus
import com.niteshray.xapps.chatify.feature.home.domain.repository.HomeRepository
import kotlinx.coroutines.tasks.await

class HomeRepositoryImpl : HomeRepository {
    
    // Firestore - For user profiles (complex queries, structured data)
    private val firestore = FirebaseFirestore.getInstance()

    /**
     * FIRESTORE: Search user by unique username
     */
    override suspend fun searchUserByUsername(username: String): Result<User?> {
        return try {
            println("DEBUG: Searching for username: $username")
            
            // FIRESTORE: Query by username (case-insensitive)
            val snapshot = firestore.collection("users")
                .whereEqualTo("username", username.lowercase())
                .get()
                .await()
            
            if (snapshot.isEmpty) {
                println("DEBUG: No user found with username: $username")
                return Result.success(null)
            }
            
            val doc = snapshot.documents.first()
            val user = User(
                uid = doc.getString("uid") ?: "",
                username = doc.getString("username") ?: "",
                name = doc.getString("name") ?: "",
                email = doc.getString("email") ?: ""
            )
            
            println("DEBUG: User found - Username: ${user.username}, Name: ${user.name}")
            Result.success(user)
        } catch (e: Exception) {
            println("DEBUG: Error searching user: ${e.message}")
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
                username = userDoc.getString("username") ?: "",
                name = userDoc.getString("name") ?: "",
                email = userDoc.getString("email") ?: ""
            )
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * FIRESTORE: Send friend request
     */
    override suspend fun sendFriendRequest(
        fromUserId: String,
        toUserId: String,
        fromUsername: String,
        fromUserName: String
    ): Result<Unit> {
        return try {
            val requestData = hashMapOf(
                "fromUserId" to fromUserId,
                "fromUsername" to fromUsername,
                "fromUserName" to fromUserName,
                "toUserId" to toUserId,
                "status" to "PENDING",
                "timestamp" to System.currentTimeMillis()
            )

            firestore.collection("friendRequests")
                .add(requestData)
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * FIRESTORE: Get friend requests for a user
     */
    override suspend fun getFriendRequests(userId: String): Result<List<FriendRequest>> {
        return try {
            val snapshot = firestore.collection("friendRequests")
                .whereEqualTo("toUserId", userId)
                .whereEqualTo("status", "PENDING")
                .get()
                .await()

            val requests = snapshot.documents.mapNotNull { doc ->
                FriendRequest(
                    id = doc.id,
                    fromUserId = doc.getString("fromUserId") ?: "",
                    fromUsername = doc.getString("fromUsername") ?: "",
                    fromUserName = doc.getString("fromUserName") ?: "",
                    toUserId = doc.getString("toUserId") ?: "",
                    status = RequestStatus.PENDING,
                    timestamp = doc.getLong("timestamp") ?: 0L
                )
            }

            Result.success(requests)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * FIRESTORE: Accept friend request and add to connections
     */
    override suspend fun acceptFriendRequest(
        requestId: String,
        currentUserId: String,
        friendId: String
    ): Result<Unit> {
        return try {
            // Update request status
            firestore.collection("friendRequests")
                .document(requestId)
                .update("status", "ACCEPTED")
                .await()

            // Add to both users' friends lists
            firestore.collection("users")
                .document(currentUserId)
                .update("connectedFriends", FieldValue.arrayUnion(friendId))
                .await()

            firestore.collection("users")
                .document(friendId)
                .update("connectedFriends", FieldValue.arrayUnion(currentUserId))
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * FIRESTORE: Decline friend request
     */
    override suspend fun declineFriendRequest(requestId: String): Result<Unit> {
        return try {
            firestore.collection("friendRequests")
                .document(requestId)
                .update("status", "DECLINED")
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * FIRESTORE: Get connected friends
     */
    override suspend fun getConnectedFriends(userId: String): Result<List<User>> {
        return try {
            val userDoc = firestore.collection("users")
                .document(userId)
                .get()
                .await()

            val friendIds = (userDoc.get("connectedFriends") as? List<*>)
                ?.mapNotNull { it as? String } ?: emptyList()

            if (friendIds.isEmpty()) {
                return Result.success(emptyList())
            }

            val friends = mutableListOf<User>()
            for (friendId in friendIds) {
                val friendDoc = firestore.collection("users")
                    .document(friendId)
                    .get()
                    .await()

                if (friendDoc.exists()) {
                    friends.add(
                        User(
                            uid = friendDoc.getString("uid") ?: "",
                            username = friendDoc.getString("username") ?: "",
                            name = friendDoc.getString("name") ?: "",
                            email = friendDoc.getString("email") ?: ""
                        )
                    )
                }
            }

            Result.success(friends)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * FIRESTORE: Check friendship status between two users
     */
    override suspend fun checkFriendshipStatus(
        currentUserId: String,
        otherUserId: String
    ): Result<RequestStatus?> {
        return try {
            // Check if already connected
            val userDoc = firestore.collection("users")
                .document(currentUserId)
                .get()
                .await()

            val friendIds = (userDoc.get("connectedFriends") as? List<*>)
                ?.mapNotNull { it as? String } ?: emptyList()

            if (friendIds.contains(otherUserId)) {
                return Result.success(RequestStatus.ACCEPTED)
            }

            // Check for pending request (sent or received)
            val sentRequest = firestore.collection("friendRequests")
                .whereEqualTo("fromUserId", currentUserId)
                .whereEqualTo("toUserId", otherUserId)
                .whereEqualTo("status", "PENDING")
                .get()
                .await()

            if (!sentRequest.isEmpty) {
                return Result.success(RequestStatus.PENDING)
            }

            val receivedRequest = firestore.collection("friendRequests")
                .whereEqualTo("fromUserId", otherUserId)
                .whereEqualTo("toUserId", currentUserId)
                .whereEqualTo("status", "PENDING")
                .get()
                .await()

            if (!receivedRequest.isEmpty) {
                return Result.success(RequestStatus.PENDING)
            }

            Result.success(null)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
