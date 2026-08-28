package com.spell.master.ui.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.spell.master.di.LocalAuthRepository
import com.spell.master.di.LocalRepository
import com.spell.master.domain.LevelWithProgress
import com.spell.master.ui.common.LottieAnim
import com.spell.master.ui.common.StarRatingRow
import com.spell.master.ui.theme.CreamBg
import com.spell.master.ui.theme.InkBrown
import com.spell.master.ui.theme.TilePalette

@Composable
fun LevelDashboardScreen(
    gradeId: Int,
    onBack: () -> Unit,
    onLevelSelected: (String) -> Unit
) {
    val repository = LocalRepository.current
    // Guarded by nav (SIGN_IN is the start destination when signed out), so this is
    // only ever null if auth state changes out from under an already-composed screen.
    val userId = LocalAuthRepository.current.currentUserId ?: return
    val viewModel: LevelDashboardViewModel = viewModel(
        factory = viewModelFactory { initializer { LevelDashboardViewModel(repository, userId, gradeId) } }
    )
    val levels by viewModel.levels.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CreamBg)
            .padding(horizontal = 20.dp, vertical = 8.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Text("⬅️", fontSize = 26.sp)
            }
            Column(Modifier.padding(start = 8.dp)) {
                Text("Grade $gradeId Dashboard", style = MaterialTheme.typography.headlineMedium, color = InkBrown)
                Text("Buzz through every level to become a Spell Master!", style = MaterialTheme.typography.bodyLarge, color = InkBrown)
            }
        }

        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 170.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 10.dp)
        ) {
            items(levels, key = { it.levelId }) { level ->
                LevelTile(level = level, onClick = { onLevelSelected(level.levelId) })
            }
        }
    }
}

@Composable
private fun LevelTile(level: LevelWithProgress, onClick: () -> Unit) {
    val color = TilePalette[level.orderIndex % TilePalette.size]
    val completed = level.stars >= 0

    Box(
        modifier = Modifier
            .aspectRatio(1.15f)
            .clip(RoundedCornerShape(20.dp))
            .background(color)
            .clickable(onClick = onClick)
            .padding(10.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = level.name,
                color = Color.White,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 17.sp,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            if (completed) {
                Spacer(modifier = Modifier.height(4.dp))
                // Two ratings side by side: how fast the kid answered, and how many
                // were right -- speed alone shouldn't hide that accuracy still matters.
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("⚡", fontSize = 12.sp)
                    StarRatingRow(
                        rating = level.stars,
                        starSizeSp = 14,
                        filledColor = Color.White,
                        emptyColor = Color.White.copy(alpha = 0.35f)
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("🎯", fontSize = 12.sp)
                    StarRatingRow(
                        rating = accuracyStarsFor(level.bestCorrectCount, level.totalWords),
                        starSizeSp = 14,
                        filledColor = Color.White,
                        emptyColor = Color.White.copy(alpha = 0.35f)
                    )
                }
            } else {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    LottieAnim(asset = "bee_mascot.json", modifier = Modifier.size(28.dp), loop = true)
                    Text("New!", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        if (completed) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(22.dp)
                    .clip(CircleShape)
                    .background(Color.White),
                contentAlignment = Alignment.Center
            ) {
                Text("✓", color = color, fontWeight = FontWeight.ExtraBold, fontSize = 14.sp)
            }
        }
    }
}

/** Accuracy rating (how many were answered right) -- separate from the speed-based [LevelWithProgress.stars]. */
private fun accuracyStarsFor(correctCount: Int, total: Int): Int {
    if (total <= 0) return 1
    val ratio = correctCount.toFloat() / total.toFloat()
    return when {
        ratio >= 0.95f -> 5
        ratio >= 0.8f -> 4
        ratio >= 0.6f -> 3
        ratio >= 0.4f -> 2
        else -> 1
    }
}
