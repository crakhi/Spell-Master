package com.spell.master.data.local.entity

import androidx.room.Entity

/**
 * Per-user progress for one level. [stars] is -1 until the user finishes the level
 * for the first time; after that it (and [bestCorrectCount]) hold the best result
 * earned so far. Mirrored to Firestore under users/{userId}/progress/{levelId} so
 * it syncs across devices for the same account.
 */
@Entity(tableName = "level_progress", primaryKeys = ["userId", "levelId"])
data class LevelProgressEntity(
    val userId: String,
    val levelId: String,
    val gradeId: Int,
    val isUnlocked: Boolean,
    val stars: Int = -1,
    val bestCorrectCount: Int = 0,
    val updatedAt: Long = 0
)
