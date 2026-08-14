package ru.lissa_lesia.iteams.presentation.screens.editproject

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.androidx.compose.getViewModel
import org.koin.core.parameter.parametersOf
import ru.lissa_lesia.iteams.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProjectScreen(
    projectId: String,
    onNavigateBack: () -> Unit
) {
    // Получаем ViewModel с параметром projectId
    val viewModel: EditProjectViewModel = getViewModel(
        parameters = { parametersOf(projectId) }
    )
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

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
                        text = "Редактирование проекта",
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp, vertical = 16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(
                    color = ActionPrimary,
                    modifier = Modifier.padding(top = 32.dp)
                )
                return@Column
            }

            OutlinedTextField(
                value = uiState.title,
                onValueChange = viewModel::updateTitle,
                label = { Text("Название проекта") },
                modifier = Modifier.fillMaxWidth(),
                enabled = !uiState.isSaving,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedBorderColor = InputBorderFocused,
                    unfocusedBorderColor = InputBorderUnfocused,
                    focusedLabelColor = InputLabelFocused
                )
            )
            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = uiState.description,
                onValueChange = viewModel::updateDescription,
                label = { Text("Описание") },
                modifier = Modifier.fillMaxWidth(),
                enabled = !uiState.isSaving,
                maxLines = 4,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedBorderColor = InputBorderFocused,
                    unfocusedBorderColor = InputBorderUnfocused,
                    focusedLabelColor = InputLabelFocused
                )
            )
            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = uiState.requiredRolesInput,
                onValueChange = viewModel::updateRequiredRolesInput,
                label = { Text("Роли (через запятую)") },
                modifier = Modifier.fillMaxWidth(),
                enabled = !uiState.isSaving,
                placeholder = { Text("Например: Разработчик, Дизайнер") },
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedBorderColor = InputBorderFocused,
                    unfocusedBorderColor = InputBorderUnfocused,
                    focusedLabelColor = InputLabelFocused
                )
            )
            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = uiState.requiredSkillsInput,
                onValueChange = viewModel::updateRequiredSkillsInput,
                label = { Text("Навыки (через запятую)") },
                modifier = Modifier.fillMaxWidth(),
                enabled = !uiState.isSaving,
                placeholder = { Text("Например: Kotlin, Figma") },
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedBorderColor = InputBorderFocused,
                    unfocusedBorderColor = InputBorderUnfocused,
                    focusedLabelColor = InputLabelFocused
                )
            )
            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = uiState.authorRole,
                onValueChange = viewModel::updateAuthorRole,
                label = { Text("Ваша роль в проекте") },
                modifier = Modifier.fillMaxWidth(),
                enabled = !uiState.isSaving,
                placeholder = { Text("Например: Team Lead") },
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedBorderColor = InputBorderFocused,
                    unfocusedBorderColor = InputBorderUnfocused,
                    focusedLabelColor = InputLabelFocused
                )
            )
            Spacer(modifier = Modifier.height(16.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = if (uiState.isOpen) "Открыт" else "Закрыт",
                    fontSize = 16.sp,
                    color = TextPrimary,
                    modifier = Modifier.weight(1f)
                )
                Switch(
                    checked = uiState.isOpen,
                    onCheckedChange = viewModel::updateIsOpen,
                    enabled = !uiState.isSaving,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = TextOnPrimary,
                        checkedTrackColor = ActionPrimary,
                        uncheckedThumbColor = TextOnPrimary,
                        uncheckedTrackColor = InputBorderUnfocused
                    )
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            if (uiState.isSaving) {
                CircularProgressIndicator(color = ActionPrimary)
            } else {
                Button(
                    onClick = { viewModel.saveProject { } },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ActionPrimary
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "Сохранить изменения",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextOnPrimary
                    )
                }
            }

            if (uiState.errorMessage != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Ошибка: ${uiState.errorMessage}",
                    color = ErrorText,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}