package com.spell.master.data.repository

import com.spell.master.data.DataSeeder
import com.spell.master.data.local.AppDatabase
import com.spell.master.data.local.entity.GradeEntity
import com.spell.master.data.local.entity.LevelProgressEntity
import com.spell.master.data.local.entity.WordAttemptEntity
import com.spell.master.data.local.entity.WordEntity
import com.spell.master.data.remote.FirestoreSyncRepository
import com.spell.master.domain.LevelWithProgress
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

class SpellRepository(
    private val db: AppDatabase,
    private val seeder: DataSeeder,
    private val firestoreSync: FirestoreSyncRepository
) {
    suspend fun ensureSeeded() = seeder.seedIfNeeded()

    /** Wipes every table (grades/levels/words/attempts/progress) -- used by the hidden dev reset gesture. */
    suspend fun clearAllData() = db.clearAllTables()

    fun observeGrades(): Flow<List<GradeEntity>> = db.gradeDao().observeAll()

    /** Catalog levels for [gradeId] merged with [userId]'s progress -- the first level
     * defaults to unlocked when the user has no progress row for it yet. */
    fun observeLevelsWithProgress(userId: String, gradeId: Int): Flow<List<LevelWithProgress>> =
        combine(
            db.levelDao().observeForGrade(gradeId),
            db.levelProgressDao().observeForUserAndGrade(userId, gradeId)
        ) { catalog, progress ->
            val progressByLevelId = progress.associateBy { it.levelId }
            catalog.sortedBy { it.orderIndex }.map { level ->
                val p = progressByLevelId[level.levelId]
                LevelWithProgress(
                    levelId = level.levelId,
                    gradeId = level.gradeId,
                    name = level.name,
                    orderIndex = level.orderIndex,
                    totalWords = level.totalWords,
                    isUnlocked = p?.isUnlocked ?: (level.orderIndex == 0),
                    stars = p?.stars ?: -1,
                    bestCorrectCount = p?.bestCorrectCount ?: 0
                )
            }
        }

    suspend fun getLevel(levelId: String) = db.levelDao().getLevel(levelId)

    suspend fun getWordsForLevel(levelId: String): List<WordEntity> = db.wordDao().getForLevel(levelId)

    suspend fun getWordsByIds(ids: List<Int>): List<WordEntity> =
        if (ids.isEmpty()) emptyList() else db.wordDao().getByIds(ids)

    suspend fun logAttempt(
        userId: String,
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
                userId = userId,
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

    suspend fun getWrongWordIds(userId: String, sessionId: String, levelId: String): List<Int> =
        db.wordAttemptDao().getWrongWordIds(userId, sessionId, levelId)

    suspend fun countCorrectMain(userId: String, sessionId: String, levelId: String): Int =
        db.wordAttemptDao().countCorrectMain(userId, sessionId, levelId)

    /**
     * Persists the star rating for the level (kept as best-ever score), unlocks the
     * next level, and mirrors both changes to Firestore for cross-device sync.
     * [stars] is computed by the caller from per-question answer speed, not accuracy.
     */
    suspend fun finishLevel(userId: String, levelId: String, gradeId: Int, stars: Int, correctCount: Int): Int {
        val now = System.currentTimeMillis()
        val existing = db.levelProgressDao().getProgress(userId, levelId)
        val bestStars = maxOf(existing?.stars ?: -1, stars)
        val bestCorrect = maxOf(existing?.bestCorrectCount ?: 0, correctCount)
        val updated = LevelProgressEntity(
            userId = userId,
            levelId = levelId,
            gradeId = gradeId,
            isUnlocked = true,
            stars = bestStars,
            bestCorrectCount = bestCorrect,
            updatedAt = now
        )
        db.levelProgressDao().upsert(updated)
        firestoreSync.pushProgress(userId, updated)

        val levelsInGrade = db.levelDao().getForGradeOnce(gradeId)
        val currentIndex = levelsInGrade.indexOfFirst { it.levelId == levelId }
        val next = levelsInGrade.getOrNull(currentIndex + 1)
        if (next != null) {
            val nextProgress = db.levelProgressDao().getProgress(userId, next.levelId)
            if (nextProgress == null || !nextProgress.isUnlocked) {
                val unlockedNext = (nextProgress ?: LevelProgressEntity(
                    userId = userId,
                    levelId = next.levelId,
                    gradeId = gradeId,
                    isUnlocked = false,
                    stars = -1,
                    bestCorrectCount = 0
                )).copy(isUnlocked = true, updatedAt = now)
                db.levelProgressDao().upsert(unlockedNext)
                firestoreSync.pushProgress(userId, unlockedNext)
            }
        }
        return bestStars
    }

    suspend fun syncFromCloud(userId: String) = firestoreSync.pullAndMerge(userId)
}
