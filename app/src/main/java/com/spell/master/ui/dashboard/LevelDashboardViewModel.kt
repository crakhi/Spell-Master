package com.spell.master.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spell.master.data.local.entity.LevelEntity
import com.spell.master.data.repository.SpellRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class LevelDashboardViewModel(repository: SpellRepository, gradeId: Int) : ViewModel() {
    val levels: StateFlow<List<LevelEntity>> = repository.observeLevels(gradeId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
}
