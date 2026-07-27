package ru.lissa_lesia.iteams.presentation.navigation

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Register : Screen("register")
    object Feed : Screen("feed")
    object CreateProject : Screen("create_project")
    object Profile : Screen("profile")
    object Details : Screen("details/{projectId}") {
        fun passProjectId(projectId: String) = "details/$projectId"
    }
}