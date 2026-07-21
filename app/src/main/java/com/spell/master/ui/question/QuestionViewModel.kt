package com.spell.master.ui.question

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spell.master.data.repository.SpellRepository
import com.spell.master.domain.EncouragementBank
import com.spell.master.domain.Question
import com.spell.master.domain.QuestionFactory
import com.spell.master.util.SoundEffects
import com.spell.master.util.SpellingSpeaker
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import kotlin.math.roundToInt

enum class QuizPhase { MAIN, FINAL_TEST }

data class QuestionUiState(
    val isLoading: Boolean = true,
    val levelName: String = "",
    val phase: QuizPhase = QuizPhase.MAIN,
    val currentIndex: Int = 0,
    val sectionTotal: Int = 0,
    val question: Question? = null,
    val selectedAnswer: String? = null,
    val answered: Boolean = false,
    val isCorrect: Boolean = false,
    val secondsLeft: Int = QuestionViewModel.TOTAL_SECONDS,
    val feedbackMessage: String? = null,
    val showHint: Boolean = false,
    val lastAnswerStars: Int = 0,
    val isSpeaking: Boolean = false,
    val finished: Boolean = false,
    val finalStars: Int = 0,
    val finalCorrect: Int = 0,
    val finalTotal: Int = 0,
    val sessionId: String = ""
)

