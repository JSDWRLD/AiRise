package com.teamnotfound.airise.serializable

//Translating UserAuth to UserAuthData to store plain data in MongoDB
fun UserAuth.toUserAuthData(): UserAuthData {
    return UserAuthData(
        email = this.email.value,
        username = this.username.value,
        password = this.password.value
    )
}