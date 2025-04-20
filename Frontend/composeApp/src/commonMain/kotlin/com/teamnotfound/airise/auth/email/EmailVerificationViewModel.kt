package com.teamnotfound.airise.auth.email

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.gitlive.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class EmailVerificationViewModel : ViewModel() {

    private val _isVerified = MutableStateFlow(false)
    val isVerified: StateFlow<Boolean> = _isVerified

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    fun sendEmailVerification(firebaseUser: FirebaseUser?) {
        viewModelScope.launch {
            try {
                firebaseUser?.sendEmailVerification()
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "An unexpected error occurred"
            }
        }
    }

    fun checkEmailVerified(firebaseUser: FirebaseUser) {
        firebaseUser?.let {
            viewModelScope.launch {
                try {
                    it.reload()
                    _isVerified.value = it.isEmailVerified
                } catch (e: Exception) {
                    _errorMessage.value = e.message ?: "Failed to check verification status"
                }
            }
        }
    }
}
