package com.spell.master.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.spell.master.data.local.entity.LevelEntity
import kotlinx.coroutines.flow.Flow

/** Pure catalog reads -- the curriculum content is the same for every user. */
@Dao
interface LevelDao {
    @Query("SELECT * FROM levels WHERE gradeId = :gradeId ORDER BY orderIndex ASC")
    fun observeForGrade(gradeId: Int): Flow<List<LevelEntity>>

    @Query("SELECT * FROM levels WHERE levelId = :levelId LIMIT 1")
    suspend fun getLevel(levelId: String): LevelEntity?

    @Query("SELECT COUNT(*) FROM levels WHERE gradeId = :gradeId")
    suspend fun countForGrade(gradeId: Int): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(levels: List<LevelEntity>)
}
