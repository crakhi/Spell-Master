package com.spell.master.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * One entry per level (e.g. "g3_level1"). [stars] is -1 until the kid finishes
 * the level for the first time; after that it holds the best rating earned so far.
 */
@Entity(tableName = "levels")
data class LevelEntity(
    @PrimaryKey val levelId: String,
    val gradeId: Int,
    val levelKey: String,
    val name: String,
    val orderIndex: Int,
    val totalWords: Int,
    val isUnlocked: Boolean,
    val stars: Int = -1,
    val bestCorrectCount: Int = 0
)
