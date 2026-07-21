package com.spell.master.ui.navigation

object Screen {
    const val SIGN_IN = "sign_in"

    const val GRADE_SELECTION = "grade_selection"

    const val LEVEL_DASHBOARD = "level_dashboard/{gradeId}"
    fun levelDashboard(gradeId: Int) = "level_dashboard/$gradeId"

    const val QUESTION = "question/{levelId}"
    fun question(levelId: String) = "question/$levelId"

    const val LEVEL_RESULT = "level_result/{levelId}/{sessionId}/{stars}/{correct}/{total}"
    fun levelResult(levelId: String, sessionId: String, stars: Int, correct: Int, total: Int) =
        "level_result/$levelId/$sessionId/$stars/$correct/$total"
}
