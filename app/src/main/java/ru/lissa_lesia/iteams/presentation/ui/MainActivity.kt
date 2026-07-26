package ru.lissa_lesia.iteams

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import ru.lissa_lesia.iteams.data.repositories.AuthRepositoryImpl
import ru.lissa_lesia.iteams.data.repositories.ProjectRepositoryImpl
import ru.lissa_lesia.iteams.presentation.navigation.NavGraph
import ru.lissa_lesia.iteams.presentation.screens.createproject.CreateProjectViewModel
import ru.lissa_lesia.iteams.presentation.screens.feed.FeedViewModel
import ru.lissa_lesia.iteams.presentation.screens.login.LoginViewModel
import ru.lissa_lesia.iteams.presentation.screens.profile.ProfileViewModel
import ru.lissa_lesia.iteams.presentation.screens.register.RegisterViewModel
import ru.lissa_lesia.iteams.ui.theme.ITeamsTheme

class MainActivity : ComponentActivity() {

    private val authRepository by lazy {
        AuthRepositoryImpl(FirebaseAuth.getInstance(), FirebaseFirestore.getInstance())
    }
    private val projectRepository by lazy {
        ProjectRepositoryImpl(FirebaseFirestore.getInstance())
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            ITeamsTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    val currentUser = authRepository.getCurrentUser()
                    val userId = currentUser?.id ?: ""

                    val loginViewModel: LoginViewModel = viewModel(
                        factory = LoginViewModel.provideFactory(authRepository)
                    )
                    val registerViewModel: RegisterViewModel = viewModel(
                        factory = RegisterViewModel.provideFactory(authRepository)
                    )
                    val feedViewModel: FeedViewModel = viewModel(
                        factory = FeedViewModel.provideFactory(projectRepository, authRepository)
                    )
                    val createProjectViewModel: CreateProjectViewModel = viewModel(
                        factory = CreateProjectViewModel.provideFactory(projectRepository, userId)
                    )
                    val profileViewModel: ProfileViewModel = viewModel(
                        factory = ProfileViewModel.provideFactory(authRepository)
                    )

                    NavGraph(
                        navController = navController,
                        loginViewModel = loginViewModel,
                        registerViewModel = registerViewModel,
                        feedViewModel = feedViewModel,
                        createProjectViewModel = createProjectViewModel,
                        profileViewModel = profileViewModel
                    )
                }
            }
        }
    }
}
