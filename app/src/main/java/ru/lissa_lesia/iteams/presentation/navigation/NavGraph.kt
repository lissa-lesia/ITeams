package ru.lissa_lesia.iteams.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import ru.lissa_lesia.iteams.domain.repositories.IAuthRepository
import ru.lissa_lesia.iteams.domain.repositories.IProjectRepository
import ru.lissa_lesia.iteams.presentation.screens.applications.ApplicationsScreen
import ru.lissa_lesia.iteams.presentation.screens.applications.ApplicationsViewModel
import ru.lissa_lesia.iteams.presentation.screens.createproject.CreateProjectScreen
import ru.lissa_lesia.iteams.presentation.screens.createproject.CreateProjectViewModel
import ru.lissa_lesia.iteams.presentation.screens.details.ProjectDetailsScreen
import ru.lissa_lesia.iteams.presentation.screens.details.ProjectDetailsViewModel
import ru.lissa_lesia.iteams.presentation.screens.editproject.EditProjectScreen
import ru.lissa_lesia.iteams.presentation.screens.editproject.EditProjectViewModel
import ru.lissa_lesia.iteams.presentation.screens.feed.FeedScreen
import ru.lissa_lesia.iteams.presentation.screens.feed.FeedViewModel
import ru.lissa_lesia.iteams.presentation.screens.login.LoginScreen
import ru.lissa_lesia.iteams.presentation.screens.login.LoginViewModel
import ru.lissa_lesia.iteams.presentation.screens.profile.ProfileScreen
import ru.lissa_lesia.iteams.presentation.screens.profile.ProfileViewModel
import ru.lissa_lesia.iteams.presentation.screens.register.RegisterScreen
import ru.lissa_lesia.iteams.presentation.screens.register.RegisterViewModel

@Composable
fun NavGraph(
    navController: NavHostController,
    authRepository: IAuthRepository,
    loginViewModel: LoginViewModel,
    registerViewModel: RegisterViewModel,
    feedViewModel: FeedViewModel,
    createProjectViewModel: CreateProjectViewModel,
    profileViewModel: ProfileViewModel,
    applicationsViewModel: ApplicationsViewModel,
    modifier: Modifier = Modifier,
    projectRepository: IProjectRepository,
    authStateManager: AuthStateManager,
) {
    NavHost(
        navController = navController,
        startDestination = if (authStateManager.authState.value?.isAuthenticated == true) {
            Screen.Feed.route
        } else {
            Screen.Login.route
        },
        modifier = modifier
    ) {
        composable(route = Screen.Login.route) {
            LoginScreen(
                viewModel = loginViewModel,
                onNavigateToRegister = { navController.navigate(Screen.Register.route) },
                onNavigateToFeed = {
                    navController.navigate(Screen.Feed.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }

        composable(route = Screen.Register.route) {
            RegisterScreen(
                viewModel = registerViewModel,
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

        composable(route = Screen.Feed.route) {
            FeedScreen(
                viewModel = feedViewModel,
                onNavigateToCreateProject = { navController.navigate(Screen.CreateProject.route) },
                onNavigateToProfile = { navController.navigate(Screen.Profile.route) },
                onNavigateToApplications = { navController.navigate(Screen.Applications.route) },
                onProjectClick = { projectId ->
                    navController.navigate(Screen.Details.passProjectId(projectId))
                },
                onLogout = {
                    authStateManager.logout()
                    loginViewModel.resetState()
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Feed.route) { inclusive = true }
                    }
                }
            )
        }

        composable(route = Screen.CreateProject.route) {
            CreateProjectScreen(
                viewModel = createProjectViewModel,
                onNavigateBack = {
                    navController.popBackStack()
                    createProjectViewModel.resetState()
                }
            )
        }

        composable(route = Screen.Profile.route) {
            ProfileScreen(
                viewModel = profileViewModel,
                onNavigateBack = { navController.popBackStack() },
                onLogout = {
                    authStateManager.logout()
                    loginViewModel.resetState()
                    registerViewModel.resetState()
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable(
            route = Screen.Details.route,
            arguments = listOf(navArgument("projectId") { type = NavType.StringType })
        ) { backStackEntry ->
            val projectId = backStackEntry.arguments?.getString("projectId") ?: ""
            val savedStateHandle = backStackEntry.savedStateHandle
            savedStateHandle["projectId"] = projectId

            val detailsViewModel: ProjectDetailsViewModel = viewModel(
                factory = ProjectDetailsViewModel.provideFactory(
                    projectRepository,
                    authRepository,
                    savedStateHandle
                )
            )
            ProjectDetailsScreen(
                viewModel = detailsViewModel,
                onNavigateBack = { navController.popBackStack() },
                onEditProject = { projectId ->
                    navController.navigate(Screen.EditProject.passProjectId(projectId))
                }
            )
        }

        composable(
            route = Screen.EditProject.route,
            arguments = listOf(navArgument("projectId") { type = NavType.StringType })
        ) { backStackEntry ->
            val projectId = backStackEntry.arguments?.getString("projectId") ?: ""
            val savedStateHandle = backStackEntry.savedStateHandle
            savedStateHandle["projectId"] = projectId

            val editViewModel: EditProjectViewModel = viewModel(
                factory = EditProjectViewModel.provideFactory(
                    projectRepository,
                    authStateManager,
                    savedStateHandle
                )
            )
            EditProjectScreen(
                viewModel = editViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(route = Screen.Applications.route) {
            ApplicationsScreen(
                viewModel = applicationsViewModel,
                onNavigateBack = { navController.popBackStack() },
                onProjectClick = { projectId ->
                    navController.navigate(Screen.Details.passProjectId(projectId))
                }
            )
        }
    }
}