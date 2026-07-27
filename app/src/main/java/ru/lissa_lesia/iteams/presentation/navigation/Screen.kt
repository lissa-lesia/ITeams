sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Register : Screen("register")
    object Feed : Screen("feed")
    object CreateProject : Screen("create_project")
    object EditProject : Screen("edit_project/{projectId}") {
        fun passProjectId(projectId: String) = "edit_project/$projectId"
    }
    object Profile : Screen("profile")
    object Applications : Screen("applications")
    object Details : Screen("details/{projectId}") {
        fun passProjectId(projectId: String) = "details/$projectId"
    }
}