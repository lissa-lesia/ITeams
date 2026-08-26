package ru.lissa_lesia.iteams.presentation.screens.createcandidate

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.androidx.compose.koinViewModel
import ru.lissa_lesia.iteams.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateCandidateScreen(
    onNavigateBack: () -> Unit
) {
    val viewModel: CreateCandidateViewModel = koinViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.loadExistingCandidate()
    }

    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) {
            onNavigateBack()
        }
    }

    val topBarGradient = Brush.horizontalGradient(
        colors = listOf(PrimaryGradientStart, PrimaryGradientEnd)
    )

    Scaffold(
        containerColor = BackgroundLight,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (uiState.existingCandidateId != null) "Редактирование заявки" else "Создание заявки",
                        color = TextOnPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Назад",
                            tint = TextOnPrimary
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
                .background(BackgroundLight)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Top
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(elevation = 8.dp, shape = RoundedCornerShape(16.dp)),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = CardBackground)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                    ) {
                        Text(
                            text = "Заполните информацию о себе",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )

                        OutlinedTextField(
                            value = uiState.desiredRolesInput,
                            onValueChange = viewModel::updateDesiredRolesInput,
                            label = { Text("Желаемые роли (через запятую)") },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !uiState.isLoading,
                            placeholder = { Text("Например: Разработчик, Дизайнер") },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = InputBorderFocused,
                                unfocusedBorderColor = InputBorderUnfocused,
                                focusedLabelColor = InputLabelFocused
                            ),
                            shape = RoundedCornerShape(12.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = uiState.skillsInput,
                            onValueChange = viewModel::updateSkillsInput,
                            label = { Text("Навыки (через запятую)") },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !uiState.isLoading,
                            placeholder = { Text("Например: Kotlin, Figma") },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = InputBorderFocused,
                                unfocusedBorderColor = InputBorderUnfocused,
                                focusedLabelColor = InputLabelFocused
                            ),
                            shape = RoundedCornerShape(12.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = uiState.description,
                            onValueChange = viewModel::updateDescription,
                            label = { Text("О себе (кратко)") },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !uiState.isLoading,
                            maxLines = 4,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = InputBorderFocused,
                                unfocusedBorderColor = InputBorderUnfocused,
                                focusedLabelColor = InputLabelFocused
                            ),
                            shape = RoundedCornerShape(12.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Статус заявки:",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSecondary,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                text = if (uiState.isActive) "Активна" else "Закрыта",
                                color = if (uiState.isActive) StatusOpen else StatusClosed,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(end = 8.dp)
                            )
                            Switch(
                                checked = uiState.isActive,
                                onCheckedChange = viewModel::updateIsActive,
                                enabled = !uiState.isLoading,
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = TextOnPrimary,
                                    checkedTrackColor = ActionPrimary,
                                    uncheckedThumbColor = TextOnPrimary,
                                    uncheckedTrackColor = InputBorderUnfocused
                                )
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        color = ActionPrimary,
                        modifier = Modifier.size(48.dp)
                    )
                } else {
                    Button(
                        onClick = { viewModel.saveCandidate { } },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ActionPrimary,
                            disabledContainerColor = InputBorderUnfocused
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = if (uiState.existingCandidateId != null) "Обновить заявку" else "Создать заявку",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextOnPrimary
                        )
                    }
                }

                if (uiState.errorMessage != null) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = ErrorBackground
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = uiState.errorMessage!!,
                            color = ErrorText,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}