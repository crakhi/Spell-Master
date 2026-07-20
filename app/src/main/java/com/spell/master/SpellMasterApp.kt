package com.spell.master

import android.app.Application
import com.spell.master.di.AppContainer

class SpellMasterApp : Application() {
    lateinit var container: AppContainer

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}
