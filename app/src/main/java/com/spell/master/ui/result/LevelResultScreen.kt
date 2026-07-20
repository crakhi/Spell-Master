package com.spell.master.ui.result

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.spell.master.di.LocalRepository
import com.spell.master.ui.common.LottieAnim
import com.spell.master.ui.common.StarRatingRow
import com.spell.master.ui.theme.CreamBg
import com.spell.master.ui.theme.GrassGreen
import com.spell.master.ui.theme.HoneyOrange
import com.spell.master.ui.theme.InkBrown
import com.spell.master.ui.theme.SkyBlue

@Composable
fun LevelResultScreen(
    levelId: String,
    stars: Int,
    correctCount: Int,
    total: Int,
    onBackToDashboard: () -> Unit,
    onReplayLevel: () -> Unit,
    onNextLevel: (String) -> Unit
) {
    val repository = LocalRepository.current
    val viewModel: LevelResultViewModel = viewModel(
        factory = viewModelFactory { initializer { LevelResultViewModel(repository, levelId) } }
    )
    val nextLevelId by viewModel.nextLevelId.collectAsStateWithLifecycle()

    Box(modifier = Modifier.fillMaxSize().background(CreamBg), contentAlignment = Alignment.Center) {
        LottieAnim(asset = "confetti_celebration.json", loop = false, modifier = Modifier.size(320.dp))

        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("🎉 Level Complete! 🎉", style = MaterialTheme.typography.headlineMedium, color = InkBrown)
            Spacer(modifier = Modifier.height(12.dp))
            StarRatingRow(rating = stars, animated = true, starSizeSp = 44)
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "You got $correctCount out of $total right!",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = InkBrown,
                textAlign = TextAlign.Center
            )
            Text(
                text = closingMessage(stars),
                fontSize = 16.sp,
                color = InkBrown,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(20.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                ResultButton(text = "🔁 Replay", color = SkyBlue, onClick = onReplayLevel)
                nextLevelId?.let { nextId ->
                    ResultButton(text = "Next Level ➜", color = GrassGreen, onClick = { onNextLevel(nextId) })
                }
                ResultButton(text = "🏠 Dashboard", color = HoneyOrange, onClick = onBackToDashboard)
            }
        }
    }
}

@Composable
private fun ResultButton(text: String, color: Color, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(containerColor = color),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.height(52.dp)
    ) {
        Text(text = text, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, color = Color.White)
    }
}

private fun closingMessage(stars: Int): String = when {
    stars >= 5 -> "Perfect buzz! You're a true Spell Master! 🐝👑"
    stars >= 4 -> "Amazing spelling, superstar! ⭐"
    stars >= 3 -> "Great job! Keep buzzing forward!"
    stars >= 2 -> "Nice try! A little more practice and you'll shine!"
    else -> "Every bee starts somewhere -- let's try again!"
}
