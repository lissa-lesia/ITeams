package ru.lissa_lesia.iteams.di

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module
import ru.lissa_lesia.iteams.data.repositories.AuthRepositoryImpl
import ru.lissa_lesia.iteams.data.repositories.ProjectRepositoryImpl
import ru.lissa_lesia.iteams.domain.repositories.IAuthRepository
import ru.lissa_lesia.iteams.domain.repositories.IProjectRepository
import ru.lissa_lesia.iteams.presentation.navigation.AuthStateManager
import ru.lissa_lesia.iteams.presentation.screens.applications.ApplicationsViewModel
import ru.lissa_lesia.iteams.presentation.screens.createproject.CreateProjectViewModel
import ru.lissa_lesia.iteams.presentation.screens.feed.FeedViewModel
import ru.lissa_lesia.iteams.presentation.screens.login.LoginViewModel
import ru.lissa_lesia.iteams.presentation.screens.profile.ProfileViewModel
import ru.lissa_lesia.iteams.presentation.screens.register.RegisterViewModel

val appModule = module {
    // Firebase instances
    single { FirebaseAuth.getInstance() }
    single { FirebaseFirestore.getInstance() }

    // Repositories
    single<IAuthRepository> { AuthRepositoryImpl(get(), get()) }
    single<IProjectRepository> { ProjectRepositoryImpl(get(), get()) }

    // AuthStateManager
    single { AuthStateManager(get()) }

    // ViewModels
    viewModel { LoginViewModel(get()) }
    viewModel { RegisterViewModel(get()) }
    viewModel { FeedViewModel(get(), get(), get()) }
    viewModel { CreateProjectViewModel(get(), get()) }
    viewModel { ProfileViewModel(get(), get()) }
    viewModel { ApplicationsViewModel(get(), get(), get()) }
}