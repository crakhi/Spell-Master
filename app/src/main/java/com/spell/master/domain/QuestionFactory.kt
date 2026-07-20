package com.spell.master.domain

import com.spell.master.data.local.entity.WordEntity

/**
 * Turns the 20 (or however many) words of a level into a randomized quiz.
 * Word order is shuffled and question types are spread roughly evenly across
 * the three kinds, then shuffled again, so the run never feels repetitive.
 */
object QuestionFactory {

    fun buildLevelQuestions(words: List<WordEntity>): List<Question> {
        if (words.isEmpty()) return emptyList()
        val shuffledWords = words.shuffled()
        val types = List(shuffledWords.size) { QuestionType.entries[it % QuestionType.entries.size] }.shuffled()
        return shuffledWords.mapIndexed { index, word -> buildQuestion(word, types[index]) }
    }

    /** Used for FinalTest re-asks: pick a fresh (possibly different) question type for the same word. */
    fun buildQuestion(word: WordEntity, type: QuestionType = QuestionType.entries.random()): Question = when (type) {
        QuestionType.CHOOSE_APT_WORD -> {
            val options = (word.aptwords + word.word).shuffled()
            Question(
                wordId = word.id,
                word = word.word,
                type = type,
                promptTitle = word.meaning,
                promptSubtitle = "Which word means this?",
                options = options,
                correctAnswer = word.word,
                hint = word.hint,
                example = word.example
            )
        }

        QuestionType.CHOOSE_RIGHT_WORD -> {
            val options = (word.rightwords + word.word).shuffled()
            Question(
                wordId = word.id,
                word = word.word,
                type = type,
                promptTitle = word.meaning,
                promptSubtitle = "Which one is spelled correctly?",
                options = options,
                correctAnswer = word.word,
                hint = null,
                example = word.example
            )
        }

        QuestionType.FILL_BLANKS -> {
            val blanked = blankedWord(word.word, word.blanks)
            val correct = correctFilling(word.word, word.blanks, word.fillings)
            Question(
                wordId = word.id,
                word = word.word,
                type = type,
                promptTitle = blanked,
                promptSubtitle = word.meaning,
                options = word.fillings.shuffled(),
                correctAnswer = correct,
                hint = null,
                example = word.example
            )
        }
    }

    private fun blankedWord(word: String, blanks: List<Int>): String {
        if (blanks.isEmpty()) return word
        val chars = word.toCharArray()
        blanks.forEach { position -> if (position in chars.indices) chars[position] = '_' }
        return chars.joinToString(separator = " ")
    }

    /** [fillings] aren't ordered correct-first in the source data, so the real answer
     * is whichever filling actually reconstructs the word at the blank positions. */
    private fun correctFilling(word: String, blanks: List<Int>, fillings: List<String>): String {
        if (blanks.isEmpty() || fillings.isEmpty()) return fillings.firstOrNull().orEmpty()
        val start = blanks.min()
        val end = blanks.max()
        if (start < 0 || end >= word.length) return fillings.first()
        val target = word.substring(start, end + 1)
        return fillings.firstOrNull { it.equals(target, ignoreCase = true) } ?: fillings.first()
    }
}
