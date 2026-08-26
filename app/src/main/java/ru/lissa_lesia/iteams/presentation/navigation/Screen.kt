sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Register : Screen("register")
    object Feed : Screen("feed")
    object CreateProject : Screen("create_project")
    object EditProject : Screen("edit_project/{projectId}") {
        fun passProjectId(projectId: String) = "edit_project/$projectId"
    }
    object Profile : Screen("profile")
    object ProfileUser : Screen("profile_user/{userId}") {
        fun passUserId(userId: String) = "profile_user/$userId"
    }
    object Applications : Screen("applications")
    object Details : Screen("details/{projectId}") {
        fun passProjectId(projectId: String) = "details/$projectId"
    }

    object CreateCandidate : Screen("create_candidate")
    object CandidateDetails : Screen("candidate_details/{candidateId}") {
        fun passCandidateId(candidateId: String) = "candidate_details/$candidateId"
    }
}