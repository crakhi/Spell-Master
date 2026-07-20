package com.spell.master.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// The app always uses this bright, high-contrast palette regardless of system
// dark mode -- a toddler-facing game should look the same at story time as it
// does at breakfast.
private val SpellMasterColors = lightColorScheme(
    primary = HoneyOrange,
    onPrimary = InkBrown,
    secondary = SkyBlue,
    onSecondary = InkBrown,
    tertiary = PetalPink,
    background = CreamBg,
    surface = androidx.compose.ui.graphics.Color.White,
    onBackground = InkBrown,
    onSurface = InkBrown
)

private val SpellMasterTypography = Typography(
    headlineLarge = TextStyle(fontWeight = FontWeight.ExtraBold, fontSize = 40.sp),
    headlineMedium = TextStyle(fontWeight = FontWeight.ExtraBold, fontSize = 30.sp),
    titleLarge = TextStyle(fontWeight = FontWeight.Bold, fontSize = 24.sp),
    titleMedium = TextStyle(fontWeight = FontWeight.Bold, fontSize = 18.sp),
    bodyLarge = TextStyle(fontWeight = FontWeight.Medium, fontSize = 18.sp),
    bodyMedium = TextStyle(fontWeight = FontWeight.Medium, fontSize = 15.sp),
    labelLarge = TextStyle(fontWeight = FontWeight.Bold, fontSize = 16.sp)
)

@Composable
fun SpellMasterTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = SpellMasterColors,
        typography = SpellMasterTypography,
        content = content
    )
}
