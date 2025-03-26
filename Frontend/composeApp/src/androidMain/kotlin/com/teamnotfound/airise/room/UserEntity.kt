package com.teamnotfound.airise.room

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val mongoId: String?,
    val email: String,
    val username: String,
    val password: String
)
