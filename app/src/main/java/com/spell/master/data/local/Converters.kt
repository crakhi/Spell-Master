package com.spell.master.data.local

import androidx.room.TypeConverter
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/** Room can't store Lists natively, so we round-trip them through JSON strings. */
class Converters {
    private val json = Json { ignoreUnknownKeys = true }

    @TypeConverter
    fun intListToString(value: List<Int>): String = json.encodeToString(value)

    @TypeConverter
    fun stringToIntList(value: String): List<Int> =
        if (value.isBlank()) emptyList() else json.decodeFromString(value)

    @TypeConverter
    fun stringListToString(value: List<String>): String = json.encodeToString(value)

    @TypeConverter
    fun stringToStringList(value: String): List<String> =
        if (value.isBlank()) emptyList() else json.decodeFromString(value)
}
