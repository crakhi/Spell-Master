package com.spell.master.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spell.master.data.repository.SpellRepository
import com.spell.master.domain.LevelWithProgress
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class LevelDashboardViewModel(repository: SpellRepository, userId: String, gradeId: Int) : ViewModel() {
    val levels: StateFlow<List<LevelWithProgress>> = repository.observeLevelsWithProgress(userId, gradeId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
}
