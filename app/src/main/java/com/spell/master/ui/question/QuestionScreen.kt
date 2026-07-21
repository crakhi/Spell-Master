package com.spell.master.ui.question

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.spell.master.di.LocalAuthRepository
import com.spell.master.di.LocalRepository
import com.spell.master.domain.Question
import com.spell.master.domain.QuestionType
import com.spell.master.ui.common.CountdownTimer
import com.spell.master.ui.common.LottieAnim
import com.spell.master.ui.common.QuizProgressBar
import com.spell.master.ui.common.StarRatingRow
import com.spell.master.ui.theme.CorrectGreen
import com.spell.master.ui.theme.CreamBg
import com.spell.master.ui.theme.InkBrown
import com.spell.master.ui.theme.LockedGray
import com.spell.master.ui.theme.PetalPink
import com.spell.master.ui.theme.SkyBlue
import com.spell.master.ui.theme.WrongRed

@Composable
fun QuestionScreen(
    levelId: String,
    onExit: () -> Unit,
    onLevelFinished: (sessionId: String, stars: Int, correct: Int, total: Int) -> Unit
) {
    val repository = LocalRepository.current
    val userId = LocalAuthRepository.current.currentUserId ?: return
    val viewModel: QuestionViewModel = viewModel(
        factory = viewModelFactory { initializer { QuestionViewModel(repository, userId, levelId) } }
    )
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(state.finished) {
        if (state.finished) {
            onLevelFinished(state.sessionId, state.finalStars, state.finalCorrect, state.finalTotal)
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(CreamBg)) {
        if (state.isLoading || state.question == null) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = SkyBlue)
            return@Box
        }

        val question = state.question!!

        Column(modifier = Modifier.fillMaxSize().padding(horizontal = 14.dp, vertical = 4.dp)) {
            // Compact single-row header: exit, "1/20" counter, progress bar, timer -- kept
            // small on purpose so the question card below gets as much height as possible.
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "✖",
                    fontSize = 18.sp,
                    color = InkBrown,
                    modifier = Modifier.clickable(onClick = onExit).padding(4.dp)
                )
                Text(
                    text = if (state.phase == QuizPhase.FINAL_TEST) "🌟 ${state.currentIndex + 1}/${state.sectionTotal}" else "${state.currentIndex + 1}/${state.sectionTotal}",
                    style = MaterialTheme.typography.labelLarge,
                    color = InkBrown,
                    modifier = Modifier.padding(end = 8.dp)
                )
                QuizProgressBar(
                    current = state.currentIndex + 1,
                    total = state.sectionTotal,
                    modifier = Modifier.weight(1f)
                )
                CountdownTimer(
                    secondsLeft = state.secondsLeft,
                    size = 48.dp,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }

            Row(modifier = Modifier.fillMaxSize().padding(top = 6.dp)) {
                QuestionPromptPanel(
                    question = question,
                    showHint = state.showHint,
                    feedbackMessage = state.feedbackMessage,
                    isCorrect = state.isCorrect,
                    answered = state.answered,
                    starsEarned = state.lastAnswerStars,
                    onToggleHint = viewModel::toggleHint,
                    modifier = Modifier.weight(1f).fillMaxHeight()
                )

                Spacer(modifier = Modifier.size(14.dp))

                OptionsPanel(
                    question = question,
                    selectedAnswer = state.selectedAnswer,
                    answered = state.answered,
                    onSelect = viewModel::selectAnswer,
                    onNext = viewModel::nextQuestion,
                    modifier = Modifier.weight(1f).fillMaxHeight()
                )
            }
        }
    }
}

