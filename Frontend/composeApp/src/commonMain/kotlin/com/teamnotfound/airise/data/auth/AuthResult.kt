package com.teamnotfound.airise.data.auth

sealed class AuthResult {
    data class Success(val data: User) : AuthResult()
    data class Failure(val errorMessage: String) : AuthResult()
}