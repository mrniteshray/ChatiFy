package com.niteshray.xapps.chatify.feature.chat.domain.repository

import com.niteshray.xapps.chatify.feature.chat.domain.model.Message
import kotlinx.coroutines.flow.Flow

interface ChatRepository {
    suspend fun sendMessage(message: Message): Result<Unit>
    suspend fun getMessages(chatId: String): Flow<Result<List<Message>>>
    suspend fun markMessageAsRead(messageId: String, chatId: String): Result<Unit>
    fun getChatId(userId1: String, userId2: String): String
}
