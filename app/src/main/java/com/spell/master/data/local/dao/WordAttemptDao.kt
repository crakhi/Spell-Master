package com.spell.master.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.spell.master.data.local.entity.WordAttemptEntity

@Dao
interface WordAttemptDao {
    @Insert
    suspend fun insert(attempt: WordAttemptEntity)

    /**
     * Distinct words the kid got wrong during the *main* run of this session
     * (i.e. not already inside the FinalTest itself) -- this is the pool the
     * FinalTest picks from.
     */
    @Query(
        "SELECT DISTINCT wordId FROM word_attempts " +
            "WHERE sessionId = :sessionId AND levelId = :levelId " +
            "AND isFinalTest = 0 AND isCorrect = 0"
    )
    suspend fun getWrongWordIds(sessionId: String, levelId: String): List<Int>

    @Query(
        "SELECT COUNT(*) FROM word_attempts " +
            "WHERE sessionId = :sessionId AND levelId = :levelId " +
            "AND isFinalTest = 0 AND isCorrect = 1"
    )
    suspend fun countCorrectMain(sessionId: String, levelId: String): Int
}
