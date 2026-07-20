package com.spell.master.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.spell.master.data.local.entity.WordEntity

@Dao
interface WordDao {
    @Query("SELECT * FROM words WHERE levelId = :levelId ORDER BY orderIndex ASC")
    suspend fun getForLevel(levelId: String): List<WordEntity>

    @Query("SELECT * FROM words WHERE id IN (:ids)")
    suspend fun getByIds(ids: List<Int>): List<WordEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(words: List<WordEntity>)
}
