package com.spell.master.di

import androidx.compose.runtime.staticCompositionLocalOf
import com.spell.master.data.repository.SpellRepository

val LocalRepository = staticCompositionLocalOf<SpellRepository> {
    error("SpellRepository not provided -- wrap content in CompositionLocalProvider from MainActivity")
}