class QuestionViewModel(
    private val repository: SpellRepository,
    private val userId: String,
    private val levelId: String
) : ViewModel() {

    private val _state = MutableStateFlow(QuestionUiState())
    val state: StateFlow<QuestionUiState> = _state.asStateFlow()

    private var mainQuestions: List<Question> = emptyList()
    private var finalQuestions: List<Question> = emptyList()
    private var timerJob: Job? = null
    private var pendingWrite: Job? = null
    private var attemptCounter = 0
    private var gradeId: Int = 0
    private val sessionId = UUID.randomUUID().toString()
    private val perQuestionStars = mutableListOf<Int>()

    init {
        viewModelScope.launch {
            val level = repository.getLevel(levelId)
            gradeId = level?.gradeId ?: 0
            val words = repository.getWordsForLevel(levelId)
            mainQuestions = QuestionFactory.buildLevelQuestions(words)
            _state.update {
                it.copy(isLoading = false, levelName = level?.name.orEmpty(), sessionId = sessionId)
            }
            if (mainQuestions.isEmpty()) {
                finishLevel()
            } else {
                showQuestion(mainQuestions[0], QuizPhase.MAIN, 0, mainQuestions.size)
            }
        }
    }

    private fun showQuestion(question: Question, phase: QuizPhase, index: Int, sectionTotal: Int) {
        timerJob?.cancel()
        _state.update {
            it.copy(
                phase = phase,
                currentIndex = index,
                sectionTotal = sectionTotal,
                question = question,
                selectedAnswer = null,
                answered = false,
                isCorrect = false,
                secondsLeft = TOTAL_SECONDS,
                feedbackMessage = null,
                showHint = false,
                lastAnswerStars = 0,
                isSpeaking = false
            )
        }
        startTimer()
    }

    private fun startTimer() {
        timerJob = viewModelScope.launch {
            for (t in TOTAL_SECONDS downTo 0) {
                _state.update { it.copy(secondsLeft = t) }
                if (t == 0) {
                    onTimeUp()
                    break
                }
                // Ticks every second so kids can hear the clock running, not just see
                // it -- sharper in the last 10s to match the timer's visual urgency.
                SoundEffects.playTick(urgent = t <= 10)
                delay(1000)
            }
        }
    }

    fun selectAnswer(answer: String) {
        val current = _state.value
        val question = current.question
        if (current.answered || question == null) return
        timerJob?.cancel()
        val correct = answer == question.correctAnswer
        val speedStars = if (correct) starsForSpeed(current.secondsLeft) else 0
        if (current.phase == QuizPhase.MAIN) {
            perQuestionStars += speedStars
        }
        _state.update {
            it.copy(
                selectedAnswer = answer,
                answered = true,
                isCorrect = correct,
                feedbackMessage = if (correct) EncouragementBank.randomPraise() else EncouragementBank.randomEncouragement(),
                showHint = false,
                lastAnswerStars = speedStars,
                // Next stays disabled until the word has been fully spoken out.
                isSpeaking = true
            )
        }
        pendingWrite = viewModelScope.launch {
            recordAttempt(question, correct)
            if (correct) SoundEffects.playCorrectChime() else SoundEffects.playWrongBuzz()
            // Small gap so the confirmation chime/buzz finishes before the letters
            // start -- otherwise they talk over each other.
            delay(SPEAK_DELAY_AFTER_CHIME_MS)
            SpellingSpeaker.speakWordAndSpelling(question.word) { onSpeakingDone() }
        }
    }

    private fun onTimeUp() {
        val current = _state.value
        val question = current.question
        if (current.answered || question == null) return
        if (current.phase == QuizPhase.MAIN) {
            perQuestionStars += 0
        }
        _state.update {
            it.copy(
                answered = true,
                isCorrect = false,
                selectedAnswer = null,
                feedbackMessage = EncouragementBank.randomEncouragement(),
                showHint = false,
                lastAnswerStars = 0,
                isSpeaking = true
            )
        }
        pendingWrite = viewModelScope.launch {
            recordAttempt(question, false)
            SoundEffects.playWhistle()
            delay(SPEAK_DELAY_AFTER_WHISTLE_MS)
            SpellingSpeaker.speakWordAndSpelling(question.word) { onSpeakingDone() }
        }
    }

    /** [SpellingSpeaker]'s callback fires on a TTS thread, not necessarily the main
     * thread -- StateFlow.update is thread-safe, so that's fine here. */
    private fun onSpeakingDone() {
        _state.update { it.copy(isSpeaking = false) }
    }

    fun toggleHint() {
        _state.update { it.copy(showHint = !it.showHint) }
    }

    /** Faster correct answers earn more stars: <=5s -> 5, <=10s -> 4, <=15s -> 3, <=20s -> 2, else -> 1. */
    private fun starsForSpeed(secondsLeft: Int): Int {
        val elapsed = TOTAL_SECONDS - secondsLeft
        return when {
            elapsed <= 5 -> 5
            elapsed <= 10 -> 4
            elapsed <= 15 -> 3
            elapsed <= 20 -> 2
            else -> 1
        }
    }

    private suspend fun recordAttempt(question: Question, correct: Boolean) {
        repository.logAttempt(
            userId = userId,
            sessionId = sessionId,
            gradeId = gradeId,
            levelId = levelId,
            wordId = question.wordId,
            questionType = question.type.name,
            isFinalTest = _state.value.phase == QuizPhase.FINAL_TEST,
            isCorrect = correct,
            attemptIndex = attemptCounter++,
            timestamp = System.currentTimeMillis()
        )
    }

    fun nextQuestion() {
        viewModelScope.launch {
            pendingWrite?.join()
            val current = _state.value
            // Defensive guard -- the Next button is disabled while speaking, but the
            // TTS completion callback lands on a background thread, so don't trust UI
            // timing alone to prevent a race here.
            if (current.isSpeaking) return@launch
            when (current.phase) {
                QuizPhase.MAIN -> {
                    val nextIndex = current.currentIndex + 1
                    if (nextIndex < mainQuestions.size) {
                        showQuestion(mainQuestions[nextIndex], QuizPhase.MAIN, nextIndex, mainQuestions.size)
                    } else {
                        startFinalTestOrFinish()
                    }
                }

                QuizPhase.FINAL_TEST -> {
                    val nextIndex = current.currentIndex + 1
                    if (nextIndex < finalQuestions.size) {
                        showQuestion(finalQuestions[nextIndex], QuizPhase.FINAL_TEST, nextIndex, finalQuestions.size)
                    } else {
                        finishLevel()
                    }
                }
            }
        }
    }

    private suspend fun startFinalTestOrFinish() {
        val wrongIds = repository.getWrongWordIds(userId, sessionId, levelId).shuffled().take(MAX_FINAL_TEST_QUESTIONS)
        if (wrongIds.isEmpty()) {
            finishLevel()
            return
        }
        val words = repository.getWordsByIds(wrongIds)
        finalQuestions = words.map { QuestionFactory.buildQuestion(it) }
        showQuestion(finalQuestions.first(), QuizPhase.FINAL_TEST, 0, finalQuestions.size)
    }

    private suspend fun finishLevel() {
        val correctMain = repository.countCorrectMain(userId, sessionId, levelId)
        val total = mainQuestions.size
        val speedStars = if (perQuestionStars.isEmpty()) 1 else perQuestionStars.average().roundToInt().coerceIn(1, 5)
        val stars = repository.finishLevel(userId, levelId, gradeId, speedStars, correctMain)
        _state.update {
            it.copy(finished = true, finalStars = stars, finalCorrect = correctMain, finalTotal = total)
        }
    }

    override fun onCleared() {
        timerJob?.cancel()
    }

    companion object {
        const val TOTAL_SECONDS = 30
        const val MAX_FINAL_TEST_QUESTIONS = 2
        private const val SPEAK_DELAY_AFTER_CHIME_MS = 400L
        private const val SPEAK_DELAY_AFTER_WHISTLE_MS = 950L
    }
}
