package com.spell.master.di

import android.content.Context
import com.spell.master.data.DataSeeder
import com.spell.master.data.auth.AuthRepository
import com.spell.master.data.local.AppDatabase
import com.spell.master.data.remote.FirestoreSyncRepository
import com.spell.master.data.repository.SpellRepository

/** Hand-rolled DI container (no Hilt) -- shared repository instances across the app. */
class AppContainer(context: Context) {
    private val database: AppDatabase by lazy { AppDatabase.getInstance(context) }
    private val seeder: DataSeeder by lazy { DataSeeder(context, database) }
    private val firestoreSync: FirestoreSyncRepository by lazy { FirestoreSyncRepository(database.levelProgressDao()) }

    val authRepository: AuthRepository by lazy { AuthRepository(context) }
    val repository: SpellRepository by lazy { SpellRepository(database, seeder, firestoreSync) }
}
