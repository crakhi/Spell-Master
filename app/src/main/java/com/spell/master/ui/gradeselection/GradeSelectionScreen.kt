package com.spell.master.ui.gradeselection

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.widget.Toast
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.spell.master.data.local.entity.GradeEntity
import com.spell.master.di.LocalAuthRepository
import com.spell.master.di.LocalRepository
import com.spell.master.ui.common.LottieAnim
import com.spell.master.ui.theme.CreamBg
import com.spell.master.ui.theme.HoneyOrange
import com.spell.master.ui.theme.InkBrown
import com.spell.master.ui.theme.LockedGray
import com.spell.master.ui.theme.TilePalette
import com.spell.master.util.SoundEffects

@Composable
fun GradeSelectionScreen(onGradeSelected: (Int) -> Unit, onSignedOut: () -> Unit) {
    val repository = LocalRepository.current
    val authRepository = LocalAuthRepository.current
    val viewModel: GradeSelectionViewModel = viewModel(
        factory = viewModelFactory { initializer { GradeSelectionViewModel(repository, authRepository) } }
    )
    val grades by viewModel.grades.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var isMuted by remember { mutableStateOf(SoundEffects.isMuted()) }

    LaunchedEffect(viewModel) {
        viewModel.dataClearedEvents.collect {
            Toast.makeText(context, "Data cleared! Fresh start 🐝", Toast.LENGTH_SHORT).show()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CreamBg)
            .padding(horizontal = 20.dp, vertical = 8.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            LottieAnim(
                asset = "bee_mascot.json",
                modifier = Modifier
                    .size(72.dp)
                    .clickable(onClick = viewModel::onBeeTapped)
            )
            Column(Modifier.padding(start = 12.dp).weight(1f)) {
                Text("Spell Master", style = MaterialTheme.typography.headlineMedium, color = InkBrown)
                Text("Pick your grade to start buzzing!", style = MaterialTheme.typography.bodyLarge, color = InkBrown)
            }
            Text(
                text = if (isMuted) "🔇" else "🔊",
                fontSize = 26.sp,
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .clickable {
                        isMuted = !isMuted
                        SoundEffects.setMuted(context, isMuted)
                    }
                    .padding(8.dp)
            )
            Text(
                text = "Sign out",
                color = InkBrown,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .clickable {
                        authRepository.signOut()
                        onSignedOut()
                    }
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            )
        }

        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 150.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 12.dp)
        ) {
            items(grades, key = { it.gradeId }) { grade ->
                GradeTile(grade = grade, onClick = { if (grade.isEnabled) onGradeSelected(grade.gradeId) })
            }
        }
    }
}

@Composable
private fun GradeTile(grade: GradeEntity, onClick: () -> Unit) {
    val color = if (grade.isEnabled) TilePalette[grade.gradeId % TilePalette.size] else LockedGray
    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(24.dp))
            .background(color)
            .clickable(enabled = grade.isEnabled, onClick = onClick)
            .alpha(if (grade.isEnabled) 1f else 0.55f),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = grade.displayName,
                color = Color.White,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 22.sp
            )
            Text(
                text = if (grade.isEnabled) "🐝 Let's play!" else "🔒 Coming soon",
                color = Color.White,
                fontSize = 13.sp
            )
        }
    }
}
