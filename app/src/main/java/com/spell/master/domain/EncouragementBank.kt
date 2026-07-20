package com.spell.master.domain

/** Kid-friendly phrase banks shown after each answer, picked at random so the game doesn't feel repetitive. */
object EncouragementBank {

    private val praise = listOf(
        "Awesome job! 🌟",
        "You're a spelling star! ⭐",
        "Fantastic work!",
        "Super duper! 🎉",
        "You nailed it!",
        "Great buzzing, bee-rilliant!",
        "Wow, brilliant!",
        "You're on fire! 🔥",
        "High five! ✋",
        "Amazing spelling!",
        "Sweet as honey! 🍯",
        "You're buzz-tastic!"
    )

    private val encouragement = listOf(
        "Almost there! Great try!",
        "So close, keep buzzing!",
        "Nice attempt, superstar!",
        "You're learning fast!",
        "Good effort! On to the next one!",
        "Don't worry, every bee makes mistakes!",
        "Keep going, you're doing great!",
        "So brave to try! Let's keep going!"
    )

    fun randomPraise(): String = praise.random()

    fun randomEncouragement(): String = encouragement.random()
}
