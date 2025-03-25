package com.teamnotfound.airise.data.auth

sealed class AuthResult {
    object Success : AuthResult()
    data class Failure(val errorMessage: String) : AuthResult()
}