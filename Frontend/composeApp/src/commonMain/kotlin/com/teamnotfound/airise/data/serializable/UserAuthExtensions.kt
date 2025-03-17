package com.teamnotfound.airise.data.serializable

//Translating UserAuth to UserAuthData to store plain data in MongoDB
fun UserAuth.toUserAuthData(): UserAuthData {
    return UserAuthData(
        id = null,
        email = this.email.value,
        username = this.username.value,
        password = this.password.value
    )
}