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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.spell.master.data.local.entity.LevelEntity
import com.spell.master.di.LocalRepository
import com.spell.master.ui.common.LottieAnim
import com.spell.master.ui.common.StarRatingRow
import com.spell.master.ui.theme.CreamBg
import com.spell.master.ui.theme.InkBrown
import com.spell.master.ui.theme.LockedGray
import com.spell.master.ui.theme.TilePalette

@Composable
fun LevelDashboardScreen(
    gradeId: Int,
    onBack: () -> Unit,
    onLevelSelected: (String) -> Unit
) {
    val repository = LocalRepository.current
    val viewModel: LevelDashboardViewModel = viewModel(
        factory = viewModelFactory { initializer { LevelDashboardViewModel(repository, gradeId) } }
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
                LevelTile(level = level, onClick = { if (level.isUnlocked) onLevelSelected(level.levelId) })
            }
        }
    }
}

@Composable
private fun LevelTile(level: LevelEntity, onClick: () -> Unit) {
    val color = if (level.isUnlocked) TilePalette[level.orderIndex % TilePalette.size] else LockedGray
    val attempted = level.stars >= 0

    Box(
        modifier = Modifier
            .aspectRatio(1.15f)
            .clip(RoundedCornerShape(20.dp))
            .background(color)
            .clickable(enabled = level.isUnlocked, onClick = onClick)
            .alpha(if (level.isUnlocked) 1f else 0.55f)
            .padding(10.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = level.name,
                color = Color.White,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 19.sp,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            if (!level.isUnlocked) {
                Text("🔒 Locked", color = Color.White, fontSize = 13.sp)
            } else if (attempted) {
                StarRatingRow(
                    rating = level.stars,
                    starSizeSp = 24,
                    filledColor = Color.White,
                    emptyColor = Color.White.copy(alpha = 0.35f)
                )
            } else {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    LottieAnim(asset = "bee_mascot.json", modifier = Modifier.size(28.dp), loop = true)
                    Text("New!", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
