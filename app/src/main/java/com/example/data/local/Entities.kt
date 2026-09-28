package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "chat_messages")
data class ChatMessageEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val sender: String, // "user", "calma", "system"
    val content: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isCrisis: Boolean = false
)

@Entity(tableName = "mood_checkins")
data class MoodCheckInEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val anxietyLevel: Int, // 1 - 10
    val dominantEmotion: String,
    val note: String = "",
    val tookBreathing: Boolean = false,
    val tookGrounding: Boolean = false
)

@Entity(tableName = "cbt_reframings")
data class CbtReframingEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val situationOrThought: String,
    val observableFacts: String,
    val fearfulInterpretation: String,
    val controllableAspect: String,
    val uncontrollableAspect: String,
    val smallNextStep: String,
    val anxietyBefore: Int, // 1 - 10
    val anxietyAfter: Int // 1 - 10
)
