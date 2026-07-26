package ru.lissa_lesia.iteams.presentation.screens.createproject

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateProjectScreen(
    viewModel: CreateProjectViewModel,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // При успешном создании возвращаемся назад
    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) {
            onNavigateBack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Создание проекта") },
                navigationIcon = {
                    // Можно добавить кнопку "Назад"
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            OutlinedTextField(
                value = uiState.title,
                onValueChange = viewModel::updateTitle,
                label = { Text("Название проекта") },
                modifier = Modifier.fillMaxWidth(),
                enabled = !uiState.isLoading
            )
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = uiState.description,
                onValueChange = viewModel::updateDescription,
                label = { Text("Описание") },
                modifier = Modifier.fillMaxWidth(),
                enabled = !uiState.isLoading,
                maxLines = 4
            )
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = uiState.requiredRolesInput,
                onValueChange = viewModel::updateRequiredRolesInput,
                label = { Text("Роли (через запятую)") },
                modifier = Modifier.fillMaxWidth(),
                enabled = !uiState.isLoading,
                placeholder = { Text("Например: Разработчик, Дизайнер") }
            )
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = uiState.requiredSkillsInput,
                onValueChange = viewModel::updateRequiredSkillsInput,
                label = { Text("Навыки (через запятую)") },
                modifier = Modifier.fillMaxWidth(),
                enabled = !uiState.isLoading,
                placeholder = { Text("Например: Kotlin, Figma") }
            )
            Spacer(modifier = Modifier.height(8.dp))

            // Переключатель статуса
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = if (uiState.isOpen) "Открыт" else "Закрыт",
                    modifier = Modifier.weight(1f)
                )
                Switch(
                    checked = uiState.isOpen,
                    onCheckedChange = viewModel::updateIsOpen,
                    enabled = !uiState.isLoading
                )
            }
            Spacer(modifier = Modifier.height(16.dp))

            if (uiState.isLoading) {
                CircularProgressIndicator()
            } else {
                Button(
                    onClick = { viewModel.createProject { /* onSuccess -> обрабатывается LaunchedEffect */ } },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Создать проект")
                }
            }

            if (uiState.errorMessage != null) {
                Text(
                    text = "Ошибка: ${uiState.errorMessage}",
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    }
}