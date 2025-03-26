package com.teamnotfound.airise.room

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "Summaries")
data class SummaryEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userId: Int,
    val summaryText: String,
    val timestamp: Long //For logging
)
