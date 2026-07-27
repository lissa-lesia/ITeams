package ru.lissa_lesia.iteams.presentation.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ru.lissa_lesia.iteams.domain.models.User

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel,
    onNavigateBack: () -> Unit,
    onLogout: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val topBarGradient = Brush.horizontalGradient(
        colors = listOf(Color(0xFF6A11CB), Color(0xFF2575FC))
    )

    LaunchedEffect(uiState.saveSuccess) {
        if (uiState.saveSuccess) {
            viewModel.cancelEditing()
        }
    }

    Scaffold(
        containerColor = Color(0xFFF5F7FA),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (uiState.isEditing) "Редактирование профиля" else "Профиль",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Назад", tint = Color.White)
                    }
                },
                actions = {
                    IconButton(
                        onClick = onLogout,
                        enabled = !uiState.isLoading
                    ) {
                        Icon(
                            Icons.Default.Logout,
                            contentDescription = "Выйти",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                ),
                modifier = Modifier
                    .background(brush = topBarGradient)
                    .shadow(4.dp)
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                uiState.isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = Color(0xFF2575FC)
                    )
                }
                uiState.errorMessage != null && uiState.user == null -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "Ошибка: ${uiState.errorMessage}",
                            color = Color.Red,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = { viewModel.loadUser() },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF2575FC)
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Повторить", color = Color.White)
                        }
                    }
                }
                uiState.user != null -> {
                    val user = uiState.user!!
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Top
                    ) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .shadow(elevation = 8.dp, shape = RoundedCornerShape(16.dp)),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                // Аватар (заглушка)
                                Box(
                                    modifier = Modifier
                                        .size(80.dp)
                                        .clip(CircleShape)
                                        .background(
                                            brush = Brush.horizontalGradient(
                                                colors = listOf(Color(0xFF6A11CB), Color(0xFF2575FC))
                                            )
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Default.Person,
                                        contentDescription = "Аватар",
                                        tint = Color.White,
                                        modifier = Modifier.size(40.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                if (uiState.isEditing) {
                                    EditProfileContent(
                                        user = user,
                                        onUserUpdate = { updatedUser -> viewModel.updateUser(updatedUser) }
                                    )

                                    Spacer(modifier = Modifier.height(16.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Button(
                                            onClick = { viewModel.saveProfile { } },
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = Color(0xFF4CAF50)
                                            ),
                                            shape = RoundedCornerShape(12.dp),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Text("Сохранить", color = Color.White)
                                        }
                                        Button(
                                            onClick = { viewModel.cancelEditing() },
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = Color(0xFFF44336)
                                            ),
                                            shape = RoundedCornerShape(12.dp),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Text("Отмена", color = Color.White)
                                        }
                                    }
                                } else {
                                    ViewProfileContent(user = user)

                                    Spacer(modifier = Modifier.height(16.dp))

                                    Button(
                                        onClick = { viewModel.enableEditing() },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = Color(0xFF2575FC)
                                        ),
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text("Редактировать профиль", color = Color.White)
                                    }
                                }
                            }
                        }

                        if (uiState.errorMessage != null && uiState.isEditing) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = uiState.errorMessage!!,
                                color = Color.Red,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ViewProfileContent(user: User) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = user.name,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1A237E)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = user.email,
            style = MaterialTheme.typography.bodyMedium,
            color = Color(0xFF78909C)
        )

        if (user.bio.isNotEmpty()) {
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = user.bio,
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF37474F),
                textAlign = TextAlign.Center
            )
        }

        if (user.skills.isNotEmpty()) {
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Навыки: ${user.skills.joinToString(", ")}",
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF455A64)
            )
        }
    }
}

@Composable
fun EditProfileContent(
    user: User,
    onUserUpdate: (User) -> Unit
) {
    var name by remember { mutableStateOf(user.name) }
    var bio by remember { mutableStateOf(user.bio) }
    var skillsInput by remember { mutableStateOf(user.skills.joinToString(", ")) }

    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = "Редактирование профиля",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF2575FC),
            modifier = Modifier.padding(bottom = 16.dp)
        )

        TextField(
            value = name,
            onValueChange = {
                name = it
                onUserUpdate(user.copy(name = it))
            },
            label = { Text("Имя") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                focusedIndicatorColor = Color(0xFF2575FC),
                unfocusedIndicatorColor = Color.Gray
            )
        )
        Spacer(modifier = Modifier.height(8.dp))

        TextField(
            value = bio,
            onValueChange = {
                bio = it
                onUserUpdate(user.copy(bio = it))
            },
            label = { Text("О себе") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                focusedIndicatorColor = Color(0xFF2575FC),
                unfocusedIndicatorColor = Color.Gray
            )
        )
        Spacer(modifier = Modifier.height(8.dp))

        TextField(
            value = skillsInput,
            onValueChange = {
                skillsInput = it
                val skills = it.split(",")
                    .map { skill -> skill.trim() }
                    .filter { it.isNotEmpty() }
                onUserUpdate(user.copy(skills = skills))
            },
            label = { Text("Навыки (через запятую)") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                focusedIndicatorColor = Color(0xFF2575FC),
                unfocusedIndicatorColor = Color.Gray
            )
        )
    }
}