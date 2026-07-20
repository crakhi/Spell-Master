package com.spell.master.di

import android.content.Context
import com.spell.master.data.DataSeeder
import com.spell.master.data.local.AppDatabase
import com.spell.master.data.repository.SpellRepository

/** Hand-rolled DI container (no Hilt) -- one repository instance shared across the app. */
class AppContainer(context: Context) {
    private val database: AppDatabase by lazy { AppDatabase.getInstance(context) }
    private val seeder: DataSeeder by lazy { DataSeeder(context, database) }
    val repository: SpellRepository by lazy { SpellRepository(database, seeder) }
}
