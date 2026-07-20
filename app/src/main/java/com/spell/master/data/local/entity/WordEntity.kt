package com.spell.master.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "words")
data class WordEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val levelId: String,
    val orderIndex: Int,
    val word: String,
    val meaning: String,
    val example: String,
    val hint: String,
    val blanks: List<Int>,
    val fillings: List<String>,
    val aptwords: List<String>,
    val rightwords: List<String>
)
