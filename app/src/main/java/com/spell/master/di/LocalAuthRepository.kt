package com.spell.master.di

import androidx.compose.runtime.staticCompositionLocalOf
import com.spell.master.data.auth.AuthRepository

val LocalAuthRepository = staticCompositionLocalOf<AuthRepository> {
    error("AuthRepository not provided -- wrap content in CompositionLocalProvider from MainActivity")
}
