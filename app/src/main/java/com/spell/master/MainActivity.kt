package com.spell.master

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.Surface
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.spell.master.di.LocalRepository
import com.spell.master.ui.navigation.SpellNavHost
import com.spell.master.ui.theme.CreamBg
import com.spell.master.ui.theme.SpellMasterTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // A toddler-facing landscape game doesn't need the clock/battery bar eating
        // vertical space on every screen -- hide it, but leave the nav bar reachable
        // (swipe from the edge briefly reveals the status bar if ever needed).
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(window, window.decorView).let { controller ->
            controller.hide(WindowInsetsCompat.Type.statusBars())
            controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }

        val repository = (application as SpellMasterApp).container.repository

        setContent {
            CompositionLocalProvider(LocalRepository provides repository) {
                SpellMasterTheme {
                    Surface(modifier = Modifier.fillMaxSize(), color = CreamBg) {
                        // Background fills edge-to-edge, but content stays clear of the
                        // status bar / 3-button nav bar / display cutouts on every side.
                        Box(modifier = Modifier.fillMaxSize().safeDrawingPadding()) {
                            SpellNavHost()
                        }
                    }
                }
            }
        }
    }
}
