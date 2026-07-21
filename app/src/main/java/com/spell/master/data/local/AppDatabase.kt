package com.spell.master.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.spell.master.data.local.dao.GradeDao
import com.spell.master.data.local.dao.LevelDao
import com.spell.master.data.local.dao.LevelProgressDao
import com.spell.master.data.local.dao.WordAttemptDao
import com.spell.master.data.local.dao.WordDao
import com.spell.master.data.local.entity.GradeEntity
import com.spell.master.data.local.entity.LevelEntity
import com.spell.master.data.local.entity.LevelProgressEntity
import com.spell.master.data.local.entity.WordAttemptEntity
import com.spell.master.data.local.entity.WordEntity

@Database(
    entities = [
        GradeEntity::class,
        LevelEntity::class,
        LevelProgressEntity::class,
        WordEntity::class,
        WordAttemptEntity::class
    ],
    version = 2,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun gradeDao(): GradeDao
    abstract fun levelDao(): LevelDao
    abstract fun levelProgressDao(): LevelProgressDao
    abstract fun wordDao(): WordDao
    abstract fun wordAttemptDao(): WordAttemptDao

    companion object {
        @Volatile private var instance: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "spell_master.db"
                )
                    // Pre-release app, no production data worth migrating -- a schema
                    // change just resets local progress rather than needing a Migration.
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { instance = it }
            }
    }
}
