package com.teamnotfound.airise.room

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [UserEntity::class, SummaryEntity::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun summaryDao(): SummaryDao
}
