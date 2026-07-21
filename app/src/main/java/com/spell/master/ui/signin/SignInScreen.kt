package com.spell.master.ui.signin

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.spell.master.di.LocalAuthRepository
import com.spell.master.ui.common.LottieAnim
import com.spell.master.ui.theme.CreamBg
import com.spell.master.ui.theme.InkBrown
import com.spell.master.ui.theme.SkyBlue
import com.spell.master.ui.theme.WrongRed
import kotlinx.coroutines.launch

@Composable
fun SignInScreen(onSignedIn: () -> Unit) {
    val authRepository = LocalAuthRepository.current
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Box(modifier = Modifier.fillMaxSize().background(CreamBg), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            LottieAnim(asset = "bee_mascot.json", modifier = Modifier.size(140.dp), loop = true)

            Spacer(modifier = Modifier.height(8.dp))
            Text("Spell Master", style = MaterialTheme.typography.headlineLarge, color = InkBrown)
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Sign in to save your progress and buzz across all your devices!",
                style = MaterialTheme.typography.bodyLarge,
                color = InkBrown,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(28.dp))

            if (isLoading) {
                CircularProgressIndicator(color = SkyBlue)
            } else {
                GoogleSignInButton(
                    onClick = {
                        scope.launch {
                            isLoading = true
                            errorMessage = null
                            val result = authRepository.signInWithGoogle(context)
                            isLoading = false
                            result.onSuccess { onSignedIn() }
                                .onFailure { errorMessage = "Couldn't sign in -- please try again." }
                        }
                    }
                )
            }

            errorMessage?.let {
                Spacer(modifier = Modifier.height(12.dp))
                Text(text = it, color = WrongRed, fontSize = 14.sp, textAlign = TextAlign.Center)
            }
        }
    }
}

@Composable
private fun GoogleSignInButton(onClick: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clip(RoundedCornerShape(28.dp))
            .background(Color.White)
            .clickable(onClick = onClick)
            .padding(horizontal = 28.dp, vertical = 16.dp)
    ) {
        Text("G", color = SkyBlue, fontWeight = FontWeight.ExtraBold, fontSize = 22.sp)
        Spacer(modifier = Modifier.width(12.dp))
        Text("Sign in with Google", color = InkBrown, fontWeight = FontWeight.Bold, fontSize = 18.sp)
    }
}
