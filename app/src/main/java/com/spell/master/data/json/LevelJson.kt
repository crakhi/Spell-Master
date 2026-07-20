package com.spell.master.data.json

import kotlinx.serialization.Serializable

/**
 * Mirrors the shape of assets/spellbee_levels.json exactly so it can be parsed
 * with kotlinx.serialization and then flattened into Room entities by the seeder.
 */
@Serializable
data class LevelJson(
    val level: String,
    val name: String,
    val words: List<WordJson>
)

@Serializable
data class WordJson(
    val word: String,
    val meaning: String,
    val example: String,
    val hint: String,
    val blanks: List<Int>,
    val fillings: List<String>,
    val aptwords: List<String>,
    val rightwords: List<String>
)
