package ru.lissa_lesia.iteams.di

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.parameter.parametersOf
import org.koin.dsl.module
import ru.lissa_lesia.iteams.data.repositories.AuthRepositoryImpl
import ru.lissa_lesia.iteams.data.repositories.CandidateRepositoryImpl
import ru.lissa_lesia.iteams.data.repositories.ProjectRepositoryImpl
import ru.lissa_lesia.iteams.domain.repositories.IAuthRepository
import ru.lissa_lesia.iteams.domain.repositories.ICandidateRepository
import ru.lissa_lesia.iteams.domain.repositories.IProjectRepository
import ru.lissa_lesia.iteams.presentation.navigation.AuthStateManager
import ru.lissa_lesia.iteams.presentation.screens.applications.ApplicationsViewModel
import ru.lissa_lesia.iteams.presentation.screens.candidatedetails.CandidateDetailsViewModel
import ru.lissa_lesia.iteams.presentation.screens.createcandidate.CreateCandidateViewModel
import ru.lissa_lesia.iteams.presentation.screens.createproject.CreateProjectViewModel
import ru.lissa_lesia.iteams.presentation.screens.details.ProjectDetailsViewModel
import ru.lissa_lesia.iteams.presentation.screens.editproject.EditProjectViewModel
import ru.lissa_lesia.iteams.presentation.screens.feed.FeedViewModel
import ru.lissa_lesia.iteams.presentation.screens.login.LoginViewModel
import ru.lissa_lesia.iteams.presentation.screens.profile.ProfileViewModel
import ru.lissa_lesia.iteams.presentation.screens.register.RegisterViewModel
import ru.lissa_lesia.iteams.presentation.screens.userprofile.UserProfileViewModel

val appModule = module {
    // Firebase instances
    single { FirebaseAuth.getInstance() }
    single { FirebaseFirestore.getInstance() }

    // Repositories
    single<IAuthRepository> { AuthRepositoryImpl(get(), get()) }
    single<IProjectRepository> { ProjectRepositoryImpl(get(), get(), get()) }
    single<ICandidateRepository> { CandidateRepositoryImpl(get()) }

    // AuthStateManager
    single { AuthStateManager(get()) }

    // ViewModels
    viewModel { LoginViewModel(get()) }
    viewModel { RegisterViewModel(get()) }
    viewModel { FeedViewModel(get(), get(), get(), get()) }
    viewModel { CreateProjectViewModel(get(), get()) }
    viewModel { ProfileViewModel(get(), get()) }
    viewModel { ApplicationsViewModel(get(), get(), get()) }

    // ViewModel с SavedStateHandle (для ProjectDetails)
    viewModel { params ->
        ProjectDetailsViewModel(
            projectRepository = get(),
            authRepository = get(),
            savedStateHandle = params.get()
        )
    }

    // ViewModel с SavedStateHandle (для EditProject)
    viewModel { params ->
        EditProjectViewModel(
            projectRepository = get(),
            authStateManager = get(),
            savedStateHandle = params.get()
        )
    }

    // ViewModel с SavedStateHandle (для CandidateDetails) - ИСПРАВЛЕНО
    viewModel { params ->
        CandidateDetailsViewModel(
            candidateRepository = get(),
            projectRepository = get(), // <-- добавлено
            authStateManager = get(),
            savedStateHandle = params.get()
        )
    }

    // ViewModel с параметром String (для UserProfile)
    viewModel { params ->
        UserProfileViewModel(
            authRepository = get(),
            userId = params.get()
        )
    }

    viewModel { CreateCandidateViewModel(get(), get()) }
}