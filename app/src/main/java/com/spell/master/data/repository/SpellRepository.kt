package com.spell.master.data.repository

import com.spell.master.data.DataSeeder
import com.spell.master.data.local.AppDatabase
import com.spell.master.data.local.entity.GradeEntity
import com.spell.master.data.local.entity.LevelEntity
import com.spell.master.data.local.entity.WordAttemptEntity
import com.spell.master.data.local.entity.WordEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class SpellRepository(
    private val db: AppDatabase,
    private val seeder: DataSeeder
) {
    suspend fun ensureSeeded() = seeder.seedIfNeeded()

    /** Wipes every table (grades/levels/words/attempts) -- used by the hidden dev reset gesture. */
    suspend fun clearAllData() = withContext(Dispatchers.IO) { db.clearAllTables() }

    fun observeGrades(): Flow<List<GradeEntity>> = db.gradeDao().observeAll()

    fun observeLevels(gradeId: Int): Flow<List<LevelEntity>> = db.levelDao().observeForGrade(gradeId)

    fun observeLevel(levelId: String): Flow<LevelEntity?> = db.levelDao().observeLevel(levelId)

    suspend fun getLevel(levelId: String): LevelEntity? = db.levelDao().getLevel(levelId)

    suspend fun getWordsForLevel(levelId: String): List<WordEntity> = db.wordDao().getForLevel(levelId)

    suspend fun getWordsByIds(ids: List<Int>): List<WordEntity> =
        if (ids.isEmpty()) emptyList() else db.wordDao().getByIds(ids)

    suspend fun logAttempt(
        sessionId: String,
        gradeId: Int,
        levelId: String,
        wordId: Int,
        questionType: String,
        isFinalTest: Boolean,
        isCorrect: Boolean,
        attemptIndex: Int,
        timestamp: Long
    ) {
        db.wordAttemptDao().insert(
            WordAttemptEntity(
                sessionId = sessionId,
                gradeId = gradeId,
                levelId = levelId,
                wordId = wordId,
                questionType = questionType,
                isFinalTest = isFinalTest,
                isCorrect = isCorrect,
                timestamp = timestamp,
                attemptIndex = attemptIndex
            )
        )
    }

    suspend fun getWrongWordIds(sessionId: String, levelId: String): List<Int> =
        db.wordAttemptDao().getWrongWordIds(sessionId, levelId)

    suspend fun countCorrectMain(sessionId: String, levelId: String): Int =
        db.wordAttemptDao().countCorrectMain(sessionId, levelId)

    /**
     * Persists the star rating for the level (kept as best-ever score) and unlocks the next level.
     * [stars] is computed by the caller from per-question answer speed, not accuracy.
     */
    suspend fun finishLevel(levelId: String, gradeId: Int, stars: Int, correctCount: Int): Int {
        db.levelDao().updateBestResult(levelId, stars, correctCount)

        val levelsInGrade = db.levelDao().getForGradeOnce(gradeId)
        val currentIndex = levelsInGrade.indexOfFirst { it.levelId == levelId }
        val next = levelsInGrade.getOrNull(currentIndex + 1)
        if (next != null && !next.isUnlocked) {
            db.levelDao().unlock(next.levelId)
        }
        return stars
    }
}
