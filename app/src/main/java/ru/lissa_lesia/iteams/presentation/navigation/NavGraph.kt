package ru.lissa_lesia.iteams.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import ru.lissa_lesia.iteams.presentation.screens.createproject.CreateProjectScreen
import ru.lissa_lesia.iteams.presentation.screens.createproject.CreateProjectViewModel
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
    // Передаём ViewModel фабрики или сами инстансы (зависит от DI)
    loginViewModel: LoginViewModel,
    registerViewModel: RegisterViewModel,
    feedViewModel: FeedViewModel,
    createProjectViewModel: CreateProjectViewModel,
    profileViewModel: ProfileViewModel,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Login.route,
        modifier = modifier
    ) {
        composable(route = Screen.Login.route) {
            LoginScreen(
                viewModel = loginViewModel,
                onNavigateToRegister = { navController.navigate(Screen.Register.route) },
                onNavigateToFeed = { navController.navigate(Screen.Feed.route) }
            )
        }
        composable(route = Screen.Register.route) {
            RegisterScreen(
                viewModel = registerViewModel,
                onNavigateToLogin = { navController.navigate(Screen.Login.route) },
                onNavigateToFeed = { navController.navigate(Screen.Feed.route) }
            )
        }
        composable(route = Screen.Feed.route) {
            FeedScreen(
                viewModel = feedViewModel,
                onNavigateToCreateProject = { navController.navigate(Screen.CreateProject.route) },
                onNavigateToProfile = { navController.navigate(Screen.Profile.route) },
                onLogout = {
                    // Здесь можно вызвать logout из AuthRepository
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Feed.route) { inclusive = true }
                    }
                }
            )
        }
        composable(route = Screen.CreateProject.route) {
            CreateProjectScreen(
                viewModel = createProjectViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(route = Screen.Profile.route) {
            ProfileScreen(
                viewModel = profileViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}