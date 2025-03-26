package com.teamnotfound.airise.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface SummaryDao{
    @Insert
    suspend fun insertSummary(summary: SummaryEntitiy): long

    @Query("SELECT * FROM Summaries WHERE userId = :userId ORDER BY timestamp DESC")
    suspend fun getSummariesForUser(userId: Int): List<SummaryEntity>



}