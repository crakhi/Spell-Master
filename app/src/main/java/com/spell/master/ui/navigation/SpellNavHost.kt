package com.spell.master.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.spell.master.di.LocalAuthRepository
import com.spell.master.ui.dashboard.LevelDashboardScreen
import com.spell.master.ui.gradeselection.GradeSelectionScreen
import com.spell.master.ui.question.QuestionScreen
import com.spell.master.ui.result.LevelResultScreen
import com.spell.master.ui.signin.SignInScreen

@Composable
fun SpellNavHost() {
    val navController = rememberNavController()
    val authRepository = LocalAuthRepository.current
    // Firebase caches the signed-in session locally, so this is available synchronously
    // -- already-signed-in users skip straight past the sign-in gate.
    val startDestination = if (authRepository.currentUserId != null) Screen.GRADE_SELECTION else Screen.SIGN_IN

    NavHost(navController = navController, startDestination = startDestination) {
        composable(Screen.SIGN_IN) {
            SignInScreen(
                onSignedIn = {
                    navController.navigate(Screen.GRADE_SELECTION) {
                        popUpTo(Screen.SIGN_IN) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.GRADE_SELECTION) {
            GradeSelectionScreen(
                onGradeSelected = { gradeId ->
                    navController.navigate(Screen.levelDashboard(gradeId))
                },
                onSignedOut = {
                    navController.navigate(Screen.SIGN_IN) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable(
            route = Screen.LEVEL_DASHBOARD,
            arguments = listOf(navArgument("gradeId") { type = NavType.IntType })
        ) { backStackEntry ->
            val gradeId = backStackEntry.arguments?.getInt("gradeId") ?: return@composable
            LevelDashboardScreen(
                gradeId = gradeId,
                onBack = { navController.popBackStack() },
                onLevelSelected = { levelId ->
                    navController.navigate(Screen.question(levelId))
                }
            )
        }

        composable(
            route = Screen.QUESTION,
            arguments = listOf(navArgument("levelId") { type = NavType.StringType })
        ) { backStackEntry ->
            val levelId = backStackEntry.arguments?.getString("levelId") ?: return@composable
            QuestionScreen(
                levelId = levelId,
                onExit = { navController.popBackStack() },
                onLevelFinished = { sessionId, stars, correct, total ->
                    navController.navigate(Screen.levelResult(levelId, sessionId, stars, correct, total)) {
                        popUpTo(Screen.LEVEL_DASHBOARD) { inclusive = false }
                    }
                }
            )
        }

        composable(
            route = Screen.LEVEL_RESULT,
            arguments = listOf(
                navArgument("levelId") { type = NavType.StringType },
                navArgument("sessionId") { type = NavType.StringType },
                navArgument("stars") { type = NavType.IntType },
                navArgument("correct") { type = NavType.IntType },
                navArgument("total") { type = NavType.IntType }
            )
        ) { backStackEntry ->
            val args = backStackEntry.arguments ?: return@composable
            LevelResultScreen(
                levelId = args.getString("levelId").orEmpty(),
                stars = args.getInt("stars"),
                correctCount = args.getInt("correct"),
                total = args.getInt("total"),
                onBackToDashboard = { navController.popBackStack(Screen.LEVEL_DASHBOARD, inclusive = false) },
                onReplayLevel = {
                    navController.navigate(Screen.question(args.getString("levelId").orEmpty())) {
                        popUpTo(Screen.LEVEL_DASHBOARD) { inclusive = false }
                    }
                },
                onNextLevel = { nextLevelId ->
                    navController.navigate(Screen.question(nextLevelId)) {
                        popUpTo(Screen.LEVEL_DASHBOARD) { inclusive = false }
                    }
                }
            )
        }
    }
}