@Composable
private fun QuestionPromptPanel(
    question: Question,
    showHint: Boolean,
    feedbackMessage: String?,
    isCorrect: Boolean,
    answered: Boolean,
    starsEarned: Int,
    onToggleHint: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(Color.White)
            .padding(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 26.dp)
    ) {
        if (!answered) {
            // Nothing to show yet besides the question -- center it in the available
            // space (with bigger type) instead of leaving a wall of empty space below.
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = questionKindLabel(question.type),
                        color = PetalPink,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = question.promptTitle,
                        color = InkBrown,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = if (question.type == QuestionType.FILL_BLANKS) 48.sp else 30.sp,
                        textAlign = TextAlign.Center
                    )
                    question.promptSubtitle?.let {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(text = it, color = InkBrown, fontSize = 20.sp, textAlign = TextAlign.Center)
                    }

                    // Hint reveal stays here where it's always been -- only the tappable
                    // bulb itself moves up to the corner (see below).
                    if (question.type == QuestionType.CHOOSE_APT_WORD && showHint) {
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = "Hint: ${question.hint}",
                            color = InkBrown,
                            fontSize = 16.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            // Hint is only useful while the kid is still working out the answer -- the
            // bulb disappears once the question is answered.
            if (question.type == QuestionType.CHOOSE_APT_WORD) {
                Text(
                    text = "💡",
                    fontSize = 28.sp,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .clickable(onClick = onToggleHint)
                )
            }
        } else {
            // Once answered, the kind label/subtitle are dropped to make room for the
            // feedback/star/example content, and the question stays vertically centered
            // (scrollable as a safety net if content ever taller than the panel).
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = question.promptTitle,
                    color = InkBrown,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = if (question.type == QuestionType.FILL_BLANKS) 36.sp else 22.sp,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(10.dp))

                if (isCorrect) {
                    feedbackMessage?.let {
                        Text(
                            text = it,
                            color = CorrectGreen,
                            fontWeight = FontWeight.Bold,
                            fontSize = 22.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    StarRatingRow(rating = starsEarned, starSizeSp = 34, animated = true)
                } else {
                    LottieAnim(asset = "oops_shake.json", loop = false, modifier = Modifier.size(64.dp))
                    feedbackMessage?.let {
                        Text(
                            text = it,
                            color = WrongRed,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
                AnimatedExample(text = question.example, exampleKey = question.wordId)
            }

            // Confetti floats on top of the content as a celebration overlay -- it must
            // never push the feedback/star/example layout around, so it lives outside
            // the Column entirely, aligned over the same Box.
            if (isCorrect) {
                key(question.wordId) {
                    LottieAnim(
                        asset = "confetti_celebration.json",
                        loop = false,
                        modifier = Modifier.align(Alignment.Center).size(220.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun OptionsPanel(
    question: Question,
    selectedAnswer: String?,
    answered: Boolean,
    onSelect: (String) -> Unit,
    onNext: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.Center) {
        question.options.forEach { option ->
            OptionButton(
                text = option,
                enabled = !answered,
                backgroundColor = optionColor(option, question, selectedAnswer, answered),
                onClick = { onSelect(option) }
            )
            Spacer(modifier = Modifier.height(10.dp))
        }

        if (answered) {
            Spacer(modifier = Modifier.height(6.dp))
            Button(
                onClick = onNext,
                colors = ButtonDefaults.buttonColors(containerColor = SkyBlue),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth().height(52.dp)
            ) {
                Text("Next", fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, color = Color.White)
                Spacer(modifier = Modifier.width(10.dp))
                LottieAnim(
                    asset = "next_arrow.json",
                    modifier = Modifier.size(36.dp),
                    loop = true
                )
            }
        }
    }
}

/** Example sentence with a pop-in entrance -- keyed on the word so it replays for every new question. */
@Composable
private fun AnimatedExample(text: String, exampleKey: Int) {
    key(exampleKey) {
        var visible by remember { mutableStateOf(false) }
        LaunchedEffect(Unit) { visible = true }
        val scale by animateFloatAsState(
            targetValue = if (visible) 1f else 0.6f,
            animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
            label = "exampleScale"
        )
        val alpha by animateFloatAsState(targetValue = if (visible) 1f else 0f, label = "exampleAlpha")
        Text(
            text = "\"$text\"",
            color = InkBrown,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .scale(scale)
                .alpha(alpha)
        )
    }
}

@Composable
private fun OptionButton(text: String, enabled: Boolean, backgroundColor: Color, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        enabled = enabled,
        colors = ButtonDefaults.buttonColors(
            containerColor = backgroundColor,
            disabledContainerColor = backgroundColor
        ),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth().height(52.dp)
    ) {
        Text(text = text, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color.White)
    }
}

private fun optionColor(option: String, question: Question, selectedAnswer: String?, answered: Boolean): Color {
    if (!answered) return SkyBlue
    return when {
        option == question.correctAnswer -> CorrectGreen
        option == selectedAnswer -> WrongRed
        else -> LockedGray
    }
}

private fun questionKindLabel(type: QuestionType): String = when (type) {
    QuestionType.CHOOSE_APT_WORD -> "Choose the apt word"
    QuestionType.CHOOSE_RIGHT_WORD -> "Choose the right word"
    QuestionType.FILL_BLANKS -> "Fill in the blanks"
}
