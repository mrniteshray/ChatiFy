package com.niteshray.xapps.chatify.feature.chat.presentation

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.niteshray.xapps.chatify.feature.chat.data.repository.ChatRepositoryImpl
import com.niteshray.xapps.chatify.feature.chat.domain.model.Message
import com.niteshray.xapps.chatify.feature.chat.domain.repository.ChatRepository
import kotlinx.coroutines.launch

class ChatViewModel(
    private val repository: ChatRepository = ChatRepositoryImpl()
) : ViewModel() {

    private val _messagesState = MutableLiveData<MessagesState>(MessagesState.Loading)
    val messagesState: LiveData<MessagesState> = _messagesState

    private val _sendMessageState = MutableLiveData<SendMessageState>(SendMessageState.Idle)
    val sendMessageState: LiveData<SendMessageState> = _sendMessageState

    private var chatId: String = ""

    fun loadMessages(userId1: String, userId2: String) {
        chatId = repository.getChatId(userId1, userId2)
        _messagesState.value = MessagesState.Loading

        viewModelScope.launch {
            try {
                repository.getMessages(chatId).collect { result ->
                    _messagesState.value = if (result.isSuccess) {
                        val messages = result.getOrNull() ?: emptyList()
                        if (messages.isEmpty()) {
                            MessagesState.Empty
                        } else {
                            MessagesState.Success(messages)
                        }
                    } else {
                        MessagesState.Error(result.exceptionOrNull()?.message ?: "Failed to load messages")
                    }
                }
            } catch (e: Exception) {
                _messagesState.value = MessagesState.Error(e.message ?: "Failed to load messages")
            }
        }
    }

    fun sendMessage(senderId: String, receiverId: String, messageText: String) {
        if (messageText.trim().isEmpty()) {
            return
        }

        _sendMessageState.value = SendMessageState.Loading

        viewModelScope.launch {
            val message = Message(
                chatId = chatId,
                senderId = senderId,
                receiverId = receiverId,
                message = messageText.trim(),
                timestamp = System.currentTimeMillis(),
                isRead = false
            )

            val result = repository.sendMessage(message)
            _sendMessageState.value = if (result.isSuccess) {
                SendMessageState.Success
            } else {
                SendMessageState.Error(result.exceptionOrNull()?.message ?: "Failed to send message")
            }
        }
    }

    fun markMessageAsRead(messageId: String) {
        viewModelScope.launch {
            repository.markMessageAsRead(messageId, chatId)
        }
    }

    fun resetSendMessageState() {
        _sendMessageState.value = SendMessageState.Idle
    }
}

sealed class MessagesState {
    object Loading : MessagesState()
    object Empty : MessagesState()
    data class Success(val messages: List<Message>) : MessagesState()
    data class Error(val message: String) : MessagesState()
}

sealed class SendMessageState {
    object Idle : SendMessageState()
    object Loading : SendMessageState()
    object Success : SendMessageState()
    data class Error(val message: String) : SendMessageState()
}
