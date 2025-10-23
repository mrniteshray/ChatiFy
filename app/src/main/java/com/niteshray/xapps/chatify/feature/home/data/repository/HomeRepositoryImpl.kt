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
    
    // Realtime Database - For friend requests (real-time updates)
    private val realtimeDb = FirebaseDatabase.getInstance().reference

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
            val connectedFriends = (doc.get("connectedFriends") as? List<*>)
                ?.mapNotNull { it as? String } ?: emptyList()
            
            val user = User(
                uid = doc.getString("uid") ?: "",
                username = doc.getString("username") ?: "",
                name = doc.getString("name") ?: "",
                email = doc.getString("email") ?: "",
                connectedFriends = connectedFriends
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
            
            val connectedFriends = (userDoc.get("connectedFriends") as? List<*>)
                ?.mapNotNull { it as? String } ?: emptyList()
            
            val user = User(
                uid = userDoc.getString("uid") ?: "",
                username = userDoc.getString("username") ?: "",
                name = userDoc.getString("name") ?: "",
                email = userDoc.getString("email") ?: "",
                connectedFriends = connectedFriends
            )
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * REALTIME DB: Send friend request
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

            // Generate unique key for the request
            val requestKey = realtimeDb.child("friendRequests").push().key
            
            if (requestKey != null) {
                realtimeDb.child("friendRequests").child(requestKey)
                    .setValue(requestData)
                    .await()
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * REALTIME DB: Get friend requests for a user
     */
    override suspend fun getFriendRequests(userId: String): Result<List<FriendRequest>> {
        return try {
            val snapshot = realtimeDb.child("friendRequests")
                .orderByChild("toUserId")
                .equalTo(userId)
                .get()
                .await()

            val requests = mutableListOf<FriendRequest>()
            snapshot.children.forEach { data ->
                val status = data.child("status").getValue(String::class.java)
                if (status == "PENDING") {
                    requests.add(
                        FriendRequest(
                            id = data.key ?: "",
                            fromUserId = data.child("fromUserId").getValue(String::class.java) ?: "",
                            fromUsername = data.child("fromUsername").getValue(String::class.java) ?: "",
                            fromUserName = data.child("fromUserName").getValue(String::class.java) ?: "",
                            toUserId = data.child("toUserId").getValue(String::class.java) ?: "",
                            status = RequestStatus.PENDING,
                            timestamp = data.child("timestamp").getValue(Long::class.java) ?: 0L
                        )
                    )
                }
            }

            Result.success(requests)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * REALTIME DB + FIRESTORE: Accept friend request and add to connections
     */
    override suspend fun acceptFriendRequest(
        requestId: String,
        currentUserId: String,
        friendId: String
    ): Result<Unit> {
        return try {
            // Update request status in Realtime DB
            realtimeDb.child("friendRequests").child(requestId)
                .child("status")
                .setValue("ACCEPTED")
                .await()

            // Add to both users' friends lists in Firestore
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
     * REALTIME DB: Decline friend request
     */
    override suspend fun declineFriendRequest(requestId: String): Result<Unit> {
        return try {
            realtimeDb.child("friendRequests").child(requestId)
                .child("status")
                .setValue("DECLINED")
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * FIRESTORE: Get connected friends
     * Optimized to fetch all friends in a single query
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

            // Firestore has a limit of 10 items for 'in' queries
            // Split into chunks of 10
            val friends = mutableListOf<User>()
            friendIds.chunked(10).forEach { chunk ->
                val friendsSnapshot = firestore.collection("users")
                    .whereIn("uid", chunk)
                    .get()
                    .await()

                friendsSnapshot.documents.forEach { doc ->
                    if (doc.exists()) {
                        val connectedFriends = (doc.get("connectedFriends") as? List<*>)
                            ?.mapNotNull { it as? String } ?: emptyList()
                        
                        friends.add(
                            User(
                                uid = doc.getString("uid") ?: "",
                                username = doc.getString("username") ?: "",
                                name = doc.getString("name") ?: "",
                                email = doc.getString("email") ?: "",
                                connectedFriends = connectedFriends
                            )
                        )
                    }
                }
            }

            Result.success(friends)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * FIRESTORE + REALTIME DB: Check friendship status between two users
     */
    override suspend fun checkFriendshipStatus(
        currentUserId: String,
        otherUserId: String
    ): Result<RequestStatus?> {
        return try {
            // Check if already connected in Firestore
            val userDoc = firestore.collection("users")
                .document(currentUserId)
                .get()
                .await()

            val friendIds = (userDoc.get("connectedFriends") as? List<*>)
                ?.mapNotNull { it as? String } ?: emptyList()

            if (friendIds.contains(otherUserId)) {
                return Result.success(RequestStatus.ACCEPTED)
            }

            // Check for pending requests in Realtime DB
            val snapshot = realtimeDb.child("friendRequests").get().await()
            
            var hasPendingRequest = false
            snapshot.children.forEach { data ->
                val status = data.child("status").getValue(String::class.java)
                val fromUserId = data.child("fromUserId").getValue(String::class.java)
                val toUserId = data.child("toUserId").getValue(String::class.java)
                
                if (status == "PENDING" && 
                    ((fromUserId == currentUserId && toUserId == otherUserId) ||
                     (fromUserId == otherUserId && toUserId == currentUserId))) {
                    hasPendingRequest = true
                    return@forEach
                }
            }

            if (hasPendingRequest) {
                return Result.success(RequestStatus.PENDING)
            }

            Result.success(null)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
