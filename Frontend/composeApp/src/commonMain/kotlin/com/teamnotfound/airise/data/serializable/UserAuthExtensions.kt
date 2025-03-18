package com.teamnotfound.airise.data.serializable

//Translating UserAuth to UserAuthData to store plain data in MongoDB
fun UserAuthData.toUserAuthData(): UserModel {
    return UserModel(
        id = null,
        email = this.email.value,
        username = this.username.value,
        password = this.password.value
    )
}