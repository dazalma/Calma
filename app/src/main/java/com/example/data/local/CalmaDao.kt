package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface CalmaDao {
    // Chat messages
    @Query("SELECT * FROM chat_messages ORDER BY timestamp ASC")
    fun getAllMessages(): Flow<List<ChatMessageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: ChatMessageEntity): Long

    @Query("DELETE FROM chat_messages")
    suspend fun clearMessages()

    // Mood Check-ins
    @Query("SELECT * FROM mood_checkins ORDER BY timestamp DESC")
    fun getAllCheckIns(): Flow<List<MoodCheckInEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCheckIn(checkIn: MoodCheckInEntity): Long

    @Query("DELETE FROM mood_checkins WHERE id = :id")
    suspend fun deleteCheckIn(id: Long)

    // CBT Reframings
    @Query("SELECT * FROM cbt_reframings ORDER BY timestamp DESC")
    fun getAllReframings(): Flow<List<CbtReframingEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReframing(reframing: CbtReframingEntity): Long

    @Query("DELETE FROM cbt_reframings WHERE id = :id")
    suspend fun deleteReframing(id: Long)
}
