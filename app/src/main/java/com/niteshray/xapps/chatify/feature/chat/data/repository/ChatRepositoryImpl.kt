package com.niteshray.xapps.chatify.feature.chat.data.repository

import com.google.firebase.database.*
import com.niteshray.xapps.chatify.feature.chat.domain.model.Message
import com.niteshray.xapps.chatify.feature.chat.domain.repository.ChatRepository
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class ChatRepositoryImpl : ChatRepository {

    /**
     * REALTIME DATABASE: Perfect for chat messages
     * Reasons:
     * 1. Real-time synchronization (messages appear instantly)
     * 2. Efficient ordering by timestamp
     * 3. Low latency for live updates
     * 4. Better for frequent small updates (typing indicators, read receipts)
     */
    private val database = FirebaseDatabase.getInstance().reference

    /**
     * REALTIME DB: Send message with real-time sync
     */
    override suspend fun sendMessage(message: Message): Result<Unit> {
        return try {
            val messageId = database.child("chats")
                .child(message.chatId)
                .child("messages")
                .push()
                .key ?: return Result.failure(Exception("Failed to generate message ID"))

            val messageWithId = message.copy(id = messageId)
            
            val messageData = hashMapOf(
                "id" to messageWithId.id,
                "chatId" to messageWithId.chatId,
                "senderId" to messageWithId.senderId,
                "receiverId" to messageWithId.receiverId,
                "message" to messageWithId.message,
                "timestamp" to messageWithId.timestamp,
                "isRead" to messageWithId.isRead
            )

            database.child("chats")
                .child(message.chatId)
                .child("messages")
                .child(messageId)
                .setValue(messageData)
                .await()

            // Update chat metadata (last message, timestamp)
            val chatMetadata = hashMapOf(
                "lastMessage" to messageWithId.message,
                "lastMessageTime" to messageWithId.timestamp,
                "lastMessageSenderId" to messageWithId.senderId
            )

            database.child("chats")
                .child(message.chatId)
                .updateChildren(chatMetadata as Map<String, Any>)
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            println("DEBUG: Error sending message: ${e.message}")
            e.printStackTrace()
            Result.failure(e)
        }
    }

    /**
     * REALTIME DB: Listen to messages with real-time updates
     * ValueEventListener provides instant sync when new messages arrive
     */
    override suspend fun getMessages(chatId: String): Flow<Result<List<Message>>> = callbackFlow {
        try {
            val messagesRef = database.child("chats").child(chatId).child("messages")
            
            val listener = object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val messages = mutableListOf<Message>()
                    
                    snapshot.children.forEach { messageSnapshot ->
                        val message = Message(
                            id = messageSnapshot.child("id").getValue(String::class.java) ?: "",
                            chatId = messageSnapshot.child("chatId").getValue(String::class.java) ?: "",
                            senderId = messageSnapshot.child("senderId").getValue(String::class.java) ?: "",
                            receiverId = messageSnapshot.child("receiverId").getValue(String::class.java) ?: "",
                            message = messageSnapshot.child("message").getValue(String::class.java) ?: "",
                            timestamp = messageSnapshot.child("timestamp").getValue(Long::class.java) ?: 0L,
                            isRead = messageSnapshot.child("isRead").getValue(Boolean::class.java) ?: false
                        )
                        messages.add(message)
                    }
                    
                    // Sort messages by timestamp
                    messages.sortBy { it.timestamp }
                    
                    trySend(Result.success(messages))
                }

                override fun onCancelled(error: DatabaseError) {
                    trySend(Result.failure(error.toException()))
                }
            }

            messagesRef.addValueEventListener(listener)

            awaitClose {
                messagesRef.removeEventListener(listener)
            }
        } catch (e: Exception) {
            trySend(Result.failure(e))
            close()
        }
    }

    /**
     * REALTIME DB: Mark message as read
     * Instant update for read receipts
     */
    override suspend fun markMessageAsRead(messageId: String, chatId: String): Result<Unit> {
        return try {
            database.child("chats")
                .child(chatId)
                .child("messages")
                .child(messageId)
                .child("isRead")
                .setValue(true)
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * REALTIME DB: Generate unique chat ID
     * Based on user IDs to ensure consistency
     */
    override fun generateChatId(userId1: String, userId2: String): String {
        // Create a consistent chat ID by sorting user IDs
        return if (userId1 < userId2) {
            "${userId1}_${userId2}"
        } else {
            "${userId2}_${userId1}"
        }
    }
}
