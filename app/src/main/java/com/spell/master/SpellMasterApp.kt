package com.spell.master

import android.app.Application
import com.spell.master.di.AppContainer
import com.spell.master.util.SoundEffects
import com.spell.master.util.SpellingSpeaker

class SpellMasterApp : Application() {
    lateinit var container: AppContainer

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
        SoundEffects.init(this)
        SpellingSpeaker.init(this)
    }
}
