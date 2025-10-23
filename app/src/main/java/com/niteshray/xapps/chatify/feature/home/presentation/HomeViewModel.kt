package com.niteshray.xapps.chatify.feature.home.presentation

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.niteshray.xapps.chatify.feature.auth.domain.model.User
import com.niteshray.xapps.chatify.feature.home.data.repository.HomeRepositoryImpl
import com.niteshray.xapps.chatify.feature.home.domain.model.FriendRequest
import com.niteshray.xapps.chatify.feature.home.domain.model.RequestStatus
import com.niteshray.xapps.chatify.feature.home.domain.repository.HomeRepository
import kotlinx.coroutines.launch

class HomeViewModel(
    private val repository: HomeRepository = HomeRepositoryImpl()
) : ViewModel() {

    private val _searchState = MutableLiveData<SearchState>(SearchState.Idle)
    val searchState: LiveData<SearchState> = _searchState

    private val _friendRequestsState = MutableLiveData<FriendRequestsState>()
    val friendRequestsState: LiveData<FriendRequestsState> = _friendRequestsState

    private val _connectedFriendsState = MutableLiveData<ConnectedFriendsState>()
    val connectedFriendsState: LiveData<ConnectedFriendsState> = _connectedFriendsState

    private val _friendshipStatusState = MutableLiveData<FriendshipStatusState>()
    val friendshipStatusState: LiveData<FriendshipStatusState> = _friendshipStatusState

    private val _requestActionState = MutableLiveData<RequestActionState>()
    val requestActionState: LiveData<RequestActionState> = _requestActionState

    fun searchUser(username: String) {
        _searchState.value = SearchState.Loading
        
        viewModelScope.launch {
            try {
                val result = repository.searchUserByUsername(username)
                _searchState.value = if (result.isSuccess) {
                    SearchState.Success(result.getOrNull())
                } else {
                    SearchState.Error(result.exceptionOrNull()?.message ?: "Failed to search user")
                }
            } catch (e: Exception) {
                _searchState.value = SearchState.Error(e.message ?: "Failed to search user")
            }
        }
    }

    fun sendFriendRequest(fromUserId: String, toUserId: String, fromUsername: String, fromUserName: String) {
        viewModelScope.launch {
            try {
                val result = repository.sendFriendRequest(fromUserId, toUserId, fromUsername, fromUserName)
                if (result.isSuccess) {
                    _requestActionState.value = RequestActionState.RequestSent
                } else {
                    _requestActionState.value = RequestActionState.Error(result.exceptionOrNull()?.message ?: "Failed to send request")
                }
            } catch (e: Exception) {
                _requestActionState.value = RequestActionState.Error(e.message ?: "Failed to send request")
            }
        }
    }

    fun loadFriendRequests(userId: String) {
        viewModelScope.launch {
            try {
                val result = repository.getFriendRequests(userId)
                _friendRequestsState.value = if (result.isSuccess) {
                    FriendRequestsState.Success(result.getOrNull() ?: emptyList())
                } else {
                    FriendRequestsState.Error(result.exceptionOrNull()?.message ?: "Failed to load requests")
                }
            } catch (e: Exception) {
                _friendRequestsState.value = FriendRequestsState.Error(e.message ?: "Failed to load requests")
            }
        }
    }

    fun acceptFriendRequest(requestId: String, currentUserId: String, friendId: String) {
        viewModelScope.launch {
            try {
                val result = repository.acceptFriendRequest(requestId, currentUserId, friendId)
                if (result.isSuccess) {
                    _requestActionState.value = RequestActionState.RequestAccepted
                    loadFriendRequests(currentUserId) // Refresh list
                    loadConnectedFriends(currentUserId) // Refresh friends
                } else {
                    _requestActionState.value = RequestActionState.Error(result.exceptionOrNull()?.message ?: "Failed to accept")
                }
            } catch (e: Exception) {
                _requestActionState.value = RequestActionState.Error(e.message ?: "Failed to accept")
            }
        }
    }

    fun declineFriendRequest(requestId: String, currentUserId: String) {
        viewModelScope.launch {
            try {
                val result = repository.declineFriendRequest(requestId)
                if (result.isSuccess) {
                    _requestActionState.value = RequestActionState.RequestDeclined
                    loadFriendRequests(currentUserId) // Refresh list
                } else {
                    _requestActionState.value = RequestActionState.Error(result.exceptionOrNull()?.message ?: "Failed to decline")
                }
            } catch (e: Exception) {
                _requestActionState.value = RequestActionState.Error(e.message ?: "Failed to decline")
            }
        }
    }

    fun loadConnectedFriends(userId: String) {
        viewModelScope.launch {
            try {
                _connectedFriendsState.value = ConnectedFriendsState.Loading
                val result = repository.getConnectedFriends(userId)
                _connectedFriendsState.value = if (result.isSuccess) {
                    ConnectedFriendsState.Success(result.getOrNull() ?: emptyList())
                } else {
                    ConnectedFriendsState.Error(result.exceptionOrNull()?.message ?: "Failed to load friends")
                }
            } catch (e: Exception) {
                _connectedFriendsState.value = ConnectedFriendsState.Error(e.message ?: "Failed to load friends")
            }
        }
    }

    fun checkFriendshipStatus(currentUserId: String, otherUserId: String) {
        viewModelScope.launch {
            try {
                val result = repository.checkFriendshipStatus(currentUserId, otherUserId)
                _friendshipStatusState.value = if (result.isSuccess) {
                    FriendshipStatusState.Status(result.getOrNull())
                } else {
                    FriendshipStatusState.Error(result.exceptionOrNull()?.message ?: "Failed to check status")
                }
            } catch (e: Exception) {
                _friendshipStatusState.value = FriendshipStatusState.Error(e.message ?: "Failed to check status")
            }
        }
    }

    fun getCurrentUserData(userId: String, callback: (User?) -> Unit) {
        viewModelScope.launch {
            try {
                val result = repository.getCurrentUserData(userId)
                callback(result.getOrNull())
            } catch (e: Exception) {
                callback(null)
            }
        }
    }
}

sealed class SearchState {
    object Idle : SearchState()
    object Loading : SearchState()
    data class Success(val user: User?) : SearchState()
    data class Error(val message: String) : SearchState()
}

sealed class FriendRequestsState {
    data class Success(val requests: List<FriendRequest>) : FriendRequestsState()
    data class Error(val message: String) : FriendRequestsState()
}

sealed class ConnectedFriendsState {
    object Loading : ConnectedFriendsState()
    data class Success(val friends: List<User>) : ConnectedFriendsState()
    data class Error(val message: String) : ConnectedFriendsState()
}

sealed class FriendshipStatusState {
    data class Status(val status: RequestStatus?) : FriendshipStatusState()
    data class Error(val message: String) : FriendshipStatusState()
}

sealed class RequestActionState {
    object RequestSent : RequestActionState()
    object RequestAccepted : RequestActionState()
    object RequestDeclined : RequestActionState()
    data class Error(val message: String) : RequestActionState()
}
