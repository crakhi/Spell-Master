package com.spell.master.data.local.entity

/**
 * One row per answered (or timed-out) question. This is the "local database
 * tracking of right/wrong answers" the app keeps -- it drives the FinalTest
 * (words the kid got wrong get re-asked) and the level's accuracy/star rating.
 */
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "word_attempts")
data class WordAttemptEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val sessionId: String,
    val gradeId: Int,
    val levelId: String,
    val wordId: Int,
    val questionType: String,
    val isFinalTest: Boolean,
    val isCorrect: Boolean,
    val timestamp: Long,
    val attemptIndex: Int
)
