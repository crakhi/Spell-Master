package com.spell.master.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Catalog row for one level (e.g. "g3_level1") -- the curriculum content itself,
 * identical for every user. Per-user progress (unlocked/stars/best score) lives
 * separately in [LevelProgressEntity] so multiple signed-in users can share this
 * same catalog without overwriting each other's progress.
 */
@Entity(tableName = "levels")
data class LevelEntity(
    @PrimaryKey val levelId: String,
    val gradeId: Int,
    val levelKey: String,
    val name: String,
    val orderIndex: Int,
    val totalWords: Int
)
