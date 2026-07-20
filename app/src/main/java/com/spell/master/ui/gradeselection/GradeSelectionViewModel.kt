package com.spell.master.ui.gradeselection

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spell.master.data.local.entity.GradeEntity
import com.spell.master.data.repository.SpellRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class GradeSelectionViewModel(private val repository: SpellRepository) : ViewModel() {

    val grades: StateFlow<List<GradeEntity>> = repository.observeGrades()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private var beeTapCount = 0

    /**
     * One-shot event (not a StateFlow) for the "data cleared" toast -- a StateFlow would
     * replay its last value to the Screen's LaunchedEffect every time the user navigates
     * back to this screen, re-showing a stale toast even though nothing new happened.
     */
    private val _dataClearedEvents = MutableSharedFlow<Unit>(replay = 0)
    val dataClearedEvents: SharedFlow<Unit> = _dataClearedEvents

    init {
        viewModelScope.launch { repository.ensureSeeded() }
    }

    /** Hidden dev/QA gesture: tapping the bee mascot 10 times in a row wipes and reseeds the database. */
    fun onBeeTapped() {
        beeTapCount++
        if (beeTapCount >= BEE_TAPS_TO_RESET) {
            beeTapCount = 0
            viewModelScope.launch {
                repository.clearAllData()
                repository.ensureSeeded()
                _dataClearedEvents.emit(Unit)
            }
        }
    }

    companion object {
        private const val BEE_TAPS_TO_RESET = 10
    }
}
