package com.spell.master.domain

/** Catalog [com.spell.master.data.local.entity.LevelEntity] merged with the current
 * user's [com.spell.master.data.local.entity.LevelProgressEntity] for display. */
data class LevelWithProgress(
    val levelId: String,
    val gradeId: Int,
    val name: String,
    val orderIndex: Int,
    val totalWords: Int,
    val isUnlocked: Boolean,
    val stars: Int,
    val bestCorrectCount: Int
)
