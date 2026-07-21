package com.spell.master.ui.result

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spell.master.data.repository.SpellRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class LevelResultViewModel(
    private val repository: SpellRepository,
    private val userId: String,
    private val levelId: String
) : ViewModel() {

    private val _nextLevelId = MutableStateFlow<String?>(null)
    val nextLevelId: StateFlow<String?> = _nextLevelId.asStateFlow()

    init {
        viewModelScope.launch {
            val level = repository.getLevel(levelId) ?: return@launch
            val levels = repository.observeLevelsWithProgress(userId, level.gradeId).first()
            val index = levels.indexOfFirst { it.levelId == levelId }
            _nextLevelId.value = levels.getOrNull(index + 1)?.levelId
        }
    }
}
