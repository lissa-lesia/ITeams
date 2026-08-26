package ru.lissa_lesia.iteams.di

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.parameter.parametersOf
import org.koin.dsl.module
import ru.lissa_lesia.iteams.data.repositories.AuthRepositoryImpl
import ru.lissa_lesia.iteams.data.repositories.CandidateRepositoryImpl
import ru.lissa_lesia.iteams.data.repositories.ProjectRepositoryImpl
import ru.lissa_lesia.iteams.data.sources.AuthDataSource
import ru.lissa_lesia.iteams.data.sources.CandidateDataSource
import ru.lissa_lesia.iteams.data.sources.ProjectDataSource
import ru.lissa_lesia.iteams.data.sources.UserDataSource
import ru.lissa_lesia.iteams.domain.repositories.IAuthRepository
import ru.lissa_lesia.iteams.domain.repositories.ICandidateRepository
import ru.lissa_lesia.iteams.domain.repositories.IProjectRepository
import ru.lissa_lesia.iteams.domain.usecases.AcceptApplicantUseCase
import ru.lissa_lesia.iteams.domain.usecases.ApplyToProjectUseCase
import ru.lissa_lesia.iteams.domain.usecases.CreateCandidateUseCase
import ru.lissa_lesia.iteams.domain.usecases.CreateProjectUseCase
import ru.lissa_lesia.iteams.domain.usecases.GetCandidateByIdUseCase
import ru.lissa_lesia.iteams.domain.usecases.GetCandidatesByUserIdUseCase
import ru.lissa_lesia.iteams.domain.usecases.GetCandidatesUseCase
import ru.lissa_lesia.iteams.domain.usecases.GetCurrentUserUseCase
import ru.lissa_lesia.iteams.domain.usecases.GetFeedProjectsUseCase
import ru.lissa_lesia.iteams.domain.usecases.GetInvitationsForUserUseCase
import ru.lissa_lesia.iteams.domain.usecases.GetProjectByIdUseCase
import ru.lissa_lesia.iteams.domain.usecases.GetProjectsByUserIdUseCase
import ru.lissa_lesia.iteams.domain.usecases.GetUserByIdUseCase
import ru.lissa_lesia.iteams.domain.usecases.InviteCandidateUseCase
import ru.lissa_lesia.iteams.domain.usecases.LoginUseCase
import ru.lissa_lesia.iteams.domain.usecases.RegisterUseCase
import ru.lissa_lesia.iteams.domain.usecases.RejectApplicantUseCase
import ru.lissa_lesia.iteams.domain.usecases.RespondToInvitationUseCase
import ru.lissa_lesia.iteams.domain.usecases.UpdateCandidateUseCase
import ru.lissa_lesia.iteams.domain.usecases.UpdateProfileUseCase
import ru.lissa_lesia.iteams.domain.usecases.UpdateProjectUseCase
import ru.lissa_lesia.iteams.domain.usecases.WithdrawApplicationUseCase
import ru.lissa_lesia.iteams.presentation.navigation.AuthStateManager
import ru.lissa_lesia.iteams.presentation.screens.applications.ApplicationsViewModel
import ru.lissa_lesia.iteams.presentation.screens.candidatedetails.CandidateDetailsViewModel
import ru.lissa_lesia.iteams.presentation.screens.candidatesfeed.CandidatesFeedViewModel
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

    // Data Sources
    single { AuthDataSource(get()) }
    single { UserDataSource(get()) }
    single { ProjectDataSource(get()) }
    single { CandidateDataSource(get()) }

    // Repositories
    single<AuthRepositoryImpl> { AuthRepositoryImpl(get(), get()) }
    single<IAuthRepository> { get<AuthRepositoryImpl>() }
    single<IProjectRepository> { ProjectRepositoryImpl(get(), get(), get()) }
    single<ICandidateRepository> { CandidateRepositoryImpl(get()) }

    // Use Cases
    single { LoginUseCase(get()) }
    single { RegisterUseCase(get()) }
    single { GetFeedProjectsUseCase(get()) }
    single { GetCandidatesUseCase(get()) }
    single { GetUserByIdUseCase(get()) }
    single { CreateProjectUseCase(get()) }
    single { GetCurrentUserUseCase(get()) }
    single { UpdateProfileUseCase(get()) }
    single { GetProjectByIdUseCase(get()) }
    single { ApplyToProjectUseCase(get()) }
    single { WithdrawApplicationUseCase(get()) }
    single { UpdateProjectUseCase(get()) }
    single { GetCandidateByIdUseCase(get()) }
    single { GetProjectsByUserIdUseCase(get()) }
    single { InviteCandidateUseCase(get()) }
    single { GetCandidatesByUserIdUseCase(get()) }
    single { CreateCandidateUseCase(get()) }
    single { UpdateCandidateUseCase(get()) }
    single { GetInvitationsForUserUseCase(get()) }
    single { AcceptApplicantUseCase(get()) }
    single { RejectApplicantUseCase(get()) }
    single { RespondToInvitationUseCase(get()) }

    // AuthStateManager
    single { AuthStateManager(get()) }

    // ViewModels
    viewModel { LoginViewModel(get()) }
    viewModel { RegisterViewModel(get()) }
    viewModel { FeedViewModel(get(), get(), get()) }
    viewModel { CreateProjectViewModel(get(), get()) }
    viewModel { ProfileViewModel(get(), get(), get(), get()) }
    viewModel { ApplicationsViewModel(get(), get(), get(), get(), get(), get(), get()) }
    viewModel { CandidatesFeedViewModel(get(), get()) }

    // ViewModel с SavedStateHandle (для ProjectDetails)
    viewModel { params ->
        ProjectDetailsViewModel(
            getProjectByIdUseCase = get(),
            getCurrentUserUseCase = get(),
            applyToProjectUseCase = get(),
            withdrawApplicationUseCase = get(),
            savedStateHandle = params.get()
        )
    }

    // ViewModel с SavedStateHandle (для EditProject)
    viewModel { params ->
        EditProjectViewModel(
            getProjectByIdUseCase = get(),
            getCurrentUserUseCase = get(),
            updateProjectUseCase = get(),
            savedStateHandle = params.get()
        )
    }

    // ViewModel с SavedStateHandle (для CandidateDetails)
    viewModel { params ->
        CandidateDetailsViewModel(
            getCandidateByIdUseCase = get(),
            getProjectsByUserIdUseCase = get(),
            getCurrentUserUseCase = get(),
            inviteCandidateUseCase = get(),
            savedStateHandle = params.get()
        )
    }

    // ViewModel с параметром String (для UserProfile)
    viewModel { params ->
        UserProfileViewModel(
            getUserByIdUseCase = get(),
            userId = params.get()
        )
    }

    // CreateCandidateViewModel (без параметров)
    viewModel {
        CreateCandidateViewModel(
            getCandidatesByUserIdUseCase = get(),
            createCandidateUseCase = get(),
            updateCandidateUseCase = get(),
            getCurrentUserUseCase = get()
        )
    }
}