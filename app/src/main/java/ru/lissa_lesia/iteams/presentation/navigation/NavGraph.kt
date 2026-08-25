package ru.lissa_lesia.iteams.presentation.navigation

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import org.koin.androidx.compose.koinViewModel
import ru.lissa_lesia.iteams.presentation.screens.applications.ApplicationsScreen
import ru.lissa_lesia.iteams.presentation.screens.candidatedetails.CandidateDetailsScreen
import ru.lissa_lesia.iteams.presentation.screens.createcandidate.CreateCandidateScreen
import ru.lissa_lesia.iteams.presentation.screens.createproject.CreateProjectScreen
import ru.lissa_lesia.iteams.presentation.screens.details.ProjectDetailsScreen
import ru.lissa_lesia.iteams.presentation.screens.editproject.EditProjectScreen
import ru.lissa_lesia.iteams.presentation.screens.feed.FeedScreen
import ru.lissa_lesia.iteams.presentation.screens.login.LoginScreen
import ru.lissa_lesia.iteams.presentation.screens.profile.ProfileScreen
import ru.lissa_lesia.iteams.presentation.screens.register.RegisterScreen
import ru.lissa_lesia.iteams.presentation.screens.userprofile.UserProfileScreen

@Composable
fun NavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier,
) {
    val authStateManager: AuthStateManager = koinViewModel()
    val authState by authStateManager.authState.collectAsState()

    NavHost(
        navController = navController,
        startDestination = if (authState?.isAuthenticated == true) {
            Screen.Feed.route
        } else {
            Screen.Login.route
        },
        modifier = modifier
    ) {
        // Экран входа
        composable(route = Screen.Login.route) {
            LoginScreen(
                onNavigateToRegister = {
                    navController.navigate(Screen.Register.route)
                },
                onNavigateToFeed = {
                    navController.navigate(Screen.Feed.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }

        // Экран регистрации
        composable(route = Screen.Register.route) {
            RegisterScreen(
                onNavigateToLogin = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Register.route) { inclusive = true }
                    }
                },
                onNavigateToFeed = {
                    navController.navigate(Screen.Feed.route) {
                        popUpTo(Screen.Register.route) { inclusive = true }
                    }
                }
            )
        }

        // Экран ленты
        composable(route = Screen.Feed.route) {
            FeedScreen(
                onNavigateToCreateProject = {
                    navController.navigate(Screen.CreateProject.route)
                },
                onNavigateToCreateCandidate = {
                    navController.navigate(Screen.CreateCandidate.route)
                },
                onNavigateToProfile = {
                    navController.navigate(Screen.Profile.route)
                },
                onNavigateToApplications = {
                    navController.navigate(Screen.Applications.route)
                },
                onProjectClick = { projectId ->
                    navController.navigate(Screen.Details.passProjectId(projectId))
                },
                onCandidateClick = { candidateId ->
                    navController.navigate(Screen.CandidateDetails.passCandidateId(candidateId))
                },
                onUserClick = { userId ->
                    if (userId.isNotBlank()) {
                        navController.navigate(Screen.ProfileUser.passUserId(userId))
                    }
                }
            )
        }

        // Экран создания проекта
        composable(route = Screen.CreateProject.route) {
            CreateProjectScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // Экран профиля пользователя (свой)
        composable(route = Screen.Profile.route) {
            ProfileScreen(
                onNavigateBack = { navController.popBackStack() },
                onLogout = {
                    authStateManager.logout()
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        // Экран деталей проекта
        composable(
            route = Screen.Details.route,
            arguments = listOf(navArgument("projectId") { type = NavType.StringType })
        ) { backStackEntry ->
            val projectId = backStackEntry.arguments?.getString("projectId") ?: ""
            ProjectDetailsScreen(
                projectId = projectId,
                onNavigateBack = { navController.popBackStack() },
                onEditProject = { projectId ->
                    navController.navigate(Screen.EditProject.passProjectId(projectId))
                },
                onUserClick = { userId ->
                    if (userId.isNotBlank()) {
                        navController.navigate(Screen.ProfileUser.passUserId(userId))
                    }
                }
            )
        }

        // Экран редактирования проекта
        composable(
            route = Screen.EditProject.route,
            arguments = listOf(navArgument("projectId") { type = NavType.StringType })
        ) { backStackEntry ->
            val projectId = backStackEntry.arguments?.getString("projectId") ?: ""
            EditProjectScreen(
                projectId = projectId,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // Экран заявок
        composable(route = Screen.Applications.route) {
            ApplicationsScreen(
                onNavigateBack = { navController.popBackStack() },
                onProjectClick = { projectId ->
                    navController.navigate(Screen.Details.passProjectId(projectId))
                },
                onUserClick = { userId ->
                    if (userId.isNotBlank()) {
                        navController.navigate(Screen.ProfileUser.passUserId(userId))
                    }
                }
            )
        }

        // Экран профиля другого пользователя
        composable(
            route = Screen.ProfileUser.route,
            arguments = listOf(navArgument("userId") { type = NavType.StringType })
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId") ?: ""
            Log.d("NavGraph", "ProfileUser received userId: $userId")
            UserProfileScreen(
                userId = userId,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // Экран создания/редактирования заявки кандидата
        composable(route = Screen.CreateCandidate.route) {
            CreateCandidateScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // Экран деталей кандидата
        composable(
            route = Screen.CandidateDetails.route,
            arguments = listOf(navArgument("candidateId") { type = NavType.StringType })
        ) { backStackEntry ->
            val candidateId = backStackEntry.arguments?.getString("candidateId") ?: ""

            CandidateDetailsScreen(
                candidateId = candidateId, // передаём в экран
                onNavigateBack = { navController.popBackStack() },
                onEditCandidate = {
                    navController.navigate(Screen.CreateCandidate.route)
                },
                onUserClick = { userId ->
                    if (userId.isNotBlank()) {
                        navController.navigate(Screen.ProfileUser.passUserId(userId))
                    }
                }
            )
        }
    }
}