package com.niteshray.xapps.chatify.feature.auth.presentation

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class AuthViewModel : ViewModel() {

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    private val _authState = MutableLiveData<AuthState>(AuthState.Idle)
    val authState: LiveData<AuthState> = _authState

    fun login(email: String, password: String) {
        _authState.value = AuthState.Loading

        auth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener { authResult ->
                _authState.value = AuthState.Success
            }
            .addOnFailureListener { exception ->
                _authState.value = AuthState.Error(
                    exception.message ?: "Login failed"
                )
            }
    }

    fun signup(email: String, password: String, name: String) {
        _authState.value = AuthState.Loading

        auth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener { authResult ->
                // Save user data to Firestore
                val userId = authResult.user?.uid ?: return@addOnSuccessListener
                val userData = hashMapOf(
                    "uid" to userId,
                    "name" to name,
                    "email" to email,
                    "isOnline" to true,
                    "lastSeen" to System.currentTimeMillis()
                )

                firestore.collection("users")
                    .document(userId)
                    .set(userData)
                    .addOnSuccessListener {
                        _authState.value = AuthState.Success
                    }
                    .addOnFailureListener { exception ->
                        _authState.value = AuthState.Error(
                            exception.message ?: "Failed to save user data"
                        )
                    }
            }
            .addOnFailureListener { exception ->
                _authState.value = AuthState.Error(
                    exception.message ?: "Signup failed"
                )
            }
    }
}

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    object Success : AuthState()
    data class Error(val message: String) : AuthState()
}