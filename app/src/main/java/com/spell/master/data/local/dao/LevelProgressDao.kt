package com.spell.master.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.spell.master.data.local.entity.LevelProgressEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface LevelProgressDao {
    @Query("SELECT * FROM level_progress WHERE userId = :userId AND gradeId = :gradeId")
    fun observeForUserAndGrade(userId: String, gradeId: Int): Flow<List<LevelProgressEntity>>

    @Query("SELECT * FROM level_progress WHERE userId = :userId AND gradeId = :gradeId")
    suspend fun getForUserAndGradeOnce(userId: String, gradeId: Int): List<LevelProgressEntity>

    @Query("SELECT * FROM level_progress WHERE userId = :userId")
    suspend fun getAllForUserOnce(userId: String): List<LevelProgressEntity>

    @Query("SELECT * FROM level_progress WHERE userId = :userId AND levelId = :levelId LIMIT 1")
    suspend fun getProgress(userId: String, levelId: String): LevelProgressEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(progress: LevelProgressEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(progress: List<LevelProgressEntity>)
}
