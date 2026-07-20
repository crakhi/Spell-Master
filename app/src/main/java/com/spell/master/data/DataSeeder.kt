package com.spell.master.data

import android.content.Context
import com.spell.master.data.json.LevelJson
import com.spell.master.data.local.AppDatabase
import com.spell.master.data.local.entity.GradeEntity
import com.spell.master.data.local.entity.LevelEntity
import com.spell.master.data.local.entity.WordEntity
import kotlinx.serialization.json.Json

/**
 * Populates the database on first run. Grades are always 1..10 so the schema
 * is ready for the other grades; only grade 3 is enabled and has its levels
 * and words loaded from assets/spellbee_levels.json today.
 */
class DataSeeder(private val context: Context, private val db: AppDatabase) {

    private val json = Json { ignoreUnknownKeys = true }

    suspend fun seedIfNeeded() {
        seedGradesIfNeeded()
        seedGradeThreeIfNeeded()
    }

    private suspend fun seedGradesIfNeeded() {
        if (db.gradeDao().count() > 0) return
        val grades = (1..10).map { gradeId ->
            GradeEntity(
                gradeId = gradeId,
                displayName = "Grade $gradeId",
                isEnabled = gradeId == ENABLED_GRADE
            )
        }
        db.gradeDao().insertAll(grades)
    }

    private suspend fun seedGradeThreeIfNeeded() {
        if (db.levelDao().countForGrade(ENABLED_GRADE) > 0) return

        val raw = context.assets.open(ASSET_FILE).bufferedReader(Charsets.UTF_8).use { it.readText() }
        val levelsJson: List<LevelJson> = json.decodeFromString(raw)

        val levelEntities = mutableListOf<LevelEntity>()
        val wordEntities = mutableListOf<WordEntity>()

        levelsJson.forEachIndexed { levelIndex, levelJson ->
            val levelId = "g${ENABLED_GRADE}_${levelJson.level}"
            levelEntities += LevelEntity(
                levelId = levelId,
                gradeId = ENABLED_GRADE,
                levelKey = levelJson.level,
                name = levelJson.name,
                orderIndex = levelIndex,
                totalWords = levelJson.words.size,
                isUnlocked = levelIndex == 0
            )
            levelJson.words.forEachIndexed { wordIndex, wordJson ->
                wordEntities += WordEntity(
                    levelId = levelId,
                    orderIndex = wordIndex,
                    word = wordJson.word,
                    meaning = wordJson.meaning,
                    example = wordJson.example,
                    hint = wordJson.hint,
                    blanks = wordJson.blanks,
                    fillings = wordJson.fillings,
                    aptwords = wordJson.aptwords,
                    rightwords = wordJson.rightwords
                )
            }
        }

        db.levelDao().insertAll(levelEntities)
        db.wordDao().insertAll(wordEntities)
    }

    companion object {
        const val ENABLED_GRADE = 3
        private const val ASSET_FILE = "spellbee_levels.json"
    }
}
