package com.teamnotfound.airise.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface SummaryDao{
    @Insert
    fun insertSummary(summary: SummaryEntity): Long

    @Query("SELECT * FROM summaries WHERE userId = :userId ORDER BY timestamp DESC")
    fun getSummariesForUser(userId: Int): List<SummaryEntity>



}