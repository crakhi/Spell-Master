package com.spell.master.domain

enum class QuestionType {
    /** "Choose the apt word": meaning shown, pick the real word among decoy [aptwords]. */
    CHOOSE_APT_WORD,

    /** "Choose the right word": meaning shown, pick the correctly spelled word among [rightwords] misspellings. */
    CHOOSE_RIGHT_WORD,

    /** "Fill the blanks": word shown with some letters replaced by dashes, pick the missing letters from [fillings]. */
    FILL_BLANKS
}

data class Question(
    val wordId: Int,
    val word: String,
    val type: QuestionType,
    val promptTitle: String,
    val promptSubtitle: String?,
    val options: List<String>,
    val correctAnswer: String,
    val hint: String?,
    val example: String
)
